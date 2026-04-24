package com.github.thundax.modules.auth.utils;

import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Md5;
import com.github.thundax.common.web.RequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wdit
 */
public class AuthUtils {

    /**
     * 获取当前检验码。
     * 规则：MD5-16(remoteAddress + userAgent)
     *
     * @return 当前检验码
     */
    public static String currentCheckCode() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        String remoteAddr = RequestUtils.getRemoteAddr(request);
        if (StringUtils.isBlank(remoteAddr)) {
            return null;
        }

        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        if (StringUtils.isBlank(userAgent)) {
            return null;
        }

        return Md5.encrypt16(remoteAddr + userAgent);
    }

    public static boolean validateCheckCode(String checkCode) {
        String currentCheckCode = currentCheckCode();

        return StringUtils.isBlank(currentCheckCode)
                || StringUtils.equals(checkCode, currentCheckCode);
    }

}
