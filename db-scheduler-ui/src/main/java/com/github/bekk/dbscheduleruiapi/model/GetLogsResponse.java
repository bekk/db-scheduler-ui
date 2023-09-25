package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GetLogsResponse {
    private final int numberOfItems;
    private final int numberOfPages;
    private final List<LogModel> items;

    @JsonCreator
    public GetLogsResponse(
            @JsonProperty("numberOfItems") int totalLogs,
            @JsonProperty("items") List<LogModel> pagedLogs,
            @JsonProperty("pageSize") int pageSize) {
        this.numberOfItems = totalLogs;
        this.numberOfPages = totalLogs == 0 ? 0 : (int) Math.ceil((double) totalLogs / pageSize);
        this.items = pagedLogs;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public List<LogModel> getItems() {
        return items;
    }
}