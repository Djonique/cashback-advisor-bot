package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankExcludedMcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface BankExcludedMccRepository extends JpaRepository<BankExcludedMcc, Long> {

    @Query("SELECT e.bank.id FROM BankExcludedMcc e WHERE e.mccCode = :mccCode")
    Set<Long> findBankIdsByMccCode(@Param("mccCode") String mccCode);

    @Query("SELECT e.bank.id FROM BankExcludedMcc e WHERE e.mccCode IN :mccCodes")
    Set<Long> findBankIdsByMccCodes(@Param("mccCodes") Collection<String> mccCodes);
}
