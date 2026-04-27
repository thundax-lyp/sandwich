package com.github.thundax.common.storage.pool;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPClientFactory implements PooledObjectFactory<FTPClient> {

    private Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);

    private static final String CONTROL_ENCODING = "utf-8";
    private static final int BUFFER_SIZE = 4096;

    private FTPClientConfig config;

    public FTPClientFactory() {}

    public FTPClientFactory(FTPClientConfig config) {
        this.setConfig(config);
    }

    /** get/set config */
    public FTPClientConfig getConfig() {
        return this.config;
    }

    public void setConfig(FTPClientConfig config) {
        this.config = config;
    }

    @Override
    public PooledObject<FTPClient> makeObject() throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(config.getConnectTimeout());
        try {
            ftpClient.connect(config.getHost(), config.getPort());
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.warn("FTPServer refused connection");
                return null;
            }

            boolean result = ftpClient.login(config.getUsername(), config.getPassword());
            if (!result) {
                logger.warn("ftpClient login failed... username is {}", config.getUsername());
            }
            if (config.getEnableAsciiType()) {
                ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
            } else {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            }
            ftpClient.setBufferSize(BUFFER_SIZE);
            ftpClient.setControlEncoding(CONTROL_ENCODING);
            ftpClient.sendCommand("OPTS " + CONTROL_ENCODING, "ON");
            if (config.getEnablePassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            } else {
                ftpClient.enterLocalActiveMode();
            }
        } catch (Exception e) {
            logger.error("create ftp connection failed...{}", e);
            throw e;
        }

        return new DefaultPooledObject<FTPClient>(ftpClient);
    }

    @Override
    public void destroyObject(PooledObject<FTPClient> obj) throws Exception {
        FTPClient ftpClient = obj.getObject();
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (Exception e) {
            logger.error("ftp client logout failed...{}", e);
            throw e;
        } finally {
            if (ftpClient != null) {
                ftpClient.disconnect();
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<FTPClient> obj) {
        FTPClient ftpClient = obj.getObject();
        try {
            return ftpClient.sendNoOp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void activateObject(PooledObject<FTPClient> obj) throws Exception {
        FTPClient ftpClient = obj.getObject();
        ftpClient.sendNoOp();
    }

    @Override
    public void passivateObject(PooledObject<FTPClient> obj) throws Exception {}
}
