package com.djonique.cashbackadvbot.bot.keyboard;

import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.dto.BankCategoryDto;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KeyboardFactoryTest {

    private final KeyboardFactory keyboardFactory = new KeyboardFactory(new BotProperties());

    @Test
    void shouldPutAllPurchasesFirstAndSortOthersAlphabetically() {
        List<BankCategoryDto> categories = List.of(
                new BankCategoryDto(1L, "supermarkets", "Супермаркеты"),
                new BankCategoryDto(2L, "all_purchases", "Все покупки"),
                new BankCategoryDto(3L, "taxi", "Такси"),
                new BankCategoryDto(4L, "pharmacies", "Аптеки"));

        InlineKeyboardMarkup markup = keyboardFactory.getCategoryKeyboard(categories, Set.of());

        assertThat(markup.getKeyboard())
                .extracting(row -> row.getFirst().getText())
                .startsWith("⬜ Все покупки", "⬜ Аптеки", "⬜ Супермаркеты", "⬜ Такси");
    }
}
