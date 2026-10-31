package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.UserBankSelection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBankSelectionRepository extends JpaRepository<UserBankSelection, Long> {

    List<UserBankSelection> findAllByUserIdAndYearMonth(Long userId, String yearMonth);

    void deleteAllByUserIdAndBankIdAndYearMonth(Long userId, Long bankId, String yearMonth);
}
