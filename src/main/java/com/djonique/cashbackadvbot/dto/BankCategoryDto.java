package com.djonique.cashbackadvbot.dto;

public record BankCategoryDto(Long id, String slug, String displayName) {

    public BankCategoryDto(Long id, String displayName) {
        this(id, null, displayName);
    }
}
