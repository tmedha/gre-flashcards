package com.verbatim.gre.dto;

import com.verbatim.gre.model.Word;
import com.verbatim.gre.model.WordProgress;

import java.time.LocalDate;
import java.util.List;

/**
 * API data-transfer objects. Grouped in one file since they are small, immutable
 * records that form the JSON contract with the frontend.
 */
public final class Dtos {

    private Dtos() {
    }

    /** A flashcard for review, including example sentences for the hint toggle. */
    public record CardDto(
            Long id,
            String word,
            String definition,
            String partOfSpeech,
            int difficulty,
            List<String> exampleSentences,
            int boxNumber) {

        public static CardDto of(Word word, int boxNumber) {
            return new CardDto(
                    word.getId(),
                    word.getWord(),
                    word.getDefinition(),
                    word.getPartOfSpeech(),
                    word.getDifficulty(),
                    word.getExampleSentences(),
                    boxNumber);
        }
    }

    public record GradeRequest(boolean correct) {
    }

    public record ProgressDto(
            Long wordId,
            int boxNumber,
            LocalDate nextReviewDate,
            int timesCorrect,
            int timesIncorrect) {

        public static ProgressDto of(WordProgress p) {
            return new ProgressDto(
                    p.getWord().getId(),
                    p.getBoxNumber(),
                    p.getNextReviewDate(),
                    p.getTimesCorrect(),
                    p.getTimesIncorrect());
        }
    }

    /** A multiple-choice question. The correct answer is not marked; the server verifies. */
    public record QuizQuestionDto(
            Long wordId,
            String word,
            String partOfSpeech,
            int difficulty,
            List<String> options) {
    }

    public record QuizAnswerRequest(String selectedDefinition) {
    }

    public record QuizResultDto(
            boolean correct,
            String correctDefinition,
            ProgressDto progress) {
    }

    public record DifficultyBreakdown(
            int difficulty,
            long totalWords,
            long seenWords,
            long masteredWords,
            int accuracyPercent) {
    }

    public record AnalyticsDto(
            long totalWords,
            long seenWords,
            long masteredWords,
            int masteryPercent,
            int accuracyPercent,
            int quizAttempts,
            int streak,
            long wordsToday,
            List<DifficultyBreakdown> byDifficulty) {
    }
}
