package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackSupport {

    private final UserService userService;
    private final UserStateService userStateService;
    private final BotSender botSender;

    public AppUser resolveUser(CallbackQuery callbackQuery) {
        Long chatId = getChatId(callbackQuery);
        if (chatId == null) {
            log.warn("Нет chatId в callback: id={}", callbackQuery.getId());
            return null;
        }
        String username = callbackQuery.getFrom() != null
                ? callbackQuery.getFrom().getUserName()
                : null;
        return userService.findOrCreate(chatId, username);
    }

    public Long getChatId(CallbackQuery callbackQuery) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        return message != null ? message.getChatId() : null;
    }

    public boolean ensureState(AppUser user, CallbackQuery callbackQuery, DialogState... allowedStates) {
        if (user == null) {
            botSender.answerCallback(callbackQuery.getId(), "Не удалось определить пользователя");
            return false;
        }
        DialogState current = userStateService.getState(user);
        if (!Set.of(allowedStates).contains(current)) {
            log.debug("Callback вне ожидаемого состояния: userId={}, current={}", user.getId(), current);
            botSender.answerCallback(callbackQuery.getId(), "Сессия устарела, начните заново");
            return false;
        }
        return true;
    }

    public Long parseId(String rawId) {
        try {
            return Long.valueOf(rawId);
        } catch (NumberFormatException e) {
            log.warn("Некорректный идентификатор в callback: '{}'", rawId);
            return null;
        }
    }

    public boolean editText(CallbackQuery callbackQuery, String text, InlineKeyboardMarkup keyboard) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        if (message == null) {
            Long chatId = getChatId(callbackQuery);
            return chatId != null && botSender.send(chatId, text, keyboard);
        }
        if (botSender.editMessageText(message.getChatId(), message.getMessageId(), text, keyboard)) {
            return true;
        }
        return botSender.send(message.getChatId(), text, keyboard);
    }

    public boolean editMarkup(CallbackQuery callbackQuery, InlineKeyboardMarkup keyboard) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        return message != null
                && botSender.editMessageReplyMarkup(message.getChatId(), message.getMessageId(), keyboard);
    }
}
