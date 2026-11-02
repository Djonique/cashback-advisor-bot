package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.TelegramMessageSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.CashbackPercent;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.dto.SelectionInput;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.service.BankCatalogService;
import com.djonique.cashbackadvbot.service.SelectionService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDialogHandler {

    private static final String EMPTY_SELECTION_TEXT = "Вы ничего не выбрали";
    private static final String INVALID_PERCENT_TEXT = "Ошибка! Введите число от 0 до 100";
    private static final String SAVED_TEXT = "✅ Настройки сохранены! Теперь вы можете выбрать карту";
    private static final String STALE_SESSION_TEXT = "Сессия настройки устарела, начните заново";
    private static final String SAVE_ERROR_TEXT = "Не удалось сохранить настройки, начните заново";

    private final UserStateService userStateService;
    private final SelectionService selectionService;
    private final BankCatalogService bankCatalogService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramMessageSender messageSender;

    public void startPercentInput(AppUser user, SelectionContext context, AbsSender sender) {
        Set<Long> selectedCategoryIds = context.selectedCategoryIds();
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            messageSender.send(user.getTelegramChatId(), EMPTY_SELECTION_TEXT, null, sender);
            return;
        }
        if (context.bankId() == null || !bankCatalogService.bankExists(context.bankId())) {
            log.warn("Банк из контекста не найден: bankId={}", context.bankId());
            userStateService.clearState(user);
            messageSender.send(user.getTelegramChatId(), "Банк не найден, начните настройку заново", null, sender);
            return;
        }

        SelectionContext updatedContext = new SelectionContext(
                context.bankId(),
                selectedCategoryIds,
                new HashMap<>(),
                new ArrayList<>(selectedCategoryIds));

        userStateService.setState(user, DialogState.ENTERING_PERCENT, updatedContext);
        log.info("Начат ввод процентов: userId={}, категорий={}",
                user.getId(), updatedContext.pendingCategories().size());
        askNextCategory(user, updatedContext, sender);
    }

    public void askNextCategory(AppUser user, SelectionContext context, AbsSender sender) {
        List<Long> pendingCategories = context.pendingCategories();
        if (pendingCategories == null || pendingCategories.isEmpty()) {
            finishSetup(user, context, sender);
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
            userStateService.setState(user, DialogState.ENTERING_PERCENT, updatedContext);
            askNextCategory(user, updatedContext, sender);
            return;
        }

        String text = "Введите процент кэшбэка для категории «" + categoryName + "» (например, 5 или 7.5):";
        messageSender.send(user.getTelegramChatId(), text, null, sender);
    }

    public void handlePercentInput(AppUser user, String input, AbsSender sender) {
        BigDecimal percent = CashbackPercent.parse(input).orElse(null);
        if (percent == null) {
            messageSender.send(user.getTelegramChatId(), INVALID_PERCENT_TEXT, null, sender);
            return;
        }

        SelectionContext updatedContext;
        try {
            updatedContext = userStateService.updateContext(
                    user,
                    DialogState.ENTERING_PERCENT,
                    SelectionContext.class,
                    current -> applyPercent(current, percent));
        } catch (ValidationException e) {
            log.warn("Ввод процента без активного контекста: userId={}", user.getId());
            userStateService.clearState(user);
            messageSender.send(user.getTelegramChatId(), STALE_SESSION_TEXT, null, sender);
            return;
        }

        log.info("Введён процент: userId={}, percent={}", user.getId(), percent);
        askNextCategory(user, updatedContext, sender);
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

    private void finishSetup(AppUser user, SelectionContext context, AbsSender sender) {
        if (context.bankId() == null) {
            log.warn("Завершение настройки без банка: userId={}", user.getId());
            userStateService.clearState(user);
            messageSender.send(user.getTelegramChatId(), STALE_SESSION_TEXT,
                    keyboardFactory.getMainMenuKeyboard(), sender);
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

        if (inputs.isEmpty() && !selectedCategoryIds.isEmpty()) {
            log.warn("Не удалось собрать проценты для выбранных категорий: userId={}", user.getId());
            userStateService.clearState(user);
            messageSender.send(user.getTelegramChatId(), SAVE_ERROR_TEXT,
                    keyboardFactory.getMainMenuKeyboard(), sender);
            return;
        }

        try {
            selectionService.replaceSelection(user.getId(), context.bankId(), inputs);
        } catch (ValidationException e) {
            log.warn("Некорректная настройка: userId={}, причина={}", user.getId(), e.getMessage());
            userStateService.clearState(user);
            messageSender.send(user.getTelegramChatId(), "Ошибка: " + e.getMessage(),
                    keyboardFactory.getMainMenuKeyboard(), sender);
            return;
        }

        userStateService.clearState(user);

        log.info("Настройки сохранены: userId={}, bankId={}, категорий={}",
                user.getId(), context.bankId(), inputs.size());
        messageSender.send(user.getTelegramChatId(), SAVED_TEXT,
                keyboardFactory.getMainMenuKeyboard(), sender);
    }

}
