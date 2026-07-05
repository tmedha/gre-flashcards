package com.verbatim.gre.service;

import com.verbatim.gre.model.Word;
import com.verbatim.gre.model.WordProgress;
import com.verbatim.gre.repository.WordProgressRepository;
import com.verbatim.gre.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SrsServiceTest {

    @Autowired
    WordRepository wordRepository;

    @Autowired
    WordProgressRepository progressRepository;

    SrsService srsService;
    Word word;

    @BeforeEach
    void setUp() {
        srsService = new SrsService(progressRepository);
        word = wordRepository.save(new Word("Ephemeral", "Lasting a very short time.",
                "adjective", 3, List.of("Fame can be ephemeral.")));
    }

    @Test
    void correctAnswerPromotesBoxAndSchedulesFurtherOut() {
        WordProgress progress = srsService.quiz(word, true);

        assertThat(progress.getBoxNumber()).isEqualTo(2);
        assertThat(progress.getTimesCorrect()).isEqualTo(1);
        assertThat(progress.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void wrongAnswerResetsToBoxOneAndIsDueImmediately() {
        srsService.quiz(word, true);  // box 2
        srsService.quiz(word, true);  // box 3

        WordProgress progress = srsService.quiz(word, false);

        assertThat(progress.getBoxNumber()).isEqualTo(1);
        assertThat(progress.getTimesIncorrect()).isEqualTo(1);
        assertThat(progress.getNextReviewDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void boxNumberCapsAtFive() {
        WordProgress progress = null;
        for (int i = 0; i < 8; i++) {
            progress = srsService.quiz(word, true);
        }

        assertThat(progress.getBoxNumber()).isEqualTo(SrsService.MAX_BOX);
        assertThat(progress.getNextReviewDate())
                .isEqualTo(LocalDate.now().plusDays(srsService.intervalDays(SrsService.MAX_BOX)));
    }

    @Test
    void wrongAnswerReappearsSoonerThanCorrectAnswer() {
        Word other = wordRepository.save(new Word("Laconic", "Using very few words.",
                "adjective", 4, List.of()));

        LocalDate afterCorrect = srsService.quiz(word, true).getNextReviewDate();
        LocalDate afterWrong = srsService.quiz(other, false).getNextReviewDate();

        assertThat(afterWrong).isBefore(afterCorrect);
    }

    @Test
    void flashcardReviewReschedulesButDoesNotCountAsAttempt() {
        WordProgress mastered = srsService.review(word, true);
        assertThat(mastered.getBoxNumber()).isEqualTo(2);
        // Self-rating must not touch the quiz-accuracy counters.
        assertThat(mastered.getTimesCorrect()).isZero();
        assertThat(mastered.getTimesIncorrect()).isZero();

        WordProgress again = srsService.review(word, false);
        assertThat(again.getBoxNumber()).isEqualTo(1);
        assertThat(again.getTimesIncorrect()).isZero();
    }
}
