package com.djonique.cashbackadvbot.dto;

import java.math.BigDecimal;

public record SelectionInput(Long categoryId, BigDecimal percent) {
}
