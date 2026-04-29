package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.entity.base.BaseLog;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Log extends BaseLog {
    public static final String BEAN_NAME = "Log";

    public static final String TYPE_ACCESS = "1";

    public static final String TYPE_EXCEPTION = "2";
    private boolean signable = false;

    public Log() {
        super();
    }

    public Log(String id) {
        super(id);
    }

    @JsonIgnore
    public User getUser() {
        return UserServiceHolder.get(EntityIdCodec.toDomain(this.getUserId()));
    }

    public boolean isSignable() {
        return signable;
    }

    public void setSignable(boolean signable) {
        this.signable = signable;
    }

    @Override
    @JsonIgnore
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    @JsonIgnore
    public String getSignBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", this.getUserId());
        map.put("type", this.getType());
        map.put("logDate", this.getLogDate());
        map.put("title", this.getTitle());
        map.put("remoteAddr", this.getRemoteAddr());
        map.put("userAgent", this.getUserAgent());
        map.put("method", this.getMethod());

        map.put("requestUri", this.getRequestUri());
        map.put("requestParams", this.getRequestParams());

        return JsonUtils.toJson(map);
    }

    public void setRequestParamMap(Map<String, String[]> paramMap) {
        if (paramMap != null) {
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
                params.append("".equals(params.toString()) ? "" : "&")
                        .append(param.getKey())
                        .append("=");
                String paramValue = "";
                if (param.getValue() != null && param.getValue().length > 0) {
                    paramValue = param.getValue()[0];
                }
                String safeParamValue = StringUtils.endsWithIgnoreCase(param.getKey(), "password") ? "" : paramValue;
                params.append(safeParamValue.length() > 100 ? safeParamValue.substring(0, 100) : safeParamValue);
            }
            String requestParams = params.toString();
            this.setRequestParams(requestParams.length() > 300 ? requestParams.substring(0, 300) : requestParams);
        }
    }

    private Query query;

    @JsonIgnore
    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_TYPE = "type";
        public static final String PROP_REMOTE_ADDR = "remoteAddr";
        public static final String PROP_TITLE = "title";
        public static final String PROP_REQUEST_URI = "requestUri";

        public static final String PROP_USER_LOGIN_NAME = "userLoginName";
        public static final String PROP_USER_NAME = "userName";

        public static final String PROP_BEGIN_DATE = "beginDate";
        public static final String PROP_END_DATE = "endDate";

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
