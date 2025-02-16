package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.util.*;

public class LemmatizationService {
    private final LuceneMorphology luceneMorph;

    public LemmatizationService() throws Exception {
        this.luceneMorph = new RussianLuceneMorphology();
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