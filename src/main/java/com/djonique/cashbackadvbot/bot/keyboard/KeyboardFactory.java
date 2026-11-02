package com.djonique.cashbackadvbot.bot.keyboard;

import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.BankCategory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Set;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup getMainMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("💳 Выбрать карту для оплаты", CallbackData.MENU_CHOOSE)))
                .keyboardRow(List.of(button("⚙️ Настроить кешбэк", CallbackData.MENU_SETUP)))
                .keyboardRow(List.of(button("❤️ Поддержать проект", CallbackData.MENU_SUPPORT)))
                .build();
    }

    public InlineKeyboardMarkup getBankListKeyboard(List<Bank> banks) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        banks.forEach(bank -> builder.keyboardRow(
                List.of(button(bank.getName(), CallbackData.SETUP_BANK_PREFIX + bank.getId()))));
        builder.keyboardRow(List.of(button("🔙 Назад", CallbackData.MENU_MAIN)));
        return builder.build();
    }

    public InlineKeyboardMarkup getCategoryKeyboard(List<BankCategory> categories, Set<Long> selectedIds) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        categories.forEach(bankCategory -> {
            Long categoryId = bankCategory.getCategory().getId();
            boolean selected = selectedIds != null && selectedIds.contains(categoryId);
            String text = (selected ? "✅ " : "⬜ ") + bankCategory.getDisplayName();
            builder.keyboardRow(List.of(button(text, CallbackData.SETUP_TOGGLE_PREFIX + categoryId)));
        });
        builder.keyboardRow(List.of(
                button("✅ Готово", CallbackData.SETUP_DONE),
                button("🔙 Назад", CallbackData.MENU_SETUP)));
        return builder.build();
    }

    public InlineKeyboardMarkup getCardSearchMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🔤 Ввести категорию", CallbackData.CARD_BY_CATEGORY)))
                .keyboardRow(List.of(button("🔢 Ввести MCC-код", CallbackData.CARD_BY_MCC)))
                .keyboardRow(List.of(button("🏪 Ввести магазин", CallbackData.CARD_BY_STORE)))
                .keyboardRow(List.of(button("🔙 Назад", CallbackData.MENU_MAIN)))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
