package com.djonique.cashbackadvbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GithubProperties(String url) {
}
