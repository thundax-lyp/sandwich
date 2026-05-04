package com.github.thundax.common.persistence;

public final class PageRules {

    private PageRules() {}

    public static int firstPageIndex() {
        return 1;
    }

    public static int defaultPageSize() {
        return 10;
    }
}
