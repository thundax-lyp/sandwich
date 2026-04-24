package com.github.thundax.common.storage.pool;

import org.apache.commons.net.ftp.FTP;

public class FTPClientConfig {

    private String host;
    private int port = FTP.DEFAULT_PORT;
    private String username;
    private String password;

    private int connectTimeout = 20000;
    private boolean enableAsciiType = false;
    private boolean enablePassiveMode = true;

    public FTPClientConfig() {

    }

    /**
     * get/set connectTimeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     * get/set host
     */
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * get/set port
     */
    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * get/set username
     */
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * get/set password
     */
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * get/set enableAsciiType
     */
    public boolean getEnableAsciiType() {
        return this.enableAsciiType;
    }

    public void setEnableAsciiType(boolean enableAsciiType) {
        this.enableAsciiType = enableAsciiType;
    }

    /**
     * get/set enablePassiveMode
     */
    public boolean getEnablePassiveMode() {
        return this.enablePassiveMode;
    }

    public void setEnablePassiveMode(boolean enablePassiveMode) {
        this.enablePassiveMode = enablePassiveMode;
    }

}
