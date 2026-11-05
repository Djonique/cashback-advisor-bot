package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppUserRepository appUserRepository;
    private final ConcurrentHashMap<Long, AppUser> cache = new ConcurrentHashMap<>();

    public AppUser findOrCreate(Long telegramChatId, String username) {
        AppUser cached = cache.get(telegramChatId);
        if (cached != null) {
            return updateUsernameIfChanged(cached, username);
        }

        AppUser user = appUserRepository.findByTelegramChatId(telegramChatId)
                .map(existing -> updateUsernameIfChanged(existing, username))
                .orElseGet(() -> createUser(telegramChatId, username));
        cache.put(telegramChatId, user);
        return user;
    }

    private AppUser createUser(Long telegramChatId, String username) {
        AppUser user = new AppUser();
        user.setTelegramChatId(telegramChatId);
        user.setUsername(username);
        try {
            AppUser saved = appUserRepository.save(user);
            log.info("Создан новый пользователь: id={}, chatId={}", saved.getId(), telegramChatId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("Пользователь уже создан параллельным запросом: chatId={}", telegramChatId);
            return appUserRepository.findByTelegramChatId(telegramChatId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Не удалось создать или найти пользователя chatId=" + telegramChatId, e));
        }
    }

    private AppUser updateUsernameIfChanged(AppUser user, String username) {
        if (!Objects.equals(user.getUsername(), username)) {
            user.setUsername(username);
            AppUser saved = appUserRepository.save(user);
            cache.put(saved.getTelegramChatId(), saved);
            log.info("Обновлён username пользователя: id={}", saved.getId());
            return saved;
        }
        return user;
    }
}
