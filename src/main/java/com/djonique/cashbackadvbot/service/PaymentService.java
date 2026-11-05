package com.djonique.cashbackadvbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    private static final String DESCRIPTION = "Спасибо за поддержку";
    private static final String STARS_CURRENCY = "XTR";
    private static final String STARS_PROVIDER_TOKEN = "xtr";

    public void sendStarsInvoice(Long chatId, String title, int amountStars, AbsSender sender) {
        SendInvoice invoice = new SendInvoice();
        invoice.setChatId(chatId.toString());
        invoice.setTitle(title);
        invoice.setDescription(DESCRIPTION);
        invoice.setPayload(UUID.randomUUID().toString());
        invoice.setCurrency(STARS_CURRENCY);
        invoice.setProviderToken(STARS_PROVIDER_TOKEN);
        invoice.setPrices(List.of(new LabeledPrice(title, amountStars)));

        try {
            sender.execute(invoice);
            log.info("Инвойс в звёздах отправлен: chatId={}, amountStars={}", chatId, amountStars);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить инвойс: chatId={}, amountStars={}",
                    chatId, amountStars, e);
        }
    }
}
