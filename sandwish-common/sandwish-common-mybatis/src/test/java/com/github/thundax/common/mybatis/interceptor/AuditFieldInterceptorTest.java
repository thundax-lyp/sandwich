package com.github.thundax.common.mybatis.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.common.security.user.CurrentUser;
import java.lang.reflect.Method;
import java.util.Collections;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class AuditFieldInterceptorTest {

    private final AuditFieldInterceptor interceptor =
            new AuditFieldInterceptor(() -> new CurrentUser("user-1", "admin", "Admin", Collections.emptyList()));

    @Test
    public void shouldFillInsertAuditFields() throws Throwable {
        AuditDataObject dataObject = new AuditDataObject();

        interceptor.intercept(invocation(SqlCommandType.INSERT, dataObject));

        assertNotNull(dataObject.getCreateDate());
        assertNotNull(dataObject.getUpdateDate());
        assertEquals("user-1", dataObject.getCreateBy());
        assertEquals("user-1", dataObject.getUpdateBy());
    }

    @Test
    public void shouldFillUpdateAuditFields() throws Throwable {
        AuditDataObject dataObject = new AuditDataObject();

        interceptor.intercept(invocation(SqlCommandType.UPDATE, dataObject));

        assertNotNull(dataObject.getUpdateDate());
        assertEquals("user-1", dataObject.getUpdateBy());
    }

    private Invocation invocation(SqlCommandType commandType, Object parameter) throws NoSuchMethodException {
        AuditTarget target = new AuditTarget();
        Method method = AuditTarget.class.getMethod("update", MappedStatement.class, Object.class);
        return new Invocation(target, method, new Object[] {mappedStatement(commandType), parameter});
    }

    private MappedStatement mappedStatement(SqlCommandType commandType) {
        Configuration configuration = new Configuration();
        SqlSource sqlSource =
                parameterObject -> new BoundSql(configuration, "", Collections.emptyList(), parameterObject);
        return new MappedStatement.Builder(configuration, "test.Mapper.method", sqlSource, commandType).build();
    }

    public static class AuditTarget {

        public Object update(MappedStatement statement, Object parameter) {
            return 1;
        }
    }

    public static class AuditDataObject {

        private java.util.Date createDate;
        private String createBy;
        private java.util.Date updateDate;
        private String updateBy;

        public java.util.Date getCreateDate() {
            return createDate;
        }

        public void setCreateDate(java.util.Date createDate) {
            this.createDate = createDate;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public java.util.Date getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(java.util.Date updateDate) {
            this.updateDate = updateDate;
        }

        public String getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(String updateBy) {
            this.updateBy = updateBy;
        }
    }
}
