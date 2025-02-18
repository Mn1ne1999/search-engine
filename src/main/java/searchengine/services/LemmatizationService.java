package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LemmatizationService {
    @Autowired
    private final LuceneMorphology luceneMorph;

    public LemmatizationService() {
        try {
            this.luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки лемматизатора", e);
        }
    }

    public Map<String, Integer> getLemmas(String text) {
        Map<String, Integer> lemmaCount = new HashMap<>();
        String[] words = text.toLowerCase().replaceAll("[^а-яё ]", "").split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;
            List<String> normalForms = luceneMorph.getNormalForms(word);
            if (!normalForms.isEmpty()) {
                String lemma = normalForms.get(0);
                lemmaCount.put(lemma, lemmaCount.getOrDefault(lemma, 0) + 1);
            }
        }
        return lemmaCount;
    }
}