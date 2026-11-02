package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateRepository extends JpaRepository<UserState, Long> {

    @Modifying
    @Query("DELETE FROM UserState s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
