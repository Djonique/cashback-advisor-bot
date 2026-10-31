package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.dto.SelectionInput;
import com.djonique.cashbackadvbot.repository.BankRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import com.djonique.cashbackadvbot.service.SelectionService;
import com.djonique.cashbackadvbot.service.UserStateService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDialogHandler {

    private static final BigDecimal MAX_PERCENT = BigDecimal.valueOf(100);
    private static final String EMPTY_SELECTION_TEXT = "Вы ничего не выбрали";
    private static final String INVALID_PERCENT_TEXT = "Ошибка! Введите число от 0 до 100";
    private static final String SAVED_TEXT = "✅ Настройки сохранены! Теперь вы можете выбрать карту";

    private final UserStateService userStateService;
    private final SelectionService selectionService;
    private final CategoryRepository categoryRepository;
    private final BankRepository bankRepository;
    private final KeyboardFactory keyboardFactory;

    public void startPercentInput(AppUser user, SelectionContext context, AbsSender sender) {
        Set<Long> selectedCategoryIds = context.selectedCategoryIds();
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            sendMessage(user.getTelegramChatId(), EMPTY_SELECTION_TEXT, null, sender);
            return;
        }
        if (context.bankId() == null || bankRepository.findById(context.bankId().longValue()).isEmpty()) {
            log.warn("Банк из контекста не найден: bankId={}", context.bankId());
            sendMessage(user.getTelegramChatId(), "Банк не найден, начните настройку заново", null, sender);
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
        String categoryName = categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse(null);

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
        sendMessage(user.getTelegramChatId(), text, null, sender);
    }

    public void handlePercentInput(AppUser user, String input, AbsSender sender) {
        SelectionContext context = userStateService.getContext(user, SelectionContext.class)
                .orElse(null);
        if (context == null || context.pendingCategories() == null || context.pendingCategories().isEmpty()) {
            log.warn("Ввод процента без активного контекста: userId={}", user.getId());
            sendMessage(user.getTelegramChatId(), "Сессия настройки устарела, начните заново", null, sender);
            return;
        }

        BigDecimal percent = parsePercent(input);
        if (percent == null) {
            sendMessage(user.getTelegramChatId(), INVALID_PERCENT_TEXT, null, sender);
            return;
        }

        Long categoryId = context.pendingCategories().getFirst();

        Map<Long, BigDecimal> percents = context.percents() == null
                ? new HashMap<>()
                : new HashMap<>(context.percents());
        percents.put(categoryId, percent);

        List<Long> pendingCategories = new ArrayList<>(context.pendingCategories());
        pendingCategories.removeFirst();

        SelectionContext updatedContext = new SelectionContext(
                context.bankId(),
                context.selectedCategoryIds(),
                percents,
                pendingCategories);

        userStateService.setState(user, DialogState.ENTERING_PERCENT, updatedContext);
        log.info("Введён процент: userId={}, categoryId={}, percent={}", user.getId(), categoryId, percent);
        askNextCategory(user, updatedContext, sender);
    }

    private void finishSetup(AppUser user, SelectionContext context, AbsSender sender) {
        Map<Long, BigDecimal> percents = context.percents() == null ? Map.of() : context.percents();
        Set<Long> selectedCategoryIds = context.selectedCategoryIds() == null
                ? Set.of()
                : context.selectedCategoryIds();
        List<SelectionInput> inputs = selectedCategoryIds.stream()
                .filter(percents::containsKey)
                .map(categoryId -> new SelectionInput(categoryId, percents.get(categoryId)))
                .toList();

        selectionService.replaceSelection(
                user.getId(),
                context.bankId().longValue(),
                inputs);
        userStateService.clearState(user);

        log.info("Настройки сохранены: userId={}, bankId={}, категорий={}",
                user.getId(), context.bankId(), inputs.size());
        sendMessage(user.getTelegramChatId(), SAVED_TEXT, keyboardFactory.getMainMenuKeyboard(), sender);
    }

    private BigDecimal parsePercent(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        BigDecimal percent;
        try {
            percent = new BigDecimal(input.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
        if (percent.signum() <= 0 || percent.compareTo(MAX_PERCENT) > 0) {
            return null;
        }
        return percent;
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
}
