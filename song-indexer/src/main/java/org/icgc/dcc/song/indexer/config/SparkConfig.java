package org.icgc.dcc.song.indexer.config;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConfigurationProperties(prefix = "spark")
public class SparkConfig {
    String master;
    String app;
    @Value("${streaming.interval}")
    int interval;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SparkSession getSparkSession(IndexerConfig.ElasticsearchData es) {
        return SparkSession.builder()
                .appName(app)
                .master(master)
                .config("es.nodes", es.getServer())
                .config("es.port", es.getPort())
                .config("es.index.auto.create", "true")
                .getOrCreate();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JavaStreamingContext getJavaStreamingContext(SparkSession session) {
        return new JavaStreamingContext(session.sparkContext().getConf(), Durations.seconds(interval));
    }
}
