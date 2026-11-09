package com.djonique.cashbackadvbot.bot;

import com.djonique.cashbackadvbot.util.LogUtils;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class TelegramMessageSender implements BotSender {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 300L;

    private final AtomicReference<AbsSender> sender = new AtomicReference<>();

    public void bind(AbsSender sender) {
        this.sender.set(sender);
    }

    @Override
    public boolean send(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        return sendAndGetMessage(chatId, text, null, keyboard) != null;
    }

    @Override
    public Integer sendWithMessageId(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        Message sent = sendAndGetMessage(chatId, text, null, keyboard);
        return sent != null ? sent.getMessageId() : null;
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) {
        try {
            executeWithRetry(() -> sender().execute(new DeleteMessage(chatId.toString(), messageId)));
        } catch (TelegramApiException e) {
            log.error("Не удалось удалить сообщение chatId={} id={}: {}",
                    LogUtils.maskChatId(chatId), messageId, LogUtils.sanitize(e));
        }
    }

    private Message sendAndGetMessage(Long chatId,
                                      String text,
                                      String parseMode,
                                      InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (parseMode != null) {
            message.setParseMode(parseMode);
        }
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            return executeWithRetry(() -> sender().execute(message));
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение chatId={}: {}",
                    LogUtils.maskChatId(chatId), LogUtils.sanitize(e));
            return null;
        }
    }

    @Override
    public void answerCallback(String callbackQueryId, String text) {
        try {
            executeWithRetry(() -> sender().execute(
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
            executeWithRetry(() -> sender().execute(editMessage));
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
            executeWithRetry(() -> sender().execute(editMarkup));
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

    private <T> T executeWithRetry(ApiCall<T> call) throws TelegramApiException {
        for (int attempt = 1; ; attempt++) {
            try {
                return call.run();
            } catch (TelegramApiRequestException e) {
                if (attempt >= MAX_ATTEMPTS || !isRetryable(e)) {
                    throw e;
                }
                sleep(retryAfterMs(e, attempt));
            } catch (TelegramApiException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
                sleep(BASE_BACKOFF_MS * attempt);
            }
        }
    }

    private boolean isRetryable(TelegramApiRequestException e) {
        Integer code = e.getErrorCode();
        return code != null && (code == 429 || code >= 500);
    }

    private long retryAfterMs(TelegramApiRequestException e, int attempt) {
        if (e.getParameters() != null && e.getParameters().getRetryAfter() != null) {
            return e.getParameters().getRetryAfter() * 1000L;
        }
        return BASE_BACKOFF_MS * (1L << (attempt - 1));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

    @FunctionalInterface
    private interface ApiCall<T> {
        T run() throws TelegramApiException;
    }
}
