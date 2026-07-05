package com.verbatim.gre.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Per-word learning state for the single implicit user. One row per {@link Word},
 * created lazily the first time the word is graded.
 */
@Entity
@Table(name = "word_progress")
public class WordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "word_id", unique = true, nullable = false)
    private Word word;

    /** Leitner box, 1 (learning) to 5 (mastered). */
    private int boxNumber = 1;

    private LocalDate nextReviewDate;

    private int timesCorrect = 0;

    private int timesIncorrect = 0;

    private LocalDateTime lastReviewed;

    protected WordProgress() {
    }

    public WordProgress(Word word) {
        this.word = word;
        this.boxNumber = 1;
        this.nextReviewDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Word getWord() {
        return word;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public int getTimesCorrect() {
        return timesCorrect;
    }

    public void incrementCorrect() {
        this.timesCorrect++;
    }

    public int getTimesIncorrect() {
        return timesIncorrect;
    }

    public void incrementIncorrect() {
        this.timesIncorrect++;
    }

    public LocalDateTime getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(LocalDateTime lastReviewed) {
        this.lastReviewed = lastReviewed;
    }
}
