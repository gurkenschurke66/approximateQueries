package queryAnswering.algorithms;

import application.Settings;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonConstructor;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonEdge;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonNode;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DijkstraTopK {

    ProductAutomatonConstructor productAutomatonConstructor;


    // π[V] - predecessor of V: <V, predecessorOfV>
    HashMap<ProductAutomatonNode, ProductAutomatonNode> predecessor;

    // set S of nodes (vertices) whose final shortest-path weights from the source have already been determined.
    HashSet<ProductAutomatonNode> setOfNodes;

    // min-priority queue Q (note that by default a priority queue in java is a min queue!)
    PriorityQueue<ProductAutomatonNode> queue;

    // answerSet
    HashMap<Pair<String, String>, Double> answerMap;

    // int value k for topK search
    int k;

    int dijkstracounter = 0;

    public DijkstraTopK(ProductAutomatonConstructor productAutomatonConstructor, int k) {
        predecessor = new HashMap<>();
        setOfNodes = new HashSet<>();
        queue = new PriorityQueue<>();
        answerMap = new HashMap<>();
        this.productAutomatonConstructor = productAutomatonConstructor;
        this.k = k;
    }

    private void algo_dijkstra(ProductAutomatonNode sourceNode) {

        int finalNodesFound = 0;

        // line 1
        DijkstraUtils.initialiseSingleSource(productAutomatonConstructor.productAutomatonGraph, sourceNode, predecessor);
        // line 2
        setOfNodes.clear();
        // line 3
        queue.addAll(productAutomatonConstructor.productAutomatonGraph.nodes);

        // line 4
        while (!queue.isEmpty() && (dijkstracounter < Settings.getRestrictionToPreventInfiniteRuns())) {
            dijkstracounter++;
            // line 5
            ProductAutomatonNode p = queue.poll();
            // line 6
            setOfNodes.add(p);
            // line 7
            for (ProductAutomatonEdge edge : p.getEdges()) {
                setOfNodes.add(edge.getTarget());

                // first check if we've found an answer (edge.target.finalState == true), then
                // check if we've found k answers here. if so -> terminate
                if (edge.getTarget().isFinalState()) {
                    finalNodesFound++;
                    if (finalNodesFound == k) {
                        return;
                    }
                }

                // line 8
                DijkstraUtils.relax(p, edge.getTarget(), edge, predecessor);

            }
        }
    }



    public HashMap<Pair<String, String>, Double> processDijkstraOverAllInitialNodes() {

        // for all initial nodes...
        for (ProductAutomatonNode initialNode : productAutomatonConstructor.productAutomatonGraph.initialNodes) {

            // clean up from previous runs...
            predecessor.clear();
            queue.clear();

            // run single-source dijkstra
            algo_dijkstra(initialNode);
            // put the new shortest-paths into the answer set
            answerMap.putAll(DijkstraUtils.retrieveResultForOneInitialNode(initialNode, setOfNodes));

            // update maxIterationStepsInDijkstraLoop
            //System.out.println("dijkstra counter: " + dijkstracounter);
            //System.out.println("current settings value: " + Settings.getMaxIterationStepsInDijkstraLoop());
            if (dijkstracounter > Settings.getMaxIterationStepsInDijkstraLoop()) {
                Settings.setMaxIterationStepsInDijkstraLoop(dijkstracounter);
            }
        }
        return answerMap;
    }

}
