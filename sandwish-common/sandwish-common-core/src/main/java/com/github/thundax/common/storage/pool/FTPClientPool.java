package com.github.thundax.common.storage.pool;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class FTPClientPool extends GenericObjectPool<FTPClient> {

    public FTPClientPool(FTPClientFactory factory) {
        super(factory);
    }

}
