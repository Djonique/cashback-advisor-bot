package com.djonique.cashbackadvbot.bot.keyboard;

import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.BankCategory;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class KeyboardFactory {

    private static final String CALLBACK_CHOOSE = "menu:choose";
    private static final String CALLBACK_SETUP = "menu:setup";
    private static final String CALLBACK_SUPPORT = "menu:support";
    private static final String CALLBACK_MAIN = "menu:main";
    private static final String CALLBACK_BANK_PREFIX = "setup:bank:";
    private static final String CALLBACK_TOGGLE_PREFIX = "setup:toggle:";
    private static final String CALLBACK_DONE = "setup:done";
    private static final String CALLBACK_CARD_BY_CATEGORY = "card:by_category";
    private static final String CALLBACK_CARD_BY_MCC = "card:by_mcc";

    public InlineKeyboardMarkup getMainMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("💳 Выбрать карту для оплаты", CALLBACK_CHOOSE)))
                .keyboardRow(List.of(button("⚙️ Настроить кэшбэк", CALLBACK_SETUP)))
                .keyboardRow(List.of(button("❤️ Поддержать проект", CALLBACK_SUPPORT)))
                .build();
    }

    public InlineKeyboardMarkup getBankListKeyboard(List<Bank> banks) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        banks.forEach(bank -> builder.keyboardRow(
                List.of(button(bank.getName(), CALLBACK_BANK_PREFIX + bank.getId()))));
        builder.keyboardRow(List.of(button("🔙 Назад", CALLBACK_MAIN)));
        return builder.build();
    }

    public InlineKeyboardMarkup getCategoryKeyboard(List<BankCategory> categories, Set<Long> selectedIds) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        categories.forEach(bankCategory -> {
            Long categoryId = bankCategory.getCategory().getId();
            boolean selected = selectedIds != null && selectedIds.contains(categoryId);
            String text = (selected ? "✅ " : "⬜ ") + bankCategory.getCategory().getName();
            builder.keyboardRow(List.of(button(text, CALLBACK_TOGGLE_PREFIX + categoryId)));
        });
        builder.keyboardRow(List.of(
                button("✅ Готово", CALLBACK_DONE),
                button("🔙 Назад", CALLBACK_SETUP)));
        return builder.build();
    }

    public InlineKeyboardMarkup getCardSearchMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🔤 Ввести категорию", CALLBACK_CARD_BY_CATEGORY)))
                .keyboardRow(List.of(button("🔢 Ввести MCC-код", CALLBACK_CARD_BY_MCC)))
                .keyboardRow(List.of(button("🔙 Назад", CALLBACK_MAIN)))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
