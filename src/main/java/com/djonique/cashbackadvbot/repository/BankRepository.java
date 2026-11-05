package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    List<Bank> findAllByOrderByDisplayOrderAsc();
}
