package com.djonique.cashbackadvbot.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Разбор и валидация процента кешбэка, который вводит пользователь.
 * Ограничиваем формат до BigDecimal, чтобы исключить огромные экспоненты.
 */
public final class CashbackPercent {

    public static final BigDecimal MAX = BigDecimal.valueOf(100);

    private static final Pattern INPUT_PATTERN = Pattern.compile("\\d{1,3}([.,]\\d{1,2})?");
    private static final int MAX_INPUT_LENGTH = 6;

    private CashbackPercent() {
    }

    public static Optional<BigDecimal> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalized = input.trim().replace(',', '.');
        if (normalized.isEmpty()
                || normalized.length() > MAX_INPUT_LENGTH
                || !INPUT_PATTERN.matcher(normalized).matches()) {
            return Optional.empty();
        }

        BigDecimal percent = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
        return isValid(percent) ? Optional.of(percent) : Optional.empty();
    }

    public static boolean isValid(BigDecimal percent) {
        return percent != null
                && percent.signum() > 0
                && percent.compareTo(MAX) <= 0;
    }
}
