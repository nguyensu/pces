/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pes.utilites;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * A discriptive statistics class
 */
public class SmallStatistic {
    NumberFormat formatter = new DecimalFormat("#0.00000");
    //track experiment
    public int f;
    public int p;
    public int r;
    public int seed;
    //end track
    private double sum = 0;
    private double sumQ = 0;
    private double max = Double.NEGATIVE_INFINITY;
    private double min = Double.POSITIVE_INFINITY;
    private double count = 0;

    /**
     * Constructor for statistic object
     */
    public SmallStatistic() {

    }

    /**
     * reset all statistics
     */
    public void reset() {
        sum = 0;
        sumQ = 0;
        max = Double.NEGATIVE_INFINITY;
        min = Double.POSITIVE_INFINITY;
        count = 0;
    }

    /**
     * add a new value
     * @param val
     */
    public void add(double val) {
        sum += val;
        sumQ += val * val;
        count++;
        if (val < min) min = val;
        if (val > max) max = val;
    }

    /**
     * get the sample size
     * @return
     */
    public double getLength() {
        return count;
    }

    /**
     * get mean/average
     * @return
     */
    public double getAverage() {
        return sum / count;
    }

    /**
     * get mean square values
     * @return
     */
    public double getMeanSquare() {
        return sumQ / count;
    }

    /**
     * get variance
     * @return
     */
    public double getVariance() {
        return sumQ / count - Math.pow(sum / count, 2);
    }

    /**
     * get unbiased variance
     * @return
     */
    public double getUnBiasedVariance() {
        return getVariance() * count / (count - 1);
    }

    /**
     * get unbiased standard deviation
     * @return
     */
    public double getUnbiasedStandardDeviation() {
        return Math.sqrt(getUnBiasedVariance());
    }

    /**
     * get coefficient of variance
     * @return
     */
    public double getCV() {
        return getUnbiasedStandardDeviation() / getAverage();
    }

    /**
     * get minimum
     * @return
     */
    public double getMin() {
        return min;
    }

    /**
     * get maximum
     * @return
     */
    public double getMax() {
        return max;
    }

    /**
     * get descriptive statistics in Latex format
     * @return
     */
    public String getLatexQuickStat() {
        String stat = "";
        stat = "$" + formatter.format(getAverage()) + " \\pm " + formatter.format(getUnbiasedStandardDeviation()) + "$";
        return stat;
    }

    /**
     * get range
     * @return
     */
    public double range() {
        return max - min;
    }

    /**
     * get
     * @return
     */
    public String toString() {
        return formatter.format(getAverage()) + " (" + formatter.format(getUnbiasedStandardDeviation()) + ") - [" + formatter.format(min) + ", " + formatter.format(max) + "]; Count = " + count;
    }
}
