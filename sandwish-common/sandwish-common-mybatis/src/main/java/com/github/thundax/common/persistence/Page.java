package com.github.thundax.common.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.utils.CookieUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;

/**
 * 分页类
 *
 * @author thundax
 */
@Alias("Page")
public class Page<T> {

    public static final String RELOAD = "reload";

    public static final int FIRST_PAGE_INDEX = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int PAGE_SIZE_COUNT = -1;

    private int pageNo = FIRST_PAGE_INDEX;
    private int pageSize = DEFAULT_PAGE_SIZE;

    private long count = 0;

    private List<T> list = new ArrayList<T>();

    public Page() {}

    /**
     * 构造方法
     *
     * @param pageNo 当前页码
     * @param pageSize 分页大小
     */
    public Page(int pageNo, int pageSize) {
        this(pageNo, pageSize, 0);
    }

    /**
     * 构造方法
     *
     * @param pageNo 当前页码
     * @param pageSize 分页大小
     * @param totalCount 数据条数
     */
    public Page(int pageNo, int pageSize, long totalCount) {
        this(pageNo, pageSize, totalCount, new ArrayList<T>());
    }

    /**
     * 构造方法
     *
     * @param pageNo 当前页码
     * @param pageSize 分页大小
     * @param count 数据条数
     * @param list 本页数据对象列表
     */
    public Page(int pageNo, int pageSize, long count, List<T> list) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.count = count;
        this.list = list;

        this.initialize();
    }

    public Page(HttpServletRequest request, HttpServletResponse response) {
        this(request, response, StringUtils.EMPTY);
    }

    public Page(HttpServletRequest request, HttpServletResponse response, String cookiePrefix) {
        String pageNo = request.getParameter("pageNo");
        if (StringUtils.isNumeric(pageNo)) {
            CookieUtils.setCookie(response, cookiePrefix + "pageNo", pageNo);
            this.setPageNo(Integer.parseInt(pageNo));
        } else if (request.getParameter(RELOAD) != null) {
            pageNo = CookieUtils.getCookie(request, cookiePrefix + "pageNo");
            if (StringUtils.isNumeric(pageNo)) {
                this.setPageNo(Integer.parseInt(pageNo));
            }
        } else {
            CookieUtils.setCookie(response, cookiePrefix + "pageNo", String.valueOf(Page.FIRST_PAGE_INDEX));
        }

        // 设置页面大小参数（传递reload参数，来记住页码大小）
        String pageSize = request.getParameter("pageSize");
        if (StringUtils.isNumeric(pageSize)) {
            CookieUtils.setCookie(response, cookiePrefix + "pageSize", pageSize);
            this.setPageSize(Integer.parseInt(pageSize));
        } else if (request.getParameter(RELOAD) != null) {
            pageSize = CookieUtils.getCookie(request, cookiePrefix + "pageSize");
            if (StringUtils.isNumeric(pageSize)) {
                this.setPageSize(Integer.parseInt(pageSize));
            }
        }

        if (getPageNo() < FIRST_PAGE_INDEX) {
            setPageNo(FIRST_PAGE_INDEX);
        }
    }


    public void initialize() {
        if (this.pageNo < FIRST_PAGE_INDEX) {
            this.pageNo = FIRST_PAGE_INDEX;
        }

        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        int totalPage = this.getTotalPage();
        if (this.pageNo > totalPage) {
            this.pageNo = totalPage;
        }
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }


    @JsonIgnore
    public int getTotalPage() {
        if (pageSize > 0) {
            return (int) ((this.count + this.pageSize - 1) / this.pageSize);
        }
        return -1;
    }

    public List<T> getList() {
        return list;
    }

    public Page<T> setList(List<T> list) {
        this.list = list;
        initialize();
        return this;
    }

    /**
     * 是否是最后一页
     *
     * @return pageNo >= getTotalPage()
     */
    public boolean isLastPage() {
        return this.pageNo >= this.getTotalPage();
    }


    @JsonIgnore
    public boolean isNotCount() {
        return this.count == -1;
    }


    public int getFirstResult() {
        return (getPageNo() - 1) * getPageSize();
    }


    public int getMaxResults() {
        return getPageSize();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("<ul class=\"pagination\">");
        sb.append("<li><a href=\"javascript:void(0)\" aria-label=\"Previous\"\n"
                + "onclick=\"javascript:page(1);\">首页</a></li>");
        if (pageNo > 1) {
            sb.append(" <li><a href=\"javascript:void(0)\" aria-label=\"Previous\"\n"
                    + "                               onclick=\"javascript:page("
                    + (pageNo - 1)
                    + ");\"><span\n"
                    + "                                aria-hidden=\"true\">&laquo;</span></a></li>");
        }
        int begin = (pageNo + 9) < getTotalPage() ? pageNo : ((getTotalPage() - 9) > 0 ? (getTotalPage() - 9) : 1);
        int end = (pageNo + 9) < getTotalPage() ? (pageNo + 9) : getTotalPage();
        for (int i = begin; i <= end; i++) {
            if (i == pageNo) {
                sb.append("<li class=\"active\"><a href=\"javascript:void(0)\" onclick=\"javascript:page("
                        + i
                        + ");\">"
                        + i
                        + "</a></li>");
            } else {
                sb.append("<li><a href=\"javascript:void(0)\" onclick=\"javascript:page("
                        + i
                        + ");\">"
                        + i
                        + "</a></li>");
            }
        }

        if (pageNo < getTotalPage()) {
            sb.append("<li><a href=\"javascript:void(0)\" aria-label=\"Next\"\n"
                    + "onclick=\"javascript:page("
                    + (pageNo + 1)
                    + ");\"><span\n"
                    + "aria-hidden=\"true\">&raquo;</span></a></li>");
        }

        sb.append("<li><a href=\"javascript:void(0)\" aria-label=\"Next\"\n"
                + "onclick=\"javascript:page("
                + getTotalPage()
                + ");\">尾页</a></li>");

        return sb.toString();
    }
}
