package com.verbatim.gre.repository;

import com.verbatim.gre.model.WordProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordProgressRepository extends JpaRepository<WordProgress, Long> {

    Optional<WordProgress> findByWordId(Long wordId);

    List<WordProgress> findByNextReviewDateLessThanEqual(LocalDate date);

    long countByBoxNumberGreaterThanEqual(int boxNumber);
}
