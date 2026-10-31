package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankExcludedMcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankExcludedMccRepository extends JpaRepository<BankExcludedMcc, Long> {

    boolean existsByBankIdAndMccCode(Long bankId, String mccCode);
}
