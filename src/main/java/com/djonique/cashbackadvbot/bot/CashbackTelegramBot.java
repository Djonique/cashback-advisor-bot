package com.djonique.cashbackadvbot.bot;

import com.djonique.cashbackadvbot.bot.handler.CardHandler;
import com.djonique.cashbackadvbot.bot.handler.MenuHandler;
import com.djonique.cashbackadvbot.bot.handler.SetupDialogHandler;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class CashbackTelegramBot extends TelegramLongPollingBot {

    private static final String UNKNOWN_TEXT = "Я вас не понял. Нажмите /start";
    private static final String UNKNOWN_COMMAND_TEXT = "Неизвестная команда. Нажмите /start";
    private static final String ERROR_TEXT = "Что-то пошло не так. Попробуйте ещё раз или нажмите /start";
    private static final String PAYMENT_THANKS_TEXT = "Огромное спасибо за поддержку! ❤️";

    private final BotProperties botProperties;
    private final MenuHandler menuHandler;
    private final SetupDialogHandler setupDialogHandler;
    private final CardHandler cardHandler;
    private final UserService userService;
    private final UserStateService userStateService;
    private final TelegramMessageSender messageSender;

    private final ExecutorService workerPool;
    private final ConcurrentHashMap<Long, ChatSerialExecutor> chatExecutors = new ConcurrentHashMap<>();

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
        this.messageSender.bind(this);
        this.workerPool = Executors.newFixedThreadPool(4,
                Thread.ofPlatform().name("bot-worker-", 0).daemon().factory());
    }

    @PreDestroy
    void shutdown() {
        workerPool.shutdownNow();
    }

    @Override
    public String getBotUsername() {
        return botProperties.getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasPreCheckoutQuery()) {
            workerPool.submit(() -> handlePreCheckoutQuery(update.getPreCheckoutQuery()));
            return;
        }
        Long chatId = extractChatId(update);
        if (chatId == null) {
            workerPool.submit(() -> process(update));
            return;
        }
        chatExecutors.computeIfAbsent(chatId, id -> new ChatSerialExecutor(workerPool))
                .execute(() -> process(update));
    }

    private void process(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
                handleSuccessfulPayment(update.getMessage());
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                menuHandler.handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Ошибка обработки update id={}", update.getUpdateId(), e);
            Long chatId = extractChatId(update);
            if (chatId != null) {
                messageSender.send(chatId, ERROR_TEXT, null);
            }
        }
    }

    private void handlePreCheckoutQuery(PreCheckoutQuery preCheckoutQuery) {
        AnswerPreCheckoutQuery answer = new AnswerPreCheckoutQuery(preCheckoutQuery.getId(), true);
        try {
            execute(answer);
            log.info("PreCheckoutQuery подтверждён: id={}", preCheckoutQuery.getId());
        } catch (TelegramApiException e) {
            log.error("Не удалось подтвердить PreCheckoutQuery: id={}",
                    preCheckoutQuery.getId(), e);
        }
    }

    private void handleSuccessfulPayment(Message message) {
        SuccessfulPayment payment = message.getSuccessfulPayment();
        log.info("Успешный платёж: chatId={}, totalAmount={}, currency={}",
                message.getChatId(), payment.getTotalAmount(), payment.getCurrency());
        messageSender.send(message.getChatId(), PAYMENT_THANKS_TEXT, null);
    }

    private void handleTextMessage(Message message) {
        String text = message.getText().trim();
        String command = resolveCommand(text);
        if (command != null) {
            menuHandler.handleCommand(command, message.getChatId(), username(message));
            return;
        }
        if (text.startsWith("/")) {
            messageSender.send(message.getChatId(), UNKNOWN_COMMAND_TEXT, null);
            return;
        }

        AppUser user = userService.findOrCreate(message.getChatId(), username(message));
        DialogState state = userStateService.getState(user);

        if (state == DialogState.CARD_INPUT_MCC
                || state == DialogState.CARD_INPUT_CATEGORY
                || state == DialogState.CARD_INPUT_STORE
                || state == DialogState.IDLE) {
            cardHandler.handleInput(user, text, state);
        } else if (state == DialogState.ENTERING_PERCENT) {
            setupDialogHandler.handlePercentInput(user, text);
        } else {
            messageSender.send(message.getChatId(), UNKNOWN_TEXT, null);
        }
    }

    private String resolveCommand(String text) {
        String command = text;
        int mentionIndex = command.indexOf('@');
        if (command.startsWith("/") && mentionIndex > 0) {
            command = command.substring(0, mentionIndex);
        }

        return switch (command) {
            case "/start" -> "start";
            case "/menu", BotButtons.MAIN_MENU -> "menu";
            case "/settings", BotButtons.SETUP -> "settings";
            case "/help", BotButtons.HELP -> "help";
            case "/donate", BotButtons.SUPPORT -> "support";
            case "/source_code", BotButtons.SOURCE_CODE -> "source_code";
            default -> null;
        };
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
