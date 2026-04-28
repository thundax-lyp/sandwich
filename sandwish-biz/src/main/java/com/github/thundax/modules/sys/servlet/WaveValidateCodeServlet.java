package com.github.thundax.modules.sys.servlet;

import com.github.thundax.modules.sys.servlet.captcha.mp3.Mp3Builder;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 生成验证码WAVE文件
 *
 * @author wdit
 */
public class WaveValidateCodeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public WaveValidateCodeServlet() {
        super();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        sendWave(request, response);
    }


    private void sendWave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer[] range = getRange(request);

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Accept-Ranges", "bytes");
        response.setDateHeader("Expires", 0);
        response.setContentType("audio/mp3");

        String validateCode = (String) request.getSession().getAttribute(ValidateCodeServlet.VALIDATE_CODE);

        List<String> codeList = Lists.newArrayList();
        codeList.add("prefix");
        for (int idx = 0; validateCode != null && idx < validateCode.length(); idx++) {
            codeList.add(validateCode.substring(idx, idx + 1));
        }

        byte[] buffer = Mp3Builder.buildAudio(codeList);

        if (range[0] != null) {
            // 设置了range
            range[0] = Math.min(range[0], buffer.length - 1);
            if (range[1] == null) {
                range[1] = buffer.length - 1;
            } else {
                range[1] = Math.min(range[1], buffer.length - 1);
            }
            response.setHeader("Content-Range", "bytes " + range[0] + "-" + range[1] + "/" + buffer.length);
            response.setContentLength(range[1] - range[0] + 1);
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(buffer, range[0], range[1] - range[0] + 1);
            outputStream.close();

        } else {
            // 没有设置range
            response.setContentLength(buffer.length);
            response.setStatus(HttpServletResponse.SC_OK);

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(buffer);
            outputStream.close();
        }
    }


    private static Integer[] getRange(HttpServletRequest request) {
        Integer[] range = new Integer[2];
        String rangeText = request.getHeader("range");
        if (StringUtils.isBlank(rangeText)) {
            return range;
        }
        rangeText = rangeText.toLowerCase().replaceAll("bytes", "").replaceAll("=", "");
        String[] num = rangeText.split("-");
        if (num.length > 0 && StringUtils.isNumeric(num[0].trim())) {
            range[0] = Integer.parseInt(num[0].trim());

            if (num.length > 1 && StringUtils.isNumeric(num[1].trim())) {
                range[1] = Integer.parseInt(num[1].trim());
            }
        }

        return range;
    }
}
