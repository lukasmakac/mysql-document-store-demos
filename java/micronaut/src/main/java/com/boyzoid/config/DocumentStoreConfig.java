package com.boyzoid.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("docstore")
public class DocumentStoreConfig {
    private String url;

    private String schema;

    private Pooling pooling;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Pooling getPooling() {
        return pooling;
    }

    public void setPooling(Pooling pooling) {
        this.pooling = pooling;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @ConfigurationProperties("pooling")
    static class Pooling {
        private Boolean enabled;
        private Integer maxSize;
        private Integer maxIdleTime;
        private Integer queueTimeout;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }

        public Integer getMaxIdleTime() {
            return maxIdleTime;
        }

        public void setMaxIdleTime(Integer maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        public Integer getQueueTimeout() {
            return queueTimeout;
        }

        public void setQueueTimeout(Integer queueTimeout) {
            this.queueTimeout = queueTimeout;
        }
    }
}
