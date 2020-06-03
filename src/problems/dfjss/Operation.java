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
public class Operation {
    private double[] processingTime = null; //operation processing time
    private int[] wcID = null; //work center that can process this operation
    private double start = -1; //start time of this operation
    private double finish = -1; //finish time of this operation
    private boolean ready = false;
    private double odd = -1;

    public Operation(double[] pr, int[] wcid, boolean red){
        processingTime = pr;
        wcID = wcid;
        ready = red;
    }
    public void setODD(double t){
        odd = t;
    }
    public void setStart(double t){
        start = t;
    }
    public void setFinish(double t){
        finish = t;
    }
    public double getStart(){
        return start;
    }
    public double getFinish(){
        return finish;
    }
    public double getODD(){
        return odd;
    }
    public int getNFlexOptions() {
        return processingTime.length;             
    }
    public int getWC(int index){
        return wcID[index];
    }
    public double getProcessingTime(int index){
        return processingTime[index];
    }
    public boolean isReady() {
        return ready;
    }
    public void release() {
        ready = true;
    }
    public double averagePrtime() {
        double avg = 0;
        for (int i = 0; i < processingTime.length; i++) {
            avg += processingTime[i];
        }
        return avg / (double) processingTime.length;
    }
    public double nMode() {
        return processingTime.length;
    }
}
