package com.djonique.cashbackadvbot;

import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.config.GithubProperties;
import com.djonique.cashbackadvbot.config.SupportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BotProperties.class, SupportProperties.class, GithubProperties.class})
public class CashbackAdvisorBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashbackAdvisorBotApplication.class, args);
    }

}
