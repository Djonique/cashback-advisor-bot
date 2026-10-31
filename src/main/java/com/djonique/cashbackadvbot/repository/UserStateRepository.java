package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateRepository extends JpaRepository<UserState, Long> {
}
