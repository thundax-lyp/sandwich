package com.github.thundax.common.web;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.EmptyCollectionException;
import com.github.thundax.common.exception.InvalidBeanException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.function.ThrowableBiConsumer;
import com.github.thundax.common.utils.function.ThrowableBiPredicate;
import com.github.thundax.common.utils.function.ThrowableFunction;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.vo.query.MoveTreeNodeQueryParam;
import com.github.thundax.common.vo.query.PageQueryParam;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public abstract class BaseApiController extends BaseController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public User currentUser() {
        return UserAccessHolder.currentUser();
    }

    /**
     * 校验并转换 VO list
     *
     * @param sourceList VO list
     * @param supplier service.get(vo.getId())
     * @param <E> entity
     * @param <V> vo
     * @return entiry list
     * @throws ApiException API异常
     */
    @NonNull
    protected static <E, V> List<E> validateList(@NonNull List<V> sourceList, @NonNull ThrowableFunction<V, E> supplier)
            throws ApiException {
        return validateList(sourceList, supplier, null, null);
    }

    /**
     * 校验并转换 VO list
     *
     * @param sourceList VO list
     * @param supplier service.get(vo.getId())
     * @param validator validate(bean, vo)
     * @param processor bean.setXXX(vo.getXXX)
     * @param <E> entity
     * @param <V> vo
     * @return entiry list
     * @throws ApiException API异常
     */
    @NonNull
    protected static <E, V> List<E> validateList(
            @NonNull List<V> sourceList,
            @NonNull ThrowableFunction<V, E> supplier,
            ThrowableBiPredicate<E, V> validator,
            ThrowableBiConsumer<E, V> processor)
            throws ApiException {
        if (sourceList == null || sourceList.isEmpty()) {
            throw new EmptyCollectionException();
        }

        List<E> beanList = new ArrayList<>();

        try {
            for (V vo : sourceList) {
                if (vo == null) {
                    continue;
                }

                E bean = supplier.apply(vo);
                if (bean == null) {
                    throw new NullBeanException(StringUtils.EMPTY, JsonUtils.toJson(vo));
                }

                if (validator != null && !validator.test(bean, vo)) {
                    throw new InvalidBeanException(StringUtils.EMPTY, JsonUtils.toJson(vo));
                }

                if (processor != null) {
                    processor.accept(bean, vo);
                }

                beanList.add(bean);
            }
        } catch (ApiException apiException) {
            throw apiException;

        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return beanList;
    }

    /**
     * 排除树形列表的节点
     *
     * @param nodeList 树形列表
     * @param support 支持类
     * @param excludeIds 排除id列表
     * @param <T> 对象类
     * @return 树形列表
     */
    protected static <T> List<T> removeTreeNode(
            @NonNull List<T> nodeList, @NonNull RemoveTreeNodeSupport<T> support, @NonNull Set<String> excludeIds) {
        // 创建id-data映射表，用于快速查询pid对应的数据
        Set<String> ids =
                new HashSet<>(nodeList.stream().map(node -> support.getId(node)).collect(Collectors.toList()));

        int size = 0;
        while (size != nodeList.size()) {
            size = nodeList.size();

            Iterator<T> itor = nodeList.iterator();
            while (itor.hasNext()) {
                T node = itor.next();
                String id = support.getId(node);
                if (excludeIds.contains(id)) {
                    itor.remove();
                    ids.remove(id);

                } else {
                    String parentId = support.getParentId(node);
                    if (!support.isRoot(node) && !ids.contains(parentId)) {
                        // 如果存在pId且pId对应的数据不存在，则移除
                        itor.remove();
                        ids.remove(id);
                    }
                }
            }
        }

        return nodeList;
    }

    protected interface RemoveTreeNodeSupport<T> {
        /**
         * 获取 id
         *
         * @param node 节点
         * @return id
         */
        String getId(T node);

        /**
         * 获取 parentId
         *
         * @param node 节点
         * @return parentId
         */
        String getParentId(T node);

        /**
         * 是否根节点
         *
         * @param node 节点
         * @return 是：true
         */
        boolean isRoot(T node);
    }

    protected TreeService.MoveTreeNodeType readMoveTreeNodeType(MoveTreeNodeQueryParam queryParam) {
        switch (queryParam.getType()) {
            case MoveTreeNodeQueryParam.TYPE_BEFORE:
                return TreeService.MoveTreeNodeType.BEFORE;

            case MoveTreeNodeQueryParam.TYPE_INSIDE:
                return TreeService.MoveTreeNodeType.INSIDE;

            case MoveTreeNodeQueryParam.TYPE_INSIDE_LAST:
                return TreeService.MoveTreeNodeType.INSIDE_LAST;

            default:
                return TreeService.MoveTreeNodeType.AFTER;
        }
    }

    public static <T> Page<T> readPage(PageQueryParam queryParam) {
        Integer pageNo = queryParam.getPageNo();
        Integer pageSize = queryParam.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<T> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    public static <T, R> PageVo<R> entityPageToVo(Page<T> page, Function<T, R> mappingFunction) {
        PageVo<R> pageVo = new PageVo<>();

        pageVo.setPageNo(page.getPageNo());
        pageVo.setPageSize(page.getPageSize());
        pageVo.setTotalPage(page.getTotalPage());
        pageVo.setCount(page.getCount());

        pageVo.setRecords(
                page.getList() == null
                        ? new ArrayList<>()
                        : page.getList().stream()
                                .map(item -> mappingFunction.apply(item))
                                .collect(Collectors.toList()));

        return pageVo;
    }
}
