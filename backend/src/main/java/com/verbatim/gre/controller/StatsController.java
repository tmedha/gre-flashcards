package com.verbatim.gre.controller;

import com.verbatim.gre.dto.Dtos.AnalyticsDto;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.repository.WordProgressRepository;
import com.verbatim.gre.repository.WordRepository;
import com.verbatim.gre.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final AnalyticsService analyticsService;
    private final WordProgressRepository progressRepository;
    private final WordRepository wordRepository;

    public StatsController(AnalyticsService analyticsService,
                           WordProgressRepository progressRepository,
                           WordRepository wordRepository) {
        this.analyticsService = analyticsService;
        this.progressRepository = progressRepository;
        this.wordRepository = wordRepository;
    }

    @GetMapping("/analytics")
    public AnalyticsDto analytics() {
        return analyticsService.snapshot();
    }

    /** Clears all study progress (the reset button). Word content is preserved. */
    @PostMapping("/progress/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset() {
        progressRepository.deleteAll();
    }

    @GetMapping("/words")
    public List<Word> words() {
        return wordRepository.findAll();
    }
}
