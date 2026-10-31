package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.repository.AppUserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public AppUser findOrCreate(Long telegramChatId, String username) {
        return appUserRepository.findByTelegramChatId(telegramChatId)
                .map(user -> updateUsernameIfChanged(user, username))
                .orElseGet(() -> createUser(telegramChatId, username));
    }

    private AppUser createUser(Long telegramChatId, String username) {
        AppUser user = new AppUser();
        user.setTelegramChatId(telegramChatId);
        user.setUsername(username);
        AppUser saved = appUserRepository.save(user);
        log.info("Создан новый пользователь: id={}, chatId={}", saved.getId(), telegramChatId);
        return saved;
    }

    private AppUser updateUsernameIfChanged(AppUser user, String username) {
        if (!Objects.equals(user.getUsername(), username)) {
            log.info("Обновлён username пользователя: id={}", user.getId());
            user.setUsername(username);
        }
        return user;
    }
}
