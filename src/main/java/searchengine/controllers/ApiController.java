package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final IndexingService indexingService;
    private final StatisticsService statisticsService;

    @GetMapping("/startIndexing")
    public String startIndexing() {
        boolean result = indexingService.startIndexing();
        return result ? "Индексация запущено" : "Индексация не запущенно";
    }

    @GetMapping("/stopIndexing")
    public String stopIndexing() {
        boolean result = indexingService.stopIndexing();
        return result ? "Индексация остановленно" : "Индексация не остановлено";
    }
    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        return statisticsService.getStatistics();
    }
}