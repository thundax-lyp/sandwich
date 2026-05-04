package com.github.thundax.common.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PageDTO<T> {

    @Setter
    private int pageNo = PageRules.firstPageIndex();

    @Setter
    private int pageSize = PageRules.defaultPageSize();

    @Setter
    private long count = 0;

    private List<T> list = new ArrayList<>();

    public PageDTO() {}

    public PageDTO(int pageNo, int pageSize) {
        this(pageNo, pageSize, 0);
    }

    public PageDTO(int pageNo, int pageSize, long totalCount) {
        this(pageNo, pageSize, totalCount, new ArrayList<>());
    }

    public PageDTO(int pageNo, int pageSize, long count, List<T> list) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.count = count;
        this.list = list;

        this.initialize();
    }

    public void initialize() {
        if (this.pageNo < PageRules.firstPageIndex()) {
            this.pageNo = PageRules.firstPageIndex();
        }

        if (pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }

        int totalPage = this.getTotalPage();
        if (this.pageNo > totalPage) {
            this.pageNo = totalPage;
        }
    }

    @JsonIgnore
    public int getTotalPage() {
        if (pageSize > 0) {
            return (int) ((this.count + this.pageSize - 1) / this.pageSize);
        }
        return -1;
    }

    public PageDTO<T> setList(List<T> list) {
        this.list = list;
        initialize();
        return this;
    }
}
