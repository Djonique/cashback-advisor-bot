package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.TelegramMessageSender;
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
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardHandler {

    private static final String INVALID_MCC_TEXT =
            "Ошибка: MCC-код должен состоять из 4 цифр. Попробуйте еще раз.";
    private static final String NOT_FOUND_TEXT =
            "По вашему запросу ничего не найдено. Возможно, вы не настроили кешбэк на этот месяц.";
    private static final String[] MEDALS = {"🥇", "🥈", "🥉"};

    private final CashbackAdvisorService cashbackAdvisorService;
    private final UserStateService userStateService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramMessageSender messageSender;

    public void handleInput(AppUser user, String input, DialogState state, AbsSender sender) {
        String trimmedInput = input == null ? "" : input.trim();
        if (state == DialogState.CARD_INPUT_MCC && !MccCode.isValid(trimmedInput)) {
            messageSender.send(user.getTelegramChatId(), INVALID_MCC_TEXT, null, sender);
            return;
        }

        List<RecommendationDto> recommendations =
                cashbackAdvisorService.recommend(user.getId(), trimmedInput);
        if (state != DialogState.IDLE) {
            userStateService.clearState(user);
        }

        if (recommendations.isEmpty()) {
            log.info("Ничего не найдено: userId={}, input='{}'", user.getId(), trimmedInput);
            messageSender.send(user.getTelegramChatId(), NOT_FOUND_TEXT,
                    keyboardFactory.getMainMenuKeyboard(), sender);
            return;
        }

        log.info("Найдено рекомендаций: {} для userId={}, input='{}'",
                recommendations.size(), user.getId(), trimmedInput);
        messageSender.send(user.getTelegramChatId(), buildRecommendationsText(trimmedInput, recommendations),
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
}
