package no.bekk.dbscheduler.ui.model;

public class TaskRequestParams {

  private final TaskFilter filter;
  private final int pageNumber;
  private final int size;
  private final TaskSort sorting;
  private final boolean asc;
  private final String searchTerm;

  public TaskRequestParams(
      TaskFilter filter,
      Integer pageNumber,
      Integer size,
      TaskSort sorting,
      Boolean asc,
      String searchTerm) {
    this.filter = filter;
    this.pageNumber = pageNumber != null ? pageNumber : 0;
    this.size = size != null ? size : 10;
    this.sorting = sorting;
    this.asc = asc != null ? asc : true;
    this.searchTerm = searchTerm;
  }

  public TaskFilter getFilter() {
    return filter;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getSize() {
    return size;
  }

  public TaskSort getSorting() {
    return sorting;
  }

  public boolean isAsc() {
    return asc;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public enum TaskFilter {
    ALL,
    FAILED,
    RUNNING,
    SCHEDULED,
    SUCCEEDED;
  }

  public enum TaskSort {
    DEFAULT,
    NAME;
  }
}
