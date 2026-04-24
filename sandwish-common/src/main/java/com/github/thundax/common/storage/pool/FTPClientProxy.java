package com.github.thundax.common.storage.pool;

import com.github.thundax.common.utils.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 代理FTPClient，处理命令编码，这里也可以使用AOP方式
 */
public class FTPClientProxy {

    private FTPClient ftpClient;

    public FTPClientProxy(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    /**
     * get/set ftpClient
     */
    public FTPClient getFTPClient() {
        return this.ftpClient;
    }

    public void setFTPClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public FTPFile[] listFiles(String pathname) throws IOException {
        return ftpClient.listFiles(convertFtpCommandArg(pathname));
    }

    public boolean rename(String from, String to) throws IOException {
        return ftpClient.rename(convertFtpCommandArg(from), convertFtpCommandArg(to));
    }

    public boolean changeWorkingDirectory(String pathname) throws IOException {
        return ftpClient.changeWorkingDirectory(convertFtpCommandArg(pathname));
    }

    public boolean makeDirectory(String pathname) throws IOException {
        return ftpClient.makeDirectory(convertFtpCommandArg(pathname));
    }

    public boolean removeDirectory(String pathname) throws IOException {
        return ftpClient.removeDirectory(convertFtpCommandArg(pathname));
    }

    public boolean deleteFile(String pathname) throws IOException {
        return ftpClient.deleteFile(convertFtpCommandArg(pathname));
    }

    public boolean storeFile(String remote, InputStream local) throws IOException {
        return ftpClient.storeFile(convertFtpCommandArg(remote), local);
    }

    public boolean retrieveFile(String remote, OutputStream local) throws IOException {
        return ftpClient.retrieveFile(convertFtpCommandArg(remote), local);
    }

    private String convertFtpCommandArg(String appString) {
        if (StringUtils.isEmpty(appString)) {
            return StringUtils.EMPTY;
        }
        return new String(appString.getBytes(), StandardCharsets.ISO_8859_1);
    }

}
