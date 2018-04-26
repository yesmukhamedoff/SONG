package org.icgc.dcc.song.server.utils.graph;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.val;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

@Value
@Builder
public class SimpleNodePath<T> {

  @NonNull
  @Singular
  private final List<T> nodes;

  public Graph<T> buildGraph(){
    val g = GraphBuilder.directed().<T>build();
    checkState(nodes.size()>0, "there should be atleast 1 node");

    for (int i = 0; i< nodes.size()-1; i++){
      val curr = nodes.get(i);
      val next = nodes.get(i+1);
      g.putEdge(curr, next);
    }
    return g;
  }
}
