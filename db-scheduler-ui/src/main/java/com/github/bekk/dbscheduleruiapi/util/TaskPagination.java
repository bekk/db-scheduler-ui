package com.github.bekk.dbscheduleruiapi.util;

import java.util.ArrayList;
import java.util.List;

public class TaskPagination {

    public static <T> List<T> paginate(List<T> allItems, int pageNumber, int pageSize) {
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allItems.size());

        return (startIndex < endIndex) ? allItems.subList(startIndex, endIndex) : new ArrayList<>();
    }
}
