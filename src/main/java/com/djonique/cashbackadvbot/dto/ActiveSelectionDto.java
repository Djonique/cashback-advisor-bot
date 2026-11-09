package com.djonique.cashbackadvbot.dto;

import java.math.BigDecimal;

public record ActiveSelectionDto(
        Long bankId,
        String bankName,
        Long categoryId,
        String categorySlug,
        String categoryName,
        BigDecimal percent) {
}
