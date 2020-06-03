/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package problems.dfjss;
import pes.utilites.SmallStatistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class Scheduling {
    private boolean ensemble = false;
    private int ensembleSize = 1;
    private int index = -1;
    private double[] weight;
    private double[] priority;
    public Scheduling(){
        
    }
    public void preprocessing(ArrayList<Job>  jobs) {}
    public double prioritise(Job job){
        return - job.duedate();
    }
    public void nextroute(Job job, ShopLevel shop) {
        int select_mode = -1;
        double max_wc_priority = Double.NEGATIVE_INFINITY;
        Operation op = job.current_operation();
        for (int i = 0; i < op.nMode(); i++) {
            double temp_priority = -shop.wcs()[op.getWC(i)].workload();
            if (temp_priority > max_wc_priority) {
                max_wc_priority = temp_priority;
                select_mode = i;
            }
        }
        job.set_route(select_mode);
    }
    public double calPriority(Job job){ return 0;}
    public int index() {
        return index;
    }
    public int ensemblesize() {
        return ensembleSize;
    }
    public void setEnsembleMode() {
        ensemble = true;
    }
    public boolean isEnsemble() {
        return ensemble;
    }
    public void assignEnsembleSize(int n) {
        ensembleSize = n;
        weight = new double[n]; Arrays.fill(weight, 1);
    }
    public void assignIndex(int in) {
        index = in;
    }
    public void assignWeight(double[] w){
        weight = w;
    }
    public double weight () {
        return weight[index];
    }
    public void reset() {
    }
    public int end_episode(ShopLevel shop, SmallStatistic currentobj, List<Double> ia, List<Double> id) {
        return 1;
    }
}

