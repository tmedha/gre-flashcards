package com.verbatim.gre.service;

import com.verbatim.gre.dto.Dtos.AnalyticsDto;
import com.verbatim.gre.dto.Dtos.DifficultyBreakdown;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.model.WordProgress;
import com.verbatim.gre.repository.WordProgressRepository;
import com.verbatim.gre.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aggregates study performance: overall mastery, accuracy, current streak, and a
 * per-difficulty breakdown. A word counts as "mastered" once it reaches box 4.
 */
@Service
public class AnalyticsService {

    static final int MASTERY_BOX = 4;

    private final WordRepository wordRepository;
    private final WordProgressRepository progressRepository;

    public AnalyticsService(WordRepository wordRepository,
                            WordProgressRepository progressRepository) {
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto snapshot() {
        List<Word> words = wordRepository.findAll();
        List<WordProgress> progressList = progressRepository.findAll();
        Map<Long, WordProgress> progressByWord = progressList.stream()
                .collect(Collectors.toMap(p -> p.getWord().getId(), Function.identity()));

        long totalWords = words.size();
        long seenWords = progressList.size();
        long masteredWords = progressList.stream()
                .filter(p -> p.getBoxNumber() >= MASTERY_BOX)
                .count();

        int masteryPercent = percent(masteredWords, totalWords);
        int accuracyPercent = accuracyPercent(progressList);
        int quizAttempts = (int) progressList.stream()
                .mapToLong(p -> p.getTimesCorrect() + p.getTimesIncorrect())
                .sum();
        int streak = currentStreak(progressList);
        long wordsToday = progressList.stream()
                .filter(p -> p.getLastReviewed() != null
                        && p.getLastReviewed().toLocalDate().equals(LocalDate.now()))
                .count();

        return new AnalyticsDto(
                totalWords, seenWords, masteredWords, masteryPercent, accuracyPercent,
                quizAttempts, streak, wordsToday, byDifficulty(words, progressByWord));
    }

    private List<DifficultyBreakdown> byDifficulty(List<Word> words,
                                                   Map<Long, WordProgress> progressByWord) {
        List<DifficultyBreakdown> result = new ArrayList<>();
        for (int level = 1; level <= 5; level++) {
            final int difficulty = level;
            List<Word> atLevel = words.stream()
                    .filter(w -> w.getDifficulty() == difficulty)
                    .toList();
            List<WordProgress> seen = atLevel.stream()
                    .map(w -> progressByWord.get(w.getId()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            long mastered = seen.stream().filter(p -> p.getBoxNumber() >= MASTERY_BOX).count();
            result.add(new DifficultyBreakdown(
                    difficulty, atLevel.size(), seen.size(), mastered, accuracyPercent(seen)));
        }
        return result;
    }

    /** Accuracy across graded quiz attempts only (flashcard self-ratings excluded). */
    private int accuracyPercent(List<WordProgress> progressList) {
        long correct = progressList.stream().mapToLong(WordProgress::getTimesCorrect).sum();
        long incorrect = progressList.stream().mapToLong(WordProgress::getTimesIncorrect).sum();
        return percent(correct, correct + incorrect);
    }

    /** Consecutive days ending today (or yesterday) on which any word was reviewed. */
    private int currentStreak(List<WordProgress> progressList) {
        Set<LocalDate> activeDays = new HashSet<>();
        for (WordProgress p : progressList) {
            if (p.getLastReviewed() != null) {
                activeDays.add(p.getLastReviewed().toLocalDate());
            }
        }
        if (activeDays.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        // Allow the streak to "count" even if the user hasn't studied yet today.
        LocalDate cursor = activeDays.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private static int percent(long numerator, long denominator) {
        return denominator == 0 ? 0 : (int) Math.round(100.0 * numerator / denominator);
    }
}
