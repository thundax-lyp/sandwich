package com.github.thundax.common.oss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sandwish.oss")
public class SandwishOssProperties {

    private String type = "local";
    private Local local = new Local();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public static class Local {

        private String rootPath = "storage";
        private String locationPrefix = "file:";

        public String getRootPath() {
            return rootPath;
        }

        public void setRootPath(String rootPath) {
            this.rootPath = rootPath;
        }

        public String getLocationPrefix() {
            return locationPrefix;
        }

        public void setLocationPrefix(String locationPrefix) {
            this.locationPrefix = locationPrefix;
        }
    }
}
