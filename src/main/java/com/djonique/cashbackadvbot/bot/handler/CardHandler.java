package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.service.CashbackAdvisorService;
import com.djonique.cashbackadvbot.service.UserStateService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardHandler {

    private static final String INVALID_MCC_TEXT =
            "Ошибка: MCC-код должен состоять из 4 цифр. Попробуйте еще раз.";
    private static final String NOT_FOUND_TEXT =
            "По вашему запросу ничего не найдено. Возможно, вы не настроили кэшбэк на этот месяц.";
    private static final String[] MEDALS = {"🥇", "🥈", "🥉"};

    private final CashbackAdvisorService cashbackAdvisorService;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;

    public void handleInput(AppUser user, String input, DialogState state, AbsSender sender) {
        if (state == DialogState.CARD_INPUT_MCC && !input.matches("\\d{4}")) {
            sendMessage(user.getTelegramChatId(), INVALID_MCC_TEXT, null, sender);
            return;
        }

        List<RecommendationDto> recommendations =
                cashbackAdvisorService.recommend(user.getId(), input);
        userStateService.clearState(user);

        if (recommendations.isEmpty()) {
            log.info("Ничего не найдено: userId={}, input='{}'", user.getId(), input);
            sendMessage(user.getTelegramChatId(), NOT_FOUND_TEXT,
                    keyboardFactory.getMainMenuKeyboard(), sender);
            return;
        }

        log.info("Найдено рекомендаций: {} для userId={}, input='{}'",
                recommendations.size(), user.getId(), input);
        sendMessage(user.getTelegramChatId(), buildRecommendationsText(input, recommendations),
                keyboardFactory.getMainMenuKeyboard(), sender);
    }

    private String buildRecommendationsText(String input, List<RecommendationDto> recommendations) {
        StringBuilder text = new StringBuilder()
                .append("Лучшие карты для «").append(input).append("»:\n\n");
        for (int index = 0; index < recommendations.size(); index++) {
            RecommendationDto recommendation = recommendations.get(index);
            text.append(medal(index)).append(' ')
                    .append(recommendation.bankName()).append(" — ")
                    .append(formatPercent(recommendation.percent())).append("% (")
                    .append(recommendation.categoryName()).append(")\n");
        }
        return text.toString().stripTrailing();
    }

    private String medal(int index) {
        return index < MEDALS.length ? MEDALS[index] : (index + 1) + ".";
    }

    private String formatPercent(BigDecimal percent) {
        return percent == null ? "?" : percent.stripTrailingZeros().toPlainString();
    }

    private void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard, AbsSender sender) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение chatId={}", chatId, e);
        }
    }
}
