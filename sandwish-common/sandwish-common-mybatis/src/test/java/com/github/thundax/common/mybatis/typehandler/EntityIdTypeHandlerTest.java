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
    public void shouldWriteEntityIdAsLong() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        typeHandler.setNonNullParameter(preparedStatement, 1, EntityId.of(1001L), JdbcType.BIGINT);

        verify(preparedStatement).setLong(1, 1001L);
    }

    @Test
    public void shouldReadEntityIdByColumnName() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(1001L);
        when(resultSet.wasNull()).thenReturn(false);

        assertEquals(EntityId.of(1001L), typeHandler.getNullableResult(resultSet, "id"));
    }

    @Test
    public void shouldReadEntityIdByColumnIndex() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong(1)).thenReturn(1001L);
        when(resultSet.wasNull()).thenReturn(false);

        assertEquals(EntityId.of(1001L), typeHandler.getNullableResult(resultSet, 1));
    }

    @Test
    public void shouldReadEntityIdFromCallableStatement() throws Exception {
        CallableStatement callableStatement = mock(CallableStatement.class);
        when(callableStatement.getLong(1)).thenReturn(1001L);
        when(callableStatement.wasNull()).thenReturn(false);

        assertEquals(EntityId.of(1001L), typeHandler.getNullableResult(callableStatement, 1));
    }

    @Test
    public void shouldReadSqlNullValueAsNull() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        assertNull(typeHandler.getNullableResult(resultSet, "id"));
    }
}
