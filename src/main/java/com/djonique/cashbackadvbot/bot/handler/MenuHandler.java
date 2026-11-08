package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.CallbackData;
import com.djonique.cashbackadvbot.bot.FixedDonation;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.config.ReferralProperties;
import com.djonique.cashbackadvbot.dto.DonationPromptContext;
import com.djonique.cashbackadvbot.dto.UserRef;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.service.PaymentService;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuHandler {

    private static final String START_TEXT =
            "Привет! Я помогу выбрать карту для оплаты. Выберите действие, либо отправьте название магазина, категорию кешбэка или МСС-код в текстовом сообщении";
    private static final String MENU_TEXT = "Отправьте сообщение с названием магазина, категорию кешбэка или МСС-код";
    private static final String EDIT_ERROR_TEXT = "Не удалось обновить меню";
    private static final String CHAT_ERROR_TEXT = "Не удалось определить чат";
    private static final String DONATION_MENU_TEXT = "Ваша поддержка очень важна. Выберите формат:";
    private static final String STARS_MENU_TEXT = "Выберите количество звезд для поддержки проекта:";
    private static final String STARS_TITLE = "Донат звездами Telegram";
    private static final String INVALID_AMOUNT_TEXT = "Некорректная сумма";
    private static final String INVOICE_ERROR_TEXT = "Не удалось выставить счёт, попробуйте позже";
    private static final Set<Integer> STARS_AMOUNTS = Set.of(1, 5, 10, 25, 50, 100);
    private static final String DONATION_TITLE_TEMPLATE = "Донат %d руб";
    private static final String DONATION_AMOUNT_PROMPT =
            "Введите сумму доната в рублях (целое число, минимум 100 ₽):";
    private static final String SEND_ERROR_TEXT = "Не удалось отправить сообщение";
    private static final String UNKNOWN_CALLBACK_TEXT = "Кнопка устарела, начните заново";
    private static final String NEW_CARD_MENU_TEXT = "Выберите банк для оформления карты:";
    private static final String NEW_CARD_LINK_TEMPLATE =
            "Для оформления карты %s перейдите по ссылке: %s";
    private static final String UNKNOWN_BANK_TEXT = "Неизвестный банк";

    private final UserService userService;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;
    private final CallbackSupport callbackSupport;
    private final HelpHandler helpHandler;
    private final CardSearchHandler cardSearchHandler;
    private final SetupHandler setupHandler;
    private final PaymentService paymentService;
    private final ObjectProvider<AbsSender> absSenderProvider;
    private final ReferralProperties referralProperties;

    public void handleStart(Long chatId, String username) {
        UserRef user = userService.findOrCreate(chatId, username);
        clearStateAndDeletePrompt(user, chatId);
        sendStart(chatId, user);
    }

    public void handleMenu(Long chatId, String username) {
        UserRef user = userService.findOrCreate(chatId, username);
        clearStateAndDeletePrompt(user, chatId);
        sendMenu(chatId, user);
    }

    public void handleCommand(String command, Long chatId, String username) {
        UserRef user = userService.findOrCreate(chatId, username);
        clearStateAndDeletePrompt(user, chatId);
        switch (command) {
            case "start" -> sendStart(chatId, user);
            case "menu" -> sendMenu(chatId, user);
            case "settings" -> setupHandler.openBankSelection(user, chatId);
            case "help" -> helpHandler.sendHelp(chatId);
            case "donate" -> sendDonationMenu(chatId);
            default -> log.warn("Неизвестная команда: {}", command);
        }
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data == null) {
            log.warn("Callback без data: id={}", callbackQuery.getId());
            botSender.answerCallback(callbackQuery.getId(), "Некорректный запрос");
            return;
        }

        // Состояние настройки сохраняют только шаги самого мастера.
        if (!isSetupCallback(data)) {
            clearStateAndDeletePrompt(callbackQuery);
        }

        switch (data) {
            case CallbackData.MENU_MAIN -> editToMainMenu(callbackQuery);
            case CallbackData.MENU_CHOOSE -> cardSearchHandler.handleChoose(callbackQuery);
            case CallbackData.CARD_MENU -> cardSearchHandler.handleMenu(callbackQuery);
            case CallbackData.CARD_BY_CATEGORY -> cardSearchHandler.handleCardByCategory(callbackQuery);
            case CallbackData.CARD_BY_MCC -> cardSearchHandler.handleCardByMcc(callbackQuery);
            case CallbackData.CARD_BY_STORE -> cardSearchHandler.handleCardByStore(callbackQuery);
            case CallbackData.MENU_SETUP -> setupHandler.handleSetup(callbackQuery);
            case CallbackData.SETUP_DONE -> setupHandler.handleDone(callbackQuery);
            case CallbackData.MENU_NEW_CARD -> showNewCardMenu(callbackQuery);
            case CallbackData.MENU_DONATE -> showDonationMenu(callbackQuery);
            case CallbackData.DONATE_STARS -> showStarsMenu(callbackQuery);
            case CallbackData.SUPPORT_100,
                 CallbackData.SUPPORT_250,
                 CallbackData.SUPPORT_500 -> handleFixedDonation(callbackQuery, data);
            case CallbackData.SUPPORT_CUSTOM -> handleCustomDonation(callbackQuery);
            case CallbackData.MENU_HELP -> helpHandler.handleMenu(callbackQuery);
            case CallbackData.HELP_GENERAL, CallbackData.HELP_SETUP, CallbackData.HELP_STORE,
                 CallbackData.HELP_CATEGORY, CallbackData.HELP_MCC ->
                    helpHandler.handleHelp(callbackQuery, data);
            default -> handlePrefixedCallback(data, callbackQuery);
        }
    }

    private boolean isSetupCallback(String data) {
        return data.equals(CallbackData.SETUP_DONE)
                || data.startsWith(CallbackData.SETUP_BANK_PREFIX)
                || data.startsWith(CallbackData.SETUP_TOGGLE_PREFIX);
    }

    private void handlePrefixedCallback(String data, CallbackQuery callbackQuery) {
        if (data.startsWith(CallbackData.DONATE_STAR_PREFIX)) {
            handleStarsDonation(callbackQuery, data);
        } else if (data.startsWith(CallbackData.NEW_CARD_PREFIX)) {
            handleNewCard(callbackQuery, data);
        } else if (data.startsWith(CallbackData.SETUP_BANK_PREFIX)) {
            setupHandler.handleBankSelected(data, callbackQuery);
        } else if (data.startsWith(CallbackData.SETUP_TOGGLE_PREFIX)) {
            setupHandler.handleCategoryToggle(data, callbackQuery);
        } else {
            log.warn("Неизвестный callback: {}", data);
            botSender.answerCallback(callbackQuery.getId(), UNKNOWN_CALLBACK_TEXT);
        }
    }

    private void sendStart(Long chatId, UserRef user) {
        log.info("Обработан /start: userId={}", user.id());
        botSender.send(chatId, START_TEXT, keyboardFactory.getMainMenuKeyboard());
    }

    private void sendMenu(Long chatId, UserRef user) {
        log.info("Обработан /menu: userId={}", user.id());
        botSender.send(chatId, MENU_TEXT, keyboardFactory.getMainMenuKeyboard());
    }

    private void sendDonationMenu(Long chatId) {
        botSender.send(chatId, DONATION_MENU_TEXT, keyboardFactory.getDonationMenuKeyboard());
    }

    private void showDonationMenu(CallbackQuery callbackQuery) {
        if (!callbackSupport.editOrSend(callbackQuery, DONATION_MENU_TEXT,
                keyboardFactory.getDonationMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private void showStarsMenu(CallbackQuery callbackQuery) {
        if (!callbackSupport.editOrSend(callbackQuery, STARS_MENU_TEXT,
                keyboardFactory.getStarsMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private void handleStarsDonation(CallbackQuery callbackQuery, String data) {
        Long chatId = callbackSupport.getChatId(callbackQuery);
        if (chatId == null) {
            botSender.answerCallback(callbackQuery.getId(), CHAT_ERROR_TEXT);
            return;
        }

        Integer amount = parseStarsAmount(data);
        if (amount == null || !STARS_AMOUNTS.contains(amount)) {
            botSender.answerCallback(callbackQuery.getId(), INVALID_AMOUNT_TEXT);
            return;
        }

        if (!paymentService.sendStarsInvoice(chatId, STARS_TITLE, amount, absSenderProvider.getObject())) {
            botSender.answerCallback(callbackQuery.getId(), INVOICE_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private Integer parseStarsAmount(String data) {
        try {
            return Integer.valueOf(data.substring(CallbackData.DONATE_STAR_PREFIX.length()));
        } catch (NumberFormatException e) {
            log.warn("Некорректное количество звезд в callback: '{}'", data);
            return null;
        }
    }

    private void showNewCardMenu(CallbackQuery callbackQuery) {
        if (!callbackSupport.editOrSend(callbackQuery, NEW_CARD_MENU_TEXT,
                keyboardFactory.getNewCardMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private void handleNewCard(CallbackQuery callbackQuery, String data) {
        String bank = data.substring(CallbackData.NEW_CARD_PREFIX.length());
        BankOffer offer = resolveBankOffer(bank);
        if (offer == null) {
            botSender.answerCallback(callbackQuery.getId(), UNKNOWN_BANK_TEXT);
            return;
        }

        String text = String.format(NEW_CARD_LINK_TEMPLATE, offer.name(), offer.url());
        if (!callbackSupport.editOrSend(callbackQuery, text,
                keyboardFactory.getBackToNewCardMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private BankOffer resolveBankOffer(String bank) {
        return switch (bank) {
            case "tbank" -> new BankOffer("Т-Банк", referralProperties.tbank());
            case "alfa" -> new BankOffer("Альфа-Банк", referralProperties.alfa());
            case "vtb" -> new BankOffer("ВТБ", referralProperties.vtb());
            case "ozon" -> new BankOffer("Ozon Банк", referralProperties.ozon());
            case "yandex" -> new BankOffer("Яндекс Банк", referralProperties.yandex());
            case "wb" -> new BankOffer("WB Банк", referralProperties.wb());
            default -> null;
        };
    }

    private void handleFixedDonation(CallbackQuery callbackQuery, String data) {
        Long chatId = callbackSupport.getChatId(callbackQuery);
        if (chatId == null) {
            botSender.answerCallback(callbackQuery.getId(), CHAT_ERROR_TEXT);
            return;
        }

        FixedDonation donation = FixedDonation.fromCallback(data).orElse(null);
        if (donation == null) {
            botSender.answerCallback(callbackQuery.getId(), INVALID_AMOUNT_TEXT);
            return;
        }

        String title = String.format(DONATION_TITLE_TEMPLATE, donation.getAmountRub());
        if (!paymentService.sendFiatInvoice(chatId, title, donation.getAmountRub(),
                absSenderProvider.getObject())) {
            botSender.answerCallback(callbackQuery.getId(), INVOICE_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private void handleCustomDonation(CallbackQuery callbackQuery) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        Long chatId = callbackSupport.getChatId(callbackQuery);
        if (user == null || chatId == null) {
            botSender.answerCallback(callbackQuery.getId(), CHAT_ERROR_TEXT);
            return;
        }

        deleteDonationPrompt(user, chatId);
        Integer promptMessageId = botSender.sendWithMessageId(chatId, DONATION_AMOUNT_PROMPT, null);
        if (promptMessageId == null) {
            botSender.answerCallback(callbackQuery.getId(), SEND_ERROR_TEXT);
            return;
        }

        userStateService.setState(user.id(), DialogState.ENTERING_DONATION_AMOUNT,
                new DonationPromptContext(promptMessageId));
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private void clearStateAndDeletePrompt(CallbackQuery callbackQuery) {
        UserRef user = callbackSupport.resolveUser(callbackQuery);
        Long chatId = callbackSupport.getChatId(callbackQuery);
        if (user == null || chatId == null) {
            return;
        }
        clearStateAndDeletePrompt(user, chatId);
    }

    private void clearStateAndDeletePrompt(UserRef user, Long chatId) {
        deleteDonationPrompt(user, chatId);
        userStateService.clearState(user.id());
    }

    private boolean deleteDonationPrompt(UserRef user, Long chatId) {
        if (userStateService.getState(user.id()) != DialogState.ENTERING_DONATION_AMOUNT) {
            return false;
        }

        try {
            userStateService.getContext(user.id(), DonationPromptContext.class)
                    .map(DonationPromptContext::promptMessageId)
                    .ifPresent(messageId -> botSender.deleteMessage(chatId, messageId));
        } catch (ValidationException e) {
            log.warn("Не удалось прочитать контекст доната: userId={}", user.id());
        }
        return true;
    }

    private void editToMainMenu(CallbackQuery callbackQuery) {
        if (!callbackSupport.editOrSend(callbackQuery, MENU_TEXT,
                keyboardFactory.getMainMenuKeyboard())) {
            botSender.answerCallback(callbackQuery.getId(), EDIT_ERROR_TEXT);
            return;
        }
        botSender.answerCallback(callbackQuery.getId(), null);
    }

    private record BankOffer(String name, String url) {
    }
}
