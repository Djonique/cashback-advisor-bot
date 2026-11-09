package com.djonique.cashbackadvbot.bot;

import com.djonique.cashbackadvbot.bot.handler.CardHandler;
import com.djonique.cashbackadvbot.bot.handler.MenuHandler;
import com.djonique.cashbackadvbot.bot.handler.SetupDialogHandler;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.config.BotProperties;
import com.djonique.cashbackadvbot.dto.UserRef;
import com.djonique.cashbackadvbot.service.PaymentService;
import com.djonique.cashbackadvbot.service.UserService;
import com.djonique.cashbackadvbot.service.UserStateService;
import com.djonique.cashbackadvbot.util.LogUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.concurrent.*;
import java.util.stream.IntStream;

@Component
@Slf4j
public class CashbackTelegramBot extends TelegramLongPollingBot {

    private static final String UNKNOWN_TEXT = "Я вас не понял. Нажмите /start";
    private static final String UNKNOWN_COMMAND_TEXT = "Неизвестная команда. Нажмите /start";
    private static final String ERROR_TEXT = "Что-то пошло не так. Попробуйте ещё раз или нажмите /start";
    private static final String PAYMENT_PENDING_TEXT =
            "Платёж получен, но пока не обработан. Мы уже разбираемся — спасибо за поддержку! ❤️";
    private static final String PRIVATE_ONLY_TEXT = "Бот работает только в личных сообщениях";
    private static final String RUB_CURRENCY = "RUB";
    private static final String STARS_CURRENCY = "XTR";
    private static final String PRIVATE_CHAT_TYPE = "private";
    private static final String PAYMENT_THANKS_RUB_TEMPLATE =
            "Спасибо за поддержку! ❤️ (ID транзакции: %s)";
    private static final String PAYMENT_THANKS_STARS_TEXT = "Спасибо за поддержку! ❤️";
    private static final String DONATION_AMOUNT_INVALID_TEXT =
            "Пожалуйста, введите целое число (например, 150):";
    private static final String DONATION_AMOUNT_TOO_SMALL_TEXT =
            "Минимальная сумма доната — 100 ₽ (ограничение Telegram). Введите другую сумму:";
    private static final String DONATION_AMOUNT_TOO_LARGE_TEXT =
            "Максимальная сумма доната — 100 000 ₽. Введите сумму поменьше.";
    private static final int DONATION_MIN_AMOUNT = 100;
    private static final int DONATION_MAX_AMOUNT = 100_000;
    private static final int PAYMENT_RETRY_ATTEMPTS = 3;
    private static final long PAYMENT_RETRY_DELAY_MS = 200L;
    private static final int WORKER_THREADS = 4;
    private static final int WORKER_QUEUE_CAPACITY = 10_000;
    private static final int CHAT_LANES = 32;

    private final BotProperties botProperties;
    private final MenuHandler menuHandler;
    private final SetupDialogHandler setupDialogHandler;
    private final CardHandler cardHandler;
    private final UserService userService;
    private final UserStateService userStateService;
    private final PaymentService paymentService;
    private final TelegramMessageSender messageSender;
    private final TelegramApiExecutor telegramApiExecutor;

    private final ExecutorService workerPool;
    private final ExecutorService[] chatLanes;

    private volatile BotSession botSession;

