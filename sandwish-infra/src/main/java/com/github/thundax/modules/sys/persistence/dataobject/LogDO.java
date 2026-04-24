package com.github.thundax.modules.sys.persistence.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thundax.common.persistence.AdminDataEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志持久化对象。
 */
public class LogDO extends AdminDataEntity<LogDO> {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String type;
    private Date logDate;
    private String title;
    private String remoteAddr;
    private String userAgent;
    private String method;
    private String requestUri;
    private String requestParams;

    public LogDO() {
        super();
    }

    public LogDO(String id) {
        super(id);
    }

    @Override
    protected Object createQueryObject() {
        return new Query();
    }

    @JsonIgnore
    public Query getQuery() {
        return (Query) this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public static class Query implements Serializable {

        private static final long serialVersionUID = 1L;

        private String type;
        private String remoteAddr;
        private String title;
        private String requestUri;
        private String userLoginName;
        private String userName;
        private Date beginDate;
        private Date endDate;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public void setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRequestUri() {
            return requestUri;
        }

        public void setRequestUri(String requestUri) {
            this.requestUri = requestUri;
        }

        public String getUserLoginName() {
            return userLoginName;
        }

        public void setUserLoginName(String userLoginName) {
            this.userLoginName = userLoginName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Date getBeginDate() {
            return beginDate;
        }

        public void setBeginDate(Date beginDate) {
            this.beginDate = beginDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }
    }
}
