package com.verbatim.gre.controller;

import com.verbatim.gre.dto.Dtos.CardDto;
import com.verbatim.gre.dto.Dtos.GradeRequest;
import com.verbatim.gre.dto.Dtos.ProgressDto;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.repository.WordRepository;
import com.verbatim.gre.service.ReviewService;
import com.verbatim.gre.service.SrsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final SrsService srsService;
    private final WordRepository wordRepository;

    public ReviewController(ReviewService reviewService, SrsService srsService,
                            WordRepository wordRepository) {
        this.reviewService = reviewService;
        this.srsService = srsService;
        this.wordRepository = wordRepository;
    }

    @GetMapping("/session")
    public List<CardDto> session() {
        return reviewService.dueSession();
    }

    @PostMapping("/{wordId}/grade")
    public ProgressDto grade(@PathVariable Long wordId, @RequestBody GradeRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown word"));
        return ProgressDto.of(srsService.review(word, request.correct()));
    }
}
