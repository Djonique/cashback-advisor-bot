package com.djonique.cashbackadvbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.referral")
public record ReferralProperties(
        String tbank,
        String alfa,
        String vtb,
        String ozon,
        String yandex,
        String wb) {
}
