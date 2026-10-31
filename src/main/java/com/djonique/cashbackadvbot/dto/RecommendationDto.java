package com.djonique.cashbackadvbot.dto;

import java.math.BigDecimal;

public record RecommendationDto(String bankName, String categoryName, BigDecimal percent) {
}
