package com.github.thundax.modules.sys.servlet.captcha.wav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author wdit
 */
public class WaveBuilder {

    private static final int WAVE_FILE_HEADER_SIZE = 44;

    private static final byte[] WAVE_FILE_HEADER = {
            'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E', 'f', 'm', 't', ' ',
            16, 0, 0, 0, 1, 0, 1, 0, 0x40, 0x1f, 0, 0, (byte) 0x80, 0x3e, 0, 0, 2, 0, 16, 0, 'd', 'a', 't', 'a', 0, 0,
            0, 0
    };

    public static byte[] buildAudio(String[] filenames) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(WAVE_FILE_HEADER);

        String pkgName = WaveBuilder.class.getPackage().getName().replaceAll("\\.", "/");
        for (String filename : filenames) {
            URL url = WaveBuilder.class.getResource("/" + pkgName + "/" + filename + ".pcm");
            InputStream inputStream = url.openStream();
            byte[] bytes = new byte[1024];
            int readBytes;
            do {
                readBytes = inputStream.read(bytes);
                if (readBytes > 0) {
                    outputStream.write(bytes, 0, readBytes);
                }
            } while (readBytes > 0);
        }

        byte[] buffer = outputStream.toByteArray();

        int size8 = buffer.length - 8;
        buffer[4] = (byte) ((size8) & 0xff);
        buffer[5] = (byte) ((size8 >> 8) & 0xff);
        buffer[6] = (byte) ((size8 >> 16) & 0xff);
        buffer[7] = (byte) ((size8 >> 24) & 0xff);

        int dataSize = buffer.length - WAVE_FILE_HEADER_SIZE;
        buffer[40] = (byte) ((dataSize) & 0xff);
        buffer[41] = (byte) ((dataSize >> 8) & 0xff);
        buffer[42] = (byte) ((dataSize >> 16) & 0xff);
        buffer[43] = (byte) ((dataSize >> 24) & 0xff);

        return buffer;
    }

}
