package org.icgc.dcc.song.server.model.analysis;

import org.icgc.dcc.song.server.model.Metadata;

public abstract class Experiment2<T extends Experiment2<T>> extends Metadata {

  abstract public Analysis2<T> getAnalysis();

}
