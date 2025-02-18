package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.*;
import searchengine.repositories.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class PageCrawler extends RecursiveTask<Void> {
    private final Site site;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final String url;

    public PageCrawler(Site site, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, String url) {
        this.site = site;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.url = url;
    }

    @Override
    protected Void compute() {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("http://www.google.com")
                    .get();

            // ✅ Сохраняем страницу
            Page page = new Page();
            page.setSite(site);
            page.setPath(url.replace(site.getUrl(), ""));
            page.setCode(200);
            page.setContent(doc.html());
            pageRepository.save(page);

            // ✅ Разбираем текст страницы и сохраняем леммы (как было)
            String text = doc.body().text();
            Map<String, Integer> lemmas = new LemmatizationService().getLemmas(text);

            for (var entry : lemmas.entrySet()) {
                String lemmaText = entry.getKey();
                int count = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, site)
                        .orElseGet(() -> {
                            Lemma newLemma = new Lemma();
                            newLemma.setSite(site);
                            newLemma.setLemma(lemmaText);
                            newLemma.setFrequency(0);
                            return lemmaRepository.save(newLemma);
                        });

                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);

                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setIndexRank(count);
                indexRepository.save(index);
            }

            // ✅ Ищем ссылки внутри страницы и добавляем в ForkJoinPool
            List<PageCrawler> subTasks = new ArrayList<>();
            Elements links = doc.select("a[href]"); // Получаем все ссылки

            for (Element link : links) {
                String nextUrl = link.absUrl("href"); // Получаем полный URL
                if (!nextUrl.isEmpty() && nextUrl.startsWith(site.getUrl())) {
                    subTasks.add(new PageCrawler(site, pageRepository, lemmaRepository, indexRepository, nextUrl));
                }
            }

            // ✅ Запускаем задачи параллельно
            invokeAll(subTasks);

        } catch (IOException e) {
            System.err.println("Ошибка при загрузке: " + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}