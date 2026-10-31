package com.djonique.cashbackadvbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.telegram")
@Data
public class BotProperties {

    private String botToken;

    private String botUsername;
}
