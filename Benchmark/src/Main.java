import org.apache.commons.lang3.time.StopWatch;
import org.jgrapht.Graph;
import org.jgrapht.alg.color.ColorRefinementAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.isomorphism.ColorRefinementIsomorphismInspector;
import org.jgrapht.alg.isomorphism.IndividualizationRefinementIsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.*;

public class Main {
    static int seed = 0;
    public static void main (String[] args) {
        // RunBenchmarkWithLinearNumberOfEdges();
        // RunRandomTestWithLinearNumberOfEdges();
         RunBenchmarkVF2IR();
        // RunBenchmarkWithEdgeProbability();
    }

    private static void RunBenchmarkVF2() {
        double e = 200;
        for (int i = 200; i <= 10400; i += 200) {
            VF2BenchmarkResult result = RunVF2BenchmarkWithEdgeProbability(i, 0.1);
            System.out.println(result.getProblemSize() + "\t" + result.getColorRefinementMilliseconds() + "\t" + result.getVf2Milliseconds());
        }
    }

    private static void RunBenchmarkVF2IR() {
        double e = 200;
        for (int i = 200; i <= 10400; i += 200) {
            VF2BenchmarkResult result = RunVF2BenchmarkIR(i, 2);
            System.out.println(result.getProblemSize() + "\t" + result.getColorRefinementMilliseconds() + "\t" + result.getVf2Milliseconds());
        }
    }

    private static void RunRandomTest() {
        for (int i = 1; i < 100; i+= 1) {
            int numberOfNonSimplicialClasses = 0;
            int nonDisreteCounter = 0;
            int limit = 1000;
            for (int j = 0; j < limit; j++) {
                BenchmarkResult result = RunBenchmarkWithEdgeProbability(i, 0.1);
                numberOfNonSimplicialClasses += result.getNumberOfNonSimplicialClasses();
                if (result.getNumberOfNonSimplicialClasses() != 0) {
                    nonDisreteCounter++;
                }
            }
            System.out.println(i + "\t" + (double)numberOfNonSimplicialClasses / limit + "\t" + (double)nonDisreteCounter / limit);
        }
    }

    private static void RunRandomTestWithLinearNumberOfEdges() {
        for (int i = 5; i < 250; i+= 1) {
            int numberOfNonSimplicialClasses = 0;
            int nonDisreteCounter = 0;
            int limit = 1000;
            for (int j = 0; j < limit; j++) {
                BenchmarkResult result = RunBenchmarkWithLinearNumberOfEdges(i, 2);
                numberOfNonSimplicialClasses += result.getNumberOfNonSimplicialClasses();
                if (result.getNumberOfNonSimplicialClasses() != 0) {
                    nonDisreteCounter++;
                }
            }
            System.out.println(i + "\t" + (double)numberOfNonSimplicialClasses / limit + "\t" + (double)nonDisreteCounter / limit);
        }
    }

    private static void RunBenchmarkWithEdgeProbability() {
        double e = 200;
        for (int i = 1; i < 21; i += 1) {
            BenchmarkResult result = RunBenchmarkWithEdgeProbability(i * 200, 0.1);
            System.out.println(result.getProblemSize() + "\t" + result.getMilliseconds() + "\t" + result.getNumberOfClasses() + "\t" + result.getNumberOfNonSimplicialClasses());
        }
    }

    private static void RunBenchmarkWithLinearNumberOfEdges() {
        double e = 200;
        for (int i = 1; i < 21; i += 1) {
            BenchmarkResult result = RunBenchmarkWithLinearNumberOfEdges(i * 200, 2);
            System.out.println(result.getProblemSize() + "\t" + result.getMilliseconds() + "\t" + result.getNumberOfClasses() + "\t" + result.getNumberOfNonSimplicialClasses());
        }
    }

    private static BenchmarkResult RunBenchmarkWithEdgeProbability(int size, double edgeProbability) {
        GnpRandomGraphGenerator<Integer, DefaultEdge> generator = new GnpRandomGraphGenerator<Integer, DefaultEdge>(
                size, edgeProbability, seed++);
        return RunBenchmark(generator, size);
    }

    private static VF2BenchmarkResult RunVF2BenchmarkWithEdgeProbability(int size, double edgeProbability) {
        GnmRandomGraphGenerator<Integer, DefaultEdge> generator = new GnmRandomGraphGenerator<Integer, DefaultEdge>(
                size, (int)(edgeProbability * (size * (size - 1) / 2)), seed++);
        return RunVF2Benchmark(generator, size);
    }

    private static VF2BenchmarkResult RunVF2BenchmarkIR(int size, double factor) {
        GnmRandomGraphGenerator<Integer, DefaultEdge> generator = new GnmRandomGraphGenerator<Integer, DefaultEdge>(
                size, (int)(factor * size), seed++);
        return RunVF2BenchmarkIR(generator, size);
    }

    private static BenchmarkResult RunBenchmarkWithLinearNumberOfEdges(int size, int multiplier) {
        GnmRandomGraphGenerator<Integer, DefaultEdge> generator = new GnmRandomGraphGenerator<Integer, DefaultEdge>(
                size, multiplier * size, seed++);
        return RunBenchmark(generator, size);
    }

    private static BenchmarkResult RunBenchmark(GraphGenerator<Integer, DefaultEdge, Integer> generator, int size) {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(graph);
        ColorRefinementAlgorithm<Integer, DefaultEdge> algorithm = new ColorRefinementAlgorithm<>(graph);
        System.gc();
        StopWatch watch = new StopWatch();
        watch.start();
        VertexColoringAlgorithm.Coloring<Integer> coloring = algorithm.getColoring();
        watch.stop();

        int numberOfNonSimplicialClasses = GetNumberOfNonSimplicialClasses(coloring);

        return new BenchmarkResult((int)watch.getTime(), coloring.getNumberColors(), numberOfNonSimplicialClasses, size);
    }

