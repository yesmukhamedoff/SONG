package org.icgc.dcc.song.server.utils.graph;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.val;

import java.util.List;

import static org.icgc.dcc.song.server.utils.graph.SimpleNode.node;

@Value
@Builder
public class SimpleGraph<T>{

  @NonNull
  @Singular
  private final List<SimpleNodePath<T>> graphPaths;

  public Graph<SimpleNode<T>> mergePaths(){
    val g = GraphBuilder.directed().<SimpleNode<T>>build();
    for(val path : graphPaths){
      val nodes = path.getNodes();
      for (int i=0; i<nodes.size()-1; i++){
        val curr = node(i, nodes.get(i));
        val next = node(i+1, nodes.get(i+1));
        g.putEdge(curr, next);
      }
    }
    return ImmutableGraph.copyOf(g);
  }

}
