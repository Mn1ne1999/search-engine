package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.SearchResponse;
import searchengine.dto.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Override
    public SearchResponse search(String query, String site, int offset, int limit) {
        try {
            RussianLuceneMorphology morphology = new RussianLuceneMorphology();
            List<String> queryLemmas = morphology.getNormalForms(query.toLowerCase());
            List<Lemma> foundLemmas = lemmaRepository.findByLemmaIn(queryLemmas);

            if (foundLemmas.isEmpty()) {
                return new SearchResponse(true, 0, new ArrayList<>());
            }

            Map<Page, Float> relevanceMap = new HashMap<>();
            for (Lemma lemma : foundLemmas) {
                List<Index> indexes = indexRepository.findByLemma(lemma);
                for (Index index : indexes) {
                    relevanceMap.put(index.getPage(), relevanceMap.getOrDefault(index.getPage(), 0f) + index.getIndexRank());
                }
            }

            List<SearchResult> results = new ArrayList<>();
            for (Map.Entry<Page, Float> entry : relevanceMap.entrySet()) {
                Page page = entry.getKey();
                float relevance = entry.getValue();

                SearchResult result = new SearchResult(
                        page.getSite().getUrl(),
                        page.getSite().getName(),
                        page.getPath(),
                        extractTitle(page.getContent()),
                        generateSnippet(page.getContent(), queryLemmas),
                        relevance
                );
                results.add(result);
            }

            results.sort(Comparator.comparing(SearchResult::getRelevance).reversed());

            return new SearchResponse(true, results.size(), results.subList(offset, Math.min(offset + limit, results.size())));
        } catch (Exception e) {
            return new SearchResponse(false, e.getMessage());
        }
    }

    private String extractTitle(String content) {
        return content.substring(0, Math.min(content.length(), 60));
    }

    private String generateSnippet(String content, List<String> queryLemmas) {
        for (String lemma : queryLemmas) {
            content = content.replaceAll("(?i)" + lemma, "<b>" + lemma + "</b>");
        }
        return content.length() > 150 ? content.substring(0, 150) + "..." : content;
    }
}