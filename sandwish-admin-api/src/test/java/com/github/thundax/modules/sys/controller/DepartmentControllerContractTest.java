package com.github.thundax.modules.sys.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.exception.MoveTreeNodeException;
import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.controller.request.DepartmentIdRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentMoveRequest;
import com.github.thundax.modules.sys.controller.response.DepartmentResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.DepartmentService;
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
        when(departmentService.list(any(Department.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(
                        department("root", null), department("child", "root"), department("peer", null))));

        List<DepartmentResponse> responses = controller.tree(Collections.singletonList(idRequest("root")));

        assertEquals(1, responses.size());
        assertEquals("peer", responses.get(0).getId());
    }

    @Test
    public void shouldMapInsideLastMoveType() throws Exception {
        DepartmentService departmentService = mock(DepartmentService.class);
        DepartmentController controller = new DepartmentController(departmentService);
        Department from = department("from", null);
        Department to = department("to", null);
        when(departmentService.getById(EntityId.of("from"))).thenReturn(from);
        when(departmentService.getById(EntityId.of("to"))).thenReturn(to);

        Boolean moved = controller.move(moveRequest("from", "to", DepartmentMoveRequest.TYPE_INSIDE_LAST));

        assertEquals(Boolean.TRUE, moved);
        verify(departmentService).moveTreeNode(from, to, TreeNodeMoveType.INSIDE_LAST);
    }

    @Test(expected = MoveTreeNodeException.class)
    public void shouldRejectMovingNodeIntoItsDescendant() throws Exception {
        DepartmentService departmentService = mock(DepartmentService.class);
        DepartmentController controller = new DepartmentController(departmentService);
        Department from = department("from", null);
        Department to = department("to", "from");
        when(departmentService.getById(EntityId.of("from"))).thenReturn(from);
        when(departmentService.getById(EntityId.of("to"))).thenReturn(to);
        when(departmentService.isChildOf(to, from)).thenReturn(true);

        controller.move(moveRequest("from", "to", DepartmentMoveRequest.TYPE_INSIDE));

        verify(departmentService, never())
                .moveTreeNode(any(Department.class), any(Department.class), any(TreeNodeMoveType.class));
    }

    private DepartmentIdRequest idRequest(String id) {
        DepartmentIdRequest request = new DepartmentIdRequest();
        request.setId(id);
        return request;
    }

    private DepartmentMoveRequest moveRequest(String fromNodeId, String toNodeId, String type) {
        DepartmentMoveRequest request = new DepartmentMoveRequest();
        request.setFromNodeId(fromNodeId);
        request.setToNodeId(toNodeId);
        request.setType(type);
        return request;
    }

    private Department department(String id, String parentId) {
        Department department = new Department();
        department.setId(EntityId.of(id));
        department.setParentId(parentId);
        department.setName(id);
        return department;
    }
}
