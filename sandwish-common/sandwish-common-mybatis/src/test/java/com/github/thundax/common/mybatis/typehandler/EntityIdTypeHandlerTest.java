package com.github.thundax.common.mybatis.typehandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.ibatis.type.JdbcType;
import org.junit.Test;

public class EntityIdTypeHandlerTest {

    private final EntityIdTypeHandler typeHandler = new EntityIdTypeHandler();

    @Test
    public void shouldWriteEntityIdAsString() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        typeHandler.setNonNullParameter(preparedStatement, 1, EntityId.of("user-1"), JdbcType.VARCHAR);

        verify(preparedStatement).setString(1, "user-1");
    }

    @Test
    public void shouldReadEntityIdByColumnName() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("id")).thenReturn("user-1");

        assertEquals(EntityId.of("user-1"), typeHandler.getNullableResult(resultSet, "id"));
    }

    @Test
    public void shouldReadEntityIdByColumnIndex() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("user-1");

        assertEquals(EntityId.of("user-1"), typeHandler.getNullableResult(resultSet, 1));
    }

    @Test
    public void shouldReadEntityIdFromCallableStatement() throws Exception {
        CallableStatement callableStatement = mock(CallableStatement.class);
        when(callableStatement.getString(1)).thenReturn("user-1");

        assertEquals(EntityId.of("user-1"), typeHandler.getNullableResult(callableStatement, 1));
    }

    @Test
    public void shouldReadBlankValueAsNull() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("id")).thenReturn(" ");

        assertNull(typeHandler.getNullableResult(resultSet, "id"));
    }
}
