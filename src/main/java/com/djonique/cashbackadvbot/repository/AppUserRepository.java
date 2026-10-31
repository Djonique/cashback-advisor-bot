package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByTelegramChatId(Long telegramChatId);
}
