package com.djonique.cashbackadvbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AbsSender sender;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldSendStarsInvoice() throws TelegramApiException {
        paymentService.sendStarsInvoice(123L, "Донат", 100, sender);

        ArgumentCaptor<SendInvoice> captor = ArgumentCaptor.forClass(SendInvoice.class);
        verify(sender).execute(captor.capture());

        SendInvoice invoice = captor.getValue();
        assertThat(invoice.getChatId()).isEqualTo("123");
        assertThat(invoice.getTitle()).isEqualTo("Донат");
        assertThat(invoice.getDescription()).isEqualTo("Поддержка проекта");
        assertThat(invoice.getCurrency()).isEqualTo("XTR");
        assertThat(invoice.getProviderToken()).isEmpty();
        assertThat(invoice.getPayload()).isNotBlank();
        assertThat(invoice.getPrices()).singleElement()
                .satisfies(price -> {
                    assertThat(price.getLabel()).isEqualTo("Донат");
                    assertThat(price.getAmount()).isEqualTo(100);
                });
    }

    @Test
    void shouldNotThrow_whenTelegramApiFails() throws TelegramApiException {
        when(sender.execute(any(SendInvoice.class)))
                .thenThrow(new TelegramApiException("fail"));

        assertThatCode(() -> paymentService.sendStarsInvoice(123L, "Донат", 100, sender))
                .doesNotThrowAnyException();
    }
}
