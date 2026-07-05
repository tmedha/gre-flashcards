package com.verbatim.gre.service;

import com.verbatim.gre.dto.Dtos.QuizQuestionDto;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Generates multiple-choice questions with <em>smart distractors</em>: the three
 * wrong options are real GRE words whose part of speech and difficulty are as close
 * as possible to the target, so the question tests genuine understanding rather than
 * being given away by an obviously unrelated definition.
 */
@Service
public class QuizService {

    static final int OPTION_COUNT = 4;
    private static final int DISTRACTORS = OPTION_COUNT - 1;

    private final WordRepository wordRepository;
    private final ReviewService reviewService;

    public QuizService(WordRepository wordRepository, ReviewService reviewService) {
        this.wordRepository = wordRepository;
        this.reviewService = reviewService;
    }

    /** Picks the next word to quiz, preferring words that are due for review. */
    @Transactional(readOnly = true)
    public Optional<QuizQuestionDto> nextQuestion() {
        List<Word> due = reviewService.dueWords();
        List<Word> pool = due.isEmpty() ? wordRepository.findAll() : due;
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        Word target = pool.get(new java.util.Random().nextInt(pool.size()));
        return Optional.of(buildQuestion(target));
    }

    QuizQuestionDto buildQuestion(Word target) {
        List<String> options = new ArrayList<>();
        options.add(target.getDefinition());
        for (Word distractor : chooseDistractors(target)) {
            options.add(distractor.getDefinition());
        }
        Collections.shuffle(options);
        return new QuizQuestionDto(
                target.getId(),
                target.getWord(),
                target.getPartOfSpeech(),
                target.getDifficulty(),
                options);
    }

    /**
     * Ranks every other word by similarity to the target and returns the closest
     * three. Same part of speech is strongly preferred, then nearest difficulty,
     * so distractors are plausible. Falls back to the whole pool if a category is
     * thin, guaranteeing three distinct distractors whenever they exist.
     */
    List<Word> chooseDistractors(Word target) {
        List<Word> candidates = new ArrayList<>();
        for (Word w : wordRepository.findAll()) {
            if (!w.getId().equals(target.getId())
                    && !w.getDefinition().equals(target.getDefinition())) {
                candidates.add(w);
            }
        }
        candidates.sort(Comparator
                .comparingInt((Word w) -> samePartOfSpeech(w, target) ? 0 : 1)
                .thenComparingInt(w -> Math.abs(w.getDifficulty() - target.getDifficulty())));
        return candidates.subList(0, Math.min(DISTRACTORS, candidates.size()));
    }

    private boolean samePartOfSpeech(Word a, Word b) {
        return a.getPartOfSpeech() != null && a.getPartOfSpeech().equals(b.getPartOfSpeech());
    }
}
