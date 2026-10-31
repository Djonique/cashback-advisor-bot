package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.BankCategory;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.repository.BankCategoryRepository;
import com.djonique.cashbackadvbot.repository.BankRepository;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHandler {

    private static final String START_TEXT =
            "Привет! Я помогу выбрать выгодную карту для оплаты. Выбери действие:";
    private static final String MENU_TEXT = "Выбери действие:";
    private static final String CHOOSE_BANK_TEXT = "Выберите банк, для которого хотите настроить кэшбэк:";
    private static final String CHOOSE_CATEGORIES_TEXT =
            "Выберите категории для кэшбэка (нажмите на категорию, чтобы отметить):";
    private static final String CHOOSE_SEARCH_TEXT = "Выберите способ поиска:";
    private static final String ENTER_CATEGORY_TEXT =
            "Введите название категории (например, Супермаркеты):";
    private static final String ENTER_MCC_TEXT =
            "Введите 4-значный MCC-код (например, 5411):";
    private static final String CALLBACK_MAIN = "menu:main";
    private static final String CALLBACK_CHOOSE = "menu:choose";
    private static final String CALLBACK_SETUP = "menu:setup";
    private static final String CALLBACK_BANK_PREFIX = "setup:bank:";
    private static final String CALLBACK_TOGGLE_PREFIX = "setup:toggle:";
    private static final String CALLBACK_DONE = "setup:done";
    private static final String CALLBACK_CARD_BY_CATEGORY = "card:by_category";
    private static final String CALLBACK_CARD_BY_MCC = "card:by_mcc";
    private static final String IN_DEVELOPMENT = "В разработке...";

    private final UserService userService;
    private final UserStateService userStateService;
    private final BankRepository bankRepository;
    private final BankCategoryRepository bankCategoryRepository;
    private final KeyboardFactory keyboardFactory;
    private final SetupDialogHandler setupDialogHandler;

    public void handleStart(Long chatId, String username, AbsSender sender) {
        AppUser user = userService.findOrCreate(chatId, username);
        log.info("Обработан /start: userId={}, chatId={}", user.getId(), chatId);
        sendMessage(chatId, START_TEXT, keyboardFactory.getMainMenuKeyboard(), sender);
    }

    public void handleCallback(CallbackQuery callbackQuery, AbsSender sender) {
        String data = callbackQuery.getData();
        if (data == null) {
            log.warn("Callback без data: id={}", callbackQuery.getId());
            return;
        }
        if (CALLBACK_MAIN.equals(data)) {
            editToMainMenu(callbackQuery, sender);
        } else if (CALLBACK_CHOOSE.equals(data)) {
            handleChoose(callbackQuery, sender);
        } else if (CALLBACK_CARD_BY_CATEGORY.equals(data)) {
            handleCardByCategory(callbackQuery, sender);
        } else if (CALLBACK_CARD_BY_MCC.equals(data)) {
            handleCardByMcc(callbackQuery, sender);
        } else if (CALLBACK_SETUP.equals(data)) {
            handleSetup(callbackQuery, sender);
        } else if (CALLBACK_DONE.equals(data)) {
            handleDone(callbackQuery, sender);
        } else if (data.startsWith(CALLBACK_BANK_PREFIX)) {
            handleBankSelected(data, callbackQuery, sender);
        } else if (data.startsWith(CALLBACK_TOGGLE_PREFIX)) {
            handleCategoryToggle(data, callbackQuery, sender);
        } else {
            answerCallback(callbackQuery.getId(), IN_DEVELOPMENT, sender);
        }
    }

    private void handleChoose(CallbackQuery callbackQuery, AbsSender sender) {
        sendMessage(getChatId(callbackQuery), CHOOSE_SEARCH_TEXT,
                keyboardFactory.getCardSearchMenuKeyboard(), sender);
        answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCardByCategory(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (user == null) {
            answerCallback(callbackQuery.getId(), "Не удалось определить пользователя", sender);
            return;
        }
        userStateService.setState(user, DialogState.CARD_INPUT_CATEGORY, null);
        sendMessage(getChatId(callbackQuery), ENTER_CATEGORY_TEXT, null, sender);
        answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCardByMcc(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (user == null) {
            answerCallback(callbackQuery.getId(), "Не удалось определить пользователя", sender);
            return;
        }
        userStateService.setState(user, DialogState.CARD_INPUT_MCC, null);
        sendMessage(getChatId(callbackQuery), ENTER_MCC_TEXT, null, sender);
        answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleSetup(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        if (user == null) {
            answerCallback(callbackQuery.getId(), "Не удалось определить пользователя", sender);
            return;
        }

        userStateService.setState(user, DialogState.CHOOSING_BANK, null);
        List<Bank> banks = bankRepository.findAllByOrderByDisplayOrderAsc();
        log.info("Настройка кэшбэка: userId={}, доступно банков={}", user.getId(), banks.size());
        sendMessage(getChatId(callbackQuery), CHOOSE_BANK_TEXT,
                keyboardFactory.getBankListKeyboard(banks), sender);
        answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleBankSelected(String data, CallbackQuery callbackQuery, AbsSender sender) {
        Long bankId = parseId(data.substring(CALLBACK_BANK_PREFIX.length()));
        AppUser user = resolveUser(callbackQuery);
        if (bankId == null || user == null) {
            answerCallback(callbackQuery.getId(), "Некорректный банк", sender);
            return;
        }

        List<BankCategory> categories = bankCategoryRepository.findAllByBankId(bankId);
        SelectionContext context = new SelectionContext(
                Math.toIntExact(bankId),
                new HashSet<>(),
                new HashMap<>(),
                new ArrayList<>());
        userStateService.setState(user, DialogState.CHOOSING_CATEGORIES, context);

        log.info("Выбран банк: bankId={}, userId={}, доступно категорий={}",
                bankId, user.getId(), categories.size());
        sendMessage(getChatId(callbackQuery), CHOOSE_CATEGORIES_TEXT,
                keyboardFactory.getCategoryKeyboard(categories, context.selectedCategoryIds()), sender);
        answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleCategoryToggle(String data, CallbackQuery callbackQuery, AbsSender sender) {
        Long categoryId = parseId(data.substring(CALLBACK_TOGGLE_PREFIX.length()));
        AppUser user = resolveUser(callbackQuery);
        SelectionContext context = user != null
                ? userStateService.getContext(user, SelectionContext.class).orElse(null)
                : null;
        if (categoryId == null || user == null || context == null || context.bankId() == null) {
            answerCallback(callbackQuery.getId(), "Контекст настройки потерян, начните заново", sender);
            return;
        }

        Set<Long> selectedIds = context.selectedCategoryIds() == null
                ? new HashSet<>()
                : new HashSet<>(context.selectedCategoryIds());
        if (!selectedIds.remove(categoryId)) {
            selectedIds.add(categoryId);
        }

        Map<Long, BigDecimal> percents = context.percents() == null
                ? new HashMap<>()
                : new HashMap<>(context.percents());
        List<Long> pendingCategories = context.pendingCategories() == null
                ? new ArrayList<>()
                : new ArrayList<>(context.pendingCategories());

        SelectionContext updatedContext = new SelectionContext(
                context.bankId(), selectedIds, percents, pendingCategories);
        userStateService.setState(user, DialogState.CHOOSING_CATEGORIES, updatedContext);
        editCategoryKeyboard(callbackQuery, updatedContext, sender);
        answerCallback(callbackQuery.getId(), null, sender);
    }

    private void handleDone(CallbackQuery callbackQuery, AbsSender sender) {
        AppUser user = resolveUser(callbackQuery);
        SelectionContext context = user != null
                ? userStateService.getContext(user, SelectionContext.class).orElse(null)
                : null;
        if (user == null || context == null) {
            answerCallback(callbackQuery.getId(), "Контекст настройки потерян, начните заново", sender);
            return;
        }

        log.info("Selected categories: {}", context.selectedCategoryIds());
        answerCallback(callbackQuery.getId(), null, sender);
        setupDialogHandler.startPercentInput(user, context, sender);
    }

    private void editCategoryKeyboard(CallbackQuery callbackQuery, SelectionContext context, AbsSender sender) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        if (message == null) {
            log.warn("Нет сообщения для обновления клавиатуры: callbackId={}", callbackQuery.getId());
            return;
        }

        List<BankCategory> categories =
                bankCategoryRepository.findAllByBankId(Long.valueOf(context.bankId()));
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
            answerCallback(callbackQuery.getId(), "Не удалось обновить меню", sender);
            return;
        }

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(message.getMessageId());
        editMessage.setText(MENU_TEXT);
        editMessage.setReplyMarkup(keyboardFactory.getMainMenuKeyboard());

        try {
            sender.execute(editMessage);
        } catch (TelegramApiException e) {
            log.warn("Не удалось отредактировать сообщение, отправляю новое", e);
            sendMessage(message.getChatId(), MENU_TEXT, keyboardFactory.getMainMenuKeyboard(), sender);
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

    private void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard, AbsSender sender) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение chatId={}", chatId, e);
        }
    }

    private void answerCallback(String callbackQueryId, String text, AbsSender sender) {
        try {
            sender.execute(new AnswerCallbackQuery(callbackQueryId, text, false, null, null));
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на callback {}", callbackQueryId, e);
        }
    }
}
