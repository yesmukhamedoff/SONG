package org.icgc.dcc.song.indexer.config;

import lombok.Data;
import org.apache.zookeeper.ZooKeeper;
import org.icgc.dcc.song.indexer.repo.KafkaOffsetStore;
import org.icgc.dcc.song.indexer.repo.impl.KafkaOffsetZkStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.util.Map;

@Configuration
public class IndexerConfig {

    @Data
    public static class ElasticsearchData {
        private String server;
        private int port;
        private String index;
    }

    @Bean
    @ConfigurationProperties(prefix = "elasticsearch")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ElasticsearchData getESData() {
        return new ElasticsearchData();
    }

    @Data
    public static class KafkaData {
        private String server;
        private int port;
        private String topic;
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public KafkaData getKafkaData() {
        return new KafkaData();
    }

    @Data
    public static class ZookeeperData {
        private String server;
        private int port;
        private String path;
    }

    @Bean
    @ConfigurationProperties(prefix = "zookeeper")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ZookeeperData getZkData() {
        return new ZookeeperData();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public KafkaOffsetStore getKafkaOffsetStore(ZooKeeper zk) {
        return new KafkaOffsetZkStore(zk);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ZooKeeper getZookeeper(ZookeeperData zk) throws IOException {
        return new ZooKeeper(zk.getServer(), zk.getPort(), null);
    }

}
