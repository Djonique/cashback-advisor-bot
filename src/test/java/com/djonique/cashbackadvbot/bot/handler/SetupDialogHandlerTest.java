package com.djonique.cashbackadvbot.bot.handler;

import com.djonique.cashbackadvbot.bot.BotSender;
import com.djonique.cashbackadvbot.bot.keyboard.KeyboardFactory;
import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.service.BankCatalogService;
import com.djonique.cashbackadvbot.service.SelectionService;
import com.djonique.cashbackadvbot.service.UserStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetupDialogHandlerTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private SelectionService selectionService;

    @Mock
    private BankCatalogService bankCatalogService;

    @Mock
    private BotSender botSender;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private SetupDialogHandler handler;

    @Test
    void shouldNotSave_whenSomeSelectedCategoriesHaveNoPercent() {
        AppUser user = user();
        SelectionContext context = new SelectionContext(
                1L,
                Set.of(10L, 20L),
                Map.of(10L, new BigDecimal("5")),
                List.of(10L));
        stubUpdateContext(user, context);

        handler.handlePercentInput(user, "6");

        verify(selectionService, never()).replaceSelection(anyLong(), anyLong(), any());
        verify(userStateService).clearState(user);
        verify(botSender).send(eq(user.getTelegramChatId()),
                eq("Не удалось сохранить настройки, начните заново"), any());
    }

    @Test
    void shouldReportConflict_whenOptimisticLockFails() {
        AppUser user = user();
        SelectionContext context = new SelectionContext(
                1L,
                Set.of(10L),
                Map.of(),
                List.of(10L));
        stubUpdateContext(user, context);

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(selectionService).replaceSelection(eq(1L), eq(1L), any());

        handler.handlePercentInput(user, "5");

        verify(userStateService).clearState(user);
        verify(botSender).send(eq(user.getTelegramChatId()),
                eq("Настройки изменились в другом сообщении, начните заново"), any());
    }

    @Test
    void shouldNotAskPercent_whenNothingSelected() {
        AppUser user = user();
        SelectionContext context = new SelectionContext(1L, Set.of(), Map.of(), List.of());

        handler.startPercentInput(user, context);

        verify(userStateService, never()).setState(any(), any(), any());
        verify(botSender).send(eq(user.getTelegramChatId()),
                eq("Вы ничего не выбрали"), any());
    }

    @Test
    void shouldSaveSelection_whenAllPercentsCollected() {
        AppUser user = user();
        SelectionContext context = new SelectionContext(
                1L,
                Set.of(10L),
                Map.of(),
                List.of(10L));
        stubUpdateContext(user, context);

        handler.handlePercentInput(user, "5");

        verify(selectionService).replaceSelection(eq(1L), eq(1L), any());
        verify(userStateService).clearState(user);
        verify(botSender).send(eq(user.getTelegramChatId()),
                eq("✅ Настройки сохранены! Теперь вы можете выбрать карту"), any());
    }

    private void stubUpdateContext(AppUser user, SelectionContext context) {
        when(userStateService.updateContext(eq(user), eq(DialogState.ENTERING_PERCENT),
                eq(SelectionContext.class), any()))
                .thenAnswer(invocation -> {
                    UnaryOperator<SelectionContext> mutator = invocation.getArgument(3);
                    return mutator.apply(context);
                });
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setTelegramChatId(100L);
        return user;
    }
}
