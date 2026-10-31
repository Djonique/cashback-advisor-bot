package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.UserState;
import com.djonique.cashbackadvbot.repository.UserStateRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStateService {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    private final UserStateRepository userStateRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void setState(AppUser user, DialogState state, Object contextObject) {
        UserState userState = userStateRepository.findById(user.getId())
                .orElseGet(() -> newUserState(user));

        userState.setState(state.name());
        userState.setContext(toJson(contextObject));
        userState.setUpdatedAt(LocalDateTime.now(MOSCOW_ZONE));

        userStateRepository.save(userState);
        log.debug("Состояние пользователя id={} сохранено: {}", user.getId(), state);
    }

    @Transactional(readOnly = true)
    public DialogState getState(AppUser user) {
        return userStateRepository.findById(user.getId())
                .map(UserState::getState)
                .map(this::parseState)
                .orElse(DialogState.IDLE);
    }

    @Transactional(readOnly = true)
    public <T> Optional<T> getContext(AppUser user, Class<T> type) {
        return userStateRepository.findById(user.getId())
                .map(UserState::getContext)
                .filter(json -> !json.isBlank())
                .map(json -> deserialize(json, type));
    }

    @Transactional
    public void clearState(AppUser user) {
        userStateRepository.findById(user.getId())
                .ifPresent(userStateRepository::delete);
        log.debug("Состояние пользователя id={} очищено", user.getId());
    }

    private UserState newUserState(AppUser user) {
        UserState userState = new UserState();
        userState.setUser(user);
        return userState;
    }

    private DialogState parseState(String state) {
        try {
            return DialogState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.warn("Неизвестное состояние '{}', сбрасываем в IDLE", state);
            return DialogState.IDLE;
        }
    }

    private String toJson(Object contextObject) {
        if (contextObject == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(contextObject);
        } catch (JacksonException e) {
            log.error("Не удалось сериализовать контекст диалога в JSON", e);
            throw new IllegalStateException("Ошибка сериализации контекста диалога", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JacksonException e) {
            log.error("Не удалось разобрать контекст диалога: {}", json, e);
            return null;
        }
    }
}
