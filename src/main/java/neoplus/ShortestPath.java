package neoplus;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.*;
import java.util.stream.Collectors;


public class ShortestPath {

    /**Shortest path procedure implementing the Dijkstra's algorithm
     *
     * @param startNode starting node for path finding
     * @param endNode end node for path finding
     * @param weightProperty the relationship property to be used as weight
     * @return the length of the shortest path
     */
    @UserFunction(name = "neoplus.shortestPathLength")
    @Description("Length of the (weighted) shortest path between two nodes")
    public Double shortestPath(
            @Name("startNode") Node startNode,
            @Name("endNode") Node endNode,
            @Name("weightProperty") String weightProperty
    ) {
        List<Node> visitedNodes = new LinkedList<>();
        Map<Node, Double> shortestDistances = new HashMap<>();
        Map<Node, Node> parentNodes = new HashMap<>();
        shortestDistances.put(startNode, 0.0);

        Node stepStartNode = startNode;
        while (true) {
            System.out.format("----- Start iteration with node %d \n", stepStartNode.getId());
            if (stepStartNode.getElementId().equals(endNode.getElementId())) {
                // we have reached the end node, stop
                System.out.println("Getting results");
                Double r = shortestDistances.get(endNode);
                System.out.format("\tResult is %2.1f\n", r);
                return r;
            }
            // find all OUTGOING or INCOMING relationships associated with stepStartNode
            Iterable<Relationship> rels = stepStartNode.getRelationships(Direction.BOTH);
            for (Relationship rel : rels) {
                // find the node at the other end of the relationship
                Node neighbor = rel.getOtherNode(stepStartNode);
                Double weight = (double) rel.getProperty(weightProperty, 1.0);
                System.out.format("-- Neighbor %s with weight %2.1f\n", neighbor.getElementId(), weight);
                // if node already visited, skip
                if (visitedNodes.contains(neighbor)) {
                    System.out.println("\t already visited, skipping node");
                    continue;
                }
                // node has not been visited yet, get the already computed distance if any
                Double previousDistance = shortestDistances.getOrDefault(neighbor, Double.POSITIVE_INFINITY);
                // and compute the new distance
                Double newDistance =
                        shortestDistances.getOrDefault(stepStartNode, 0.0)
                                + (double) rel.getProperty(weightProperty, 1.0);
                // if new distance is shorter than the previous distance, we have a new shortest path
                if (newDistance < previousDistance) {
                    System.out.format("\t Found new shortest path between %s and %s : %2.1f \n",
                            startNode.getElementId(), neighbor.getElementId(), newDistance);
                    shortestDistances.put(neighbor, newDistance);
                    parentNodes.put(neighbor, stepStartNode);
                } else {
                    System.out.format("\t Distance %2.1f higher than previous one %2.1f, skipping\n",
                            newDistance, previousDistance);
                }
            }
            // add the current node to the list of visited nodes
            visitedNodes.add(stepStartNode);

            // find the next node
            // first filter out the already visited nodes
            Map <Node, Double> fmap = shortestDistances.entrySet().stream().filter(
                    entry -> !visitedNodes.contains(entry.getKey())
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // then select the node with the shortest distance to the start node
            stepStartNode = Collections.min(fmap.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
    }
}


