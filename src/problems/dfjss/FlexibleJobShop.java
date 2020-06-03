
package problems.dfjss;

/**
 * This is the discrete-event simulator for flexible job shop scheduling. The details of this simulator is discuss in the following papers:
 * 1. Su Nguyen, Mengjie Zhang, Damminda Alahakoon, Kay Chen Tan. “Visualising the Evolution of Computer Programs for Production Scheduling”, 2018, IEEE Computational Intelligence Magazine.
 * 2. Su Nguyen, Mengjie Zhang, Kay Chen Tan. “Adaptive Charting Genetic Programming for Dynamic Flexible Job Shop Scheduling”, 2018, GECCO’2018.
 *
 * Different shop configurations can be examined with this simulator. Users can freely change the number of work centres, the number of parallel machines for each work centre, shop utilisation, and due date assignment rules.
 *
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */

import cern.jet.random.AbstractDistribution;
import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import edu.princeton.cs.algs4.MinPQ;
import pes.utilites.Miscellaneous;
import pes.utilites.SmallStatistic;
import java.util.ArrayList;
import java.util.List;

public class FlexibleJobShop {
    //simulation parameter
    private static double simtime = 0; //simulation time
    private static double warmtime = 0;
    private static int warmnum = 0;
    public static double simetimeref = Double.POSITIVE_INFINITY;
    public static boolean withWeight = false;
    private MinPQ<Event> eventlist = new MinPQ<Event>(); //event list
    //random engines/generator
    private RandomEngine engineJob = new MersenneTwister(1);
    private AbstractDistribution interArrivalTime = new Gamma(1, 0.9, engineJob);
    //shop parameters
    private int nLevel = -1;
    public static double diversity = 1;
    private double allowance = 4;
    private ShopLevel slv;
    private ArrayList<Job> JobinShop = new ArrayList<Job>(); //All orders in the shop
    private int countID = 0;
    static final int // define events
            ARRIVE = 0,
            VISIT = 1,
            PROCESS = 2,
            FINISH = 3,
            PERIODPLAN = 4;
    // shop statistics
    private static int throughput = 0;
    private static double workload_balance = 1;
    public double counttardy = 0;
    public SmallStatistic leadtime = new SmallStatistic();
    public SmallStatistic tardy = new SmallStatistic();
    public SmallStatistic wtardy = new SmallStatistic();
    // data collection
    public ArrayList attributes;
    public ArrayList<Double> outputs;
    public static ArrayList recordlist;
    public static boolean havePeriodicReview = false;
    public static double period = 10;
    public static String objective;
    public static int initJob = 0;
    public int maxIDtoRecord = Integer.MAX_VALUE;
    public boolean termination = false;
    public static double test_window = -1; //then number of arrival jobs --> time to collect data and measure performance of rules (apply to only online learning)
    public SmallStatistic obj_record;
    public List<Double> interArrival;
    public List<Double> interDeparture;
    public double lastArrival = 0;
    public double lastDeparture = 0;
    public static boolean unstable = false;

