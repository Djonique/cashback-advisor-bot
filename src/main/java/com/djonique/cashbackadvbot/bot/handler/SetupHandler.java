package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.dto.BankCategoryDto;
import com.djonique.cashbackadvbot.dto.BankDto;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.dto.UserRef;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.service.BankCatalogService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetupHandler {

    private static final String CHOOSE_BANK_TEXT = "Выберите банк, для которого хотите настроить кешбэк:";
    private static final String CHOOSE_CATEGORIES_TEXT =
            "Выберите категории для кешбэка (нажмите на категорию, чтобы отметить):";
    private static final String CONTEXT_LOST_TEXT = "Контекст настройки потерян, начните заново";
    private static final String USER_ERROR_TEXT = "Не удалось определить пользователя";
    private static final String EDIT_ERROR_TEXT = "Не удалось обновить меню";
    private static final String INVALID_BANK_TEXT = "Некорректный банк";

    private final CallbackSupport callbackSupport;
    private final UserStateService userStateService;
    private final BankCatalogService bankCatalogService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;
    private final SetupDialogHandler setupDialogHandler;

    public void handleSetup(CallbackQuery callbackQuery) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (user == null) {
            botSender.answerCallback(callbackQuery.getId(), USER_ERROR_TEXT);
            return;
        }
        List<BankDto> banks = prepareBankSelection(user);
        if (!callbackSupport.editOrSend(callbackQuery, CHOOSE_BANK_TEXT,
                keyboardFactory.getBankListKeyboard(banks))) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    public void openBankSelection(UserRef user, Long chatId) {
        List<BankDto> banks = prepareBankSelection(user);
        botSender.send(chatId, CHOOSE_BANK_TEXT, keyboardFactory.getBankListKeyboard(banks));
    }

    private List<BankDto> prepareBankSelection(UserRef user) {
        userStateService.setState(user.id(), DialogState.CHOOSING_BANK, null);
        List<BankDto> banks = bankCatalogService.getBanksOrdered();
        log.info("Настройка кешбэка: userId={}, доступно банков={}", user.id(), banks.size());
        return banks;
    }

    public void handleBankSelected(String data, CallbackQuery callbackQuery) {
        Long bankId = callbackSupport.parseId(data.substring(CallbackData.SETUP_BANK_PREFIX.length()));
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (callbackSupport.ensureState(user, callbackQuery, DialogState.CHOOSING_BANK)) {
            return;
        }
        if (bankCatalogService.isBankMissing(bankId)) {
            botSender.answerCallback(callbackQuery.getId(), INVALID_BANK_TEXT);
            return;
        }

        List<BankCategoryDto> categories = bankCatalogService.getBankCategories(bankId);
        SelectionContext context = new SelectionContext(
                bankId,
                new LinkedHashSet<>(),
                new HashMap<>(),
                new ArrayList<>());
        userStateService.setState(user.id(), DialogState.CHOOSING_CATEGORIES, context);

        log.info("Выбран банк: bankId={}, userId={}, доступно категорий={}",
                bankId, user.id(), categories.size());
        if (!callbackSupport.editOrSend(callbackQuery, CHOOSE_CATEGORIES_TEXT,
                keyboardFactory.getCategoryKeyboard(categories, context.selectedCategoryIds()))) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    public void handleCategoryToggle(String data, CallbackQuery callbackQuery) {
        Long categoryId = callbackSupport.parseId(data.substring(CallbackData.SETUP_TOGGLE_PREFIX.length()));
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (callbackSupport.ensureState(user, callbackQuery, DialogState.CHOOSING_CATEGORIES)) {
            return;
        }

        SelectionContext context;
        try {
            context = userStateService.getContext(user.id(), SelectionContext.class).orElse(null);
        } catch (ValidationException e) {
            userStateService.clearState(user.id());
            botSender.answerCallback(callbackQuery.getId(), CONTEXT_LOST_TEXT);
            return;
        }
        if (categoryId == null || context == null || context.bankId() == null) {
            botSender.answerCallback(callbackQuery.getId(), CONTEXT_LOST_TEXT);
            return;
        }

        if (!bankCatalogService.categoryBelongsToBank(context.bankId(), categoryId)) {
            botSender.answerCallback(callbackQuery.getId(),
                    "Эта категория недоступна для выбранного банка");
            return;
        }

        SelectionContext updatedContext;
        try {
            updatedContext = userStateService.updateContext(
                    user.id(),
                    DialogState.CHOOSING_CATEGORIES,
                    SelectionContext.class,
                    current -> toggleCategory(current, categoryId));
        } catch (ValidationException e) {
            log.warn("Контекст изменился при переключении категории: userId={}", user.id());
            userStateService.clearState(user.id());
            botSender.answerCallback(callbackQuery.getId(), CONTEXT_LOST_TEXT);
            return;
        }

        List<BankCategoryDto> categories = bankCatalogService.getBankCategories(updatedContext.bankId());
        editCategoryKeyboard(callbackQuery, updatedContext, categories);
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    public void handleDone(CallbackQuery callbackQuery) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        if (callbackSupport.ensureState(user, callbackQuery, DialogState.CHOOSING_CATEGORIES)) {
            return;
        }

        SelectionContext context;
        try {
            context = userStateService.getContext(user.id(), SelectionContext.class).orElse(null);
        } catch (ValidationException e) {
            userStateService.clearState(user.id());
            botSender.answerCallback(callbackQuery.getId(), CONTEXT_LOST_TEXT);
            return;
        }
        if (context == null || context.bankId() == null) {
            userStateService.clearState(user.id());
            botSender.answerCallback(callbackQuery.getId(), CONTEXT_LOST_TEXT);
            return;
        }

        log.debug("Выбранные категории: {}", context.selectedCategoryIds());
        botSender.answerCallback(callbackQuery.getId(), null);
        setupDialogHandler.startPercentInput(user, context, callbackQuery);
    }

    private SelectionContext toggleCategory(SelectionContext current, Long categoryId) {
        if (current == null || current.bankId() == null) {
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

    private void editCategoryKeyboard(CallbackQuery callbackQuery,
                                      SelectionContext context,
                                      List<BankCategoryDto> categories) {
        InlineKeyboardMarkup keyboard =
                keyboardFactory.getCategoryKeyboard(categories, context.selectedCategoryIds());
        if (callbackSupport.editMarkup(callbackQuery, keyboard)) {
            return;
        }
        Long chatId = callbackSupport.getChatId(callbackQuery);
        if (chatId == null) {
            log.warn("Нет чата для обновления клавиатуры: callbackId={}", callbackQuery.getId());
            return;
        }
        botSender.send(chatId, CHOOSE_CATEGORIES_TEXT, keyboard);
    }
}
