package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.bot.state.DialogState;
import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.UserState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(properties = {
        "telegrambots.enabled=false",
        "app.telegram.registration-enabled=false",
        "app.telegram.bot-token=test-token",
        "app.telegram.bot-username=TestCashbackBot"
})
class RepositoryQueryIntegrationTest {

    @Autowired
    private BankCategoryRepository bankCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserBankSelectionRepository userBankSelectionRepository;

    @Autowired
    private BankExcludedMccRepository bankExcludedMccRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldExecuteCatalogQueries() {
        assertThat(bankCategoryRepository.findAllByBankId(1L)).isNotNull();
        assertThat(categoryRepository.findByNameOrAliasContaining("супер")).isNotNull();
        assertThat(storeRepository.findByNameOrAliasContaining("пятерочка")).isNotEmpty();
    }

    @Test
    void shouldExecuteSelectionQueryWithEntityGraph() {
        assertThat(userBankSelectionRepository.findAllByUserIdAndYearMonth(1L, "2026-11")).isNotNull();
    }

    @Test
    void shouldExecuteExcludedMccQuery() {
        assertThat(bankExcludedMccRepository.findBankIdsByMccCode("5411")).isNotNull();
    }

    @Test
    @Transactional
    void shouldIncrementUserStateVersion() {
        AppUser user = new AppUser();
        user.setTelegramChatId(9_999_999L);
        entityManager.persist(user);

        UserState state = new UserState();
        state.setUser(user);
        state.setState(DialogState.IDLE.name());
        entityManager.persist(state);
        entityManager.flush();

        assertThat(state.getVersion()).isZero();

        state.setState(DialogState.CARD_INPUT_MCC.name());
        entityManager.flush();

        assertThat(state.getVersion()).isEqualTo(1L);
    }
}
