package pes.tda.mapping;

import pes.tda.algorithm.AdaptiveGNG;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is the adaptive growing neural gas (GNG, a variant of self-organizing map). This algorithm will take program characteristics
 * as inputs and generate a network (or map) that optimally represent the topological relations of programs evolved by GP algorithm.
 * At the moment, the algorithm can be called and the GNG network is updated in each generation. To improve the efficiency of the algorithm,
 * principal component analysis (PCA) is applied.
 */


public class Mapper {
    public static boolean interactive = false;
    public static boolean CROSS_GENERATION =true;
    public static boolean SHOW_GNG = true;
    public static boolean AGGREGATEFIT = true;
    static AdaptiveGNG gng;
    static double[][] alldata = new double[0][];
    static ArrayList<String> alllabels = new ArrayList<>();
    public static ArrayList<Double> allvalues = new ArrayList<>();
    public static ArrayList<Integer> allsizes = new ArrayList<>();
    public static int bestloc = -1;
    public static double rel_tollerance = 0.00001;
    public static PrincipalComponentAnalysis pca;
    public static MapperModel gngmodel = new MapperModel();
    public static int MAXDATA = 2000;
    public static int NPC = 2;
    public static ArrayList<Double>[] benchmarkSample;
    public static String[] benchmarkName = null;
    public static int theta = 50;

    public static void clearLabels() {
        for (int i = 0; i < alllabels.size(); i++) {
            alllabels.set(i, "");
        }
    }

    public static double[][] pca(double[][] data, int ncomp, boolean freshrun) {
        if (freshrun) {
            pca = new PrincipalComponentAnalysis();
            pca.setup(data.length, data[0].length);
            for (int i = 0; i < data.length; i++) {
                pca.addSample(data[i]);
            }
            pca.computeBasis(ncomp);
            gngmodel.pca = pca;
        }
        double[][] transData = new double[data.length][ncomp];
        for (int i = 0; i < data.length; i++) {
            transData[i] = pca.sampleToEigenSpace(data[i]);
        }
        return transData;
    }


    public static void mapping(double[][] data, ArrayList<String> labels, ArrayList<Integer> sizes, ArrayList<Double> values, int maxiteration, boolean freshstart, boolean takestreenshot, String outputname) throws IOException, InterruptedException {
        int cutpoint = 0; int datalength = data.length;
        if (alldata.length + datalength > MAXDATA) {
            cutpoint = alldata.length + datalength - MAXDATA;
        }

        double[][] dat = preprocessing(freshstart, cutpoint, datalength);

        for (int i = alldata.length - cutpoint; i < datalength - cutpoint + alldata.length; i++) {
            double[] vec = new double[data[i-alldata.length +cutpoint].length];
            for (int j = 0; j < vec.length; j++) {
                vec[j] = data[i-alldata.length + cutpoint][j];
            }
            dat[i] = vec;
        }

        alldata = dat;
        if (cutpoint > 0) {
            for (int i = 0; i < cutpoint; i++) {
                alllabels.remove(0);
                allvalues.remove(0);
                allsizes.remove(0);
            }
        }
        alllabels.addAll(labels);
        allvalues.addAll(values);
        allsizes.addAll(sizes);

        startMapping(pca(alldata,NPC, true), alllabels, allsizes, allvalues, maxiteration, freshstart,takestreenshot, outputname); //pca(alldata,20)
    }

    public static void mapping(ArrayList[] data, ArrayList<String> labels, ArrayList<Integer> sizes, ArrayList<Double> values, int maxiteration, boolean freshstart, boolean takestreenshot, String outputname) throws IOException, InterruptedException {
        int cutpoint = 0; int datalength = data.length;
        if (alldata.length + datalength > MAXDATA) {
            cutpoint = alldata.length + datalength - MAXDATA;
        }

        double[][] dat = preprocessing(freshstart, cutpoint, datalength);

        for (int i = alldata.length - cutpoint; i < datalength - cutpoint + alldata.length; i++) {
            double[] vec = new double[data[i-alldata.length +cutpoint].size()];
            for (int j = 0; j < vec.length; j++) {
                vec[j] = (int) data[i-alldata.length + cutpoint].get(j);
            }
            dat[i] = vec;
        }

        alldata = dat;
        if (cutpoint > 0) {
            for (int i = 0; i < cutpoint; i++) {
                alllabels.remove(0);
                allvalues.remove(0);
                allsizes.remove(0);
            }
        }
        alllabels.addAll(labels);
        allvalues.addAll(values);
        allsizes.addAll(sizes);

        startMapping(pca(alldata,NPC, true), alllabels, allsizes, allvalues, maxiteration, freshstart,takestreenshot, outputname); //pca(alldata,20)
    }

