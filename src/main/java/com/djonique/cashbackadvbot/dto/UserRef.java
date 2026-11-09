package com.djonique.cashbackadvbot.dto;

/*
 * Ссылка на пользователя без утечки JPA-сущности в слой бота.
 */
public record UserRef(Long id, Long telegramChatId) {
}
