package org.icgc.dcc.song.indexer.repo;

import org.apache.commons.lang3.tuple.Pair;

public interface KafkaOffsetStore {
    void store(long start, long end);
    Pair<Long, Long> retrieve();
}
