package com.verbatim.gre.controller;

import com.verbatim.gre.dto.Dtos.QuizAnswerRequest;
import com.verbatim.gre.dto.Dtos.QuizQuestionDto;
import com.verbatim.gre.dto.Dtos.QuizResultDto;
import com.verbatim.gre.dto.Dtos.ProgressDto;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.model.WordProgress;
import com.verbatim.gre.repository.WordRepository;
import com.verbatim.gre.service.QuizService;
import com.verbatim.gre.service.SrsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;
    private final SrsService srsService;
    private final WordRepository wordRepository;

    public QuizController(QuizService quizService, SrsService srsService,
                          WordRepository wordRepository) {
        this.quizService = quizService;
        this.srsService = srsService;
        this.wordRepository = wordRepository;
    }

    @GetMapping("/next")
    public ResponseEntity<QuizQuestionDto> next() {
        return quizService.nextQuestion()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{wordId}/answer")
    public QuizResultDto answer(@PathVariable Long wordId, @RequestBody QuizAnswerRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown word"));
        boolean correct = word.getDefinition().equals(request.selectedDefinition());
        WordProgress progress = srsService.quiz(word, correct);
        return new QuizResultDto(correct, word.getDefinition(), ProgressDto.of(progress));
    }
}
