package pes.tda.mapping;

import edu.princeton.cs.algs4.SET;

import java.util.ArrayList;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is a simple class representing a mapper model used to estimate the fitness of evolved program based on their
 * charateristics (e.g. phenotypic behaviours) and the mapper information (i.e. adaptive growing neural gas).
 */

public class MapperModel {
    public PrincipalComponentAnalysis pca;
    public double[] case_labels;
    public double[][] case_features;
    public int[] case_density;
    public int[] case_nneighbors;
    public int[] case_lengths;
    public int[] case_preferences;
    public int[] case_nselect;
    public ArrayList<Integer>[] case_neighbors;
    public double total_denst = 0;
    public double total_nselect = 0;
    SET<String> vectorset;
    public static double neighbors_size = 0.01;
    public static int bloat_control = 8;
    public void resetPool() {
        total_denst = 0;
        vectorset = new SET<String>();
        case_nselect = new int[case_labels.length];
        for (int i = 0; i < case_density.length; i++) {
            total_denst += case_density[i];
            case_nselect[i] = 0;
        }
        total_nselect = case_labels.length;
    }

    public double density_gng(double[] feature_vec) {
        double[] trans_data = pca.sampleToEigenSpace(feature_vec);
        int bestmatch = 0;
        double mindist = distance(trans_data, case_features[0]);
        for (int i = 1; i < case_features.length; i++) {
            double distance = distance(trans_data, case_features[i]);
            if (mindist > distance) {
                mindist = distance;
                bestmatch = i;
            }
        }
        case_nselect[bestmatch] += 1;
        int neighbor_density = case_density[bestmatch];
        double maxprogsize = case_lengths[bestmatch];

        for (int i = 0; i < case_nneighbors[bestmatch]; i++) {
            neighbor_density += case_density[case_neighbors[bestmatch].get(i)];
        }
        double select_pressure = 1;
        select_pressure = 1 + Math.pow(case_density[bestmatch]/total_denst, neighbors_size);

        return select_pressure;
    }

    public double surrogate_gng(double[] feature_vec) {
        double[] trans_data = pca.sampleToEigenSpace(feature_vec);
        int bestmatch = 0;
        double mindist = distance(trans_data, case_features[0]);
        for (int i = 1; i < case_features.length; i++) {
            double distance = distance(trans_data, case_features[i]);
            if (mindist > distance) {
                mindist = distance;
                bestmatch = i;
            }
        }
        return case_labels[bestmatch];
    }

    public double control_factor(double[] feature_vec, int size) {
        double[] trans_data = pca.sampleToEigenSpace(feature_vec);
        int bestmatch = 0;
        double mindist = distance(trans_data, case_features[0]);
        for (int i = 1; i < case_features.length; i++) {
            double distance = distance(trans_data, case_features[i]);
            if (mindist > distance) {
                mindist = distance;
                bestmatch = i;
            }
        }
//        if (mindist == 0) {
//            return Double.POSITIVE_INFINITY;
//        }

        int neighbor_density = case_density[bestmatch];
        double maxprogsize = case_lengths[bestmatch];
        for (int i = 0; i < case_nneighbors[bestmatch]; i++) {
            neighbor_density += case_density[case_neighbors[bestmatch].get(i)];
        }
        double denst = 1;
        if (neighbor_density/total_denst > neighbors_size) {
            denst = (neighbor_density/total_denst)/neighbors_size;
        }
        double bloat = 1;
        if (size > maxprogsize) {
            bloat = 1 + Math.pow(Math.min(size/maxprogsize,2)-1, bloat_control);
//            System.out.println(denst + " <-- " + neighbor_density + "/" + total_denst );
        }
        return denst*bloat/(1.0 + (case_preferences[bestmatch]-1)/4.0);
    }

    public double control_factor(ArrayList<Integer> feature_vec, int size) {
        String vectorStr = convert(feature_vec);
//        if (vectorset.contains(vectorStr))
//            return Double.POSITIVE_INFINITY;
//        else
//            vectorset.add(vectorStr);
        double[] data = new double[feature_vec.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = feature_vec.get(i);
        }
        return control_factor(data, size);
    }

    public double predict(double[] feature_vec) {
        double[] trans_data = pca.sampleToEigenSpace(feature_vec);
        int bestmatch = 0;
        double mindist = distance(trans_data, case_features[0]);
        for (int i = 1; i < case_features.length; i++) {
            double distance = distance(trans_data, case_features[i]);
            if (mindist > distance) {
                mindist = distance;
                bestmatch = i;
            }
        }
        if (mindist == 0) {
            return Double.POSITIVE_INFINITY;
        }
//        if (case_density[bestmatch] > 10) {
//            return Double.POSITIVE_INFINITY;
//        }

        return case_labels[bestmatch];
    }

    public double predict(ArrayList<Integer> feature_vec) {
        String vectorStr = convert(feature_vec);
//        if (vectorset.contains(vectorStr))
//            return Double.POSITIVE_INFINITY;
//        else
//            vectorset.add(vectorStr);
        double[] data = new double[feature_vec.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = feature_vec.get(i);
        }
        return predict(data);
    }

    public double distance(double[] a, double[] b) {
        if (a.length!=b.length) {
            System.err.println("dimensions of decision vectors are not matched!!!");
            System.exit(0);
        }
        double distance = 0;
        for (int i = 0; i < a.length; i++) {
            distance += (a[i] - b[i])*(a[i] - b[i]);
        }
        return Math.sqrt(distance);
    }

    String convert(ArrayList<Integer> list) {
        String x = "";
        for (int i = 0; i < list.size(); i++) {
            x += list.get(i);
        }
        return x;
    }
}
