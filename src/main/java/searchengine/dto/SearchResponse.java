package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SearchResponse {
    private boolean result;
    private int count;
    private List<SearchResult> data;

    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.count = 0;
        this.data = null;
    }
}
