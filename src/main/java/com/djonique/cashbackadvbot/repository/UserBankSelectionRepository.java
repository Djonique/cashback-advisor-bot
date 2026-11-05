package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.UserBankSelection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBankSelectionRepository extends JpaRepository<UserBankSelection, Long> {

    @EntityGraph(attributePaths = {"bank", "category"})
    List<UserBankSelection> findAllByUserIdAndYearMonth(Long userId, String yearMonth);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM UserBankSelection s
            WHERE s.user.id = :userId
              AND s.bank.id = :bankId
              AND s.yearMonth = :yearMonth
            """)
    void deleteSelection(@Param("userId") Long userId,
                         @Param("bankId") Long bankId,
                         @Param("yearMonth") String yearMonth);

    @Modifying
    @Query("DELETE FROM UserBankSelection u WHERE u.yearMonth < :currentMonth")
    void deleteOldSelections(@Param("currentMonth") String currentMonth);
}
