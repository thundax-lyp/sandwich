package com.github.thundax.common.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentUtils {

    /**
     * 获取客户端浏览器信息
     */
    public static String getBrowserInfo(HttpServletRequest req) {
        String browserInfo = "other";
        String ua = req.getHeader("User-Agent").toLowerCase();
        String s;
        String version;
        String msieP = "msie ([\\d.]+)";
        String ieheighP = "rv:([\\d.]+)";
        String firefoxP = "firefox\\/([\\d.]+)";
        String chromeP = "chrome\\/([\\d.]+)";
        String operaP = "opr.([\\d.]+)";
        String safariP = "version\\/([\\d.]+).*safari";

        Pattern pattern = Pattern.compile(msieP);
        Matcher mat = pattern.matcher(ua);
        if (mat.find()) {
            s = mat.group();
            version = s.split(" ")[1];
            browserInfo = "ie " + version.substring(0, version.indexOf("."));
            return browserInfo;
        }

        pattern = Pattern.compile(firefoxP);
        mat = pattern.matcher(ua);
        if (mat.find()) {
            s = mat.group();
            version = s.split("/")[1];
            browserInfo = "firefox " + version.substring(0, version.indexOf("."));
            return browserInfo;
        }

        pattern = Pattern.compile(ieheighP);
        mat = pattern.matcher(ua);
        if (mat.find()) {
            s = mat.group();
            version = s.split(":")[1];
            browserInfo = "ie " + version.substring(0, version.indexOf("."));
            return browserInfo;
        }

        pattern = Pattern.compile(operaP);
        mat = pattern.matcher(ua);
        if (mat.find()) {
            s = mat.group();
            version = s.split("/")[1];
            browserInfo = "opera " + version.substring(0, version.indexOf("."));
            return browserInfo;
        }

        pattern = Pattern.compile(chromeP);
        mat = pattern.matcher(ua);
        if (mat.find()) {
            s = mat.group();
            version = s.split("/")[1];
            browserInfo = "chrome " + version.substring(0, version.indexOf("."));
            return browserInfo;
        }

        pattern = Pattern.compile(safariP);
        mat = pattern.matcher(ua);
        if (mat.find()) {
            s = mat.group();
            version = s.split("/")[1].split(" ")[0];
            browserInfo = "safari " + version.substring(0, version.indexOf("."));
            return browserInfo;
        }
        return browserInfo;
    }

    private static final Map<String, Pattern> CLIENT_OS_MAP = new HashMap<String, Pattern>() {{
        put("Win 8", Pattern.compile(".*(Windows NT 6\\.2).*"));
        put("Win 7", Pattern.compile(".*(Windows NT 6\\.1).*"));
        put("WinXP", Pattern.compile(".*(Windows NT 5\\.1|Windows XP).*"));
        put("Win2003", Pattern.compile(".*(Windows NT 5\\.2).*"));
        put("Win2000", Pattern.compile(".*(Win2000|Windows 2000|Windows NT 5\\.0).*"));
        put("MAC", Pattern.compile(".*(Mac|apple|MacOS8).*"));
        put("WinNT", Pattern.compile(".*(WinNT|Windows NT).*"));
        put("Linux", Pattern.compile(".*Linux.*"));
        put("Mac68k", Pattern.compile(".*(68k|68000).*"));
        put("Win9x", Pattern.compile(".*(9x 4.90|Win9(5|8)|Windows 9(5|8)|95/NT|Win32|32bit).*"));
    }};

    private static final String CLIENT_OS_UNKNOWN = "unknown os";

    /**
     * 获取客户端操作系统信息
     */
    public static String getClientOs(HttpServletRequest req) {
        String userAgent = req.getHeader("User-Agent");

        System.out.println(CLIENT_OS_MAP.keySet());
        for (Map.Entry<String, Pattern> entry : CLIENT_OS_MAP.entrySet()) {
            if (entry.getValue().matcher(userAgent).find()) {
                return entry.getKey();
            }
        }

        return CLIENT_OS_UNKNOWN;
    }

}
