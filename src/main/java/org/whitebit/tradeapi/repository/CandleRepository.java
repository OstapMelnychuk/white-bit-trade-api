package org.whitebit.tradeapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.whitebit.tradeapi.entity.CandleEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Long> {
    /**
     * Finds the last inserted candle (the one with the biggest ID).
     */
    Optional<CandleEntity> findTopByOrderByIdDesc();

    /**
     * Finds all candles within a specific day by timestamp range (Unix seconds or millis).
     * You should pass start and end of the day boundaries.
     */
    @Query("SELECT c FROM CandleEntity c WHERE c.timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY c.timestamp ASC")
    List<CandleEntity> findAllByDay(@Param("startOfDay") long startOfDay, @Param("endOfDay") long endOfDay);
}
