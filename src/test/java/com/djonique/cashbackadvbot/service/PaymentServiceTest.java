package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.TelegramApiExecutor;
import com.djonique.cashbackadvbot.config.PaymentProperties;
import com.djonique.cashbackadvbot.domain.PaymentInvoice;
import com.djonique.cashbackadvbot.repository.PaymentInvoiceRepository;
import com.djonique.cashbackadvbot.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AbsSender sender;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentInvoiceRepository paymentInvoiceRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                new PaymentProperties("live-provider-token"),
                paymentRepository,
                paymentInvoiceRepository,
                new TelegramApiExecutor());
    }

    @Test
    void shouldSendStarsInvoice() throws TelegramApiException {
        stubSavedInvoice();
        boolean sent = paymentService.sendStarsInvoice(123L, "Донат", 100, sender);

        assertThat(sent).isTrue();

        ArgumentCaptor<SendInvoice> captor = ArgumentCaptor.forClass(SendInvoice.class);
        verify(sender).execute(captor.capture());

        SendInvoice invoice = captor.getValue();
        assertThat(invoice.getChatId()).isEqualTo("123");
        assertThat(invoice.getTitle()).isEqualTo("Донат");
        assertThat(invoice.getDescription()).isEqualTo("Поддержка проекта «Кешбэк Помощник»");
        assertThat(invoice.getCurrency()).isEqualTo("XTR");
        assertThat(invoice.getProviderToken()).isEqualTo("xtr");
        assertThat(invoice.getPayload()).isNotBlank();
        assertThat(invoice.getPrices()).singleElement()
                .satisfies(price -> {
                    assertThat(price.getLabel()).isEqualTo("Донат");
                    assertThat(price.getAmount()).isEqualTo(100);
                });
    }

    @Test
    void shouldSendFiatInvoiceWithPriceInKopecks() throws TelegramApiException {
        stubSavedInvoice();
        boolean sent = paymentService.sendFiatInvoice(123L, "Донат 100 руб", 100, sender);

        assertThat(sent).isTrue();

        ArgumentCaptor<SendInvoice> captor = ArgumentCaptor.forClass(SendInvoice.class);
        verify(sender).execute(captor.capture());

        SendInvoice invoice = captor.getValue();
        assertThat(invoice.getChatId()).isEqualTo("123");
        assertThat(invoice.getTitle()).isEqualTo("Донат 100 руб");
        assertThat(invoice.getDescription()).isEqualTo("Поддержка проекта «Кешбэк Помощник»");
        assertThat(invoice.getCurrency()).isEqualTo("RUB");
        assertThat(invoice.getProviderToken()).isEqualTo("live-provider-token");
        assertThat(invoice.getPayload()).isNotBlank();
        assertThat(invoice.getPrices()).singleElement()
                .satisfies(price -> {
                    assertThat(price.getLabel()).isEqualTo("Донат 100 руб");
                    assertThat(price.getAmount()).isEqualTo(10_000);
                });
    }

    @Test
    void shouldReturnFalse_whenTelegramApiFails_andDeleteStoredInvoice() throws TelegramApiException {
        stubSavedInvoice();
        when(sender.execute(any(SendInvoice.class)))
                .thenThrow(new TelegramApiException("fail"));

        assertThat(paymentService.sendStarsInvoice(123L, "Донат", 100, sender)).isFalse();

        ArgumentCaptor<PaymentInvoice> captor = ArgumentCaptor.forClass(PaymentInvoice.class);
        verify(paymentInvoiceRepository).save(captor.capture());
        verify(paymentInvoiceRepository).deleteById(42L);
        assertThat(captor.getValue().getTelegramChatId()).isEqualTo(123L);
        assertThat(captor.getValue().getCurrency()).isEqualTo("XTR");
        assertThat(captor.getValue().getAmount()).isEqualTo(100L);
    }

    @Test
    void shouldStoreFiatInvoiceInKopecks() {
        stubSavedInvoice();
        paymentService.sendFiatInvoice(123L, "Донат 100 руб", 100, sender);

        ArgumentCaptor<PaymentInvoice> captor = ArgumentCaptor.forClass(PaymentInvoice.class);
        verify(paymentInvoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(10_000L);
        assertThat(captor.getValue().getCurrency()).isEqualTo("RUB");
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentInvoice.STATUS_CREATED);
    }

    @Test
    void shouldValidatePrecheckoutAgainstStoredInvoice() {
        PaymentInvoice invoice = new PaymentInvoice();
        invoice.setPayload("payload-1");
        invoice.setTelegramChatId(123L);
        invoice.setAmount(10_000L);
        invoice.setCurrency("RUB");
        when(paymentInvoiceRepository.findByPayload("payload-1"))
                .thenReturn(java.util.Optional.of(invoice));

        assertThat(paymentService.validatePreCheckout("payload-1", 10_000, "RUB", 123L)).isTrue();
        assertThat(paymentService.validatePreCheckout("payload-1", 9_999, "RUB", 123L)).isFalse();
        assertThat(paymentService.validatePreCheckout("payload-1", 10_000, "XTR", 123L)).isFalse();
        assertThat(paymentService.validatePreCheckout("payload-1", 10_000, "RUB", 999L)).isFalse();
        assertThat(paymentService.validatePreCheckout("payload-1", 10_000, "RUB", null)).isFalse();
        assertThat(paymentService.validatePreCheckout("unknown", 10_000, "RUB", 123L)).isFalse();
    }

    @Test
    void shouldMarkInvoicePaid_whenPaymentRegisteredFirstTime() {
        when(paymentRepository.insertIfAbsent(
                1L, "tg-charge", "provider-charge", "payload-1", 10_000L, "RUB"))
                .thenReturn(1);
        when(paymentInvoiceRepository.markPaid(
                any(), any(), any(), any())).thenReturn(1);

        assertThat(paymentService.registerPayment(
                1L, "tg-charge", "provider-charge", 10_000L, "RUB", "payload-1")).isTrue();

        verify(paymentInvoiceRepository).markPaid(
                eq("payload-1"),
                eq(PaymentInvoice.STATUS_PAID),
                eq(PaymentInvoice.STATUS_CREATED),
                any());
    }

    @Test
    void shouldSkipMarkPaid_whenPaymentIsDuplicate() {
        when(paymentRepository.insertIfAbsent(
                1L, "tg-charge", "provider-charge", "payload-1", 10_000L, "RUB"))
                .thenReturn(0);

        assertThat(paymentService.registerPayment(
                1L, "tg-charge", "provider-charge", 10_000L, "RUB", "payload-1")).isFalse();

        verify(paymentInvoiceRepository, never()).markPaid(any(), any(), any(), any());
    }

    private void stubSavedInvoice() {
        when(paymentInvoiceRepository.save(any(PaymentInvoice.class)))
                .thenAnswer(invocation -> {
                    PaymentInvoice invoice = invocation.getArgument(0);
                    invoice.setId(42L);
                    return invoice;
                });
    }
}
