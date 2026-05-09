package com.github.thundax.modules.sys.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.exception.MoveTreeNodeException;
import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.modules.sys.controller.request.DepartmentIdRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentMoveRequest;
import com.github.thundax.modules.sys.controller.response.DepartmentResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.command.MoveDepartmentCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

public class DepartmentControllerContractTest {

    @BeforeClass
    public static void setUpMessages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("common.exception.move-tree-node", Locale.getDefault(), "{0}:{1}->{2}");
        new I18nMessages(messageSource);
    }

    @Test
    public void shouldExcludeSelectedDepartmentAndDescendantsFromTree() {
        DepartmentService departmentService = mock(DepartmentService.class);
        DepartmentController controller = new DepartmentController(departmentService);
        when(departmentService.list(any(DepartmentQuery.class)))
                .thenReturn(
                        new ArrayList<>(Arrays.asList(department(1L, null), department(2L, 1L), department(3L, null))));

        List<DepartmentResponse> responses = controller.tree(Collections.singletonList(idRequest(1L)));

        assertEquals(1, responses.size());
        assertEquals(Long.valueOf(3L), responses.get(0).getId());
    }

    @Test
    public void shouldMapInsideLastMoveType() throws Exception {
        DepartmentService departmentService = mock(DepartmentService.class);
        DepartmentController controller = new DepartmentController(departmentService);
        Department from = department(1L, null);
        Department to = department(2L, null);
        when(departmentService.get(any(DepartmentId.class))).thenReturn(from, to);

        Boolean moved = controller.move(moveRequest(1L, 2L, DepartmentMoveRequest.TYPE_INSIDE_LAST));

        assertEquals(Boolean.TRUE, moved);
        verify(departmentService).move(any(MoveDepartmentCommand.class));
    }

    @Test(expected = MoveTreeNodeException.class)
    public void shouldRejectMovingNodeIntoItsDescendant() throws Exception {
        DepartmentService departmentService = mock(DepartmentService.class);
        DepartmentController controller = new DepartmentController(departmentService);
        Department from = department(1L, null);
        Department to = department(2L, 1L);
        when(departmentService.get(any(DepartmentId.class))).thenReturn(from, to);
        when(departmentService.existsChildRelation(any(DepartmentQuery.class))).thenReturn(true);

        controller.move(moveRequest(1L, 2L, DepartmentMoveRequest.TYPE_INSIDE));

        verify(departmentService, never()).move(any(MoveDepartmentCommand.class));
    }

    private DepartmentIdRequest idRequest(Long id) {
        DepartmentIdRequest request = new DepartmentIdRequest();
        request.setId(id);
        return request;
    }

    private DepartmentMoveRequest moveRequest(Long fromNodeId, Long toNodeId, String type) {
        DepartmentMoveRequest request = new DepartmentMoveRequest();
        request.setFromNodeId(fromNodeId);
        request.setToNodeId(toNodeId);
        request.setType(type);
        return request;
    }

    private Department department(Long id, Long parentId) {
        Department department = new Department();
        department.setId(DepartmentIdCodec.toDomain(id));
        department.setParentId(DepartmentIdCodec.toDomain(parentId));
        department.setName(String.valueOf(id));
        return department;
    }
}
