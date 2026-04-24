package com.github.thundax.modules.sys.ueditor;

import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.ueditor.define.ActionMap;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 *
 * @author wdit
 */
public final class ConfigManager {

    private static final String CONFIG_FILENAME = "/uedit/config.json";

    private Config config = null;
    private final String rootPath;

    public ConfigManager(String rootPath) {
        this.rootPath = rootPath;
        this.initEnv();
    }

    /**
     * 验证配置文件加载是否正确
     *
     * @return 正确:true
     */
    public boolean valid() {
        return this.config != null;
    }

    public Config getConfig() {
        return this.config;
    }

    public Map<String, Object> getConfig(int type) {
        Map<String, Object> conf = new HashMap<>(8);
        String savePath = null;
        if (type == ActionMap.UPLOAD_IMAGE) {
            conf.put("isBase64", "false");
            conf.put("maxSize", this.config.getImageMaxSize());
            conf.put("allowFiles", this.config.getImageAllowFiles());
            conf.put("fieldName", this.config.getImageFieldName());
            savePath = this.config.getImagePathFormat();
        }
        conf.put("savePath", savePath);
        conf.put("rootPath", rootPath);
        return conf;
    }

    private void initEnv() {
        try {
            Resource resource = new ClassPathResource(CONFIG_FILENAME);

            String configContent = FileUtils.readFileToString(resource.getFile(), "UTF-8");
            configContent = configContent.replaceAll("/\\*[\\s\\S]*?\\*/", "");

            this.config = JsonUtils.fromJson(configContent, Config.class);
        } catch (Exception e) {
            this.config = null;
        }
    }

}
