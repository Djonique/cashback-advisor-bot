package com.djonique.cashbackadvbot.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.telegram")
@Validated
@Getter
@Setter
public class BotProperties {

    @NotBlank
    private String botToken;

    @NotBlank
    private String botUsername;

    @Override
    public String toString() {
        return "BotProperties{botUsername='" + botUsername + "'}";
    }
}
