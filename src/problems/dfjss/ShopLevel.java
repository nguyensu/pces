/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package problems.dfjss;


import cern.jet.random.AbstractDistribution;
import cern.jet.random.Gamma;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import cern.jet.random.sampling.RandomSampler;
import pes.utilites.SmallStatistic;

import java.util.ArrayList;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class ShopLevel {
    public enum ShopType {PJS,GFS,HJS};
    private double hjsTheta = 0.3;
    private double[] weightDistribution = {0.2,0.6,0.2};
    private int[] weightValues = {1,2,4};
    //simulation distributions
    private AbstractDistribution orderWeightDistribution;
    private RandomEngine engineJob;    
    private AbstractDistribution distNumberJob;
    private AbstractDistribution distJobLength;    
    private AbstractDistribution distFlex;
    private AbstractDistribution distProcessingTime;
    private AbstractDistribution distBottleneckProcessingTime;
    private AbstractDistribution orderMachine;    
    //shop parameter
    private ShopType st;
    private int id = -1;
    private Workcenter[] wcs; //work center in this level
    private int nwc = -1;
    private int flexibility = -1;
    private double meanOperationTime = 1;
    private double lowerbound, upperbound;
    private int maxjobs = 6;
    private boolean discreteProcessingTime;
    private boolean revisit;
    private boolean equalWeightProbability;
    private double arrivalRate;

    //shop statistics
    private ArrayList<Job> JobinShop = new ArrayList<Job>(); //All job in this shop level
    public SmallStatistic process = new SmallStatistic();
    //
    public ShopLevel(ShopType s,int shopid, int numwc, int[] nmc, int flex, double meanTime,int lowerBound, int upperBound, int maxj, boolean rev){

        st = s;
        id = shopid;
        nwc = numwc;
        flexibility = flex;
        maxjobs = maxj;
        wcs = new Workcenter[nwc];
        for (int i = 0; i < wcs.length; i++) {
            wcs[i] = new Workcenter(nmc[i], this);
        }
        lowerbound = lowerBound;
        upperbound = upperBound;
        meanOperationTime = meanTime;
        revisit = rev;
    }
    public ArrayList<Job> jobInShop() {
        return JobinShop;
    }

    public double urgency() {
        if (JobinShop.isEmpty()) return 0;
        double urgency = 0;
        for (Job job: JobinShop){
            urgency += job.duedate() - (FlexibleJobShop.now() + job.remainingTime());
        }
        return urgency;
    }

    public double total_workload() {
        if (JobinShop.isEmpty()) return 0;
        double rworkload = 0;
        for (Job job: JobinShop){
            rworkload += job.remainingTime();
        }
        return rworkload;
    }

    public double total_op() {
        if (JobinShop.isEmpty()) return 0;
        double opjob = 0;
        for (Job job: JobinShop){
            opjob += job.remainingOperation();
        }
        return opjob;
    }

    public double getOrderArrivalRate() {
        return arrivalRate/(0.5*(maxjobs+1));
    }
    public double randomOrderWeight() {
        return weightValues[(int)(orderWeightDistribution.nextDouble()*weightValues.length)];
    }
    public AbstractDistribution setup(RandomEngine rnd,  double util, double botUtil, String prDis, double param, double arrRatePrevShop){
        if (FlexibleJobShop.diversity<1.0) {
            param = (int) (param + (1- FlexibleJobShop.diversity)*(meanOperationTime-param));
        }
        AbstractDistribution interArrivalTime;
        // your favourite distribution goes here
        engineJob = rnd;
        // job length distribution
        distJobLength = new Uniform(lowerbound,upperbound,engineJob);
        distFlex = new Uniform(1,flexibility,engineJob);
        distNumberJob = new Uniform(1,maxjobs,engineJob);
        // arrival process distribution
        arrivalRate = getArrivalRate(util,nwc,lowerbound,upperbound,meanOperationTime);
        double MarrivalRate = arrivalRate*0.5*(upperbound+lowerbound)/nwc;
        if (id == 1) {
            MarrivalRate = arrRatePrevShop*0.5*(upperbound+lowerbound)/nwc;
        }
        interArrivalTime = new Gamma(1,arrivalRate/(0.5*(maxjobs+1)),engineJob);
        // processing time distribution
        if ("erlang".equals(prDis)) {
            distProcessingTime = new Gamma(param,MarrivalRate*param/util,engineJob);
            distBottleneckProcessingTime = new Gamma(param,MarrivalRate*param/botUtil,engineJob);
        } //mean = p/p2
        else if ("uniform".equals(prDis)) {
            distProcessingTime = new Uniform(param,2*meanOperationTime-param,engineJob);
            distBottleneckProcessingTime = new Uniform(param,2*meanOperationTime-param,engineJob); //2*meanOperationTime-p,
        } else if ("duniform".equals(prDis)) {
            distProcessingTime = new Uniform(param,2*meanOperationTime-param,engineJob);
            distBottleneckProcessingTime = new Uniform(param,2*meanOperationTime-param,engineJob); //2*meanOperationTime-p,
            discreteProcessingTime = true;
        }//mean = 0.5*(p+p2)
        else {
            distProcessingTime = new Gamma(1,MarrivalRate/util,engineJob);
            distBottleneckProcessingTime = new Gamma(1,MarrivalRate/botUtil,engineJob);
        }
        if (revisit){
            orderMachine = new Uniform(0,nwc-1,engineJob);
        }
        orderWeightDistribution = new cern.jet.random.Empirical(weightDistribution, cern.jet.random.Empirical.NO_INTERPOLATION, engineJob);
        return interArrivalTime;
    }
    double getArrivalRate(double utilisation, double N, double l, double u, double mu){
        //mu is processing rate, N is the number of work centers in the shop, l and u
        //are the lower/upper bounds of number of operations
        double rate = utilisation*N/(mu*0.5*(u+l));
        return rate;
    }
    public Job generateJob(double now){
        int[][] randomRoute = new int[distJobLength.nextInt()][];
        double[][] randomProcessingTime = new double[randomRoute.length][];
        //generate random flexibility
        for (int i = 0; i < randomRoute.length; i++) {
            randomRoute[i] = new int[distFlex.nextInt()];
            randomProcessingTime[i] = new double[randomRoute[i].length];
        }
        if (flexibility==1) {
            long[] rR = new long[randomRoute.length];
            //generate random basic route
            //SRSWOR algorithm ~ O(n)
            if (st == ShopType.PJS) {
                if (!revisit){
                    RandomSampler.sample(randomRoute.length, nwc, randomRoute.length, 0, rR, 0, engineJob);
                    shuffleArray(rR,engineJob);
                } else {
                    rR[0] = orderMachine.nextInt();
                    for (int i = 1; i < randomRoute.length; i++) {
                        do{
                            rR[i] = orderMachine.nextInt();
                        } while(rR[i]==rR[i-1]);
                    }
                }
            } else if (st == ShopType.GFS){
                RandomSampler.sample(randomRoute.length, nwc, randomRoute.length, 0, rR, 0, engineJob);
            } else if (st == ShopType.HJS){
                RandomSampler.sample(randomRoute.length, nwc, randomRoute.length, 0, rR, 0, engineJob);
                shuffleArrayPartial(rR,engineJob,hjsTheta);
            }
            for (int i = 0; i < randomRoute.length; i++) {
                randomRoute[i][0] = (int) rR[i];
            }
        } else {
            // random route with flexibility > 1
            for (int i = 0; i < randomRoute.length; i++) {
                long[] r = new long[randomRoute[i].length];
                RandomSampler.sample(r.length, nwc, r.length, 0, r, 0, engineJob);
                shuffleArray(r,engineJob);
                for (int j = 0; j < r.length; j++) {
                    randomRoute[i][j] = (int) r[j];
                }
            }
        }
        //generate random processing time (and adding alternative rounting)
        for (int i = 0; i < randomRoute.length; i++) {
            for (int j = 0; j < randomRoute[i].length; j++) {
                double mratio = (double)wcs[(int)randomRoute[i][j]].nmc();
                if (!discreteProcessingTime){
                    if ((int)randomRoute[i][j]!=0) {
                        randomProcessingTime[i][j] = mratio*distProcessingTime.nextDouble();
                    }
                    else {
                        randomProcessingTime[i][j] = mratio*distBottleneckProcessingTime.nextDouble();
                    }
                } else {
                    if ((int)randomRoute[i][j]!=0) {
                        randomProcessingTime[i][j] = mratio*distProcessingTime.nextInt();
                    }
                    else {
                        randomProcessingTime[i][j] = mratio*distBottleneckProcessingTime.nextInt();
                    }
                }
                process.add(randomProcessingTime[i][j]);
            }
        }
        double weight = 1;
        if (FlexibleJobShop.withWeight) weight = randomOrderWeight();
        Job newjob = new Job(this, randomRoute,randomProcessingTime, now, weight);
        return newjob;
    }
    // print the utilisation of each workcenter in the shops
    public String printShopStatistic(){
        String stat = "Shop ID " + id + "\nUtilisation \n";
        for (int i = 0; i < wcs.length; i++) {
            stat += "WC #"+ i + ":\t" + wcs[i].Utilisation()+ "\n";
        }
        return stat;
    }
    /*
     * shuffle numbers in an array (Knuth algorithm) ~ O(n)
     */
    private void shuffleArray(long[] randomRoute,RandomEngine engine) {
        Uniform.staticSetRandomEngine(engine);
        for (int j = 0; j < randomRoute.length-1; j++) {
            int r = Uniform.staticNextIntFromTo(j, randomRoute.length-1);
            long temp = randomRoute[r];
            randomRoute[r] = randomRoute[j];
            randomRoute[j] = temp;
        }
    }
    private void shuffleArrayPartial(long[] randomRoute,RandomEngine engine, double theta) {
        Uniform.staticSetRandomEngine(engine);
        for (int j = 0; j < randomRoute.length-1; j++) {
            if (engine.nextDouble() < theta) {
                int r = Uniform.staticNextIntFromTo(j, randomRoute.length-1);
                long temp = randomRoute[r];
                randomRoute[r] = randomRoute[j];
                randomRoute[j] = temp;
            }
        }
    }
    public int getNJob(){
        return JobinShop.size();
    }
    public int id(){
        return id;
    }
    public Workcenter[] wcs() {
        return wcs;
    }
}
