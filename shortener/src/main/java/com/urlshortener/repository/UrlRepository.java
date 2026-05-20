package com.urlshortener.repository;

import com.urlshortener.domain.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    @Query("SELECT u FROM Url u ORDER BY u.accessCount DESC LIMIT :n")
    List<Url> findTopNByAccessCount(int n);

    @Modifying
    @Query("DELETE FROM Url u WHERE u.expiredAt IS NOT NULL AND u.expiredAt < :now")
    void deleteExpired(OffsetDateTime now);

    @Modifying
    @Query("UPDATE Url u SET u.accessCount = u.accessCount + :delta WHERE u.shortCode = :shortCode")
    void incrementAccessCount(String shortCode, long delta);
}