    private static double[][] preprocessing(boolean freshstart, int cutpoint, int datalength) {
        double[][] dat = new double[datalength][];
        if (!freshstart) {
            dat = new double[datalength + alldata.length - cutpoint][];
            bestloc = alldata.length;
        }
        else {
            alldata = new double[0][];
            alllabels.clear();
            allvalues.clear();
            allsizes.clear();
            bestloc = 0;
        }

        for (int i = cutpoint; i < alldata.length; i++) {
            dat[i - cutpoint] = alldata[i];
        }
        return dat;
    }


    public static void startMapping(double[][] data, ArrayList<String> labels, ArrayList<Integer> sizes, ArrayList<Double> values, int maxiteration, boolean freshstart, boolean takestreenshot, String outputname) throws IOException, InterruptedException {
        if (benchmarkName != null) {
            Visualisation.reference_names = benchmarkName;
            Visualisation.reference_corrs = new double[benchmarkName.length][];
            for (int i = 0; i < benchmarkName.length; i++) {
                double[] features = new double[benchmarkSample[i].size()];
                for (int j = 0; j < benchmarkSample[i].size(); j++) {
                    features[j] = benchmarkSample[i].get(j);
                }
                Visualisation.reference_corrs[i] = pca.sampleToEigenSpace(features);
            }
        }
        Visualisation.markbest = -1;
        Visualisation.setNodeSize(5,10);
        double mindistance = Double.POSITIVE_INFINITY;
        double maxdistance = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length && i !=j ; j++) {
                double distance = 0;
                for (int k = 0; k < data[i].length; k++) {
                    distance += (data[i][k] - data[j][k])*(data[i][k] - data[j][k]);
                }
                mindistance = Math.min(mindistance, distance);
                maxdistance = Math.max(maxdistance, distance);
            }
        }
        if (gng==null) {
            gng = new AdaptiveGNG(1);
            gng.updateData(data);
            gng.prepareAlgo(gng.dimension);
            gng.lambdaGNG = data.length; // number of steps before inserting a new node
            gng.MAX_STEPSIZE = gng.stepSize = data.length; // number of steps;
            gng.alphaGNG = 0.5f; // split error between two nodes when insert new node
            gng.betaGNG = 5e-4f; // decay factor E = E*(1-Beta)
            gng.MAX_EDGE_AGE = data.length; // max age for edges in in GNG
            gng.utilityGNG = theta;
            gng.maxNodes = Math.min(500, data.length);
            gng.reset();
        } else {
            gng.updateData(data);
            gng.maxNodes = Math.min(500, data.length);
            for (int j = 0; j < gng.nNodes; j++) {
                gng.nodes[j].feature = pca.sampleToEigenSpace(gng.nodes[j].feature_original);
            }
        }

        gng.id_sig = 0;
        double preverror = 0;
        int countnoninprove = 5;
        for (int iteration = 0; iteration < maxiteration; iteration++) {
//            System.out.println(iteration);
//            System.out.println("number of node" + algorithm.nNodes);
            gng.learn();
            double error = gng.valueGraph/ data.length;
//            System.out.println(error);
            if (error <= mindistance+rel_tollerance*(maxdistance-mindistance)){//) { //toleraance error/range = 1e-5
                gng.noNewNodesGNGB = true;
//                System.out.println("No more Node!");
            } else
                gng.noNewNodesGNGB = false;
            if (Math.abs(error - preverror)/preverror < 0.0001) break;
            preverror = error;
        }
