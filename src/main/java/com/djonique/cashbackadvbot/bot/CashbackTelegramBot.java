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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class CashbackTelegramBot extends TelegramLongPollingBot {

    private static final String UNKNOWN_TEXT = "Я вас не понял. Нажмите /start";

    private final BotProperties botProperties;
    private final MenuHandler menuHandler;
    private final SetupDialogHandler setupDialogHandler;
    private final CardHandler cardHandler;
    private final UserService userService;
    private final UserStateService userStateService;

    public CashbackTelegramBot(
            BotProperties botProperties,
            MenuHandler menuHandler,
            SetupDialogHandler setupDialogHandler,
            CardHandler cardHandler,
            UserService userService,
            UserStateService userStateService) {
        super(botProperties.getBotToken());
        this.botProperties = botProperties;
        this.menuHandler = menuHandler;
        this.setupDialogHandler = setupDialogHandler;
        this.cardHandler = cardHandler;
        this.userService = userService;
        this.userStateService = userStateService;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botProperties.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            menuHandler.handleCallback(update.getCallbackQuery(), this);
        }
    }

    private void handleTextMessage(Message message) {
        String text = message.getText();
        if (text.startsWith("/")) {
            if ("/start".equals(text)) {
                menuHandler.handleStart(message.getChatId(), username(message), this);
            }
            return;
        }

        AppUser user = userService.findOrCreate(message.getChatId(), username(message));
        DialogState state = userStateService.getState(user);

        if (state == DialogState.CARD_INPUT_MCC || state == DialogState.CARD_INPUT_CATEGORY) {
            cardHandler.handleInput(user, text, state, this);
        } else if (state == DialogState.ENTERING_PERCENT) {
            setupDialogHandler.handlePercentInput(user, text, this);
        } else {
            sendUnknownMessage(message.getChatId());
        }
    }

    private String username(Message message) {
        return message.getFrom() != null ? message.getFrom().getUserName() : null;
    }

    private void sendUnknownMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(UNKNOWN_TEXT);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение chatId={}", chatId, e);
        }
    }
}
