package com.djonique.cashbackadvbot.config;

import com.djonique.cashbackadvbot.bot.CashbackTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.telegram", name = "registration-enabled",
        havingValue = "true", matchIfMissing = true)
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public ApplicationRunner botRegistrationRunner(TelegramBotsApi botsApi, CashbackTelegramBot bot) {
        return args -> {
            try {
                BotSession session = botsApi.registerBot(bot);
                bot.bindSession(session);
                log.info("Telegram-бот зарегистрирован: {}", bot.getBotUsername());
            } catch (TelegramApiException e) {
                throw new IllegalStateException(
                        "Не удалось зарегистрировать Telegram-бота. Проверьте TELEGRAM_BOT_TOKEN", e);
            }
        };
    }
}
