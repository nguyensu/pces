/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package problems.dfjss;

import edu.princeton.cs.algs4.IndexMaxPQ;

import java.util.ArrayList;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class Workcenter {
    private static Scheduling scheduler;
    private ShopLevel shop;
    private Machine[] mcs; //parallel in this workcenter (have the same capacity)
    private ArrayList<Job> queue = new ArrayList<Job>(); //queue of waiting jobs
    private int nmc = -1; //number of machine
    private double workload = 0;
    public double[] minatr = new double[9];
    public double[] maxatr = new double[9];
    public static Scheduling getScheduler() {
        return scheduler;
    }
    public static void setScheduler(Scheduling sched) {
        scheduler = sched;
    }
    public Workcenter(int n, ShopLevel sh){
        nmc = n;
        shop = sh;
        mcs = new Machine[nmc];
        for (int i = 0; i < mcs.length; i++) {
            mcs[i] = new Machine(this, i);
        }
    }
    public void deduceWorkload(double pr) {
        workload -= pr;
    }
    public void addWorkload(double pr) {
        workload += pr;
    }
    public double workload(){
        return workload;
    }

    public double workload_ratio_morethan_processingtime(double pr){
        if (queue.isEmpty()) return 0;
        double rworkload = 0;
        for (Job job: queue) {
            if (job.currentProcesstime() > pr) rworkload += job.currentProcesstime();
         }
        return rworkload/workload;
    }

    public double workload_ratio_more_urgent(double slack){
        if (queue.isEmpty()) return 0;
        double rworkload = 0;
        for (Job job: queue) {
            double jslack = job.duedate() - (FlexibleJobShop.now() + job.remainingTime_currentmode());
            if (jslack < slack) rworkload += job.currentProcesstime();
        }
        return rworkload/workload;
    }

    public void joinQueue(Job job) {
        queue.add(job);
        workload += job.currentProcesstime();
    }
    public boolean availjob() {
        if (queue.isEmpty()) return false;
        else {
            for (Job j : queue) {
                if (j.ready()) {
                    j.resetRank();
                    return true;
                }
            }
        }
        return false;
    }

    public Job dispatchNextJob(){
        Job nextjob = null;
        if (queue.size()==1&&queue.get(0).ready()) {
            nextjob = queue.get(0);
            queue.remove(nextjob);
            return nextjob;
        }
        if (scheduler.isEnsemble()) {
            return ensembleDispatch();
        }
        double highestprio = Double.NEGATIVE_INFINITY;
        scheduler.preprocessing(queue);
        IndexMinPQ<Double> rank = new IndexMinPQ<Double>(queue.size());

        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).ready()) {
                double p = scheduler.prioritise(queue.get(i));
                rank.insert(i,-p);
                if (highestprio < p) {
                    highestprio = p;
                    nextjob = queue.get(i);
                }
            }
        }
        if (nextjob==null) {
            System.out.println();
            scheduler.prioritise(queue.get(0));
            System.out.println();
        }
        queue.remove(nextjob);
        return nextjob;
    }

    public Job ensembleDispatch() {
        for (int k = 0; k < scheduler.ensemblesize(); k++) {
            scheduler.assignIndex(k);
            IndexMaxPQ<Double> sorted = new IndexMaxPQ<Double>(queue.size());
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).ready()) {
                    if (k == 0) queue.get(i).resetRank();
                    double p = scheduler.prioritise(queue.get(i));
                    sorted.insert(i, p);
                }
            }

            int rank = sorted.size();
            while (!sorted.isEmpty()) {
                queue.get(sorted.delMax()).accummulateRank(scheduler.weight()*rank);
                rank --;
            }
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        Job nextjob = null;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).ready()) {
                double r = queue.get(i).rank();
                if (bestScore < r) {
                    bestScore = r;
                    nextjob = queue.get(i);
                }
            }
        }
        queue.remove(nextjob);
        return nextjob;
    }

    public int hasAvaliableMachine(){ //return -1 if there are no avaliable machine
        for (Machine m: mcs){
            if (!m.isBusy()) {
                return m.ID();
            }
        }
        return -1;
    }

    public double Utilisation(){
        double uti = 0;
        for (int i = 0; i < mcs.length; i++) {
            uti += mcs[i].calUtilisation();
        }
        return uti/(double)mcs.length;
    }
    public int nmc() {
        return nmc;
    }
    public Machine[] mcs() {
        return mcs;
    }
    public double jobinqueue (){
        return queue.size();
    }

    public double minProcessingTime() {
        double min = Double.POSITIVE_INFINITY;
        for (Job job: queue) {
            min = Math.min(min,job.currentProcesstime());
        }
        return min;
    }

    public double maxProcessingTime() {
        double max = Double.NEGATIVE_INFINITY;
        for (Job job: queue) {
            max = Math.max(max,job.currentProcesstime());
        }
        return max;
    }

    public double minDueDate() {
        double min = Double.POSITIVE_INFINITY;
        for (Job job: queue) {
            min = Math.min(min,job.duedate());
        }
        return min;
    }

    public double maxDueDate() {
        double max = Double.NEGATIVE_INFINITY;
        for (Job job: queue) {
            max = Math.max(max,job.duedate());
        }
        return max;
    }

    public double maxWeight() {
        double max = Double.NEGATIVE_INFINITY;
        for (Job job: queue) {
            max = Math.max(max,job.weight());
        }
        return max;
    }

}
