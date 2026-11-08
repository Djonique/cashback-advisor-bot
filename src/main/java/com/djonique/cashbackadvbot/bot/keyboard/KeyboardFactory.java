package com.djonique.cashbackadvbot.bot.keyboard;

import com.djonique.cashbackadvbot.bot.BotButtons;
import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.FixedDonation;
import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.dto.BankCategoryDto;
import com.djonique.cashbackadvbot.dto.BankDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class KeyboardFactory {

    private final BotProperties botProperties;

    private static final String SHARE_TEXT =
            "Узнай, какой картой выгоднее оплатить покупку, и получай максимальный кешбэк!";
    private static final String ALL_PURCHASES_SLUG = "all_purchases";
    private static final Set<String> ALL_PURCHASES_NAMES =
            Set.of("Все покупки", "На все покупки", "На все", "На всё");
    public static final String URL = "https://t.me/share/url?url=https://t.me/%s&text=%s";

    public InlineKeyboardMarkup getMainMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("💳 " + BotButtons.CHOOSE_CARD, CallbackData.MENU_CHOOSE)))
                .keyboardRow(List.of(button("⚙️ " + BotButtons.SETUP, CallbackData.MENU_SETUP)))
                .keyboardRow(List.of(button("💎 " + BotButtons.NEW_CARD, CallbackData.MENU_NEW_CARD)))
                .keyboardRow(List.of(button("❤️ " + BotButtons.SUPPORT, CallbackData.MENU_DONATE)))
                .keyboardRow(List.of(urlButton("🤝 " + BotButtons.SHARE, buildShareUrl())))
                .keyboardRow(List.of(button("🆘 " + BotButtons.HELP, CallbackData.MENU_HELP)))
                .build();
    }

    public InlineKeyboardMarkup getBackToMainKeyboard() {
        return InlineKeyboardMarkup.builder()
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

    public InlineKeyboardMarkup getBankListKeyboard(List<BankDto> banks) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        banks.forEach(bank -> builder.keyboardRow(
                List.of(button(bank.name(), CallbackData.SETUP_BANK_PREFIX + bank.id()))));
        builder.keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)));
        return builder.build();
    }

    public InlineKeyboardMarkup getCategoryKeyboard(List<BankCategoryDto> categories, Set<Long> selectedIds) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        categories.stream()
                .sorted(Comparator.comparing(bankCategory -> !isAllPurchases(bankCategory)))
                .forEach(bankCategory -> {
                    Long categoryId = bankCategory.id();
                    boolean selected = selectedIds != null && selectedIds.contains(categoryId);
                    String text = (selected ? "✅ " : "⬜ ") + bankCategory.displayName();
                    builder.keyboardRow(List.of(button(text, CallbackData.SETUP_TOGGLE_PREFIX + categoryId)));
                });
        builder.keyboardRow(List.of(
                button("✅ " + BotButtons.DONE, CallbackData.SETUP_DONE),
                button("🔙 " + BotButtons.BACK, CallbackData.MENU_SETUP)));
        return builder.build();
    }

    public InlineKeyboardMarkup getDonationMenuKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⭐ " + BotButtons.DONATE_STARS, CallbackData.DONATE_STARS)));
        for (FixedDonation donation : FixedDonation.values()) {
            builder.keyboardRow(List.of(button(donation.getLabel(), donation.getCallbackData())));
        }
        return builder
                .keyboardRow(List.of(button("💰 " + BotButtons.DONATE_CUSTOM,
                        CallbackData.SUPPORT_CUSTOM)))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    public InlineKeyboardMarkup getStarsMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("1 ⭐️", CallbackData.DONATE_STAR_PREFIX + 1),
                        button("5 ⭐️", CallbackData.DONATE_STAR_PREFIX + 5),
                        button("10 ⭐️", CallbackData.DONATE_STAR_PREFIX + 10)))
                .keyboardRow(List.of(
                        button("25 ⭐️", CallbackData.DONATE_STAR_PREFIX + 25),
                        button("50 ⭐️", CallbackData.DONATE_STAR_PREFIX + 50),
                        button("100 ⭐️", CallbackData.DONATE_STAR_PREFIX + 100)))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK, CallbackData.MENU_DONATE)))
                .build();
    }

    public InlineKeyboardMarkup getNewCardMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("Т-Банк", CallbackData.NEW_CARD_PREFIX + "tbank"),
                        button("Альфа-Банк", CallbackData.NEW_CARD_PREFIX + "alfa"),
                        button("ВТБ", CallbackData.NEW_CARD_PREFIX + "vtb")))
                .keyboardRow(List.of(
                        button("Ozon Банк", CallbackData.NEW_CARD_PREFIX + "ozon"),
                        button("Яндекс Банк", CallbackData.NEW_CARD_PREFIX + "yandex"),
                        button("WB Банк", CallbackData.NEW_CARD_PREFIX + "wb")))
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK_TO_MENU, CallbackData.MENU_MAIN)))
                .build();
    }

    public InlineKeyboardMarkup getBackToNewCardMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🔙 " + BotButtons.BACK, CallbackData.MENU_NEW_CARD)))
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

    private boolean isAllPurchases(BankCategoryDto bankCategory) {
        return ALL_PURCHASES_SLUG.equals(bankCategory.slug())
                || ALL_PURCHASES_NAMES.contains(bankCategory.displayName());
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private InlineKeyboardButton urlButton(String text, String url) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
    }

    private String buildShareUrl() {
        return String.format(
                URL,
                botProperties.getBotUsername(),
                URLEncoder.encode(SHARE_TEXT, StandardCharsets.UTF_8));
    }
}
