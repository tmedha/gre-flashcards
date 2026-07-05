package com.verbatim.gre.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A single GRE vocabulary word and its teaching content.
 */
@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String word;

    @Column(nullable = false, length = 1000)
    private String definition;

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    /** Subjective difficulty on a 1 (easier) to 5 (hardest) scale. */
    private int difficulty;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "word_examples", joinColumns = @JoinColumn(name = "word_id"))
    @Column(name = "example", length = 1000)
    private List<String> exampleSentences = new ArrayList<>();

    protected Word() {
    }

    public Word(String word, String definition, String partOfSpeech, int difficulty,
                List<String> exampleSentences) {
        this.word = word;
        this.definition = definition;
        this.partOfSpeech = partOfSpeech;
        this.difficulty = difficulty;
        this.exampleSentences = exampleSentences != null ? exampleSentences : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public List<String> getExampleSentences() {
        return exampleSentences;
    }
}