//        System.out.println("Number of nodes is " + algorithm.nNodes);
        if (false) {
            for (int i = 0; i < gng.nNodes; i++) {
                System.out.print("Node " + i + ":");
                for (int j = 0; j < gng.dimension; j++) {
                    System.out.print((gng.nodes[i].feature[j]) + ",");
                }
                System.out.println();
            }
        }

        Visualisation.ns = Visualisation.NODE_LABEL_STYLE.USERDDEFINED_LABEL;
        String[] node_annotations = new String[gng.nNodes];
        double[] node_values = new double[gng.nNodes];
        int[] node_nneighbors = new int[gng.nNodes];
        int[] node_density = new int[gng.nNodes];
        ArrayList<Integer>[] node_neighbors = new ArrayList[gng.nNodes];
        double[][] node_corr = new double[gng.nNodes][data[0].length];
        int[] node_maxprogsizes = new int[gng.nNodes];
        double[] node_avgprogsizes = new double[gng.nNodes];
        int[] node_sizes = new int[gng.nNodes];
        Arrays.fill(node_annotations, "");
        double[] mindis = new double[data.length];
        Arrays.fill(mindis, Double.POSITIVE_INFINITY);
        for (int j = 0; j < gng.nNodes; j++) {
            int bestmatch = -1;
            double md = Double.POSITIVE_INFINITY;
            for (int i = 0; i < data.length ; i++) {
//            for (int i = data.length-1; i >=0 ; i--) {
                double distance = 0;
                for (int k = 0; k < data[i].length; k++) {
                    distance += (data[i][k] - gng.nodes[j].feature[k])*(data[i][k] - gng.nodes[j].feature[k]);
                }
                if (md > distance) {
                    md = distance;
                    bestmatch = i;
                }
            }
            if (labels.get(bestmatch).length()<=3) {
                node_annotations[j] = labels.get(bestmatch);
            } else {
                node_annotations[j] = "#"+bestmatch;
            }
            if (!values.isEmpty() && !AGGREGATEFIT) {
                node_values[j] = values.get(bestmatch);
                node_values[j] = - node_values[j]; // just to change the node color (green for later generations)
            }
            node_corr[j] = data[bestmatch];
        }

        Visualisation.markbest = -1;

        for (int i = 0; i < data.length; i++) {
            int bestmatch = -1;
            double md = Double.POSITIVE_INFINITY;
            for (int j = 0; j < gng.nNodes; j++) {
                double distance = 0;
                for (int k = 0; k < data[i].length; k++) {
                    distance += (data[i][k] - gng.nodes[j].feature[k])*(data[i][k] - gng.nodes[j].feature[k]);
                }
                if (md > distance) {
                    md = distance;
                    bestmatch = j;
                }
            }
            if (i==bestloc) {
                Visualisation.markbest = bestmatch;
//                node_annotations[bestmatch] = "*";
            }
            if (AGGREGATEFIT) {
                node_values[bestmatch] += values.get(i);
                node_maxprogsizes[bestmatch] = Math.max(sizes.get(i), node_maxprogsizes[bestmatch]);
                node_avgprogsizes[bestmatch] +=  sizes.get(i);
            }
            node_sizes[bestmatch]++;
            node_nneighbors[bestmatch] = gng.nodes[bestmatch].getNNeighbors();
        }
        for (int i = 0; i < gng.nNodes; i++) {
            node_neighbors[i] = new ArrayList();
            for (int j = 0; j < gng.nodes[i].getNNeighbors(); j++) {
                node_neighbors[i].add(gng.nodes[i].getNeighbors()[j]);
            }
        }

        node_density = node_sizes.clone();
        if (AGGREGATEFIT) {
            for (int j = 0; j < gng.nNodes; j++) {
                if (node_sizes[j]!=0) {
                    node_values[j] = node_values[j] / node_sizes[j];
                    node_maxprogsizes[j] = node_maxprogsizes[j];
                    node_avgprogsizes[j] = node_avgprogsizes[j] / node_sizes[j];
                } else {
                    node_values[j] = Double.POSITIVE_INFINITY;
                    node_maxprogsizes[j] = 1;
                    node_avgprogsizes[j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        int minsize = Integer.MAX_VALUE;
        int maxsize = Integer.MIN_VALUE;
        for (int j = 0; j < gng.nNodes; j++) {
            minsize = Math.min(node_sizes[j], minsize);
            maxsize = Math.max(node_sizes[j], maxsize);
        }
        for (int j = 0; j < gng.nNodes; j++) {
            gng.nodes[j].preference = 1;
            double scale = (double)(node_sizes[j]-minsize)/(maxsize-minsize);
            node_sizes[j] = (int) (Visualisation.nodeSize*(1 + 3*scale));
//            if (!SHOWFIT) node_values[j] = node_maxprogsizes[j];
            gng.nodes[j].feature_original = pca.eigenToSampleSpace(gng.nodes[j].feature);
        }
//        Visualisation.addLabels(node_annotations);
//        if (!values.isEmpty()) Visualisation.addValues(node_values);

        if (Mapper.SHOW_GNG) {
            Visualisation.display = true;
        } else {
            Visualisation.display = false;
        }

        Visualisation.generateGraph(gng, node_annotations, node_values, node_avgprogsizes, node_sizes, node_corr, false, interactive, false, false, takestreenshot, outputname);
        System.out.println("Complete drawing GNG ...\n");
        int[] preferences = new int[gng.nNodes];
        for (int j = 0; j < gng.nNodes; j++) {
            preferences[j] = gng.nodes[j].preference;
            node_corr[j] = gng.nodes[j].feature;
        }

        gngmodel.case_features = node_corr;
        gngmodel.case_labels = node_values;
        gngmodel.case_lengths = node_maxprogsizes;
        gngmodel.case_nneighbors = node_nneighbors;
        gngmodel.case_neighbors = node_neighbors;
        gngmodel.case_density = node_density;
        gngmodel.case_preferences = preferences;
        rel_tollerance *= 0.01;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
