package org.icgc.dcc.song.server.utils.graph;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.val;

import java.util.List;

@Value
@Builder
public class SimpleNodePath<T> {

  @NonNull
  @Singular
  private final List<T> nodes;

  public Graph<T> buildGraph(){
    val g = GraphBuilder.directed().<T>build();
    for (int i = 0; i< nodes.size()-1; i++){
      val curr = nodes.get(i);
      val next = nodes.get(i+1);
      g.putEdge(curr, next);
    }
    return g;
  }
}
