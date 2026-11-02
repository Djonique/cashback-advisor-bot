package com.djonique.cashbackadvbot.domain;

import java.util.regex.Pattern;

/**
 * MCC-код — всегда строка из четырёх цифр с ведущими нулями.
 */
public final class MccCode {

    private static final Pattern PATTERN = Pattern.compile("\\d{4}");

    private MccCode() {
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches();
    }
}
