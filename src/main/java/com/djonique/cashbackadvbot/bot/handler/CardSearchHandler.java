package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.dto.UserRef;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardSearchHandler {

    private static final String CHOOSE_SEARCH_TEXT = "Выберите способ поиска:";
    private static final String USER_ERROR_TEXT = "Не удалось определить пользователя";
    private static final String EDIT_ERROR_TEXT = "Не удалось обновить меню";

    private static final String ENTER_STORE_TEXT =
            "Введите название магазина (например, Пятёрочка):";
    private static final String ENTER_CATEGORY_TEXT =
            "Введите название категории (например, Супермаркеты):";
    private static final String ENTER_MCC_TEXT =
            "Введите 4-значный MCC-код (например, 5411):";

    private final CallbackSupport callbackSupport;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;

    public void handleChoose(CallbackQuery callbackQuery) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (callbackSupport.ensureState(user, callbackQuery, DialogState.IDLE)) {
            return;
        }
        userStateService.clearState(user.id());
        showSearchMenu(callbackQuery);
    }

    public void handleMenu(CallbackQuery callbackQuery) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (user == null) {
            botSender.answerCallback(callbackQuery.getId(), USER_ERROR_TEXT);
            return;
        }
        userStateService.clearState(user.id());
        showSearchMenu(callbackQuery);
    }

    public void handleCardByStore(CallbackQuery callbackQuery) {
        openInput(callbackQuery, DialogState.CARD_INPUT_STORE, ENTER_STORE_TEXT);
    }

    public void handleCardByCategory(CallbackQuery callbackQuery) {
        openInput(callbackQuery, DialogState.CARD_INPUT_CATEGORY, ENTER_CATEGORY_TEXT);
    }

    public void handleCardByMcc(CallbackQuery callbackQuery) {
        openInput(callbackQuery, DialogState.CARD_INPUT_MCC, ENTER_MCC_TEXT);
    }

    private void openInput(CallbackQuery callbackQuery, DialogState state, String text) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (callbackSupport.ensureState(user, callbackQuery,
                DialogState.IDLE,
                DialogState.CARD_INPUT_STORE,
                DialogState.CARD_INPUT_CATEGORY,
                DialogState.CARD_INPUT_MCC)) {
            return;
        }
        userStateService.setState(user.id(), state, null);
        if (!callbackSupport.editOrSend(callbackQuery, text,
                keyboardFactory.getBackToCardMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private void showSearchMenu(CallbackQuery callbackQuery) {
        if (!callbackSupport.editOrSend(callbackQuery, CHOOSE_SEARCH_TEXT,
                keyboardFactory.getCardSearchMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }
}
