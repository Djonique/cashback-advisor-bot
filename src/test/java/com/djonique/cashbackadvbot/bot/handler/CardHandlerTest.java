package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.service.CashbackAdvisorService;
import com.djonique.cashbackadvbot.service.UserStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardHandlerTest {

    @Mock
    private CashbackAdvisorService cashbackAdvisorService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private BotSender botSender;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private CardHandler handler;

    @Test
    void shouldRejectInvalidMcc_withoutCallingAdvisor() {
        AppUser user = user();

        handler.handleInput(user, "12", DialogState.CARD_INPUT_MCC);

        verify(cashbackAdvisorService, never()).recommend(any(), anyString());
        verify(botSender).send(eq(user.getTelegramChatId()),
                eq("Ошибка: MCC-код должен состоять из 4 цифр. Попробуйте еще раз."), any());
    }

    @Test
    void shouldClearStateAndReportNotFound_whenNoRecommendations() {
        AppUser user = user();
        when(cashbackAdvisorService.recommend(1L, "Пятёрочка")).thenReturn(List.of());

        handler.handleInput(user, "Пятёрочка", DialogState.CARD_INPUT_STORE);

        verify(userStateService).clearState(user);
        verify(botSender).send(eq(user.getTelegramChatId()),
                eq("Я не нашел карт с настроенным кешбэком, подходящих под Ваш запрос."), any());
    }

    @Test
    void shouldFormatRecommendations_withMedalsAndPercents() {
        AppUser user = user();
        when(cashbackAdvisorService.recommend(1L, "5411")).thenReturn(List.of(
                new RecommendationDto("Bank1", "Супермаркеты", new BigDecimal("5.00")),
                new RecommendationDto("Bank2", "Продукты", new BigDecimal("3"))));

        handler.handleInput(user, "5411", DialogState.CARD_INPUT_MCC);

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(botSender).send(any(), text.capture(), any());
        assertThat(text.getValue())
                .contains("🥇 Bank1 — 5% (Супермаркеты)")
                .contains("🥈 Bank2 — 3% (Продукты)");
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setTelegramChatId(100L);
        return user;
    }
}
