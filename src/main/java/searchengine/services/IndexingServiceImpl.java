package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;  // Загружаем список сайтов

    private ForkJoinPool forkJoinPool = null;

    @Override
    public synchronized boolean startIndexing() {
        // Проверяем, существует ли ForkJoinPool, если нет — создаём
        if (forkJoinPool == null || forkJoinPool.isShutdown()) {
            forkJoinPool = new ForkJoinPool();
        }

        // Очищаем базу перед индексацией
        indexRepository.deleteAll();
        pageRepository.deleteAll();
        lemmaRepository.deleteAll();
        siteRepository.deleteAll();

        //  Запускаем обход сайтов
        for (var siteConfig : sitesList.getSites()) {
            Site site = new Site();
            site.setUrl(siteConfig.getUrl());
            site.setName(siteConfig.getName());
            site.setStatus(Site.Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            forkJoinPool.execute(new PageCrawler(site, pageRepository, lemmaRepository, indexRepository, site.getUrl()));
        }

        return true;
    }

    @Override
    public boolean stopIndexing() {
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdownNow();
        }
        return true;
    }
}