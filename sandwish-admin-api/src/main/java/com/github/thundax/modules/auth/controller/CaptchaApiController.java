package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.api.CaptchaServiceApi;
import com.github.thundax.modules.auth.api.vo.LoginFormVo;
import com.github.thundax.modules.auth.service.AuthService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * @author thundax
 */
@RestController
public class CaptchaApiController extends BaseApiController implements CaptchaServiceApi {

    private static final int DEFAULT_CAPTCHA_WIDTH = 200;
    private static final int DEFAULT_CAPTCHA_HEIGHT = 80;
    private static final int MAX_CAPTCHA_WIDTH = 480;
    private static final int MAX_CAPTCHA_HEIGHT = 320;

    private static final int BACKGROUND_LINE_COUNT = 16;
    private static final int MAX_COLOR = 255;

    private final AuthService authService;

    @Autowired
    public CaptchaApiController(Validator validator, AuthService authService) {
        super(validator);

        this.authService = authService;
    }

    @Override
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String loginToken = request.getParameter("loginToken");
        if (StringUtils.isBlank(loginToken)) {
            writeResponse(response, -1, "invalidate login token");
            return;
        }

        try {
            String captcha = authService.getCaptcha(loginToken);
            writeImage(request, response, captcha);

        } catch (ApiException e) {
            writeResponse(response, -1, e.getMessage());
        }

        /*
        String captcha = request.getParameter("captcha");
        if (StringUtils.isNotBlank(captcha)) {
            //validate captcha
            try {
                String savedCaptcha = authService.getCaptcha(loginToken);
                if (StringUtils.equals(savedCaptcha, captcha)) {
                    writeResponse(response, 0, "success");

                } else {
                    writeResponse(response, 1, "bad captcha");
                }

            } catch (ApiException e) {
                writeResponse(response, -1, e.getMessage());
            }

        } else {
            //write captcha image
            try {
                captcha = authService.createCaptcha(loginToken);
                writeImage(request, response, captcha);

            } catch (ApiException e) {
                e.printStackTrace();
                writeResponse(response, -1, e.getMessage());
            }

        }
        */
    }

    @Override
    public Boolean refreshCaptcha(@RequestBody LoginFormVo form) throws ApiException {
        if (StringUtils.isBlank(form.getLoginToken())) {
            throw new InvalidParameterException("loginToken");
        }

        authService.createCaptcha(form.getLoginToken());

        return true;
    }

    private void writeResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.getWriter().print("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
    }

    private void writeImage(HttpServletRequest request, HttpServletResponse response, String captcha) throws IOException {

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        /*
         * 得到参数高，宽，都为数字时，则使用设置高宽，否则使用默认值
         */
        int width = DEFAULT_CAPTCHA_WIDTH;
        int height = DEFAULT_CAPTCHA_HEIGHT;

        String paramWidth = request.getParameter("width");
        String paramHeight = request.getParameter("height");
        if (StringUtils.isNumeric(paramWidth) && StringUtils.isNumeric(paramHeight)) {
            width = NumberUtils.toInt(paramWidth);
            height = NumberUtils.toInt(paramHeight);
        }

        if (width <= 0 || width > MAX_CAPTCHA_WIDTH) {
            width = DEFAULT_CAPTCHA_WIDTH;
        }
        if (height <= 0 || height > MAX_CAPTCHA_HEIGHT) {
            height = DEFAULT_CAPTCHA_HEIGHT;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        // 生成背景
        createBackground(graphics, width, height);

        // 画字符
        drawCharacter(graphics, width, height, captcha);

        graphics.dispose();

        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "JPEG", out);
        out.close();

    }

    private Color getRandColor(int fc, int bc) {
        int f = fc;
        int b = bc;
        Random random = new Random();
        if (f > MAX_COLOR) {
            f = MAX_COLOR;
        }
        if (b > MAX_COLOR) {
            b = MAX_COLOR;
        }
        return new Color(f + random.nextInt(b - f),
                f + random.nextInt(b - f),
                f + random.nextInt(b - f));
    }

    private void createBackground(Graphics graphics, int width, int height) {
        // 填充背景
        graphics.setColor(getRandColor(220, 250));
        graphics.fillRect(0, 0, width, height);
        // 加入干扰线条
        for (int i = 0; i < BACKGROUND_LINE_COUNT; i++) {
            graphics.setColor(getRandColor(40, 150));
            Random random = new Random();
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            graphics.drawLine(x, y, x1, y1);
        }
    }

    private void drawCharacter(Graphics graphics, int width, int height, String captcha) {
        String[] fontTypes = {
                "Arial", "Arial Black", "AvantGarde Bk BT", "Calibri"
        };

        char[] codes = captcha.toCharArray();

        int fontSize = Math.max(height, 10);
        int charWidth = (width - 20) / codes.length;

        Random random = new Random();

        for (int idx = 0; idx < codes.length; idx++) {
            String code = String.valueOf(codes[idx]);
            Font font = new Font(fontTypes[random.nextInt(fontTypes.length)], Font.PLAIN, fontSize);

            graphics.setColor(getRandColor(50, 100));
            graphics.setFont(font);

            //只有数字，字符高度取 fontMetrics.getAscent()
            int fontHeight = graphics.getFontMetrics().getAscent();

            int yOffset = height < fontHeight ? 0 : random.nextInt(height - fontHeight);
            graphics.drawString(code, charWidth * idx + 10, height - 5 - yOffset);
        }
    }

}
