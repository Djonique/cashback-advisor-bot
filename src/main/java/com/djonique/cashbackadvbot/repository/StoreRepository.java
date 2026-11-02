package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT s FROM Store s
            WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :text, '%')) ESCAPE '!'
               OR LOWER(s.aliases) LIKE LOWER(CONCAT('%', :text, '%')) ESCAPE '!'
            """)
    List<Store> findByNameOrAliasContaining(@Param("text") String text);
}
