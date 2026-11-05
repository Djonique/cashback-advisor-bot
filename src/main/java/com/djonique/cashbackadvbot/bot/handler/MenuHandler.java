package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHandler {

    private static final String START_TEXT =
            "Привет! Я помогу выбрать карту для оплаты. Выберите действие, либо отправьте МСС-код, " +
                    "название магазина или категорию кешбэка в текстовом сообщении";
    private static final String MENU_TEXT = "Отправьте сообщение с названием магазина, категорию кешбэка или МСС-код";
    private static final String IN_DEVELOPMENT = "В разработке...";
    private static final String EDIT_ERROR_TEXT = "Не удалось обновить меню";

    private final UserService userService;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;
    private final CallbackSupport callbackSupport;
    private final HelpHandler helpHandler;
    private final SupportHandler supportHandler;
    private final CardSearchHandler cardSearchHandler;
    private final SetupHandler setupHandler;

    public void handleStart(Long chatId, String username) {
        AppUser user = userService.findOrCreate(chatId, username);
        userStateService.clearState(user);
        log.info("Обработан /start: userId={}", user.getId());
        botSender.send(chatId, START_TEXT, keyboardFactory.getMainMenuKeyboard());
    }

    public void handleMenu(Long chatId, String username) {
        AppUser user = userService.findOrCreate(chatId, username);
        userStateService.clearState(user);
        log.info("Обработан /menu: userId={}", user.getId());
        botSender.send(chatId, MENU_TEXT, keyboardFactory.getMainMenuKeyboard());
    }

    public void handleCommand(String command, Long chatId, String username) {
        switch (command) {
            case "start" -> handleStart(chatId, username);
            case "menu" -> handleMenu(chatId, username);
            case "settings" -> handleSettings(chatId, username);
            case "help" -> helpHandler.sendHelp(chatId);
            case "support" -> supportHandler.sendMenu(chatId);
            case "source_code" -> supportHandler.sendSourceCode(chatId);
            default -> log.warn("Неизвестная команда: {}", command);
        }
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data == null) {
            log.warn("Callback без data: id={}", callbackQuery.getId());
            botSender.answerCallback(callbackQuery.getId(), "Некорректный запрос");
            return;
        }

        switch (data) {
            case CallbackData.MENU_MAIN -> editToMainMenu(callbackQuery);
            case CallbackData.MENU_CHOOSE -> cardSearchHandler.handleChoose(callbackQuery);
            case CallbackData.CARD_MENU -> cardSearchHandler.handleMenu(callbackQuery);
            case CallbackData.CARD_BY_CATEGORY -> cardSearchHandler.handleCardByCategory(callbackQuery);
            case CallbackData.CARD_BY_MCC -> cardSearchHandler.handleCardByMcc(callbackQuery);
            case CallbackData.CARD_BY_STORE -> cardSearchHandler.handleCardByStore(callbackQuery);
            case CallbackData.MENU_SETUP -> setupHandler.handleSetup(callbackQuery);
            case CallbackData.SETUP_DONE -> setupHandler.handleDone(callbackQuery);
            case CallbackData.MENU_SUPPORT -> supportHandler.handleMenu(callbackQuery);
            case CallbackData.MENU_HELP -> helpHandler.handleMenu(callbackQuery);
            case CallbackData.SUPPORT_100, CallbackData.SUPPORT_250, CallbackData.SUPPORT_500 ->
                    supportHandler.handlePayment(callbackQuery, data);
            case CallbackData.HELP_GENERAL, CallbackData.HELP_SETUP, CallbackData.HELP_STORE,
                 CallbackData.HELP_CATEGORY, CallbackData.HELP_MCC ->
                    helpHandler.handleHelp(callbackQuery, data);
            default -> handlePrefixedCallback(data, callbackQuery);
        }
    }

    private void handlePrefixedCallback(String data, CallbackQuery callbackQuery) {
        if (data.startsWith(CallbackData.SETUP_BANK_PREFIX)) {
            setupHandler.handleBankSelected(data, callbackQuery);
        } else if (data.startsWith(CallbackData.SETUP_TOGGLE_PREFIX)) {
            setupHandler.handleCategoryToggle(data, callbackQuery);
        } else {
            botSender.answerCallback(callbackQuery.getId(), IN_DEVELOPMENT);
        }
    }

    private void handleSettings(Long chatId, String username) {
        AppUser user = userService.findOrCreate(chatId, username);
        setupHandler.openBankSelection(user, chatId);
    }

    private void editToMainMenu(CallbackQuery callbackQuery) {
        AppUser user = callbackSupport.resolveUser(callbackQuery);
        if (user != null) {
            userStateService.clearState(user);
        }
        if (!callbackSupport.editText(callbackQuery, MENU_TEXT, keyboardFactory.getMainMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }
}
