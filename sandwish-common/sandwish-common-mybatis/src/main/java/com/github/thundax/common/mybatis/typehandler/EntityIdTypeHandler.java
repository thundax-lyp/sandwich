package com.github.thundax.common.mybatis.typehandler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(EntityId.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EntityIdTypeHandler extends BaseTypeHandler<EntityId> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EntityId parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, EntityIdCodec.toValue(parameter));
    }

    @Override
    public EntityId getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EntityIdCodec.toDomain(rs.getString(columnName));
    }

    @Override
    public EntityId getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EntityIdCodec.toDomain(rs.getString(columnIndex));
    }

    @Override
    public EntityId getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EntityIdCodec.toDomain(cs.getString(columnIndex));
    }
}
