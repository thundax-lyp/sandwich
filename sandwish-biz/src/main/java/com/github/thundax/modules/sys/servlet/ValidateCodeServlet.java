package com.github.thundax.modules.sys.servlet;

import com.github.thundax.common.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * 生成随机验证码
 *
 * @author wdit
 */
public class ValidateCodeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String VALIDATE_CODE = "validateCode";
    public static final int VALIDATE_CODE_LENGTH = 4;

    private static final int DEFAULT_WIDTH = 140;
    private static final int DEFAULT_HEIGHT = 50;
    private static final int MAX_WIDTH = 280;
    private static final int MAX_HEIGHT = 100;

    private static final int MAX_COLOR = 255;
    private static final int BACK_LINE_SIZE = 8;

    private static String whiteCaptcha;

    private int w = DEFAULT_WIDTH;
    private int h = DEFAULT_HEIGHT;

    public ValidateCodeServlet() {
        super();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public static void setWhiteCaptcha(String whiteCaptchaString) {
        whiteCaptcha = whiteCaptchaString;
    }

    public static boolean validate(HttpServletRequest request) {
        return validate(request, request.getParameter(VALIDATE_CODE));
    }

    public static boolean validate(HttpServletRequest request, String validateCode) {
        String code = (String) request.getSession().getAttribute(VALIDATE_CODE);
        if (validateCode == null) {
            return false;
        } else {
            if (StringUtils.isNotBlank(whiteCaptcha) && StringUtils.equals(whiteCaptcha, validateCode)) {
                return true;
            }
            return StringUtils.equalsIgnoreCase(validateCode, code);
        }
    }

    public static void clear(HttpServletRequest request) {
        request.getSession().removeAttribute(VALIDATE_CODE);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String validateCode = request.getParameter(VALIDATE_CODE);
        if (StringUtils.isNotBlank(validateCode)) {
            response.setContentType("text/html");
            response.getOutputStream().print(validate(request, validateCode) ? "true" : "false");
        } else {
            this.doPost(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        createImage(request, response);
    }

    private void createImage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        /*
         * 得到参数高，宽，都为数字时，则使用设置高宽，否则使用默认值
         */
        String width = request.getParameter("width");
        String height = request.getParameter("height");
        if (StringUtils.isNumeric(width) && StringUtils.isNumeric(height)) {
            w = NumberUtils.toInt(width);
            h = NumberUtils.toInt(height);
        }

        if (w <= 0 || w > MAX_WIDTH) {
            w = DEFAULT_WIDTH;
        }
        if (h <= 0 || h > MAX_HEIGHT) {
            h = DEFAULT_HEIGHT;
        }

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        /*
         * 生成背景
         */
        createBackground(g);

        /*
         * 生成字符
         */
        String codeString = createCharacter(g);
        HttpSession session = request.getSession();
        session.setAttribute(VALIDATE_CODE, codeString);

        g.dispose();
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
        return new Color(f + random.nextInt(b - f), f + random.nextInt(b - f),
                f + random.nextInt(b - f));
    }

    private void createBackground(Graphics g) {
        // 填充背景
        g.setColor(getRandColor(220, 250));
        g.fillRect(0, 0, w, h);
        // 加入干扰线条
        for (int i = 0; i < BACK_LINE_SIZE; i++) {
            g.setColor(getRandColor(40, 150));
            Random random = new Random();
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int x1 = random.nextInt(w);
            int y1 = random.nextInt(h);
            g.drawLine(x, y, x1, y1);
        }
    }

    private String createCharacter(Graphics g) {
        /*
         * char[] codeSeq = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
         * 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2',
         * '3', '4', '5', '6', '7', '8', '9' };
         */
        char[] codeSeq = {
                '2', '3', '4', '5', '6', '7', '8', '9'
        };
        String[] fontTypes = {
                "Arial", "Arial Black", "AvantGarde Bk BT", "Calibri"
        };
        Random random = new Random();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < VALIDATE_CODE_LENGTH; i++) {
            String r = String.valueOf(codeSeq[random.nextInt(codeSeq.length)]);
            g.setColor(new Color(50 + random.nextInt(100), 50 + random.nextInt(100),
                    50 + random.nextInt(100)));
            g.setFont(new Font(fontTypes[random.nextInt(fontTypes.length)], Font.PLAIN, 40));
            g.drawString(r, 25 * i + 10, 35 + random.nextInt(10));
            s.append(r);
        }
        return s.toString();
    }

}
