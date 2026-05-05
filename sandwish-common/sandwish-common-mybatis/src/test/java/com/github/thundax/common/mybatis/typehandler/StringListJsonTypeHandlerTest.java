package com.github.thundax.common.mybatis.typehandler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import org.apache.ibatis.type.JdbcType;
import org.junit.Test;

public class StringListJsonTypeHandlerTest {

    private final StringListJsonTypeHandler typeHandler = new StringListJsonTypeHandler();

    @Test
    public void shouldWriteStringListAsJson() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        typeHandler.setNonNullParameter(preparedStatement, 1, Arrays.asList("admin", "user"), JdbcType.VARCHAR);

        verify(preparedStatement).setString(1, "[\"admin\",\"user\"]");
    }

    @Test
    public void shouldWriteEmptyListAsJsonArray() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        typeHandler.setNonNullParameter(preparedStatement, 1, Collections.emptyList(), JdbcType.VARCHAR);

        verify(preparedStatement).setString(1, "[]");
    }

    @Test
    public void shouldReadStringListByColumnName() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("roles")).thenReturn("[\"admin\",\"user\"]");

        assertEquals(Arrays.asList("admin", "user"), typeHandler.getNullableResult(resultSet, "roles"));
    }

    @Test
    public void shouldReadStringListByColumnIndex() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("[\"admin\"]");

        assertEquals(Collections.singletonList("admin"), typeHandler.getNullableResult(resultSet, 1));
    }

    @Test
    public void shouldReadStringListFromCallableStatement() throws Exception {
        CallableStatement callableStatement = mock(CallableStatement.class);
        when(callableStatement.getString(1)).thenReturn("[\"admin\"]");

        assertEquals(Collections.singletonList("admin"), typeHandler.getNullableResult(callableStatement, 1));
    }

    @Test
    public void shouldReadEmptyDatabaseValueAsEmptyList() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("roles")).thenReturn(" ");

        assertEquals(Collections.emptyList(), typeHandler.getNullableResult(resultSet, "roles"));
    }
}
