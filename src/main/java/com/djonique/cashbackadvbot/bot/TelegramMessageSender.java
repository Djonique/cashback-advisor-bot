package com.djonique.cashbackadvbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramMessageSender {

    public boolean send(Long chatId, String text, InlineKeyboardMarkup keyboard, AbsSender sender) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            sender.execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение chatId={}", chatId, e);
            return false;
        }
    }

    public boolean answerCallback(String callbackQueryId, String text, AbsSender sender) {
        try {
            sender.execute(new AnswerCallbackQuery(callbackQueryId, text, false, null, null));
            return true;
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на callback {}", callbackQueryId, e);
            return false;
        }
    }
}
