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

    private ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Override
    public boolean startIndexing() {
        // 1. Убеждаемся, что старый ForkJoinPool завершён, и создаём новый
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdownNow();
        }
        forkJoinPool = new ForkJoinPool(); // Пересоздаём пул потоков перед запуском

        // 2. Удаляем старые данные (в правильном порядке)
        indexRepository.deleteAll(); // Удаляем индексы
        pageRepository.deleteAll();   // Удаляем страницы
        lemmaRepository.deleteAll();  // Удаляем леммы
        siteRepository.deleteAll();   // Удаляем сайты

        // 3. Запускаем индексацию заново
        for (var siteConfig : sitesList.getSites()) {
            Site site = new Site();
            site.setUrl(siteConfig.getUrl());
            site.setName(siteConfig.getName());
            site.setStatus(Site.Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            // Отправляем задачу в ForkJoinPool (обход страниц)
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