    private static VF2BenchmarkResult RunVF2Benchmark(GraphGenerator<Integer, DefaultEdge, Integer> generator, int size) {
        Graph<Integer, DefaultEdge> graph1 = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(graph1);
        Graph<Integer, DefaultEdge> graph2 = getRandomPermutation(graph1, seed++);

        ColorRefinementIsomorphismInspector<Integer, DefaultEdge> colorRefinementIsomorphismInspector = new ColorRefinementIsomorphismInspector<>(graph1, graph2);
        VF2GraphIsomorphismInspector<Integer, DefaultEdge> vf2IsomorphismInspector = new VF2GraphIsomorphismInspector<>(graph1, graph2);
        System.gc();
        StopWatch watch = new StopWatch();
        watch.start();
        boolean resultColorRefinement = colorRefinementIsomorphismInspector.isomorphismExists();
        watch.stop();
        long colorRefinementTime = watch.getTime();
        System.gc();
        watch = new StopWatch();
        watch.start();
        boolean resultVf2 = vf2IsomorphismInspector.isomorphismExists();
        watch.stop();
        long vf2Time = watch.getTime();

        if (resultColorRefinement != resultVf2) {
            // throw new RuntimeException("Tests were different, CR=" + resultColorRefinement);
        }

        return new VF2BenchmarkResult((int)colorRefinementTime, (int)vf2Time, size);
    }

    private static VF2BenchmarkResult RunVF2BenchmarkIR(GraphGenerator<Integer, DefaultEdge, Integer> generator, int size) {
        Graph<Integer, DefaultEdge> graph1 = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(graph1);
        Graph<Integer, DefaultEdge> graph2 = getRandomPermutation(graph1, seed++);

        IndividualizationRefinementIsomorphismInspector<Integer, DefaultEdge> colorRefinementIsomorphismInspector = new IndividualizationRefinementIsomorphismInspector<>(graph1, graph2);
        VF2GraphIsomorphismInspector<Integer, DefaultEdge> vf2IsomorphismInspector = new VF2GraphIsomorphismInspector<>(graph1, graph2);
        System.gc();
        StopWatch watch = new StopWatch();
        watch.start();
        boolean resultColorRefinement = colorRefinementIsomorphismInspector.isomorphismExists();
        watch.stop();
        long colorRefinementTime = watch.getTime();
        System.gc();
        watch = new StopWatch();
        watch.start();
        boolean resultVf2 = vf2IsomorphismInspector.isomorphismExists();
        watch.stop();
        long vf2Time = watch.getTime();

        if (resultColorRefinement != resultVf2) {
            // throw new RuntimeException("Tests were different, CR=" + resultColorRefinement);
        }

        return new VF2BenchmarkResult((int)colorRefinementTime, (int)vf2Time, size);
    }

    private static Graph<Integer, DefaultEdge> getRandomPermutation(Graph<Integer, DefaultEdge> graph, int seed) {
        Random random = new Random(seed);

        ArrayList<Integer> remaining = new ArrayList<>(graph.vertexSet().size());

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            remaining.add(i);
        }

        Map<Integer, Integer> hashSet = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            Integer value = remaining.get(random.nextInt(remaining.size()));
            hashSet.put(i, value);
            remaining.remove(value);
        }
        Graph<Integer, DefaultEdge> resultGraph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            resultGraph.addVertex(i);
        }
        for (int i = 0; i < graph.vertexSet().size(); i++) {

            for (int j = i + 1; j < graph.vertexSet().size(); j++) {
                if (graph.containsEdge(i, j)) {
                    resultGraph.addEdge(hashSet.get(i), hashSet.get(j));
                }
            }
        }

        return resultGraph;
    }

    private static int GetNumberOfNonSimplicialClasses(VertexColoringAlgorithm.Coloring<Integer> coloring) {
        int numberOfNonSimplicialClasses = 0;
        for (Set<Integer> cl : coloring.getColorClasses()){
            if (cl.size() > 1) {
                numberOfNonSimplicialClasses++;
            }
        }
        return numberOfNonSimplicialClasses;
    }

    private static class BenchmarkResult {
        private int milliseconds;
        private int numberOfClasses;
        private int numberOfNonSimplicialClasses;
        private int problemSize;

        private BenchmarkResult(int milliseconds, int numberOfClasses, int numberOfNonSimplicialClasses, int problemSize) {
            this.milliseconds = milliseconds;
            this.numberOfClasses = numberOfClasses;
            this.numberOfNonSimplicialClasses = numberOfNonSimplicialClasses;
            this.problemSize = problemSize;
        }

        public int getNumberOfClasses() {
            return numberOfClasses;
        }

        public int getMilliseconds() {
            return milliseconds;
        }

        public int getNumberOfNonSimplicialClasses() {
            return numberOfNonSimplicialClasses;
        }

        public int getProblemSize() {
            return problemSize;
        }
    }

    private static class VF2BenchmarkResult {
        private int colorRefinementMilliseconds;
        private int vf2Milliseconds;
        private int problemSize;

        private VF2BenchmarkResult(int colorRefinementMilliseconds, int vf2Milliseconds, int problemSize) {
            this.colorRefinementMilliseconds = colorRefinementMilliseconds;
            this.vf2Milliseconds = vf2Milliseconds;
            this.problemSize = problemSize;
        }

        public int getVf2Milliseconds() {
            return vf2Milliseconds;
        }

        public int getColorRefinementMilliseconds() {
            return colorRefinementMilliseconds;
        }

        public int getProblemSize() {
            return problemSize;
        }
    }
}
