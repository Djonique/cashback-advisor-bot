package com.djonique.cashbackadvbot.config;

import com.djonique.cashbackadvbot.bot.CashbackTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BotConfig {

    private final ObjectProvider<TelegramBotsApi> telegramBotsApiProvider;
    private final ObjectProvider<CashbackTelegramBot> cashbackTelegramBotProvider;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = telegramBotsApiProvider.getObject();
            CashbackTelegramBot bot = cashbackTelegramBotProvider.getObject();
            botsApi.registerBot(bot);
            log.info("Telegram-бот зарегистрирован: {}", bot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Не удалось зарегистрировать Telegram-бота. Проверьте TELEGRAM_BOT_TOKEN", e);
        }
    }
}
