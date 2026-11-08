package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.MccCode;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.dto.UserRef;
import com.djonique.cashbackadvbot.service.CashbackAdvisorService;
import com.djonique.cashbackadvbot.service.UserStateService;
import com.djonique.cashbackadvbot.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardHandler {

    private static final String INVALID_MCC_TEXT =
            "Ошибка: MCC-код должен состоять из 4 цифр. Попробуйте еще раз.";
    private static final String NOT_FOUND_TEXT =
            "Я не нашел карт с настроенным кешбэком, подходящих под Ваш запрос.";
    private static final String[] MEDALS = {"🥇", "🥈", "🥉"};
    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final int MAX_INPUT_DISPLAY_LENGTH = 100;

    private final CashbackAdvisorService cashbackAdvisorService;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;

    public void handleInput(UserRef user, String input, DialogState state) {
        String trimmedInput = input == null ? "" : input.trim();
        if (state == DialogState.CARD_INPUT_MCC && !MccCode.isValid(trimmedInput)) {
            botSender.send(user.telegramChatId(), INVALID_MCC_TEXT,
                    keyboardFactory.getBackToMainKeyboard());
            return;
        }

        List<RecommendationDto> recommendations =
                cashbackAdvisorService.recommend(user.id(), trimmedInput);
        if (state != DialogState.IDLE) {
            userStateService.clearState(user.id());
        }

        if (recommendations.isEmpty()) {
            log.info("Ничего не найдено: userId={}, inputLength={}",
                    user.id(), trimmedInput.length());
            botSender.send(user.telegramChatId(), NOT_FOUND_TEXT,
                    keyboardFactory.getMainMenuKeyboard());
            return;
        }

        log.info("Найдено рекомендаций: {} для userId={}",
                recommendations.size(), user.id());
        String text = buildRecommendationsText(trimmedInput, recommendations);
        if (!botSender.send(user.telegramChatId(), text,
                keyboardFactory.getMainMenuKeyboard())) {
            log.warn("Не удалось отправить рекомендации chatId={}",
                    LogUtils.maskChatId(user.telegramChatId()));
        }
    }

    private String buildRecommendationsText(String input, List<RecommendationDto> recommendations) {
        String safeInput = input.length() <= MAX_INPUT_DISPLAY_LENGTH
                ? input
                : input.substring(0, MAX_INPUT_DISPLAY_LENGTH - 1) + "…";
        StringBuilder text = new StringBuilder()
                .append("Лучшие карты для «").append(safeInput).append("»:\n\n");

        for (int index = 0; index < recommendations.size(); index++) {
            RecommendationDto recommendation = recommendations.get(index);
            String line = medal(index) + " "
                    + recommendation.bankName() + " — "
                    + formatPercent(recommendation.percent()) + "% ("
                    + recommendation.categoryName() + ")\n";
            if (text.length() + line.length() > MAX_MESSAGE_LENGTH) {
                if (text.length() < MAX_MESSAGE_LENGTH) {
                    text.append('…');
                }
                break;
            }
            text.append(line);
        }
        return text.toString().stripTrailing();
    }

    private String medal(int index) {
        return index < MEDALS.length ? MEDALS[index] : (index + 1) + ".";
    }

    private String formatPercent(BigDecimal percent) {
        return percent == null ? "?" : percent.stripTrailingZeros().toPlainString();
    }
}
