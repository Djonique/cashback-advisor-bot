package com.djonique.cashbackadvbot.dto;

import java.math.BigDecimal;

public record ActiveSelectionDto(
        Long bankId,
        String bankName,
        Long categoryId,
        String categorySlug,
        String categoryName,
        BigDecimal percent) {

    public ActiveSelectionDto(Long bankId,
                              String bankName,
                              Long categoryId,
                              String categoryName,
                              BigDecimal percent) {
        this(bankId, bankName, categoryId, null, categoryName, percent);
    }
}
