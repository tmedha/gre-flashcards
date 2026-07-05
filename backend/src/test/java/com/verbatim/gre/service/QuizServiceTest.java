package com.verbatim.gre.service;

import com.verbatim.gre.dto.Dtos.QuizQuestionDto;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.repository.WordProgressRepository;
import com.verbatim.gre.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuizServiceTest {

    @Autowired
    WordRepository wordRepository;

    @Autowired
    WordProgressRepository progressRepository;

    QuizService quizService;
    Word target;

    @BeforeEach
    void setUp() {
        ReviewService reviewService = new ReviewService(wordRepository, progressRepository, 15);
        quizService = new QuizService(wordRepository, reviewService);

        target = wordRepository.save(new Word("Garrulous", "Excessively talkative.",
                "adjective", 4, List.of()));
        // Same part of speech, near difficulty -> preferred distractors.
        wordRepository.save(new Word("Loquacious", "Tending to talk a great deal.", "adjective", 4, List.of()));
        wordRepository.save(new Word("Taciturn", "Saying little; reserved.", "adjective", 4, List.of()));
        wordRepository.save(new Word("Laconic", "Using very few words.", "adjective", 4, List.of()));
        // Different part of speech / far difficulty -> should be avoided when possible.
        wordRepository.save(new Word("Squander", "To waste recklessly.", "verb", 2, List.of()));
        wordRepository.save(new Word("Paucity", "Smallness of quantity.", "noun", 4, List.of()));
    }

    @Test
    void buildsFourDistinctOptionsIncludingTheAnswer() {
        QuizQuestionDto question = quizService.buildQuestion(target);

        assertThat(question.options()).hasSize(QuizService.OPTION_COUNT);
        assertThat(question.options()).contains(target.getDefinition());
        assertThat(question.options()).doesNotHaveDuplicates();
        assertThat(question.word()).isEqualTo("Garrulous");
    }

    @Test
    void distractorsNeverIncludeTheCorrectDefinition() {
        List<Word> distractors = quizService.chooseDistractors(target);

        assertThat(distractors).hasSize(3);
        assertThat(distractors).noneMatch(w -> w.getDefinition().equals(target.getDefinition()));
    }

    @Test
    void prefersSamePartOfSpeechDistractors() {
        List<Word> distractors = quizService.chooseDistractors(target);

        // Three same-POS, same-difficulty options exist, so all distractors should match.
        assertThat(distractors).allMatch(w -> w.getPartOfSpeech().equals("adjective"));
    }
}
