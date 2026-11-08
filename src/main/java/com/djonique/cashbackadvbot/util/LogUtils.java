package com.djonique.cashbackadvbot.util;

import java.util.regex.Pattern;

/**
 * Утилиты для безопасного логирования: маскирование chat_id и токенов Telegram.
 */
public final class LogUtils {

    private static final Pattern BOT_TOKEN = Pattern.compile("bot\\d+:[A-Za-z0-9_-]+");
    private static final Pattern PAYMENT_PROVIDER_TOKEN =
            Pattern.compile("\\b\\d{6,12}:(?:TEST|LIVE):[A-Za-z0-9_-]+\\b");

    private LogUtils() {
    }

    public static String maskChatId(Long chatId) {
        if (chatId == null) {
            return "null";
        }
        String value = chatId.toString();
        return value.length() <= 4 ? "***" : "***" + value.substring(value.length() - 4);
    }

    public static String sanitize(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null) {
            return throwable.getClass().getSimpleName();
        }
        String sanitized = BOT_TOKEN.matcher(message).replaceAll("bot<TOKEN>");
        return PAYMENT_PROVIDER_TOKEN.matcher(sanitized).replaceAll("<PAYMENT_PROVIDER_TOKEN>");
    }
}
