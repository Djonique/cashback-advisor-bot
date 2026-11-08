package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.UserState;
import com.djonique.cashbackadvbot.dto.SelectionContext;
import com.djonique.cashbackadvbot.repository.UserStateRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStateServiceTest {

    @Mock
    private UserStateRepository userStateRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserStateService userStateService;

    @Test
    void shouldReturnIdle_whenStateDoesNotExist() {
        when(userStateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(userStateService.getState(1L)).isEqualTo(DialogState.IDLE);
    }

    @Test
    void shouldReturnIdle_whenStateIsUnknown() {
        UserState state = new UserState();
        state.setState("UNKNOWN");
        when(userStateRepository.findById(1L)).thenReturn(Optional.of(state));

        assertThat(userStateService.getState(1L)).isEqualTo(DialogState.IDLE);
    }

    @Test
    void shouldDeleteStateByUserId() {
        userStateService.clearState(1L);

        verify(userStateRepository).deleteByUserId(1L);
    }

    @Test
    void shouldUpdateContextInSingleTransaction() {
        UserState state = new UserState();
        state.setState(DialogState.IDLE.name());

        when(userStateRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(state));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"bankId\":2}");

        SelectionContext result = userStateService.updateContext(
                1L,
                DialogState.CHOOSING_BANK,
                SelectionContext.class,
                current -> new SelectionContext(2L, Set.of(10L), Map.of(), List.of()));

        assertThat(result.bankId()).isEqualTo(2L);
        assertThat(state.getState()).isEqualTo(DialogState.CHOOSING_BANK.name());
        assertThat(state.getContext()).isEqualTo("{\"bankId\":2}");
        verify(userStateRepository).save(state);
    }

    @Test
    void shouldReferenceUser_whenStateIsCreated() {
        AppUser user = new AppUser();
        user.setId(1L);
        when(userStateRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        when(entityManager.getReference(AppUser.class, 1L)).thenReturn(user);

        userStateService.setState(1L, DialogState.CHOOSING_BANK, null);

        verify(entityManager).getReference(AppUser.class, 1L);
        verify(userStateRepository).save(any(UserState.class));
    }
}
