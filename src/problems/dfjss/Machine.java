/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package problems.dfjss;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class Machine {
    private int id;
    private boolean isbusy = false; //state (busy/idle) of this machine
    private Workcenter wc = null; //work center which this machine belongs to
    private Job jobinprocess; //current job processed by this machine
    private double idleTime = 0;
    private double ready = 0;
    public Machine(Workcenter w, int ID){
        wc = w;
        id = ID;
    }
    public Workcenter getWC(){
        return wc;
    }
    public void occupy(Job job){
        if (FlexibleJobShop.throughput()> FlexibleJobShop.warmnum()) {
            idleTime += FlexibleJobShop.now() - ready;
        }
        isbusy = true;
        jobinprocess = job;
    }
    public Job inprocess() {
        return jobinprocess;
    }
    public void idle(){
        ready = FlexibleJobShop.now();
        isbusy = false;
        jobinprocess = null;
    }
    public boolean isBusy(){
        return isbusy;
    }
    public int ID(){
        return id;
    }
    public double calUtilisation(){
        return 1 - idleTime/(FlexibleJobShop.now()- FlexibleJobShop.warmtime());
    }
}
