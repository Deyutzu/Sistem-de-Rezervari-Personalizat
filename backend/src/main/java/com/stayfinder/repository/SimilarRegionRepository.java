package com.stayfinder.repository;

import com.stayfinder.model.SimilarRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SimilarRegionRepository extends JpaRepository<SimilarRegion, Long> {

    /**
     * Relația e simetrică, dar o stocăm o singură dată (A,B).
     * De aceea căutăm regiunea în ambele coloane.
     */
    @Query("SELECT s FROM SimilarRegion s WHERE s.regionA = :region OR s.regionB = :region")
    List<SimilarRegion> findAllInvolving(@Param("region") String region);
}
