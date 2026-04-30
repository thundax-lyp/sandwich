package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.JsonUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Log implements Signable {
    private EntityId id;

    private String userId;

    private LogType type;
    private Date logDate;
    private String title;
    private String remoteAddr;
    private String userAgent;
    private String method;
    private String requestUri;
    private String requestParams;
    private String remarks;
    private Date createDate;

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }

    public static final String BEAN_NAME = "Log";

    private boolean signable = false;

    public boolean isSignable() {
        return signable;
    }

    public void setSignable(boolean signable) {
        this.signable = signable;
    }

    @Override
    public String getSignName() {
        return BEAN_NAME;
    }

    @Override
    public String getSignBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", this.getUserId());
        map.put("type", getType() == null ? null : getType().value());
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

    public void setType(String type) {
        this.type = StringUtils.isBlank(type) ? null : LogType.from(type);
    }

    public void setType(LogType type) {
        this.type = type;
    }

    private Query query;

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

        private LogType type;
        private String remoteAddr;
        private String title;
        private String requestUri;

        private String userLoginName;
        private String userName;

        private Date beginDate;
        private Date endDate;

        public LogType getType() {
            return type;
        }

        public void setType(String type) {
            this.type = StringUtils.isBlank(type) ? null : LogType.from(type);
        }

        public void setType(LogType type) {
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
