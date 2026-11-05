package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.config.GithubProperties;
import com.djonique.cashbackadvbot.config.SupportProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupportHandler {

    private static final String SUPPORT_MENU_TEXT =
            "Спасибо, что хотите поддержать проект! Выберите вариант:";
    private static final String SUPPORT_THANKS_TEXT =
            "Спасибо за поддержку! Перейдите по ссылке для перевода: ";
    private static final String SOURCE_CODE_TEXT =
            "Исходный код проекта доступен на GitHub: %s";
    private static final String IN_DEVELOPMENT = "В разработке...";
    private static final String EDIT_ERROR_TEXT = "Не удалось обновить сообщение";

    private final BotSender botSender;
    private final KeyboardFactory keyboardFactory;
    private final SupportProperties supportProperties;
    private final GithubProperties githubProperties;
    private final CallbackSupport callbackSupport;

    public void sendMenu(Long chatId) {
        botSender.send(chatId, SUPPORT_MENU_TEXT, keyboardFactory.getSupportMenuKeyboard());
    }

    public void handleMenu(CallbackQuery callbackQuery) {
        if (!callbackSupport.editText(callbackQuery, SUPPORT_MENU_TEXT,
                keyboardFactory.getSupportMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    public void handlePayment(CallbackQuery callbackQuery, String data) {
        String url = switch (data) {
            case CallbackData.SUPPORT_100 -> supportProperties.burgerUrl();
            case CallbackData.SUPPORT_250 -> supportProperties.serverUrl();
            case CallbackData.SUPPORT_500 -> supportProperties.deployUrl();
            default -> null;
        };
        if (url == null) {
            botSender.answerCallback(callbackQuery.getId(), IN_DEVELOPMENT);
            return;
        }
        if (!callbackSupport.editText(callbackQuery, SUPPORT_THANKS_TEXT + url,
                keyboardFactory.getMainMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    public void sendSourceCode(Long chatId) {
        String text = String.format(SOURCE_CODE_TEXT, githubProperties.url());
        botSender.send(chatId, text, keyboardFactory.getMainMenuKeyboard());
    }
}
