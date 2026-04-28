package com.github.thundax.modules.sys.servlet.captcha.mp3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class Mp3Builder {

    private static final int FILE_HEADER_SIZE = 45;

    public static byte[] buildAudio(List<String> filenames) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        boolean firstFile = true;

        String pkgName = Mp3Builder.class.getPackage().getName().replaceAll("\\.", "/");
        for (String filename : filenames) {
            URL url = Mp3Builder.class.getResource("/" + pkgName + "/" + filename + ".mp3");

            InputStream inputStream = url.openStream();
            byte[] bytes = new byte[1024];
            int readBytes = 0;

            if (firstFile) {
                firstFile = false;
            } else {
                // 如果不是第一个文件，需要去除掉mp3头
                inputStream.read(bytes, 0, FILE_HEADER_SIZE);
            }

            do {
                readBytes = inputStream.read(bytes);
                if (readBytes > 0) {
                    outputStream.write(bytes, 0, readBytes);
                }
            } while (readBytes > 0);
        }

        return outputStream.toByteArray();
    }
}
