package com.verbatim.gre.service;

import com.verbatim.gre.dto.Dtos.CardDto;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.model.WordProgress;
import com.verbatim.gre.repository.WordProgressRepository;
import com.verbatim.gre.repository.WordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the daily study queue. A word is "due" when it has never been seen or when
 * its next review date has arrived. New words are surfaced first, then the words
 * that are most overdue, capped at the configured daily limit.
 */
@Service
public class ReviewService {

    private final WordRepository wordRepository;
    private final WordProgressRepository progressRepository;
    private final int dailyLimit;

    public ReviewService(WordRepository wordRepository,
                         WordProgressRepository progressRepository,
                         @Value("${verbatim.daily-limit:15}") int dailyLimit) {
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
        this.dailyLimit = dailyLimit;
    }

    @Transactional(readOnly = true)
    public List<CardDto> dueSession() {
        return dueWords().stream()
                .map(word -> {
                    WordProgress p = progressByWordId().get(word.getId());
                    return CardDto.of(word, p != null ? p.getBoxNumber() : 1);
                })
                .toList();
    }

    /** The words due today, new ones first, most-overdue next, capped at the daily limit. */
    @Transactional(readOnly = true)
    public List<Word> dueWords() {
        LocalDate today = LocalDate.now();
        Map<Long, WordProgress> progress = progressByWordId();

        List<Word> newWords = new ArrayList<>();
        List<Word> overdue = new ArrayList<>();
        for (Word word : wordRepository.findAll()) {
            WordProgress p = progress.get(word.getId());
            if (p == null) {
                newWords.add(word);
            } else if (!p.getNextReviewDate().isAfter(today)) {
                overdue.add(word);
            }
        }
        // Most overdue (oldest review date) first.
        overdue.sort(Comparator.comparing(w -> progress.get(w.getId()).getNextReviewDate()));

        List<Word> due = new ArrayList<>(newWords);
        due.addAll(overdue);
        return due.size() > dailyLimit ? due.subList(0, dailyLimit) : due;
    }

    private Map<Long, WordProgress> progressByWordId() {
        return progressRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getWord().getId(), Function.identity()));
    }

    public int getDailyLimit() {
        return dailyLimit;
    }
}
