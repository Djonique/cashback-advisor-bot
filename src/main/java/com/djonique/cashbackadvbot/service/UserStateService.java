package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.UserState;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.repository.UserStateRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStateService {

    private final UserStateRepository userStateRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Transactional
    public void setState(Long userId, DialogState state, Object contextObject) {
        UserState userState = userStateRepository.findByIdForUpdate(userId)
                .orElseGet(() -> newUserState(userId));

        userState.setState(state.name());
        userState.setContext(toJson(contextObject));

        userStateRepository.save(userState);
        log.debug("Состояние пользователя id={} сохранено: {}", userId, state);
    }

    @Transactional(readOnly = true)
    public DialogState getState(Long userId) {
        return userStateRepository.findById(userId)
                .map(UserState::getState)
                .map(this::parseState)
                .orElse(DialogState.IDLE);
    }

    @Transactional(readOnly = true)
    public <T> Optional<T> getContext(Long userId, Class<T> type) {
        return userStateRepository.findById(userId)
                .map(UserState::getContext)
                .filter(json -> !json.isBlank())
                .map(json -> deserialize(userId, json, type));
    }

    /*
     * Атомарно читает и изменяет контекст в одной транзакции.
     * Благодаря этому @Version на user_state действительно защищает от потерянных обновлений.
     */
    @Transactional
    public <T> T updateContext(Long userId,
                               DialogState state,
                               Class<T> type,
                               UnaryOperator<T> mutator) {
        UserState userState = userStateRepository.findByIdForUpdate(userId)
                .orElseGet(() -> newUserState(userId));

        T current = userState.getContext() == null || userState.getContext().isBlank()
                ? null
                : deserialize(userId, userState.getContext(), type);
        T updated = mutator.apply(current);

        userState.setState(state.name());
        userState.setContext(toJson(updated));

        userStateRepository.save(userState);
        log.debug("Состояние пользователя id={} сохранено: {}", userId, state);
        return updated;
    }

    @Transactional
    public void clearState(Long userId) {
        userStateRepository.deleteByUserId(userId);
        log.debug("Состояние пользователя id={} очищено", userId);
    }

    @Transactional
    public void clearStates(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        userStateRepository.deleteByUserIds(userIds);
        log.debug("Состояния {} пользователей очищены", userIds.size());
    }

    private UserState newUserState(Long userId) {
        UserState userState = new UserState();
        userState.setUser(entityManager.getReference(AppUser.class, userId));
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

    private <T> T deserialize(Long userId, String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JacksonException e) {
            log.error("Не удалось разобрать контекст диалога userId={} в тип {}",
                    userId, type.getSimpleName(), e);
            throw new ValidationException("Контекст диалога повреждён, начните заново");
        }
    }
}
