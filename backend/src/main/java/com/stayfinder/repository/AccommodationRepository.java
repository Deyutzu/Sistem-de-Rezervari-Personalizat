package com.stayfinder.repository;

import com.stayfinder.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data generează automat implementarea la pornire.
 * JpaRepository -> CRUD + paginare (findAll(Pageable)).
 * JpaSpecificationExecutor -> findAll(Specification, Pageable), folosit pentru
 * filtrele opționale combinate (vezi AccommodationSpecifications).
 */
public interface AccommodationRepository
        extends JpaRepository<Accommodation, Long>, JpaSpecificationExecutor<Accommodation> {
}
