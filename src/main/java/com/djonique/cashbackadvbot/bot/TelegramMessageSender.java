package com.djonique.cashbackadvbot.bot;

import com.djonique.cashbackadvbot.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageSender implements BotSender {

    private final TelegramApiExecutor telegramApiExecutor;
    private final AtomicReference<AbsSender> sender = new AtomicReference<>();

    public void bind(AbsSender sender) {
        this.sender.set(sender);
    }

    @Override
    public boolean send(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        return sendAndGetMessage(chatId, text, keyboard) != null;
    }

    @Override
    public Integer sendWithMessageId(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        Message sent = sendAndGetMessage(chatId, text, keyboard);
        return sent != null ? sent.getMessageId() : null;
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) {
        try {
            telegramApiExecutor.executeWithRetry(sender(),
                    s -> s.execute(new DeleteMessage(chatId.toString(), messageId)));
        } catch (TelegramApiException e) {
            log.error("Не удалось удалить сообщение chatId={} id={}: {}",
                    LogUtils.maskChatId(chatId), messageId, LogUtils.sanitize(e));
        }
    }

    private Message sendAndGetMessage(Long chatId,
                                      String text,
                                      InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            return telegramApiExecutor.executeWithRetry(sender(), s -> s.execute(message));
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение chatId={}: {}",
                    LogUtils.maskChatId(chatId), LogUtils.sanitize(e));
            return null;
        }
    }

    @Override
    public void answerCallback(String callbackQueryId, String text) {
        try {
            telegramApiExecutor.executeWithRetry(sender(), s -> s.execute(
                    new AnswerCallbackQuery(callbackQueryId, text, false, null, null)));
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на callback {}: {}",
                    callbackQueryId, LogUtils.sanitize(e));
        }
    }

    @Override
    public boolean editMessageText(Long chatId,
                                   Integer messageId,
                                   String text,
                                   InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(keyboard);

        try {
            telegramApiExecutor.executeWithRetry(sender(), s -> s.execute(editMessage));
            return true;
        } catch (TelegramApiException e) {
            if (isMessageNotModified(e)) {
                log.debug("Сообщение chatId={} id={} не изменилось",
                        LogUtils.maskChatId(chatId), messageId);
                return true;
            }
            log.error("Не удалось отредактировать сообщение chatId={} id={}: {}",
                    LogUtils.maskChatId(chatId), messageId, LogUtils.sanitize(e));
            return false;
        }
    }

    @Override
    public boolean editMessageReplyMarkup(Long chatId,
                                          Integer messageId,
                                          InlineKeyboardMarkup keyboard) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(keyboard);

        try {
            telegramApiExecutor.executeWithRetry(sender(), s -> s.execute(editMarkup));
            return true;
        } catch (TelegramApiException e) {
            if (isMessageNotModified(e)) {
                log.debug("Клавиатура chatId={} id={} не изменилась",
                        LogUtils.maskChatId(chatId), messageId);
                return true;
            }
            log.error("Не удалось обновить клавиатуру chatId={} id={}: {}",
                    LogUtils.maskChatId(chatId), messageId, LogUtils.sanitize(e));
            return false;
        }
    }

    private boolean isMessageNotModified(TelegramApiException e) {
        return e.getMessage() != null
                && e.getMessage().toLowerCase(Locale.ROOT).contains("message is not modified");
    }

    private AbsSender sender() {
        AbsSender current = sender.get();
        if (current == null) {
            throw new IllegalStateException("Bot sender is not bound");
        }
        return current;
    }
}
