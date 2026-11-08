package com.djonique.cashbackadvbot.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface BotSender {

    boolean send(Long chatId, String text, InlineKeyboardMarkup keyboard);

    Integer sendWithMessageId(Long chatId, String text, InlineKeyboardMarkup keyboard);

    boolean deleteMessage(Long chatId, Integer messageId);

    void answerCallback(String callbackQueryId, String text);

    boolean editMessageText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard);

    boolean editMessageReplyMarkup(Long chatId, Integer messageId, InlineKeyboardMarkup keyboard);
}
