package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.repository.AppUserRepository;
import com.djonique.cashbackadvbot.repository.UserBankSelectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyNotificationService {

    private static final String NOTIFICATION_TEXT = "Выберите категории кешбэка на %s";
    private static final ZoneId STORAGE_ZONE = ZoneId.of("Europe/Moscow");
    private static final int PAGE_SIZE = 500;
    private static final long RATE_LIMIT_DELAY_MS = 50L;

    private final AppUserRepository appUserRepository;
    private final UserBankSelectionRepository userBankSelectionRepository;
    private final BotSender botSender;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;

    /**
     * Очистка старых выборов выполняется отдельно от рассылки и по московскому
     * календарю — той же зоне, в которой сохраняются выборы пользователя.
     */
    @Scheduled(cron = "0 20 0 1 * *", zone = "Europe/Moscow")
    public void deleteOldSelections() {
        String currentMonth = YearMonth.now(STORAGE_ZONE).toString();
        userBankSelectionRepository.deleteOldSelections(currentMonth);
        log.info("Удалены выборы старше месяца: {}", currentMonth);
    }

    /**
     * Рассылка в 00:05 первого числа по Москве — в той же зоне, в которой
     * хранится year_month выборов пользователя.
     */
    @Scheduled(cron = "0 5 0 1 * *", zone = "Europe/Moscow")
    public void processNewMonth() {
        String monthName = YearMonth.now(STORAGE_ZONE)
                .getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.of("ru"));

        int page = 0;
        Page<AppUser> users;
        do {
            users = appUserRepository.findAll(
                    PageRequest.of(page++, PAGE_SIZE, Sort.by("id")));
            if (users.isEmpty()) {
                break;
            }
            log.info("Новый месяц: month={}, страница={}, пользователей={}",
                    YearMonth.now(STORAGE_ZONE), page, users.getNumberOfElements());

            List<Long> userIds = users.stream().map(AppUser::getId).toList();
            userStateService.clearStates(userIds);

            for (AppUser user : users) {
                sendNotification(user, monthName);
                sleepBetweenMessages();
            }
        } while (users.hasNext());
    }

    private void sendNotification(AppUser user, String monthName) {
        String text = String.format(NOTIFICATION_TEXT, monthName);
        if (botSender.send(user.getTelegramChatId(), text,
                keyboardFactory.getMonthlyNotificationKeyboard())) {
            log.info("Напоминание отправлено: userId={}", user.getId());
        } else {
            log.error("Не удалось отправить напоминание: userId={}", user.getId());
        }
    }

    private void sleepBetweenMessages() {
        try {
            Thread.sleep(RATE_LIMIT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
