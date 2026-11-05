package com.djonique.cashbackadvbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.support")
public record SupportProperties(String burgerUrl, String serverUrl, String deployUrl) {
}
