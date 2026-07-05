package com.verbatim.gre.seed;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.verbatim.gre.model.Word;
import com.verbatim.gre.repository.WordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Loads the curated GRE word list from {@code words.json} into the database on
 * startup, but only when the words table is empty so existing progress is kept.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final WordRepository wordRepository;
    private final ObjectMapper objectMapper;

    public DataSeeder(WordRepository wordRepository, ObjectMapper objectMapper) {
        this.wordRepository = wordRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (wordRepository.count() > 0) {
            return;
        }
        try (InputStream in = new ClassPathResource("words.json").getInputStream()) {
            List<SeedWord> seeds = objectMapper.readValue(in, new TypeReference<List<SeedWord>>() {});
            List<Word> words = seeds.stream()
                    .map(s -> new Word(s.word(), s.definition(), s.partOfSpeech(),
                            s.difficulty(), s.examples()))
                    .toList();
            wordRepository.saveAll(words);
        }
    }

    /** Shape of one entry in words.json. */
    private record SeedWord(
            String word,
            String definition,
            String partOfSpeech,
            int difficulty,
            List<String> examples) {
    }
}
