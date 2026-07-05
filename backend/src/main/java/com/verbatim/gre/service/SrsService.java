package com.verbatim.gre.service;

import com.verbatim.gre.model.Word;
import com.verbatim.gre.model.WordProgress;
import com.verbatim.gre.repository.WordProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implements a Leitner-style spaced repetition system.
 *
 * <p>Each word lives in one of five boxes. A success promotes the word one box (up
 * to box 5) and schedules its next review further out; a failure demotes it all the
 * way back to box 1 so it resurfaces immediately. The interval for a box grows
 * roughly geometrically, so mastered words are seen rarely while struggling words
 * are drilled often.
 *
 * <p>There are two deliberately distinct entry points:
 * <ul>
 *   <li>{@link #review} — a flashcard self-rating ("Mastered" / "Review Again"). It
 *       reschedules the word (driving mastery) but is <em>not</em> counted as a
 *       graded attempt, so it never moves quiz accuracy.</li>
 *   <li>{@link #quiz} — an objective multiple-choice answer. It reschedules the word
 *       <em>and</em> records a correct/incorrect attempt used for accuracy.</li>
 * </ul>
 */
@Service
public class SrsService {

    static final int MAX_BOX = 5;

    /** Days until the next review for boxes 1..5 (index 0 unused). */
    private static final int[] BOX_INTERVAL_DAYS = {0, 1, 2, 4, 8, 16};

    private final WordProgressRepository progressRepository;

    public SrsService(WordProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    /** Fetches the progress row for a word, creating a fresh box-1 row if none exists. */
    @Transactional
    public WordProgress getOrCreate(Word word) {
        return progressRepository.findByWordId(word.getId())
                .orElseGet(() -> progressRepository.save(new WordProgress(word)));
    }

    /**
     * Records a flashcard self-rating and reschedules the word. Does not count as a
     * graded attempt, so it does not affect quiz accuracy.
     *
     * @param recalled whether the learner marked the word "Mastered" (true) or
     *                 "Review Again" (false)
     */
    @Transactional
    public WordProgress review(Word word, boolean recalled) {
        return apply(word, recalled, false);
    }

    /**
     * Records the outcome of a multiple-choice answer and reschedules the word. This
     * counts as a graded attempt and feeds quiz accuracy.
     */
    @Transactional
    public WordProgress quiz(Word word, boolean correct) {
        return apply(word, correct, true);
    }

    private WordProgress apply(Word word, boolean success, boolean countAttempt) {
        WordProgress progress = getOrCreate(word);
        LocalDate today = LocalDate.now();

        if (success) {
            if (countAttempt) {
                progress.incrementCorrect();
            }
            int newBox = Math.min(progress.getBoxNumber() + 1, MAX_BOX);
            progress.setBoxNumber(newBox);
            progress.setNextReviewDate(today.plusDays(BOX_INTERVAL_DAYS[newBox]));
        } else {
            if (countAttempt) {
                progress.incrementIncorrect();
            }
            progress.setBoxNumber(1);
            // Due again immediately so it comes back within the same study session.
            progress.setNextReviewDate(today);
        }

        progress.setLastReviewed(LocalDateTime.now());
        return progressRepository.save(progress);
    }

    /** Number of days a word in the given box waits before its next review. */
    public int intervalDays(int boxNumber) {
        return BOX_INTERVAL_DAYS[boxNumber];
    }
}
