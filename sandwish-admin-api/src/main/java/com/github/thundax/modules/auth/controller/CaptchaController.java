package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
import com.github.thundax.modules.auth.assembler.CaptchaInterfaceAssembler;
import com.github.thundax.modules.auth.controller.request.CaptchaRefreshRequest;
import com.github.thundax.modules.auth.controller.response.CaptchaRefreshResponse;
import com.github.thundax.modules.auth.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "01-02. 鉴权-图形验证码")
@RequestMapping(value = "/api/auth")
@RestController
@PublicApi
public class CaptchaController {

    private static final String APPLICATION_JSON_UTF8_VALUE = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";
    private static final int DEFAULT_CAPTCHA_WIDTH = 200;
    private static final int DEFAULT_CAPTCHA_HEIGHT = 80;
    private static final int MAX_CAPTCHA_WIDTH = 480;
    private static final int MAX_CAPTCHA_HEIGHT = 320;

    private static final int NOISE_LINE_COUNT = 12;
    private static final int MAX_COLOR = 255;

    private final AuthService authService;

    @Autowired
    public CaptchaController(AuthService authService) {

        this.authService = authService;
    }

    @ApiOperation(value = "图形验证码")
    @GetMapping(value = "captcha")
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

    @ApiOperation(value = "刷新图形验证码")
    @PostMapping(value = "captcha/refresh")
    @WrappedApiResponse
    public CaptchaRefreshResponse refreshCaptcha(@Valid @RequestBody CaptchaRefreshRequest request)
            throws ApiException {
        if (StringUtils.isBlank(request.getLoginToken())) {
            throw new InvalidParameterException("loginToken");
        }

        authService.createCaptcha(request.getLoginToken());

        return CaptchaInterfaceAssembler.toRefreshResponse(true);
    }

    private void writeResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().print("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
    }

    private void writeImage(HttpServletRequest request, HttpServletResponse response, String captcha)
            throws IOException {

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(MediaType.IMAGE_PNG_VALUE);

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

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 透明底上保留轻量干扰线，避免前端容器背景被图片盖住。
        drawNoise(graphics, width, height);

        // 画字符
        drawCharacter(graphics, width, height, captcha);

        graphics.dispose();

        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "PNG", out);
        }
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
        if (b <= f) {
            b = f + 1;
        }
        return new Color(f + random.nextInt(b - f), f + random.nextInt(b - f), f + random.nextInt(b - f));
    }

    private Color getRandColor(int fc, int bc, int alpha) {
        Color color = getRandColor(fc, bc);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private void drawNoise(Graphics2D graphics, int width, int height) {
        for (int i = 0; i < NOISE_LINE_COUNT; i++) {
            graphics.setColor(getRandColor(70, 170, 70));
            Random random = new Random();
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            graphics.drawLine(x, y, x1, y1);
        }
    }

    private void drawCharacter(Graphics2D graphics, int width, int height, String captcha) {
        String[] fontTypes = {"Arial", "Arial Black", "AvantGarde Bk BT", "Calibri"};

        char[] codes = captcha.toCharArray();
        if (codes.length == 0) {
            return;
        }

        int horizontalPadding = Math.max(8, width / 18);
        int verticalPadding = Math.max(4, height / 10);
        int drawableWidth = Math.max(width - horizontalPadding * 2, codes.length);
        int drawableHeight = Math.max(height - verticalPadding * 2, 12);
        int fontSize = Math.max(18, Math.min(drawableHeight, drawableWidth / codes.length + 8));
        int charWidth = drawableWidth / codes.length;

        Random random = new Random();

        for (int idx = 0; idx < codes.length; idx++) {
            String code = String.valueOf(codes[idx]);
            Font font = new Font(fontTypes[random.nextInt(fontTypes.length)], Font.PLAIN, fontSize);

            graphics.setColor(getRandColor(35, 95, 220));
            graphics.setFont(font);

            FontMetrics fontMetrics = graphics.getFontMetrics();
            int charX = horizontalPadding
                    + charWidth * idx
                    + Math.max((charWidth - fontMetrics.charWidth(codes[idx])) / 2, 0);
            int baseline = (height - fontMetrics.getHeight()) / 2
                    + fontMetrics.getAscent()
                    + random.nextInt(Math.max(verticalPadding * 2, 1))
                    - verticalPadding;

            AffineTransform transform = graphics.getTransform();
            double angle = Math.toRadians(random.nextInt(17) - 8);
            graphics.rotate(angle, charX + charWidth / 2.0, height / 2.0);
            graphics.drawString(code, charX, baseline);
            graphics.setTransform(transform);
        }
    }
}
