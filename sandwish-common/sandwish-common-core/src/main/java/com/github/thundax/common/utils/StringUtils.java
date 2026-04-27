package com.github.thundax.common.utils;

import com.github.thundax.common.utils.ext.StringUtilEx;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;

/** @author thundax */
public class StringUtils extends StringUtilEx {

    public static String unwrap(String str, String prefix, String suffix) {
        return unwrap(str, prefix, suffix, false);
    }

    public static String unwrapIgnoreCase(String str, String prefix, String suffix) {
        return unwrap(str, prefix, suffix, true);
    }

    /**
     * 缩略字符串（不区分中英文字符）
     *
     * @param str 目标字符串
     * @param length 截取长度
     */
    public static String abbr(String str, int length) {
        return abbr(str, length, "...");
    }

    public static String abbr(String str, int length, String appendString) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        if (StringUtils.isEmpty(appendString)) {
            appendString = "...";
        }
        try {
            int appendLength = appendString.getBytes("GBK").length;
            StringBuilder sb = new StringBuilder();
            int currentLength = 0;
            for (char c : replaceHtml(StringEscapeUtils.unescapeHtml4(str)).toCharArray()) {
                currentLength += String.valueOf(c).getBytes("GBK").length;
                if (currentLength <= length - appendLength) {
                    sb.append(c);
                } else {
                    sb.append(appendString);
                    break;
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String concat(String s1, String s2) {
        return join(s1, s2);
    }

    /** 替换掉HTML标签方法 */
    public static String replaceHtml(String html) {
        if (isBlank(html)) {
            return "";
        }
        String regEx = "<.+?>";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(html);
        return m.replaceAll("");
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String tmp = (Integer.toHexString(b & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0").append(tmp);
            } else {
                sb.append(tmp);
            }
        }
        return sb.toString();
    }

    private static final int HEX_CHAR_LENGTH = 2;

    public static byte[] hex2byte(String s) {
        byte[] res = new byte[s.length() / HEX_CHAR_LENGTH];
        char[] chs = s.toCharArray();
        for (int n = 0, c = 0; n < chs.length; n += HEX_CHAR_LENGTH, c++) {
            res[c] = (byte) (Integer.parseInt(new String(chs, n, HEX_CHAR_LENGTH), 16));
        }
        return res;
    }

    public static String toString(Object o) {
        if (o != null) {
            return o.toString();
        } else {
            return null;
        }
    }

    private static String unwrap(String str, String prefix, String suffix, boolean ignoreCase) {
        if (isEmpty(str)) {
            return str;
        }

        int startIndex = 0;
        int endIndex = str.length();

        if (isNotEmpty(prefix)) {
            if (ignoreCase && !startsWithIgnoreCase(str, prefix)) {
                return str;
            }

            if (!ignoreCase && !startsWith(str, prefix)) {
                return str;
            }

            startIndex = prefix.length();
        }

        if (isNotEmpty(suffix)) {
            if (ignoreCase && !endsWithIgnoreCase(str, suffix)) {
                return str;
            }

            if (!ignoreCase && !endsWith(str, suffix)) {
                return str;
            }

            endIndex = str.length() - suffix.length();
        }

        return str.substring(startIndex, endIndex);
    }
}
