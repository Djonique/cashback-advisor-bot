package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface BankCategoryRepository extends JpaRepository<BankCategory, Long> {

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT bc FROM BankCategory bc
            WHERE bc.bank.id = :bankId
            ORDER BY bc.category.name
            """)
    List<BankCategory> findAllByBankId(@Param("bankId") Long bankId);

    @EntityGraph(attributePaths = {"category", "bank"})
    List<BankCategory> findAllByBankIdIn(Collection<Long> bankIds);

    @EntityGraph(attributePaths = {"category", "bank"})
    @Query("""
            SELECT bc FROM BankCategory bc
            WHERE bc.bank.id = :bankId
              AND bc.category.id IN :categoryIds
            """)
    List<BankCategory> findAllByBankIdAndCategoryIds(
            @Param("bankId") Long bankId,
            @Param("categoryIds") Collection<Long> categoryIds);

    @Query("SELECT bc.category.id FROM BankCategory bc WHERE bc.bank.id = :bankId")
    Set<Long> findCategoryIdsByBankId(@Param("bankId") Long bankId);

    boolean existsByBank_IdAndCategory_Id(Long bankId, Long categoryId);
}
