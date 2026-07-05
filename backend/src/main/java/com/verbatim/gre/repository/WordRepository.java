package com.verbatim.gre.repository;

import com.verbatim.gre.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByPartOfSpeech(String partOfSpeech);
}