    public static void main(String[] args) {
        FlexibleJobShop.recordlist = new ArrayList();
        objective = "twt";
        String[] druleset = {"FIFO", "EDD", "SPT", "WSPT", "WATC", "SJ"};
        String[] rruleset = {"LWT"};
        double[] utiset = {0.85, 0.95}; // shop utilisation
        double[] flexset = {1, 3, 5, 7, 9}; // shop flexibility

        for (double testu : utiset) {
            for (double testf : flexset) {
                for (String dr : druleset) {
                    for (String rr : rruleset) {
                        int nlevel = 1;
                        ShopLevel.ShopType[] LV1 = {ShopLevel.ShopType.PJS};
                        int[] nwc = {10};
                        int[][] nmc = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}}; // number of machines for each work centre
                        double[] lowUti = {testu};
                        double[] highUti = {testu};
                        String[] processdist = {"erlang"}; // processing time distribution
                        double[] processdist_param = {1}; // processubg time parameters
                        int[] flex = {(int) testf};
                        double[] meantime = {1};
                        int[] lowop = {1}; // minimum number of operations
                        int[] upop = {10}; // maximum number of operations
                        double allowance = 2;
                        int slength = 5000; // simulation length (number of completed jobs)
                        int wlength = 500; // simulation warm-up length (number of arriving jobs)
                        SchedulingTest scheduler = new SchedulingTest(rr, dr);
                        // default_setup the flexble job shop environments
                        ShopParameter shopparam = new ShopParameter(scheduler, nlevel, LV1, nwc, nmc, lowUti, highUti, processdist, processdist_param, flex, meantime, lowop, upop, true, allowance);
                        SmallStatistic twt = new SmallStatistic(); // total weighted tardiness as the scheduling objective
                        FlexibleJobShop.withWeight = true;

                        for (int i = 0; i < 50; i++) {
                            FlexibleJobShop shop = new FlexibleJobShop(shopparam, slength, wlength, Miscellaneous.SimSeed[i]);
                            twt.add(shop.getObjective("twt"));
                            System.out.println(rr + "," + dr + "," + testu + "," + testf + "," + i + "," + shop.getObjective("twt"));
                        }
                    }
                }
            }
        }
        System.out.println("===========================================");
    }

    /**
     * Constructor
     * @param shopconfig
     * @param simelength
     * @param warmup
     * @param seed
     */
    public FlexibleJobShop(ShopParameter shopconfig, int simelength, int warmup, int seed) {
        unstable = false;
        countID = 0;
        Job.countID = 0;
        maxIDtoRecord -= warmup;
        engineJob = new MersenneTwister(seed);
        if (initJob > 0) maxIDtoRecord = simelength;
        simtime = 0;
        throughput = 0;
        warmnum = warmup;
        warmtime = -1;
        allowance = shopconfig.allowance;
        attributes = new ArrayList();
        outputs = new ArrayList<Double>();
        setupShop(shopconfig);
        eventlist = new MinPQ<Event>(); //event list
        for (int i = 0; i < initJob; i++) {
            generateJob();
        }
        eventlist.insert(new Event<Job>(ARRIVE, 0, null));
        if (havePeriodicReview) eventlist.insert(new Event<Job>(PERIODPLAN, period, null));
        while (!eventlist.isEmpty() && throughput < simelength && !termination) {
            kick(eventlist.delMin());
        }
    }

    // build your job shops :)
    private void setupShop(ShopParameter shopconfig) {
        Workcenter.setScheduler(shopconfig.scheduler);
        Job.setScheduler(shopconfig.scheduler);
        nLevel = shopconfig.nlevel;
        for (int i = 0; i < 1; i++) {
            slv = new ShopLevel(shopconfig.LV1[i], i, shopconfig.nwc[i], shopconfig.nmc[i], shopconfig.flex[i], shopconfig.meantime[i], shopconfig.lowop[i], shopconfig.upop[i], 1, shopconfig.revisit);
            interArrivalTime = slv.setup(engineJob, shopconfig.lowUti[i], shopconfig.highUti[i], shopconfig.processdist[i], shopconfig.processdist_param[i], -1);
        }
    }

    private void generateJob() {
        countID++;
        Job job = slv.generateJob(simtime);
        job.setduedate(allowance);
        JobinShop.add(job);
        eventlist.insert(new Event<Job>(VISIT, simtime, job));
    }

    private void kick(Event<Job> e) {
        simtime = e.time();
        switch (e.ID()) {
            case ARRIVE:
                arrive(e.getEntity());
                break;
            case VISIT:
                visit(e.getEntity());
                break;
            case PROCESS:
                process(e.getEntity());
                break;
            case FINISH:
                finish(e.getEntity());
                break;
            case PERIODPLAN:
                periodplan(e.getEntity());
                break;
        }
    }

    void periodplan(Job job) {
        /**
         * add some periodic decisions if needed
         */
        eventlist.insert(new Event<Job>(PERIODPLAN, simtime + period, null));
    }

    void arrive(Job job) {
        if (simtime > 1.5 * simetimeref || (JobinShop.size() > 500)) {
            unstable = true;
            termination = true;
        }
        generateJob();
        eventlist.insert(new Event<Job>(ARRIVE, simtime + interArrivalTime.nextDouble(), null));
    }

    void visit(Job job) {
        Job.getScheduler().nextroute(job, slv);
        Workcenter wc = job.route();
        int isAvail = wc.hasAvaliableMachine();
        if (isAvail != -1) {
            job.addworkloadtowc();
            job.assignMachine(wc.mcs()[isAvail]);
            eventlist.insert(new Event<Job>(PROCESS, simtime, job));
        } else {
            wc.joinQueue(job);
        }
    }

    void process(Job job) {
        double processTime = job.startOperation();
        eventlist.insert(new Event<Job>(FINISH, simtime + processTime, job));
    }

    void check_workload_balance() {
        double minwl = Double.POSITIVE_INFINITY;
        double maxwl = Double.NEGATIVE_INFINITY;
        double avg = 0;
        for (int i = 0; i < slv.wcs().length; i++) {
            minwl = Math.min(minwl, slv.wcs()[i].workload());
            maxwl = Math.max(maxwl, slv.wcs()[i].workload());
            avg += slv.wcs()[i].workload();
//            System.out.println("wc" + i + "=" + slv.wcs()[i].workload());
        }
        avg /= (double) slv.wcs().length;
        if (minwl == maxwl) workload_balance = 1;
        else workload_balance = avg / maxwl;
    }

    public static double workload_balance() {
        return workload_balance;
    }

    void finish(Job job) {
        job.endOperation();
        if (job.wc().availjob()) {
            check_workload_balance();
            Job nextjob = job.wc().dispatchNextJob();
            nextjob.assignMachine(job.mc());
            job.mc().occupy(nextjob);
            eventlist.insert(new Event<Job>(PROCESS, simtime, nextjob));
        }
        if (job.isDone()) {
            job.shop().jobInShop().remove(job);
            // order has been completed, start to collect data
            if (job.shop().id() == 0) {
                if (countID > warmnum && job.id() < maxIDtoRecord + warmnum) {
                    throughput++;
                    leadtime.add(job.leadtime());
                    double tardiness = job.tardy();
                    double wtardiness = job.weight() * tardiness;
                    if (test_window > 0) {
                        obj_record.add(wtardiness);
                        interDeparture.add(simtime - lastDeparture);
                        lastDeparture = simtime;
                    }
                    if (tardiness > 0) counttardy++;
                    tardy.add(tardiness);
                    wtardy.add(wtardiness);
                    if (throughput == warmnum) warmtime = simtime;
                }
                if (initJob > 0 && throughput >= maxIDtoRecord) {
                    termination = true;
                }
                JobinShop.remove(job);
            }
        } else {
            eventlist.insert(new Event<Job>(VISIT, simtime, job));
        }
    }

    /**
     * get the current simulation time
     */
    public static double now() {
        return simtime;
    }

    public static double warmtime() {
        return warmtime;
    }

    public static int warmnum() {
        return warmnum;
    }

    public static int throughput() {
        return throughput;
    }

    public double getObjective(String obj) {
        if (obj.equals("tmean")) {
            return tardy.getAverage();
        } else if (obj.equals("tmax")) {
            return tardy.getMax();
        } else if (obj.equals("fmean")) {
            return leadtime.getAverage();
        } else if (obj.equals("fmax")) {
            return leadtime.getMax();
        }
        if (obj.equals("%t")) {
            return 100 * counttardy / throughput();
        }
        if (obj.equals("twt")) {
            return wtardy.getAverage();
        } else {
            System.err.println("No matched objective found!");
            System.exit(0);
        }
        return -1;
    }

    private void showEventList() {
        for (Event e : eventlist) {
            System.out.println(e);
        }
    }

}
