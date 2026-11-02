package com.djonique.cashbackadvbot.bot;

import com.djonique.cashbackadvbot.bot.handler.CardHandler;
import com.djonique.cashbackadvbot.bot.handler.MenuHandler;
import com.djonique.cashbackadvbot.bot.handler.SetupDialogHandler;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class CashbackTelegramBot extends TelegramLongPollingBot {

    private static final String UNKNOWN_TEXT = "Я вас не понял. Нажмите /start";
    private static final String ERROR_TEXT = "Что-то пошло не так. Попробуйте ещё раз или нажмите /start";

    private final BotProperties botProperties;
    private final MenuHandler menuHandler;
    private final SetupDialogHandler setupDialogHandler;
    private final CardHandler cardHandler;
    private final UserService userService;
    private final UserStateService userStateService;
    private final TelegramMessageSender messageSender;

    public CashbackTelegramBot(
            BotProperties botProperties,
            MenuHandler menuHandler,
            SetupDialogHandler setupDialogHandler,
            CardHandler cardHandler,
            UserService userService,
            UserStateService userStateService,
            TelegramMessageSender messageSender) {
        super(botProperties.getBotToken());
        this.botProperties = botProperties;
        this.menuHandler = menuHandler;
        this.setupDialogHandler = setupDialogHandler;
        this.cardHandler = cardHandler;
        this.userService = userService;
        this.userStateService = userStateService;
        this.messageSender = messageSender;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                menuHandler.handleCallback(update.getCallbackQuery(), this);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки update id={}", update.getUpdateId(), e);
            Long chatId = extractChatId(update);
            if (chatId != null) {
                messageSender.send(chatId, ERROR_TEXT, null, this);
            }
        }
    }

    private void handleTextMessage(Message message) {
        String text = message.getText().trim();
        if (text.startsWith("/")) {
            if (text.equals("/start") || text.startsWith("/start@")) {
                menuHandler.handleStart(message.getChatId(), username(message), this);
            }
            return;
        }

        AppUser user = userService.findOrCreate(message.getChatId(), username(message));
        DialogState state = userStateService.getState(user);

        if (state == DialogState.CARD_INPUT_MCC
                || state == DialogState.CARD_INPUT_CATEGORY
                || state == DialogState.CARD_INPUT_STORE
                || state == DialogState.IDLE) {
            cardHandler.handleInput(user, text, state, this);
        } else if (state == DialogState.ENTERING_PERCENT) {
            setupDialogHandler.handlePercentInput(user, text, this);
        } else {
            messageSender.send(message.getChatId(), UNKNOWN_TEXT, null, this);
        }
    }

    private String username(Message message) {
        return message.getFrom() != null ? message.getFrom().getUserName() : null;
    }

    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage() != null) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }
}
