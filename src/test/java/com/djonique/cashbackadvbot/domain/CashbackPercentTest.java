package com.djonique.cashbackadvbot.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CashbackPercentTest {

    @Test
    void shouldParsePercent_whenInputIsValid() {
        assertThat(CashbackPercent.parse("5"))
                .hasValueSatisfying(percent -> assertThat(percent).isEqualByComparingTo("5"));
        assertThat(CashbackPercent.parse("5,5"))
                .hasValueSatisfying(percent -> assertThat(percent).isEqualByComparingTo("5.5"));
        assertThat(CashbackPercent.parse("100"))
                .hasValueSatisfying(percent -> assertThat(percent).isEqualByComparingTo("100"));
        assertThat(CashbackPercent.parse(" 7.25 "))
                .hasValueSatisfying(percent -> assertThat(percent).isEqualByComparingTo("7.25"));
    }

    @Test
    void shouldRejectPercent_whenInputIsOutOfRange() {
        assertThat(CashbackPercent.parse("0")).isEmpty();
        assertThat(CashbackPercent.parse("-1")).isEmpty();
        assertThat(CashbackPercent.parse("100.01")).isEmpty();
        assertThat(CashbackPercent.parse("101")).isEmpty();
    }

    @Test
    void shouldRejectPercent_whenInputHasUnexpectedFormat() {
        assertThat(CashbackPercent.parse("")).isEmpty();
        assertThat(CashbackPercent.parse("   ")).isEmpty();
        assertThat(CashbackPercent.parse("abc")).isEmpty();
        assertThat(CashbackPercent.parse("5,555")).isEmpty();
        assertThat(CashbackPercent.parse("1234")).isEmpty();
        assertThat(CashbackPercent.parse("1e5")).isEmpty();
    }

    @Test
    void shouldRejectPercent_whenInputUsesHugeExponent() {
        assertThat(CashbackPercent.parse("1e999999999")).isEmpty();
        assertThat(CashbackPercent.parse("1e-999999999")).isEmpty();
    }

    @Test
    void shouldValidatePercentValue() {
        assertThat(CashbackPercent.isValid(new BigDecimal("5"))).isTrue();
        assertThat(CashbackPercent.isValid(new BigDecimal("100"))).isTrue();
        assertThat(CashbackPercent.isValid(new BigDecimal("0"))).isFalse();
        assertThat(CashbackPercent.isValid(new BigDecimal("100.01"))).isFalse();
        assertThat(CashbackPercent.isValid(null)).isFalse();
    }
}
