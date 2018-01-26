package org.icgc.dcc.song.indexer.repo.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.zookeeper.ZooKeeper;
import org.icgc.dcc.song.indexer.repo.KafkaOffsetStore;

@RequiredArgsConstructor
public class KafkaOffsetZkStore implements KafkaOffsetStore{

    @NonNull
    private ZooKeeper zk;

    @Override
    public void store(long start, long end) {

    }

    @Override
    public Pair<Long, Long> retrieve() {
        return null;
    }
}
