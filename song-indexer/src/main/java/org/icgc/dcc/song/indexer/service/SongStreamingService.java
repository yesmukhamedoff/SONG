package org.icgc.dcc.song.indexer.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.icgc.dcc.song.indexer.config.IndexerConfig;
import org.icgc.dcc.song.indexer.repo.KafkaOffsetStore;
import org.jvnet.hk2.annotations.Service;

@Service
@RequiredArgsConstructor
public class SongStreamingService {

    @NonNull
    private JavaStreamingContext jssc;
    @NonNull
    private IndexerConfig.ElasticsearchData es;
    @NonNull
    private IndexerConfig.KafkaData kafka;
    @NonNull
    private KafkaOffsetStore store;


    public void run() {
//        KafkaUtils.createDirectStream()
    }
}
