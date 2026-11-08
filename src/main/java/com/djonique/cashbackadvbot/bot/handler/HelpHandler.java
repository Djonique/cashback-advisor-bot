package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotButtons;
import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelpHandler {

    private static final String HELP_MENU_TEXT = "Выберите интересующий Вас раздел:";

    private static final String HELP_GENERAL_TEXT = """
            🤖 Как пользоваться ботом:
            
            1. Настройте категории в разделе «Настроить кешбэк».
            2. Напишите боту название магазина, категорию кешбэка или МСС-код, и он подскажет, какой картой выгоднее платить.
            
            ⚠️ Важно! Как точнее подобрать карту:
            
            1. По МСС-коду: вероятность ошибки минимальна.
            2. Если кода нет — по категории кешбэка: менее точно, вероятность ошибки выше.
            3. По названию магазина — самый неточный вариант: зависит от настроек терминала в конкретном магазине.""";

    private static final String HELP_SETUP_TEXT = """
            ⚙️ Как настроить кешбэк:
            
            1. Нажмите «Настроить кешбэк» в главном меню.
            2. Выберите банк, в котором у вас есть карта.
            3. Отметьте нужные категории.
            4. Введите процент кешбэка для каждой выбранной категории.
            5. Нажмите «Готово».
            
            Настройки сохраняются на текущий календарный месяц.""";

    private static final String HELP_STORE_TEXT = """
            🏪 Как искать по магазину:
            
            Просто напишите боту название магазина (например, «Пятёрочка», «Золотое Яблоко» или «DNS»). Бот сопоставит магазин с его МСС-кодом и найдет карту с максимальным кешбэком.
            
            Бот распознает текст независимо от варианта написания: «пятёрочка», «Пятерочка» или «PYATEROCHKA».

            ⚠️ Важно! Функция носит рекомендательный характер: итоговый МСС-код зависит от настроек терминала в конкретном магазине.""";

    private static final String HELP_CATEGORY_TEXT = """
            🗂 Как искать по категории:
            
            Напишите боту название категории (например, «Супермаркеты», «Продукты» или «Кафе»). Бот найдет карту с максимальным кешбэком для этой категории.""";

    private static final String HELP_MCC_TEXT = """
            🔢 Как искать по МСС-коду:
            
            Введите 4 цифры МСС-кода (например, 5411). Бот найдет карту, где у вас настроен кешбэк на категорию, включающую этот код.

            Где можно узнать код? В приложении банка в деталях операции или на чеке.""";

    private static final String EDIT_ERROR_TEXT = "Не удалось обновить сообщение";

    private final BotSender botSender;
    private final KeyboardFactory keyboardFactory;
    private final CallbackSupport callbackSupport;

    public void sendHelp(Long chatId) {
        botSender.send(chatId, HELP_MENU_TEXT, keyboardFactory.getHelpMenuKeyboard());
    }

    public void handleMenu(CallbackQuery callbackQuery) {
        editHelpMessage(callbackQuery, HELP_MENU_TEXT, keyboardFactory.getHelpMenuKeyboard());
    }

    public void handleHelp(CallbackQuery callbackQuery, String data) {
        switch (data) {
            case CallbackData.HELP_GENERAL ->
                    editHelpMessage(callbackQuery, HELP_GENERAL_TEXT, backToHelpKeyboard());
            case CallbackData.HELP_SETUP ->
                    editHelpMessage(callbackQuery, HELP_SETUP_TEXT, backToHelpKeyboard());
            case CallbackData.HELP_STORE ->
                    editHelpMessage(callbackQuery, HELP_STORE_TEXT, backToHelpKeyboard());
            case CallbackData.HELP_CATEGORY ->
                    editHelpMessage(callbackQuery, HELP_CATEGORY_TEXT, backToHelpKeyboard());
            case CallbackData.HELP_MCC ->
                    editHelpMessage(callbackQuery, HELP_MCC_TEXT, backToHelpKeyboard());
            default -> log.warn("Неизвестный help callback: {}", data);
        }
    }

    private void editHelpMessage(CallbackQuery callbackQuery,
                                 String text,
                                 InlineKeyboardMarkup keyboard) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        if (message == null) {
            Long chatId = callbackSupport.getChatId(callbackQuery);
            if (chatId != null) {
                botSender.send(chatId, text, keyboard);
            } else {
                botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
                return;
            }
        } else if (!botSender.editMessageText(message.getChatId(), message.getMessageId(), text, keyboard)) {
            botSender.send(message.getChatId(), text, keyboard);
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private InlineKeyboardMarkup backToHelpKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text("🔙 " + BotButtons.BACK)
                        .callbackData(CallbackData.MENU_HELP)
                        .build()))
                .build();
    }
}
