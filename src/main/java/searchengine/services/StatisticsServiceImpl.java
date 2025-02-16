package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.DetailedStatisticsItem;
import searchengine.dto.StatisticsData;
import searchengine.dto.StatisticsResponse;
import searchengine.dto.TotalStatistics;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.LemmaRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        // Получаем сайты из базы
        List<Site> indexedSites = siteRepository.findAll();
        total.setSites(indexedSites.size());
        total.setIndexing(false);

        for (Site site : indexedSites) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setStatus(site.getStatus().toString());
            item.setStatusTime(site.getStatusTime().toEpochSecond(java.time.ZoneOffset.UTC));

            // Получаем реальное количество страниц и лемм
            int pages = pageRepository.countBySite(site);
            int lemmas = lemmaRepository.countBySite(site);

            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setError(site.getLastError());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        // Формируем ответ
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}