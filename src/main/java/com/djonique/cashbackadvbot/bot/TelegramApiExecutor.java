package com.djonique.cashbackadvbot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
public class TelegramApiExecutor {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 300L;

    public <T> T executeWithRetry(AbsSender sender, ApiCall<T> call) throws TelegramApiException {
        for (int attempt = 1; ; attempt++) {
            try {
                return call.run(sender);
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

    @FunctionalInterface
    public interface ApiCall<T> {
        T run(AbsSender sender) throws TelegramApiException;
    }
}