    public CashbackTelegramBot(
            BotProperties botProperties,
            MenuHandler menuHandler,
            SetupDialogHandler setupDialogHandler,
            CardHandler cardHandler,
            UserService userService,
            UserStateService userStateService,
            PaymentService paymentService,
            TelegramMessageSender messageSender,
            TelegramApiExecutor telegramApiExecutor) {
        super(botProperties.getBotToken());
        this.botProperties = botProperties;
        this.menuHandler = menuHandler;
        this.setupDialogHandler = setupDialogHandler;
        this.cardHandler = cardHandler;
        this.userService = userService;
        this.userStateService = userStateService;
        this.paymentService = paymentService;
        this.messageSender = messageSender;
        this.telegramApiExecutor = telegramApiExecutor;
        this.messageSender.bind(this);
        this.workerPool = new ThreadPoolExecutor(
                WORKER_THREADS,
                WORKER_THREADS,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(WORKER_QUEUE_CAPACITY),
                Thread.ofPlatform().name("bot-worker-", 0).daemon().factory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.chatLanes = IntStream.range(0, CHAT_LANES)
                .mapToObj(i -> Executors.newSingleThreadExecutor(
                        Thread.ofPlatform().name("bot-chat-" + i).daemon().factory()))
                .toArray(ExecutorService[]::new);
    }

    public void bindSession(BotSession session) {
        this.botSession = session;
    }

    @PreDestroy
    void shutdown() {
        BotSession session = botSession;
        if (session != null) {
            try {
                session.stop();
                log.info("BotSession остановлена");
            } catch (RuntimeException e) {
                log.warn("Не удалось остановить BotSession: {}", LogUtils.sanitize(e));
            }
        }
        workerPool.shutdownNow();
        for (ExecutorService lane : chatLanes) {
            lane.shutdownNow();
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            dispatch(update);
        } catch (Exception e) {
            log.error("Не удалось обработать update id={}: {}",
                    update.getUpdateId(), LogUtils.sanitize(e));
            Long chatId = extractChatId(update);
            if (chatId != null) {
                messageSender.send(chatId, ERROR_TEXT, null);
            }
        }
    }

    private void dispatch(Update update) {
        if (update.hasPreCheckoutQuery()) {
            handlePreCheckout(update.getPreCheckoutQuery());
            return;
        }
        if (rejectNonPrivateChat(update)) {
            return;
        }
        Long chatId = extractChatId(update);
        if (chatId == null) {
            workerPool.submit(() -> process(update));
            return;
        }
        int lane = Math.floorMod(chatId.hashCode(), chatLanes.length);
        chatLanes[lane].execute(() -> process(update));
    }

    private void handlePreCheckout(PreCheckoutQuery query) {
        log.info("Получен PreCheckoutQuery: id={}, currency={}, totalAmount={}",
                query.getId(), query.getCurrency(), query.getTotalAmount());
        Long payerChatId = query.getFrom() != null ? query.getFrom().getId() : null;
        boolean valid;
        try {
            valid = paymentService.validatePreCheckout(
                    query.getInvoicePayload(), query.getTotalAmount(), query.getCurrency(), payerChatId);
        } catch (RuntimeException e) {
            log.error("Ошибка проверки PreCheckoutQuery: id={}: {}",
                    query.getId(), LogUtils.sanitize(e));
            answerPreCheckout(query.getId(), false, "Временная ошибка, попробуйте позже");
            return;
        }
        if (valid) {
            answerPreCheckout(query.getId(), true, null);
        } else {
            log.warn("Отклонён PreCheckoutQuery: id={}, currency={}, totalAmount={}",
                    query.getId(), query.getCurrency(), query.getTotalAmount());
            answerPreCheckout(query.getId(), false, "Некорректный платёж");
        }
    }

    private void answerPreCheckout(String queryId, boolean ok, String errorMessage) {
        try {
            telegramApiExecutor.executeWithRetry(this,
                    sender -> sender.execute(new AnswerPreCheckoutQuery(queryId, ok, errorMessage)));
            log.info("Ответ на PreCheckoutQuery отправлен: id={}, ok={}", queryId, ok);
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на PreCheckoutQuery: id={}: {}",
                    queryId, LogUtils.sanitize(e));
        }
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
            log.error("Ошибка обработки update id={}: {}",
                    update.getUpdateId(), LogUtils.sanitize(e));
            Long chatId = extractChatId(update);
            if (chatId != null) {
                messageSender.send(chatId, ERROR_TEXT, null);
            }
        }
    }

    private void handleSuccessfulPayment(Message message) {
        SuccessfulPayment payment = message.getSuccessfulPayment();
        log.info("Успешный платёж: chatId={}, totalAmount={}, currency={}",
                LogUtils.maskChatId(message.getChatId()),
                payment.getTotalAmount(), payment.getCurrency());

        UserRef user = resolveUserWithRetry(message.getChatId(), username(message));
        if (user == null) {
            return;
        }
        long totalAmount = payment.getTotalAmount() == null ? 0L : payment.getTotalAmount();
        boolean processed = persistPayment(user, payment, totalAmount);
        if (!processed) {
            log.info("Повторный или отложенный successful_payment: chatId={}, chargeId={}",
                    LogUtils.maskChatId(message.getChatId()),
                    payment.getTelegramPaymentChargeId());
            return;
        }

        if (RUB_CURRENCY.equals(payment.getCurrency())) {
            String text = String.format(PAYMENT_THANKS_RUB_TEMPLATE,
                    payment.getTelegramPaymentChargeId());
            messageSender.send(message.getChatId(), text, null);
        } else if (STARS_CURRENCY.equals(payment.getCurrency())) {
            messageSender.send(message.getChatId(), PAYMENT_THANKS_STARS_TEXT, null);
        } else {
            log.warn("Неизвестная валюта успешного платежа: chatId={}, currency={}",
                    LogUtils.maskChatId(message.getChatId()), payment.getCurrency());
        }
    }

    private UserRef resolveUserWithRetry(Long chatId, String username) {
        for (int attempt = 1; attempt <= PAYMENT_RETRY_ATTEMPTS; attempt++) {
            try {
                return userService.findOrCreate(chatId, username);
            } catch (RuntimeException e) {
                log.error("Не удалось определить пользователя платежа (попытка {}/{}): chatId={}: {}",
                        attempt, PAYMENT_RETRY_ATTEMPTS,
                        LogUtils.maskChatId(chatId), LogUtils.sanitize(e));
                if (attempt < PAYMENT_RETRY_ATTEMPTS) {
                    sleepQuietly();
                }
            }
        }
        log.error("КРИТИЧНО: платёж не сохранён — не удалось определить пользователя: chatId={}",
                LogUtils.maskChatId(chatId));
        messageSender.send(chatId, PAYMENT_PENDING_TEXT, null);
        return null;
    }

    private boolean persistPayment(UserRef user, SuccessfulPayment payment, long totalAmount) {
        for (int attempt = 1; attempt <= PAYMENT_RETRY_ATTEMPTS; attempt++) {
            try {
                return paymentService.registerPayment(
                        user.id(),
                        payment.getTelegramPaymentChargeId(),
                        payment.getProviderPaymentChargeId(),
                        totalAmount,
                        payment.getCurrency(),
                        payment.getInvoicePayload());
            } catch (RuntimeException e) {
                log.error("Не удалось сохранить платёж (попытка {}/{}): chatId={}, chargeId={}: {}",
                        attempt, PAYMENT_RETRY_ATTEMPTS,
                        LogUtils.maskChatId(user.telegramChatId()),
                        payment.getTelegramPaymentChargeId(), LogUtils.sanitize(e));
                if (attempt < PAYMENT_RETRY_ATTEMPTS) {
                    sleepQuietly();
                }
            }
        }
        // Деньги уже списаны Telegram — не пугаем пользователя и не теряем факт оплаты.
        log.error("КРИТИЧНО: платёж не сохранён после {} попыток: chatId={}, chargeId={}, amount={}, currency={}",
                PAYMENT_RETRY_ATTEMPTS,
                LogUtils.maskChatId(user.telegramChatId()),
                payment.getTelegramPaymentChargeId(),
                totalAmount,
                payment.getCurrency());
        messageSender.send(user.telegramChatId(), PAYMENT_PENDING_TEXT, null);
        return false;
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(PAYMENT_RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

        UserRef user = userService.findOrCreate(message.getChatId(), username(message));
        DialogState state = userStateService.getState(user.id());

        if (state == DialogState.ENTERING_DONATION_AMOUNT) {
            handleDonationAmountInput(user, message.getChatId(), text);
        } else if (state == DialogState.CARD_INPUT_MCC
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

    private void handleDonationAmountInput(UserRef user, Long chatId, String text) {
        int amount;
        try {
            amount = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            messageSender.send(chatId, DONATION_AMOUNT_INVALID_TEXT, null);
            return;
        }

        if (amount < DONATION_MIN_AMOUNT) {
            messageSender.send(chatId, DONATION_AMOUNT_TOO_SMALL_TEXT, null);
            return;
        }
        if (amount > DONATION_MAX_AMOUNT) {
            messageSender.send(chatId, DONATION_AMOUNT_TOO_LARGE_TEXT, null);
            return;
        }

        if (!paymentService.sendFiatInvoice(chatId, "Донат " + amount + " руб", amount, this)) {
            messageSender.send(chatId, ERROR_TEXT, null);
            return;
        }
        userStateService.clearState(user.id());
    }

    private boolean rejectNonPrivateChat(Update update) {
        Message message = extractMessage(update);
        if (message == null || message.getChat() == null) {
            return false;
        }
        String chatType = message.getChat().getType();
        if (PRIVATE_CHAT_TYPE.equals(chatType)) {
            return false;
        }
        log.warn("Отклонено сообщение из неподдерживаемого чата: chatId={}, type={}",
                LogUtils.maskChatId(message.getChatId()), chatType);
        messageSender.send(message.getChatId(), PRIVATE_ONLY_TEXT, null);
        return true;
    }

    private Message extractMessage(Update update) {
        if (update.hasMessage()) {
            return update.getMessage();
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery.getMessage() instanceof Message message) {
                return message;
            }
        }
        return null;
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
            case "/donate", BotButtons.SUPPORT -> "donate";
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
