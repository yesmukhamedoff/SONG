package org.icgc.dcc.song.server.repository;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.Traverser;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

import static org.icgc.dcc.song.server.repository.FetchPlanner.SimpleNode.node;

@Slf4j
@RequiredArgsConstructor
public class FetchPlanner<T> {

  @NonNull private final Class<T> type;
  @NonNull private final EntityManagerFactory entityManagerFactory;

  @Getter
  private final EntityGraph<T> graph;


  @Value
  public static class SimpleNode<T> {
    @NonNull private final int id;
    @NonNull private final T value;

    public static <T> SimpleNode<T> node(int id, T value) {
      return new SimpleNode<>(id, value);
    }
  }

  public static void main(String[] args){
    val path1 = SimpleNodePath.<String>builder()
        .node("donors")
        .node("specimens")
        .node("samples")
        .node("info")
        .build();

    val donorInfo = SimpleNodePath.<String>builder()
        .node("donors")
        .node("info")
        .build();

    val donorRob = SimpleNodePath.<String>builder()
        .node("donors")
        .node("rob")
        .build();

    val specimenInfo = SimpleNodePath.<String>builder()
        .node("donors")
        .node("specimens")
        .node("info")
        .node("rob")
        .build();

    val simpleStringGraph = SimpleGraph.<String>builder()
        .graphPath(path1)
        .graphPath(donorRob)
        .graphPath(donorInfo)
        .graphPath(specimenInfo)
        .build();


    Traverser.forGraph(simpleStringGraph.mergePaths()).breadthFirst(node(0, "donors")).forEach(x -> log.info(x.getValue()));




  }

  public <T> void ttt(EntityManager entityManager, Class<T> tClass, Graph<SimpleNode<String>> graph){
    val g = entityManager.createEntityGraph(tClass);

  }

  @Value
  @Builder
  public static class SimpleNodePath<T> {

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

  @Value
  @Builder
  public static class SimpleGraph<T>{

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




}
