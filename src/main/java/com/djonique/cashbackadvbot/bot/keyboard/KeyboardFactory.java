package com.djonique.cashbackadvbot.bot.keyboard;

import com.djonique.cashbackadvbot.bot.BotButtons;
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
                .keyboardRow(List.of(button("💳 " + BotButtons.CHOOSE_CARD, CallbackData.MENU_CHOOSE)))
                .keyboardRow(List.of(button("⚙️ " + BotButtons.SETUP, CallbackData.MENU_SETUP)))
                .keyboardRow(List.of(button("❤️ " + BotButtons.SUPPORT, CallbackData.MENU_SUPPORT)))
                .keyboardRow(List.of(button("🆘 " + BotButtons.HELP, CallbackData.MENU_HELP)))
                .build();
    }

    public InlineKeyboardMarkup getBackToMainKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    public InlineKeyboardMarkup getSupportMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🍔 " + BotButtons.SUPPORT_BURGER, CallbackData.SUPPORT_100)))
                .keyboardRow(List.of(button("🖥 " + BotButtons.SUPPORT_SERVER, CallbackData.SUPPORT_250)))
                .keyboardRow(List.of(button("🚀 " + BotButtons.SUPPORT_DEPOSIT, CallbackData.SUPPORT_500)))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    public InlineKeyboardMarkup getHelpMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("❓ " + BotButtons.HELP_GENERAL, CallbackData.HELP_GENERAL)))
                .keyboardRow(List.of(button("⚙️ " + BotButtons.HELP_SETUP, CallbackData.HELP_SETUP)))
                .keyboardRow(List.of(button("🏪 " + BotButtons.HELP_STORE, CallbackData.HELP_STORE)))
                .keyboardRow(List.of(button("🗂 " + BotButtons.HELP_CATEGORY, CallbackData.HELP_CATEGORY)))
                .keyboardRow(List.of(button("🔢 " + BotButtons.HELP_MCC, CallbackData.HELP_MCC)))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    public InlineKeyboardMarkup getBankListKeyboard(List<Bank> banks) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        banks.forEach(bank -> builder.keyboardRow(
                List.of(button(bank.getName(), CallbackData.SETUP_BANK_PREFIX + bank.getId()))));
        builder.keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)));
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
                button("✅ " + BotButtons.DONE, CallbackData.SETUP_DONE),
                button("🔙 " + BotButtons.BACK, CallbackData.MENU_SETUP)));
        return builder.build();
    }

    public InlineKeyboardMarkup getDonationMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⭐ " + BotButtons.DONATE_STARS, CallbackData.DONATE_STARS)))
                .keyboardRow(List.of(button("🖥 " + BotButtons.DONATE_SERVER,
                        CallbackData.DONATE_YOOKASSA_100)))
                .keyboardRow(List.of(button("💵 " + BotButtons.DONATE_DEPOT,
                        CallbackData.DONATE_YOOKASSA_300)))
                .keyboardRow(List.of(button("😻 " + BotButtons.DONATE_CATS,
                        CallbackData.DONATE_YOOKASSA_500)))
                .keyboardRow(List.of(button("💰 " + BotButtons.DONATE_CUSTOM,
                        CallbackData.DONATE_YOOKASSA_CUSTOM)))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    public InlineKeyboardMarkup getMonthlyNotificationKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⚙️ " + BotButtons.SETUP, CallbackData.MENU_SETUP)))
                .build();
    }

    public InlineKeyboardMarkup getBackToCardMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK, CallbackData.CARD_MENU)))
                .build();
    }

    public InlineKeyboardMarkup getCardSearchMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🏪 " + BotButtons.CARD_BY_STORE, CallbackData.CARD_BY_STORE)))
                .keyboardRow(List.of(button("🗂 " + BotButtons.CARD_BY_CATEGORY, CallbackData.CARD_BY_CATEGORY)))
                .keyboardRow(List.of(button("🔢 " + BotButtons.CARD_BY_MCC, CallbackData.CARD_BY_MCC)))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
