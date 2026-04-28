package com.github.thundax.modules.sys.servlet;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.dao.SmsValidateCodeDao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;

/**
 * 生成随机验证码
 *
 * @author wdit
 */
public class SmsValidateCodeServlet extends HttpServlet {

    public static final String SMS_VALIDATE_CODE = "smsValidateCode";
    public static final String SMS_VALIDATE_MOBILE = "smsValidateMobile";

    public static final int CODE_LENGTH = 6;

    public static final int CACHE_EXPIRED_SECONDS = 1;

    private static String whiteCaptcha;
    private final SmsValidateCodeDao smsValidateCodeDao;

    public SmsValidateCodeServlet(SmsValidateCodeDao smsValidateCodeDao) {
        super();
        this.smsValidateCodeDao = smsValidateCodeDao;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public static void setWhiteCaptcha(String whiteCaptchaString) {
        whiteCaptcha = whiteCaptchaString;
    }

    public static boolean validate(HttpServletRequest request) {
        return validate(request, request.getParameter(SMS_VALIDATE_MOBILE), request.getParameter(SMS_VALIDATE_CODE));
    }

    public static boolean validate(HttpServletRequest request, String validateMobile, String validateCode) {
        if (StringUtils.isEmpty(validateCode)) {
            return false;
        }

        if (StringUtils.isNotBlank(whiteCaptcha) && StringUtils.equals(whiteCaptcha, validateCode)) {
            return true;
        }

        String mobile = (String) request.getSession().getAttribute(SMS_VALIDATE_MOBILE);
        if (!StringUtils.isEmpty(validateMobile) && !StringUtils.equalsIgnoreCase(validateMobile, mobile)) {
            return false;
        }

        String code = (String) request.getSession().getAttribute(SMS_VALIDATE_CODE);
        return StringUtils.equalsIgnoreCase(validateCode, code);
    }

    public static void clear(HttpServletRequest request) {
        request.getSession().removeAttribute(SMS_VALIDATE_CODE);
        request.getSession().removeAttribute(SMS_VALIDATE_MOBILE);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String validateMobile = request.getParameter(SMS_VALIDATE_MOBILE);
        String validateCode = request.getParameter(SMS_VALIDATE_CODE);

        response.setContentType("text/html");
        response.getOutputStream().print(validate(request, validateMobile, validateCode) ? "true" : "false");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ValidateCodeServlet.validate(request)) {
            sendResult(response, false, "验证码错误");
            return;
        }

        String validateMobile = request.getParameter(SMS_VALIDATE_MOBILE);
        if (StringUtils.isBlank(validateMobile)) {
            sendResult(response, false, "手机号错误");
            return;
        }

        if (!smsValidateCodeDao.canSend(validateMobile)) {
            sendResult(response, false, "手机号发送过于频繁");
            return;
        }

        sendMessage(request);

        smsValidateCodeDao.markSent(validateMobile, CACHE_EXPIRED_SECONDS);

        sendResult(response, true, "验证码已发送");
    }

    private void sendResult(HttpServletResponse response, boolean result, String message) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("result", result);
        map.put("message", message);

        response.setContentType("application/json");
        response.getOutputStream().write(JsonUtils.toJson(map).getBytes(StandardCharsets.UTF_8));
    }

    private void sendMessage(HttpServletRequest request) throws IOException {
        String validateMobile = request.getParameter(SMS_VALIDATE_MOBILE);

        String codeString = createCharacter();
        HttpSession session = request.getSession();
        session.setAttribute(SMS_VALIDATE_CODE, codeString);
        session.setAttribute(SMS_VALIDATE_MOBILE, validateMobile);

        String message = "您的验证码是 " + codeString + "，有效期10分钟";
        System.out.println(message);
    }

    private String createCharacter() {
        char[] codeSeq = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        Random random = new Random();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            String r = String.valueOf(codeSeq[random.nextInt(codeSeq.length)]);
            s.append(r);
        }
        return s.toString();
    }
}
