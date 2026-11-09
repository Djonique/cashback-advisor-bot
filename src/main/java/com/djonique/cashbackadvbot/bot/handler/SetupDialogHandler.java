package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.CashbackPercent;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.dto.SelectionInput;
import com.djonique.cashbackadvbot.dto.UserRef;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.service.BankCatalogService;
import com.djonique.cashbackadvbot.service.SelectionService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDialogHandler {

    private static final String EMPTY_SELECTION_TEXT = "Вы ничего не выбрали";
    private static final String INVALID_PERCENT_TEXT = "Ошибка! Введите число от 0 до 100";
    private static final String SAVED_TEXT = "✅ Настройки сохранены!";
    private static final String STALE_SESSION_TEXT = "Сессия настройки устарела, начните заново";
    private static final String SAVE_ERROR_TEXT = "Не удалось сохранить настройки, начните заново";
    private static final String BANK_NOT_FOUND_TEXT = "Банк не найден, начните настройку заново";

    private final UserStateService userStateService;
    private final SelectionService selectionService;
    private final BankCatalogService bankCatalogService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;

    public void startPercentInput(UserRef user, SelectionContext context) {
        startPercentInput(user, context, null);
    }

    public void startPercentInput(UserRef user, SelectionContext context, CallbackQuery callbackQuery) {
        Set<Long> selectedCategoryIds = context.selectedCategoryIds();
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            reply(user, callbackQuery, EMPTY_SELECTION_TEXT, keyboardFactory.getBackToMainKeyboard());
            return;
        }
        if (bankCatalogService.isBankMissing(context.bankId())) {
            log.warn("Банк из контекста не найден: bankId={}", context.bankId());
            userStateService.clearState(user.id());
            reply(user, callbackQuery, BANK_NOT_FOUND_TEXT, keyboardFactory.getMainMenuKeyboard());
            return;
        }

        SelectionContext updatedContext = new SelectionContext(
                context.bankId(),
                selectedCategoryIds,
                new HashMap<>(),
                new ArrayList<>(selectedCategoryIds));

        userStateService.setState(user.id(), DialogState.ENTERING_PERCENT, updatedContext);
        log.info("Начат ввод процентов: userId={}, категорий={}",
                user.id(), updatedContext.pendingCategories().size());
        askNextCategory(user, updatedContext, callbackQuery);
    }

    public void handlePercentInput(UserRef user, String input) {
        BigDecimal percent = CashbackPercent.parse(input).orElse(null);
        if (percent == null) {
            botSender.send(user.telegramChatId(), INVALID_PERCENT_TEXT, null);
            return;
        }

        SelectionContext updatedContext;
        try {
            updatedContext = userStateService.updateContext(
                    user.id(),
                    DialogState.ENTERING_PERCENT,
                    SelectionContext.class,
                    current -> applyPercent(current, percent));
        } catch (ValidationException e) {
            log.warn("Ввод процента без активного контекста: userId={}", user.id());
            userStateService.clearState(user.id());
            botSender.send(user.telegramChatId(), STALE_SESSION_TEXT,
                    keyboardFactory.getMainMenuKeyboard());
            return;
        }

        log.debug("Введён процент: userId={}, percent={}", user.id(), percent);
        askNextCategory(user, updatedContext);
    }

    private void askNextCategory(UserRef user, SelectionContext context) {
        askNextCategory(user, context, null);
    }

    private void askNextCategory(UserRef user, SelectionContext context, CallbackQuery callbackQuery) {
        List<Long> pendingCategories = context.pendingCategories();
        if (pendingCategories == null || pendingCategories.isEmpty()) {
            finishSetup(user, context);
            return;
        }

        Long categoryId = pendingCategories.getFirst();
        String categoryName = bankCatalogService.findCategoryName(categoryId).orElse(null);

        if (categoryName == null) {
            log.warn("Категория из контекста не найдена: categoryId={}, пропускаем", categoryId);
            List<Long> remaining = new ArrayList<>(pendingCategories);
            remaining.removeFirst();
            SelectionContext updatedContext = new SelectionContext(
                    context.bankId(),
                    context.selectedCategoryIds(),
                    context.percents(),
                    remaining);
            userStateService.setState(user.id(), DialogState.ENTERING_PERCENT, updatedContext);
            askNextCategory(user, updatedContext, callbackQuery);
            return;
        }

        String text = "Введите процент кешбэка для категории «" + categoryName + "» (например, 5 или 7.5):";
        reply(user, callbackQuery, text, null);
    }

    private void reply(UserRef user,
                       CallbackQuery callbackQuery,
                       String text,
                       InlineKeyboardMarkup keyboard) {
        if (callbackQuery != null && callbackQuery.getMessage() != null) {
            MaybeInaccessibleMessage message = callbackQuery.getMessage();
            if (botSender.editMessageText(message.getChatId(), message.getMessageId(), text, keyboard)) {
                return;
            }
        }
        botSender.send(user.telegramChatId(), text, keyboard);
    }

    private void finishSetup(UserRef user, SelectionContext context) {
        if (context.bankId() == null) {
            log.warn("Завершение настройки без банка: userId={}", user.id());
            userStateService.clearState(user.id());
            botSender.send(user.telegramChatId(), STALE_SESSION_TEXT,
                    keyboardFactory.getMainMenuKeyboard());
            return;
        }

        Map<Long, BigDecimal> percents = context.percents() == null ? Map.of() : context.percents();
        Set<Long> selectedCategoryIds = context.selectedCategoryIds() == null
                ? Set.of()
                : context.selectedCategoryIds();
        List<SelectionInput> inputs = selectedCategoryIds.stream()
                .filter(percents::containsKey)
                .map(categoryId -> new SelectionInput(categoryId, percents.get(categoryId)))
                .toList();

        if (inputs.size() != selectedCategoryIds.size()) {
            log.warn("Не удалось собрать проценты для всех категорий: userId={}, выбрано={}, собрано={}",
                    user.id(), selectedCategoryIds.size(), inputs.size());
            userStateService.clearState(user.id());
            botSender.send(user.telegramChatId(), SAVE_ERROR_TEXT,
                    keyboardFactory.getMainMenuKeyboard());
            return;
        }

        try {
            selectionService.replaceSelection(user.id(), context.bankId(), inputs);
        } catch (ValidationException e) {
            log.warn("Некорректная настройка: userId={}, причина={}", user.id(), e.getMessage());
            userStateService.clearState(user.id());
            botSender.send(user.telegramChatId(), "Ошибка: " + e.getMessage(),
                    keyboardFactory.getMainMenuKeyboard());
            return;
        } catch (DataIntegrityViolationException e) {
            log.warn("Конфликт при сохранении настроек: userId={}, bankId={}",
                    user.id(), context.bankId(), e);
            userStateService.clearState(user.id());
            botSender.send(user.telegramChatId(),
                    "Настройки изменились в другом сообщении, начните заново",
                    keyboardFactory.getMainMenuKeyboard());
            return;
        }

        userStateService.clearState(user.id());

        log.info("Настройки сохранены: userId={}, bankId={}, категорий={}",
                user.id(), context.bankId(), inputs.size());
        botSender.send(user.telegramChatId(), SAVED_TEXT, keyboardFactory.getMainMenuKeyboard());
    }

    private SelectionContext applyPercent(SelectionContext current, BigDecimal percent) {
        if (current == null
                || current.pendingCategories() == null
                || current.pendingCategories().isEmpty()) {
            throw new ValidationException(STALE_SESSION_TEXT);
        }

        Long categoryId = current.pendingCategories().getFirst();

        Map<Long, BigDecimal> percents = current.percents() == null
                ? new HashMap<>()
                : new HashMap<>(current.percents());
        percents.put(categoryId, percent);

        List<Long> pendingCategories = new ArrayList<>(current.pendingCategories());
        pendingCategories.removeFirst();

        return new SelectionContext(
                current.bankId(),
                current.selectedCategoryIds(),
                percents,
                pendingCategories);
    }
}
