package com.github.thundax.common.persistence.entity;

import com.github.thundax.common.utils.EncryptUtils;
import com.github.thundax.common.utils.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 默认加密转换器
 *
 * @author wdit
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes({String.class})
public class DefaultEncryptTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, String s, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, encrypt(s));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return decrypt(resultSet.getString(s));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return decrypt(resultSet.getString(i));

    }

    @Override
    public String getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return decrypt(callableStatement.getString(i));
    }

    public static String encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        return EncryptUtils.sm4Encrypt(value);
    }

    public static String decrypt(String encryptedValue) {
        if (StringUtils.isEmpty(encryptedValue)) {
            return encryptedValue;
        }
        return EncryptUtils.sm4Decrypt(encryptedValue);
    }

}
