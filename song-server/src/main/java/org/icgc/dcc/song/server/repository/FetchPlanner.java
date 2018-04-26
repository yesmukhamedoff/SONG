package org.icgc.dcc.song.server.repository;

import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import com.google.common.graph.Traverser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.icgc.dcc.song.server.utils.graph.SimpleNode;

import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.icgc.dcc.song.server.utils.graph.SimpleNode.node;

/**
 * Stores a fetchplan for the entity
 * Example Usage
 * <code>
 * SimpleNodePath<String> path1 = SimpleNodePath.<String>builder()
 *            .node("donors")
 *            .node("specimens")
 *            .node("samples")
 *            .node("info")
 *            .build();
 *
 * SimpleNodePath<String> path2 = SimpleNodePath.<String>builder()
 *            .node("donors")
 *            .node("specimens")
 *            .node("info")
 *            .build();
 *
 * SimpleGraph<String> simpleStringGraph = SimpleGraph.<String>builder()
 *             .graphPath(path1)
 *             .graphPath(path2)
 *             .build();
 *
 * Graph<SimpleNode<String>> mergedGraph = simpleStringGraph.mergePaths();
 * FetchPlanner<FullStudyEntity, String> fetchPlanner =
 *                                          FetchPlanner.createFetchPlanner(FullStudyEntity.class,
 *                                                      entityManager, mergedGraph, "donors"); //donors is the root
 * FullStudyEntity study = fetchPlanner.fetch("ABC123);
 * </code>
 *
 * This will then only fetch the graph specified, maximizing the use of joins and avoiding the N+1 Select problem. The
 * output entity is DETACHED meaning call a getter on a field that is Lazily initialized and that was not apart of the
 * fetchplan will throw a LazyInitializationException.
 */
@Slf4j
@RequiredArgsConstructor
public class FetchPlanner<T, ID> {

  private static final String FETCHGRAPH_PROPERTY_KEY = "javax.persistence.fetchgraph";

  @NonNull private final Class<T> entityClass;
  @NonNull private final EntityManager entityManager;
  @NonNull private final Graph<SimpleNode<String>> graph;
  @NonNull private final SimpleNode<String> rootNode;

  public Optional<T> fetch(ID id){
    val result = Optional.ofNullable(entityManager.find(entityClass, id,
        buildFetchPlanHint(entityManager, entityClass, graph, rootNode)));
    result.ifPresent(entityManager::detach);
    val session = entityManager.unwrap(Session.class);
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
    hints.put(FETCHGRAPH_PROPERTY_KEY, g);
    return hints;
  }

  public static <T, ID> FetchPlanner<T, ID> createFetchPlanner(Class<T> entityClass, EntityManager entityManager,
      Graph<SimpleNode<String>> graph, SimpleNode<String> rootNode) {
    return new FetchPlanner<>(entityClass, entityManager, graph, rootNode);
  }

  public static <T, ID> FetchPlanner<T, ID> createFetchPlanner(Class<T> entityClass, EntityManager entityManager,
      Graph<SimpleNode<String>> graph, String rootNodeValue) {
    return createFetchPlanner(entityClass, entityManager, graph, node(0, rootNodeValue));
  }


}
