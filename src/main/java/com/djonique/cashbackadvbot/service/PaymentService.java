package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.TelegramApiExecutor;
import com.djonique.cashbackadvbot.config.PaymentProperties;
import com.djonique.cashbackadvbot.domain.PaymentInvoice;
import com.djonique.cashbackadvbot.repository.PaymentInvoiceRepository;
import com.djonique.cashbackadvbot.repository.PaymentRepository;
import com.djonique.cashbackadvbot.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String DESCRIPTION = "Поддержка проекта «Кешбэк Помощник»";
    private static final String FIAT_CURRENCY = "RUB";
    private static final String STARS_CURRENCY = "XTR";
    private static final String STARS_PROVIDER_TOKEN = "xtr";
    private static final String STARS_PRICE_LABEL = "Донат";

    private final PaymentProperties paymentProperties;
    private final PaymentRepository paymentRepository;
    private final PaymentInvoiceRepository paymentInvoiceRepository;
    private final TelegramApiExecutor telegramApiExecutor;

    public boolean sendFiatInvoice(Long chatId, String title, int amountRub, AbsSender sender) {
        int amountMinor;
        try {
            amountMinor = Math.multiplyExact(amountRub, 100);
        } catch (ArithmeticException e) {
            log.error("Некорректная сумма счёта: chatId={}, amountRub={}",
                    LogUtils.maskChatId(chatId), amountRub);
            return false;
        }

        String payload = UUID.randomUUID().toString();
        SendInvoice invoice = new SendInvoice();
        invoice.setChatId(chatId.toString());
        invoice.setTitle(title);
        invoice.setDescription(DESCRIPTION);
        invoice.setPayload(payload);
        invoice.setProviderToken(paymentProperties.providerToken());
        invoice.setCurrency(FIAT_CURRENCY);
        invoice.setPrices(List.of(new LabeledPrice(title, amountMinor)));

        PaymentInvoice saved = createInvoice(chatId, payload, amountMinor, FIAT_CURRENCY);
        try {
            telegramApiExecutor.executeWithRetry(sender, s -> s.execute(invoice));
            log.info("Инвойс в рублях отправлен: chatId={}, amountRub={}",
                    LogUtils.maskChatId(chatId), amountRub);
            return true;
        } catch (TelegramApiException e) {
            deleteInvoiceQuietly(saved);
            log.error("Не удалось отправить инвойс в рублях: chatId={}, amountRub={}: {}",
                    LogUtils.maskChatId(chatId), amountRub, LogUtils.sanitize(e));
            return false;
        }
    }

    public boolean sendStarsInvoice(Long chatId, String title, int amountStars, AbsSender sender) {
        String payload = UUID.randomUUID().toString();
        SendInvoice invoice = new SendInvoice();
        invoice.setChatId(chatId.toString());
        invoice.setTitle(title);
        invoice.setDescription(DESCRIPTION);
        invoice.setPayload(payload);
        invoice.setCurrency(STARS_CURRENCY);
        invoice.setProviderToken(STARS_PROVIDER_TOKEN);
        invoice.setPrices(List.of(new LabeledPrice(STARS_PRICE_LABEL, amountStars)));

        PaymentInvoice saved = createInvoice(chatId, payload, amountStars, STARS_CURRENCY);
        try {
            telegramApiExecutor.executeWithRetry(sender, s -> s.execute(invoice));
            log.info("Инвойс в звёздах отправлен: chatId={}, amountStars={}",
                    LogUtils.maskChatId(chatId), amountStars);
            return true;
        } catch (TelegramApiException e) {
            deleteInvoiceQuietly(saved);
            log.error("Не удалось отправить инвойс: chatId={}, amountStars={}: {}",
                    LogUtils.maskChatId(chatId), amountStars, LogUtils.sanitize(e));
            return false;
        }
    }

    /**
     * Проверяет PreCheckoutQuery по сохранённому счёту: payload, сумма, валюта и плательщик.
     */
    @Transactional(readOnly = true)
    public boolean validatePreCheckout(String payload,
                                       Integer totalAmount,
                                       String currency,
                                       Long payerChatId) {
        if (payload == null || totalAmount == null || currency == null) {
            return false;
        }
        return paymentInvoiceRepository.findByPayload(payload)
                .filter(invoice -> PaymentInvoice.STATUS_CREATED.equals(invoice.getStatus()))
                .filter(invoice -> invoice.getAmount() == totalAmount.longValue())
                .filter(invoice -> currency.equals(invoice.getCurrency()))
                .filter(invoice -> payerChatId != null
                        && payerChatId.equals(invoice.getTelegramChatId()))
                .isPresent();
    }

    /**
     * Атомарно сохраняет платёж. Возвращает false, если платёж с таким
     * telegram_payment_charge_id уже был обработан (повторная доставка update).
     */
    @Transactional
    public boolean registerPayment(Long userId,
                                   String telegramChargeId,
                                   String providerChargeId,
                                   long amount,
                                   String currency,
                                   String invoicePayload) {
        int inserted = paymentRepository.insertIfAbsent(
                userId, telegramChargeId, providerChargeId, invoicePayload, amount, currency);
        if (inserted != 1) {
            return false;
        }
        if (invoicePayload != null) {
            int updated = paymentInvoiceRepository.markPaid(
                    invoicePayload,
                    PaymentInvoice.STATUS_PAID,
                    PaymentInvoice.STATUS_CREATED,
                    Instant.now());
            if (updated == 0) {
                log.warn("Счёт не найден или уже оплачен: payload={}", invoicePayload);
            }
        }
        return true;
    }

    private PaymentInvoice createInvoice(Long chatId, String payload, long amount, String currency) {
        PaymentInvoice invoice = new PaymentInvoice();
        invoice.setPayload(payload);
        invoice.setTelegramChatId(chatId);
        invoice.setAmount(amount);
        invoice.setCurrency(currency);
        invoice.setStatus(PaymentInvoice.STATUS_CREATED);
        return paymentInvoiceRepository.save(invoice);
    }

    private void deleteInvoiceQuietly(PaymentInvoice invoice) {
        if (invoice == null || invoice.getId() == null) {
            return;
        }
        try {
            paymentInvoiceRepository.deleteById(invoice.getId());
        } catch (RuntimeException e) {
            log.warn("Не удалось удалить неотправленный счёт: payload={}", invoice.getPayload());
        }
    }
}
