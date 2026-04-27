package com.github.thundax.common.storage;

import com.google.common.collect.Lists;
import com.github.thundax.common.storage.pool.FTPClientPool;
import com.github.thundax.common.storage.pool.FTPClientProxy;
import com.github.thundax.common.utils.FilenameUtils;
import com.github.thundax.common.utils.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class FtpFileService implements MetaFileService {

    private FTPClientPool pool;
    private String pathPrefix;

    public FTPClientPool getFTPClientPool() {
        return this.pool;
    }

    public void setFTPClientPool(FTPClientPool pool) {
        this.pool = pool;
    }

    /**
     * get/set pathPrefix
     */
    public String getPathPrefix() {
        return this.pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = FilenameUtils.getCanonicalPathname(pathPrefix);
    }

    private String metaFilenameToFtpFilename(String metaFilename) {
        return this.pathPrefix + metaFilename;
    }

    private MetaFile ftpFileToMetaFile(String path, FTPFile ftpFile) {
        MetaFile metaFile = new MetaFile();
        ftpFileToMetaFile(path, ftpFile, metaFile);
        return metaFile;
    }

    private void ftpFileToMetaFile(String path, FTPFile ftpFile, MetaFile metaFile) {
        metaFile.setPathname(path + MetaFile.separator + ftpFile.getName());
        metaFile.setLength(ftpFile.getSize());
        if (ftpFile.isFile()) {
            metaFile.setType(MetaFile.TYPE_FILE);
        } else if (ftpFile.isDirectory()) {
            metaFile.setType(MetaFile.TYPE_FOLDER);
        } else {
            metaFile.setType(MetaFile.TYPE_UNKNOWN);
        }

        if (ftpFile.getTimestamp() != null) {
            metaFile.setLastModified(ftpFile.getTimestamp().getTime());
        } else {
            metaFile.setLastModified(new Date(0));
        }
    }


    @Override
    public boolean ensureObject(MetaFile metaFile) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            FTPFile[] ftpFiles = ftpClientProxy.listFiles(metaFilenameToFtpFilename(metaFile.getPath()));
            for (FTPFile ftpFile : ftpFiles) {
                if (metaFile.isFile() || metaFile.isFolder()) {
                    if (metaFile.isFile() && !ftpFile.isFile()) {
                        continue;
                    }
                    if (metaFile.isFolder() && !ftpFile.isDirectory()) {
                        continue;
                    }
                }
                if (ftpFile.getName().endsWith(metaFile.getName())) {
                    ftpFileToMetaFile(metaFile.getPath(), ftpFile, metaFile);
                    return true;
                }
            }
            return false;

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }


    @Override
    public boolean exists(MetaFile metaFile) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            FTPFile[] ftpFiles = ftpClientProxy.listFiles(metaFilenameToFtpFilename(metaFile.getPath()));
            for (FTPFile ftpFile : ftpFiles) {
                if (metaFile.isFile() && !ftpFile.isFile()) {
                    continue;
                }
                if (metaFile.isFolder() && !ftpFile.isDirectory()) {
                    continue;
                }
                if (ftpFile.getName().endsWith(metaFile.getName())) {
                    return true;
                }
            }

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);
        }
        return false;
    }


    private List<MetaFile> list(MetaFile metaFolder, Function<FTPFile, Boolean> filter) {
        List<MetaFile> mfList = Lists.newArrayList();
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return mfList;
        }

        try {
            String pathname = metaFolder.getPathname();
            FTPFile[] ftpFiles = ftpClientProxy.listFiles(metaFilenameToFtpFilename(pathname));
            if (ftpFiles != null) {
                for (FTPFile ftpFile : ftpFiles) {
                    if (StringUtils.equals(ftpFile.getName(), ".") || StringUtils.equals(ftpFile.getName(), "..")) {
                        continue;
                    }
                    if (filter == null || filter.apply(ftpFile)) {
                        MetaFile metaFile = this.ftpFileToMetaFile(pathname, ftpFile);
                        mfList.add(metaFile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            returnFTPClient(ftpClientProxy);
        }

        return mfList;
    }


    @Override
    public List<MetaFile> list(MetaFile metaFolder) {
        return list(metaFolder, null);
    }

    @Override
    public List<MetaFile> listFolders(MetaFile metaFolder) {
        return list(metaFolder, FTPFile::isDirectory);
    }

    @Override
    public List<MetaFile> listFiles(MetaFile metaFolder) {
        return list(metaFolder, FTPFile::isFile);
    }

    @Override
    public boolean rename(MetaFile src, MetaFile dest) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            ftpClientProxy.rename(metaFilenameToFtpFilename(src.getPathname()),
                    metaFilenameToFtpFilename(dest.getPathname()));
            return true;

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }

    @Override
    public boolean createFolder(MetaFile folder) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            String pathname = metaFilenameToFtpFilename(folder.getPathname());
            List<String> folders = Lists.newArrayList(StringUtils.split(pathname, MetaFile.separator));
            return createFolder(ftpClientProxy, folders);

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }

    private boolean createFolder(@NonNull FTPClientProxy ftpClientProxy, List<String> folders) throws IOException {
        if (folders.size() > 1) {
            List<String> parentFolders = folders.subList(0, folders.size() - 1);
            String parent = MetaFile.separator + StringUtils.join(parentFolders, MetaFile.separator);
            if (!(ftpClientProxy.changeWorkingDirectory(parent))) {
                createFolder(ftpClientProxy, parentFolders);
            }
        }
        return ftpClientProxy.makeDirectory(MetaFile.separator + StringUtils.join(folders, MetaFile.separator));
    }

    @Override
    public boolean deleteFolder(MetaFile folder) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            return ftpClientProxy.removeDirectory(metaFilenameToFtpFilename(folder.getPathname()));

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }

    @Override
    public boolean copyFolder(MetaFile src, MetaFile dest) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteFile(MetaFile file) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            return ftpClientProxy.deleteFile(metaFilenameToFtpFilename(file.getPathname()));

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }

    @Override
    public boolean copyFile(MetaFile src, MetaFile dest) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean writeFile(MetaFile metaFile, byte[] b, int off, int len) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(b, off, len);
        try {
            return writeFile(metaFile, inputStream);

        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean writeFile(MetaFile file, InputStream inputStream) {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return false;
        }

        try {
            System.out.println("=========begin upload===" + file.getPathname());

            ftpClientProxy.changeWorkingDirectory(metaFilenameToFtpFilename(file.getPath()));
            return ftpClientProxy.storeFile(file.getName(), inputStream);

        } catch (IOException e) {
            return false;

        } finally {
            returnFTPClient(ftpClientProxy);

            System.out.println("=========upload success");
        }
    }

    @Override
    public String readFileAsString(MetaFile file, Charset charset) throws IOException {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ftpClientProxy.changeWorkingDirectory(metaFilenameToFtpFilename(file.getPath()));
            ftpClientProxy.retrieveFile(file.getName(), outputStream);
            return new String(outputStream.toByteArray(), charset);

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }

    @Override
    public byte[] readFileAsByteArray(MetaFile file) throws IOException {
        FTPClientProxy ftpClientProxy = getFTPClient();
        if (ftpClientProxy == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ftpClientProxy.changeWorkingDirectory(metaFilenameToFtpFilename(file.getPath()));
            ftpClientProxy.retrieveFile(file.getName(), outputStream);
            return outputStream.toByteArray();

        } finally {
            returnFTPClient(ftpClientProxy);
        }
    }

    @Override
    public InputStream readFileAsStream(MetaFile metaFile) throws IOException {
        return new ByteArrayInputStream(this.readFileAsByteArray(metaFile));
    }

    private FTPClientProxy getFTPClient() {
        try {
            FTPClient ftpClient = pool.borrowObject();
            return new FTPClientProxy(ftpClient);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void returnFTPClient(FTPClientProxy ftpClientProxy) {
        try {
            pool.returnObject(ftpClientProxy.getFTPClient());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
