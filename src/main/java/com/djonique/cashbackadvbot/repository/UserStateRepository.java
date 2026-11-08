package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.UserState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserStateRepository extends JpaRepository<UserState, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserState s WHERE s.id = :id")
    Optional<UserState> findByIdForUpdate(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserState s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserState s WHERE s.user.id IN :userIds")
    void deleteByUserIds(@Param("userIds") Collection<Long> userIds);
}
