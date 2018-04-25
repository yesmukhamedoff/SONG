package org.icgc.dcc.song.server.repository;

import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import com.google.common.graph.Traverser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.utils.graph.SimpleGraph;
import org.icgc.dcc.song.server.utils.graph.SimpleNode;
import org.icgc.dcc.song.server.utils.graph.SimpleNodePath;

import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.icgc.dcc.song.server.utils.graph.SimpleNode.node;

@Slf4j
@RequiredArgsConstructor
public class FetchPlanner<T, ID> {

  @NonNull private final Class<T> entityClass;
  @NonNull private final EntityManager entityManager;
  @NonNull private final Graph<SimpleNode<String>> graph;
  @NonNull private final SimpleNode<String> rootNode;

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

    val finalGraph = simpleStringGraph.mergePaths();



    val root = node(0, "donors");
    SimpleNode<String> prev = root;
//    SimpleNode<String> curr = root;
//    Traverser.forGraph(simpleStringGraph.mergePaths()).breadthFirst(node(0, "donors")).forEach(x -> log.info(x.getValue()));
    for (val curr : Traverser.forGraph(simpleStringGraph.mergePaths()).breadthFirst(root)){
      if (curr.equals(prev)){
        continue;
      }
      if (finalGraph.hasEdgeConnecting(prev, curr)){

      }


    }

  }

  public Optional<T> fetch(ID id){
    val result = Optional.ofNullable(entityManager.find(entityClass, id,
        buildFetchPlanHint(entityManager, entityClass, graph, rootNode)));
    result.ifPresent(entityManager::detach);
    return result;
  }


  public static <T> Map<String, Object> buildFetchPlanHint(EntityManager entityManager, Class<T> tClass, Graph<SimpleNode<String>> graph, SimpleNode<String> root){
    checkArgument(graph.nodes().contains(root),
        "The root node '%s' is not apart of the graph", root);
    val g = entityManager.createEntityGraph(tClass);
    Subgraph<String> prevSubgraph = null;
    Subgraph<String> currSubgraph = null;

    SimpleNode<String> prev = root;
    for (val curr : Traverser.forGraph(graph).breadthFirst(root)){
      if (curr.equals(prev)){
        currSubgraph = g.addSubgraph(curr.getValue());
        prevSubgraph = currSubgraph;
        continue;
      }
      if (!graph.hasEdgeConnecting(prev, curr)) {
        prevSubgraph = currSubgraph;
      }
      currSubgraph = prevSubgraph.addSubgraph(curr.getValue());
    }

    val hints = Maps.<String,Object>newHashMap();
    hints.put("javax.persistence.fetchgraph", g);
    return hints;
  }

}
