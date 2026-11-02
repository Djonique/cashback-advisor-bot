package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.TelegramMessageSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.BankCategory;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.service.BankCatalogService;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHandler {

    private static final String START_TEXT =
            "Привет! Я помогу выбрать выгодную карту для оплаты. Выберите действие:";
    private static final String MENU_TEXT = "Я помогу выбрать выгодную карту для оплаты. Выберите действие:";
    private static final String CHOOSE_BANK_TEXT = "Выберите банк, для которого хотите настроить кешбэк:";
    private static final String CHOOSE_CATEGORIES_TEXT =
            "Выберите категории для кешбэка (нажмите на категорию, чтобы отметить):";
    private static final String CHOOSE_SEARCH_TEXT = "Выберите способ поиска:";
    private static final String ENTER_CATEGORY_TEXT =
            "Введите название категории (например, Супермаркеты):";
    private static final String ENTER_MCC_TEXT =
            "Введите 4-значный MCC-код (например, 5411):";
    private static final String ENTER_STORE_TEXT =
            "Введите название магазина (например, Пятерочка):";
    private static final String IN_DEVELOPMENT = "В разработке...";

    private final UserService userService;
    private final UserStateService userStateService;
    private final BankCatalogService bankCatalogService;
    private final KeyboardFactory keyboardFactory;
    private final SetupDialogHandler setupDialogHandler;
    private final TelegramMessageSender messageSender;

    public void handleStart(Long chatId, String username, AbsSender sender) {
        AppUser user = userService.findOrCreate(chatId, username);
        userStateService.clearState(user);
        log.info("Обработан /start: userId={}, chatId={}", user.getId(), chatId);
        messageSender.send(chatId, START_TEXT, keyboardFactory.getMainMenuKeyboard(), sender);
    }

    public void handleCallback(CallbackQuery callbackQuery, AbsSender sender) {
        String data = callbackQuery.getData();
        if (data == null) {
            log.warn("Callback без data: id={}", callbackQuery.getId());
            messageSender.answerCallback(callbackQuery.getId(), "Некорректный запрос", sender);
            return;
        }

        switch (data) {
            case CallbackData.MENU_MAIN -> editToMainMenu(callbackQuery, sender);
            case CallbackData.MENU_CHOOSE -> handleChoose(callbackQuery, sender);
            case CallbackData.CARD_BY_CATEGORY -> handleCardByCategory(callbackQuery, sender);
            case CallbackData.CARD_BY_MCC -> handleCardByMcc(callbackQuery, sender);
            case CallbackData.CARD_BY_STORE -> handleCardByStore(callbackQuery, sender);
            case CallbackData.MENU_SETUP -> handleSetup(callbackQuery, sender);
            case CallbackData.SETUP_DONE -> handleDone(callbackQuery, sender);
            default -> handlePrefixedCallback(data, callbackQuery, sender);
        }
    }

    private void handlePrefixedCallback(String data, CallbackQuery callbackQuery, AbsSender sender) {
        if (data.startsWith(CallbackData.SETUP_BANK_PREFIX)) {
            handleBankSelected(data, callbackQuery, sender);
        } else if (data.startsWith(CallbackData.SETUP_TOGGLE_PREFIX)) {
            handleCategoryToggle(data, callbackQuery, sender);
        } else {
            messageSender.answerCallback(callbackQuery.getId(), IN_DEVELOPMENT, sender);
        }
    }

    private void handleChoose(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        Long chatId = getChatId(callbackQuery);
        if (user == null || chatId == null) {
            messageSender.answerCallback(callbackQuery.getId(), "Не удалось определить пользователя", sender);
            return;
        }
        if (!ensureState(user, callbackQuery, sender, DialogState.IDLE)) {
            return;
        }
        userStateService.clearState(user);
        messageSender.send(chatId, CHOOSE_SEARCH_TEXT,
                keyboardFactory.getCardSearchMenuKeyboard(), sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCardByCategory(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (!ensureState(user, callbackQuery, sender,
                DialogState.IDLE,
                DialogState.CARD_INPUT_CATEGORY,
                DialogState.CARD_INPUT_MCC,
                DialogState.CARD_INPUT_STORE)) {
            return;
        }
        userStateService.setState(user, DialogState.CARD_INPUT_CATEGORY, null);
        messageSender.send(getChatId(callbackQuery), ENTER_CATEGORY_TEXT, null, sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCardByMcc(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (!ensureState(user, callbackQuery, sender,
                DialogState.IDLE,
                DialogState.CARD_INPUT_CATEGORY,
                DialogState.CARD_INPUT_MCC,
                DialogState.CARD_INPUT_STORE)) {
            return;
        }
        userStateService.setState(user, DialogState.CARD_INPUT_MCC, null);
        messageSender.send(getChatId(callbackQuery), ENTER_MCC_TEXT, null, sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCardByStore(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (!ensureState(user, callbackQuery, sender,
                DialogState.IDLE,
                DialogState.CARD_INPUT_CATEGORY,
                DialogState.CARD_INPUT_MCC,
                DialogState.CARD_INPUT_STORE)) {
            return;
        }
        userStateService.setState(user, DialogState.CARD_INPUT_STORE, null);
        messageSender.send(getChatId(callbackQuery), ENTER_STORE_TEXT, null, sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleSetup(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (user == null) {
            messageSender.answerCallback(callbackQuery.getId(), "Не удалось определить пользователя", sender);
            return;
        }

        userStateService.setState(user, DialogState.CHOOSING_BANK, null);
        List<Bank> banks = bankCatalogService.getBanksOrdered();
        log.info("Настройка кешбэка: userId={}, доступно банков={}", user.getId(), banks.size());
        messageSender.send(getChatId(callbackQuery), CHOOSE_BANK_TEXT,
                keyboardFactory.getBankListKeyboard(banks), sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleBankSelected(String data, CallbackQuery callbackQuery, AbsSender sender) {
        Long bankId = parseId(data.substring(CallbackData.SETUP_BANK_PREFIX.length()));
        AppUser user = resolveUser(callbackQuery);
        if (!ensureState(user, callbackQuery, sender, DialogState.CHOOSING_BANK)) {
            return;
        }
        if (bankId == null || !bankCatalogService.bankExists(bankId)) {
            messageSender.answerCallback(callbackQuery.getId(), "Некорректный банк", sender);
            return;
        }

        List<BankCategory> categories = bankCatalogService.getBankCategories(bankId);
        SelectionContext context = new SelectionContext(
                bankId,
                new LinkedHashSet<>(),
                new HashMap<>(),
                new ArrayList<>());
        userStateService.setState(user, DialogState.CHOOSING_CATEGORIES, context);

        log.info("Выбран банк: bankId={}, userId={}, доступно категорий={}",
                bankId, user.getId(), categories.size());
        messageSender.send(getChatId(callbackQuery), CHOOSE_CATEGORIES_TEXT,
                keyboardFactory.getCategoryKeyboard(categories, context.selectedCategoryIds()), sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCategoryToggle(String data, CallbackQuery callbackQuery, AbsSender sender) {
        Long categoryId = parseId(data.substring(CallbackData.SETUP_TOGGLE_PREFIX.length()));
        AppUser user = resolveUser(callbackQuery);
        if (!ensureState(user, callbackQuery, sender, DialogState.CHOOSING_CATEGORIES)) {
            return;
        }

        SelectionContext context = userStateService.getContext(user, SelectionContext.class).orElse(null);
        if (categoryId == null || context == null || context.bankId() == null) {
            messageSender.answerCallback(callbackQuery.getId(),
                    "Контекст настройки потерян, начните заново", sender);
            return;
        }

        List<BankCategory> categories = bankCatalogService.getBankCategories(context.bankId());
        boolean belongsToBank = categories.stream()
                .anyMatch(bankCategory -> categoryId.equals(bankCategory.getCategory().getId()));
        if (!belongsToBank) {
            messageSender.answerCallback(callbackQuery.getId(),
                    "Эта категория недоступна для выбранного банка", sender);
            return;
        }

        SelectionContext updatedContext;
        try {
            updatedContext = userStateService.updateContext(
                    user,
                    DialogState.CHOOSING_CATEGORIES,
                    SelectionContext.class,
                    current -> toggleCategory(current, context.bankId(), categoryId));
        } catch (ValidationException e) {
            log.warn("Контекст изменился при переключении категории: userId={}", user.getId());
            messageSender.answerCallback(callbackQuery.getId(),
                    "Контекст настройки потерян, начните заново", sender);
            return;
        }

        editCategoryKeyboard(callbackQuery, updatedContext, categories, sender);
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
    }

    private SelectionContext toggleCategory(SelectionContext current, Long bankId, Long categoryId) {
        if (current == null || !bankId.equals(current.bankId())) {
            throw new ValidationException("Контекст настройки изменился");
        }

        Set<Long> selectedIds = new LinkedHashSet<>(current.selectedCategoryIds());
        if (!selectedIds.remove(categoryId)) {
            selectedIds.add(categoryId);
        }

        return new SelectionContext(
                current.bankId(),
                selectedIds,
                new HashMap<>(current.percents()),
                new ArrayList<>(current.pendingCategories()));
    }

    private void handleDone(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (!ensureState(user, callbackQuery, sender, DialogState.CHOOSING_CATEGORIES)) {
            return;
        }
        SelectionContext context = userStateService.getContext(user, SelectionContext.class).orElse(null);
        if (context == null || context.bankId() == null) {
            userStateService.clearState(user);
            messageSender.answerCallback(callbackQuery.getId(),
                    "Контекст настройки потерян, начните заново", sender);
            return;
        }

        log.debug("Выбранные категории: {}", context.selectedCategoryIds());
        messageSender.answerCallback(callbackQuery.getId(), null, sender);
        setupDialogHandler.startPercentInput(user, context, sender);
    }

    private void editCategoryKeyboard(CallbackQuery callbackQuery,
                                      SelectionContext context,
                                      List<BankCategory> categories,
                                      AbsSender sender) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        if (message == null) {
            log.warn("Нет сообщения для обновления клавиатуры: callbackId={}", callbackQuery.getId());
            return;
        }

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(message.getChatId());
        editMarkup.setMessageId(message.getMessageId());
        editMarkup.setReplyMarkup(
                keyboardFactory.getCategoryKeyboard(categories, context.selectedCategoryIds()));

        try {
            sender.execute(editMarkup);
        } catch (TelegramApiException e) {
            log.error("Не удалось обновить клавиатуру категорий chatId={}", message.getChatId(), e);
        }
    }

    private void editToMainMenu(CallbackQuery callbackQuery, AbsSender sender) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        if (message == null) {
            log.warn("Нет сообщения для редактирования: callbackId={}", callbackQuery.getId());
            messageSender.answerCallback(callbackQuery.getId(), "Не удалось обновить меню", sender);
            return;
        }

        AppUser user = resolveUser(callbackQuery);
        if (user != null) {
            userStateService.clearState(user);
        }
        messageSender.answerCallback(callbackQuery.getId(), null, sender);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(message.getMessageId());
        editMessage.setText(MENU_TEXT);
        editMessage.setReplyMarkup(keyboardFactory.getMainMenuKeyboard());

        try {
            sender.execute(editMessage);
        } catch (TelegramApiException e) {
            log.warn("Не удалось отредактировать сообщение, отправляю новое", e);
            messageSender.send(message.getChatId(), MENU_TEXT,
                    keyboardFactory.getMainMenuKeyboard(), sender);
        }
    }

    private Long parseId(String rawId) {
        try {
            return Long.valueOf(rawId);
        } catch (NumberFormatException e) {
            log.warn("Некорректный идентификатор в callback: '{}'", rawId);
            return null;
        }
    }

    private boolean ensureState(AppUser user,
                                CallbackQuery callbackQuery,
                                AbsSender sender,
                                DialogState... allowedStates) {
        if (user == null) {
            messageSender.answerCallback(callbackQuery.getId(),
                    "Не удалось определить пользователя", sender);
            return false;
        }
        DialogState current = userStateService.getState(user);
        if (!Set.of(allowedStates).contains(current)) {
            log.debug("Callback вне ожидаемого состояния: userId={}, current={}",
                    user.getId(), current);
            messageSender.answerCallback(callbackQuery.getId(),
                    "Сессия устарела, начните заново", sender);
            return false;
        }
        return true;
    }

    private AppUser resolveUser(CallbackQuery callbackQuery) {
        Long chatId = getChatId(callbackQuery);
        if (chatId == null) {
            log.warn("Нет chatId в callback: id={}", callbackQuery.getId());
            return null;
        }
        String username = callbackQuery.getFrom() != null ? callbackQuery.getFrom().getUserName() : null;
        return userService.findOrCreate(chatId, username);
    }

    private Long getChatId(CallbackQuery callbackQuery) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        return message != null ? message.getChatId() : null;
    }
}
