package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.repository.AppUserRepository;
import com.djonique.cashbackadvbot.repository.UserBankSelectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyNotificationService {

    private static final List<String> RUSSIAN_TIMEZONES = List.of(
            "Europe/Kaliningrad",
            "Europe/Moscow",
            "Europe/Samara",
            "Asia/Yekaterinburg",
            "Asia/Omsk",
            "Asia/Krasnoyarsk",
            "Asia/Irkutsk",
            "Asia/Yakutsk",
            "Asia/Vladivostok",
            "Asia/Magadan",
            "Asia/Kamchatka");

    private static final String NOTIFICATION_TEXT = "Выберите категории кэшбэка на %s";

    private final AppUserRepository appUserRepository;
    private final UserBankSelectionRepository userBankSelectionRepository;
    private final AbsSender absSender;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;

    /**
     * Запускается каждый час в 05 минут. Для каждой зоны РФ проверяет,
     * наступило ли в ней 00:05 первого числа, и рассылает напоминание.
     */
    @Scheduled(cron = "0 5 * * * *")
    @Transactional
    public void processNewMonth() {
        for (String timezone : RUSSIAN_TIMEZONES) {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            if (now.getDayOfMonth() != 1 || now.getHour() != 0) {
                continue;
            }

            YearMonth currentMonth = YearMonth.now(zoneId);
            userBankSelectionRepository.deleteOldSelections(currentMonth.toString());

            List<AppUser> users = appUserRepository.findAllByTimezone(timezone);
            if (users.isEmpty()) {
                continue;
            }

            String monthName = now.getMonth()
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.of("ru"));
            log.info("Новый месяц: timezone={}, month={}, пользователей={}",
                    timezone, currentMonth, users.size());

            for (AppUser user : users) {
                userStateService.clearState(user);
                sendNotification(user, monthName);
            }
        }
    }

    private void sendNotification(AppUser user, String monthName) {
        SendMessage message = new SendMessage();
        message.setChatId(user.getTelegramChatId());
        message.setText(String.format(NOTIFICATION_TEXT, monthName));
        message.setReplyMarkup(keyboardFactory.getMonthlyNotificationKeyboard());

        try {
            absSender.execute(message);
            log.info("Напоминание отправлено: userId={}, timezone={}",
                    user.getId(), user.getTimezone());
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить напоминание: userId={}, chatId={}",
                    user.getId(), user.getTelegramChatId(), e);
        }
    }
}
