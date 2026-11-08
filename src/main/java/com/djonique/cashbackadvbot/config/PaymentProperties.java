package com.djonique.cashbackadvbot.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.telegram.payments")
@Validated
public record PaymentProperties(@NotBlank String providerToken) {

    @Override
    public String toString() {
        return "PaymentProperties[providerToken=***]";
    }
}
