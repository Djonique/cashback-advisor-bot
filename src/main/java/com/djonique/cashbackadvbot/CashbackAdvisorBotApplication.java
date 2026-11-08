package com.djonique.cashbackadvbot;

import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.config.PaymentProperties;
import com.djonique.cashbackadvbot.config.ReferralProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({BotProperties.class, ReferralProperties.class, PaymentProperties.class})
public class CashbackAdvisorBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashbackAdvisorBotApplication.class, args);
    }

}
