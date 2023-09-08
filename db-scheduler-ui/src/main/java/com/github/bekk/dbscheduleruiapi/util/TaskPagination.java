package com.github.bekk.dbscheduleruiapi.util;

import java.util.ArrayList;
import java.util.List;

public class TaskPagination {

    public static <T> List<T> paginate(List<T> allItems, int pageNumber, int pageSize) {
        System.out.println(allItems.size());
        System.out.println(pageNumber);
        System.out.println(pageSize);
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allItems.size());

        return (startIndex < endIndex) ? allItems.subList(startIndex, endIndex) : new ArrayList<>();
    }
}
