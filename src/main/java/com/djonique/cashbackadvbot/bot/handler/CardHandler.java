package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.MccCode;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.service.CashbackAdvisorService;
import com.djonique.cashbackadvbot.service.UserStateService;
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

    private final CashbackAdvisorService cashbackAdvisorService;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;
    private final BotSender botSender;

    public void handleInput(AppUser user, String input, DialogState state) {
        String trimmedInput = input == null ? "" : input.trim();
        if (state == DialogState.CARD_INPUT_MCC && !MccCode.isValid(trimmedInput)) {
            botSender.send(user.getTelegramChatId(), INVALID_MCC_TEXT,
                    keyboardFactory.getBackToMainKeyboard());
            return;
        }

        List<RecommendationDto> recommendations =
                cashbackAdvisorService.recommend(user.getId(), trimmedInput);
        if (state != DialogState.IDLE) {
            userStateService.clearState(user);
        }

        if (recommendations.isEmpty()) {
            log.info("Ничего не найдено: userId={}, inputLength={}",
                    user.getId(), trimmedInput.length());
            botSender.send(user.getTelegramChatId(), NOT_FOUND_TEXT,
                    keyboardFactory.getMainMenuKeyboard());
            return;
        }

        log.info("Найдено рекомендаций: {} для userId={}",
                recommendations.size(), user.getId());
        if (!botSender.send(user.getTelegramChatId(),
                buildRecommendationsText(trimmedInput, recommendations),
                keyboardFactory.getMainMenuKeyboard())) {
            log.warn("Не удалось отправить рекомендации chatId={}", user.getTelegramChatId());
        }
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
}
