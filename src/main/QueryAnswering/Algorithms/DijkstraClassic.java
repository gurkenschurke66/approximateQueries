package Algorithms;

import Application.Settings;
import ProductAutomatonSpecification.ProductAutomatonConstructor;
import ProductAutomatonSpecification.ProductAutomatonEdge;
import ProductAutomatonSpecification.ProductAutomatonGraph;
import ProductAutomatonSpecification.ProductAutomatonNode;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

// note: we do not use the distance here since we store the weight in every ProductAutomatonSpecification.ProductAutomatonNode.
public class DijkstraClassic {

    ProductAutomatonConstructor productAutomatonConstructor;


    // π[V] - predecessor of V: <V, predecessorOfV>
    HashMap<ProductAutomatonNode, ProductAutomatonNode> predecessor;

    // set S of nodes (vertices) whose final shortest-path weights from the source have already been determined.
    HashSet<ProductAutomatonNode> setOfNodes;

    // min-priority queue Q (note that by default a priority queue in java is a min queue!)
    PriorityQueue<ProductAutomatonNode> queue;

    // answerSet
    HashMap<Pair<String, String>, Double> answerMap;

    int dijkstracounter = 0;


    public DijkstraClassic(ProductAutomatonConstructor productAutomatonConstructor) {
        predecessor = new HashMap<>();
        setOfNodes = new HashSet<>();
        queue = new PriorityQueue<>();
        answerMap = new HashMap<>();
        this.productAutomatonConstructor = productAutomatonConstructor;


    }




    /**
     * Dijkstra algorithm
     * explanations taken from "Introduction to algorithms" (Cormen, Leiserson, Rivest, Stein)
     * page 595
     * comments refer to the pseudo-code presented there
     *
     * @param sourceNode the source node
     */

    private void algo_dijkstra(ProductAutomatonNode sourceNode) {
        //System.out.println("algo dijkstra");



        // line 1
        initialiseSingleSource(productAutomatonConstructor.productAutomatonGraph, sourceNode);
        // line 2
        setOfNodes.clear();
        // line 3
        queue.addAll(productAutomatonConstructor.productAutomatonGraph.nodes);
        // todo: queue.remove(sourceNode)
        //       queue.add(sourceNode)
        //       to start with the source node??
        // System.out.println(queue);

        // line 4
        // we need the second condition for a proper termination in possible infinite runs.
        // the condition is chosen dynamically, i.e. for larger input structures it iterates more often.
        // the infinite loop arises when we use "searchAll()" over an input that allows for loops with weight 0, thus dijkstra endlessly explores the "new" infinite path
        while (!queue.isEmpty() && (dijkstracounter < Settings.getRestrictionToPreventInfiniteRuns())) {
            dijkstracounter++;
            //System.out.println(dijkstracounter);

            // line 5
            ProductAutomatonNode p = queue.poll();
            // todo: if p has larger weight: return "threshold is reached"
            // line 6
            setOfNodes.add(p);
            // line 7
            for (ProductAutomatonEdge edge : p.edges) {
                setOfNodes.add(edge.target);
                // line 8
                relax(p, edge.target, edge);

                // this will keep the priorityQueue ordered.
                queue.remove(edge.target);
                queue.add(edge.target);

            }
        }
    }

    private void initialiseSingleSource(ProductAutomatonGraph graph, ProductAutomatonNode sourceNode) {

        // line 1.1
        for (ProductAutomatonNode node : graph.nodes) {

            // line 1.2
            node.setWeight(Double.POSITIVE_INFINITY);

            // line 1.3
            predecessor.put(node, null);
        }
        // line 1.4
        sourceNode.setWeight(0.0);
    }

    private void relax(ProductAutomatonNode u, ProductAutomatonNode v, ProductAutomatonEdge edge) {

        Double newCost = u.getWeight() + edge.cost;

        // line 8.1
        if (v.getWeight() >= newCost && !newCost.isInfinite()) {

            // line 8.2
            v.setWeight(newCost);

            // line 8.3
            // predecessor of v is u.
            predecessor.put(v, u);

        }

    }

    public HashMap<Pair<String, String>, Double> processDijkstraOverAllInitialNodes() {


        // for all initial nodes...
        for (ProductAutomatonNode initialNode : productAutomatonConstructor.productAutomatonGraph.initialNodes) {
            //System.out.print("dijkstra for initial node: " );
            //initialNode.print();

            // clean up from previous runs...
            predecessor.clear();
            queue.clear();

            // run single-source dijkstra
            algo_dijkstra(initialNode);
            // put the new shortest-paths into the answer set
            retrieveResultForOneInitialNode(initialNode);

            // update maxIterationStepsInDijkstraLoop
            //System.out.println("dijkstra counter: " + dijkstracounter);
            //System.out.println("current settings value: " + Settings.getMaxIterationStepsInDijkstraLoop());
            if (dijkstracounter > Settings.getMaxIterationStepsInDijkstraLoop()) {
                Settings.setMaxIterationStepsInDijkstraLoop(dijkstracounter);
            }

        }

        return answerMap;

    }

    private void retrieveResultForOneInitialNode(ProductAutomatonNode initialNode) {

        // for all nodes in set S
        for (ProductAutomatonNode node : setOfNodes) {
            // if you are a final state and your weight is not infinite (that means there is a path from the source to you)
            if (node.finalState && !node.getWeight().isInfinite()) {
                // I add you to the final answerSet in the form of ((source, target), weight)
                Pair<String, String> answerPair = new Pair(initialNode.identifier.getValue2().identifier, node.identifier.getValue2().identifier);
                answerMap.put(answerPair, node.getWeight());
            }
        }
    }
}
