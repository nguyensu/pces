package problems.dfjss;
/**
 *
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 *
 */

import java.util.Random;


public class SchedulingSampleReference extends Scheduling {
    //GP related
    public static int[] count = new int[2];
    public static double[][][] sample = new double[2][][];
    public static double[][] situation = new double[2][100000];
    public static int[] sitcount = new int[2];
    public Workcenter temptM = null;
    public double tempT = -1;
    Random rnd = new Random(99);

    public SchedulingSampleReference() {
        count = new int[2];
        sample = new double[2][][];
        situation = new double[2][100000];
        sitcount = new int[2];
        sample[0] = new double[100000][10];
        sample[1] = new double[100000][4];
    }

    String[] benchmarkrules = {"WSPT-LWT", "EDD-LWT", "FIFO-LWT", "SL-LWT"};

    public static double benchmark_sequencing(double[] inputdat, String rname){
        double marrive = inputdat[0]; //= t - job.marrivetime();
        double jarrive = inputdat[1]; // = t - job.releasetime();
        double RO = inputdat[2]; // = job.remainingOperation();
        double RT = inputdat[3]; // = job.remainingTime_currentmode();
        double PT = inputdat[4]; // = job.currentProcesstime();
        double rDD = inputdat[5]; // = job.duedate() - t;
        double SL = inputdat[6]; // = job.duedate() - (t + sample[0][count[0]][3]);
        double WLQ = inputdat[7]; // = job.workloadnextQ_average();
        double NPT = inputdat[8]; // = job.nextprocesstime_average();
        double W = inputdat[9]; // = job.weight();

        if (rname.equals("WSPT-LWT")) {
            return W / PT;
        } else if (rname.equals("EDD-LWT")) {
            return -rDD;
        } else if (rname.equals("FIFO-LWT")) {
            return -marrive;
        } else if (rname.equals("SL-LWT")) {
            return - (SL);
        } else {
            System.out.println(rname + " is not an available rule!!!");
            System.exit(0);
            return -1;
        }
    }

    public static double benchmark_routing(double[] inputdat, String rname){
        double WL = inputdat[0];// = shop.wcs()[op.getWC(i)].workload();
        double JIQ = inputdat[1];// = shop.wcs()[op.getWC(i)].jobinqueue();
        double rPT = inputdat[2];// = shop.wcs()[op.getWC(i)].workload_ratio_morethan_processingtime(op.getProcessingTime(i));
        double rSLACK = inputdat[3];// = shop.wcs()[op.getWC(i)].workload_ratio_more_urgent(job.duedate() - (FlexibleJobShop.now() + job.remainingTime()));

        if (rname.equals("WSPT-LWT")) {
            return -WL;
        } else if (rname.equals("EDD-LWT")) {
            return -WL;
        } else if (rname.equals("FIFO-LWT")) {
            return -WL;
        } else if (rname.equals("SL-LWT")) {
            return -WL;
        } else {
            System.out.println(rname + " is not an available rule!!!");
            System.exit(0);
            return -1;
        }
    }

    public double prioritise(Job job){
        if (count[0] < 100000) {
            double t = FlexibleJobShop.now();
            if (tempT==-1 || tempT!=t || temptM!=job.getWC()) {
                tempT = t; temptM = job.getWC();
                sitcount[0]++;
            }
            sample[0][count[0]][0] = t - job.marrivetime();
            sample[0][count[0]][1] = t - job.releasetime();
            sample[0][count[0]][2] = job.remainingOperation();
            sample[0][count[0]][3] = job.remainingTime_currentmode();
            sample[0][count[0]][4] = job.currentProcesstime();
            sample[0][count[0]][5] = job.duedate() - t;
            sample[0][count[0]][6] = job.duedate() - (t + sample[0][count[0]][3]);
            sample[0][count[0]][7] = job.workloadnextQ_average();
            sample[0][count[0]][8] = job.nextprocesstime_average();
            sample[0][count[0]][9] = job.weight();
            situation[0][count[0]] = sitcount[0];
        }
        count[0]++;
        String[] benchmarkrules = {"WSPT-LWT", "EDD-LWT", "FIFO-LWT", "SL-LWT"};
        return job.weight() / job.currentProcesstime();
    }

    public void nextroute(Job job, ShopLevel shop) {
        int select_mode = -1;
        double max_wc_priority = Double.NEGATIVE_INFINITY;
        Operation op = job.current_operation();
        if (count[1] < 100000)
            sitcount[1]++;
        for (int i = 0; i < op.nMode(); i++) {
            if (count[1] < 100000) {
                sample[1][count[1]][0] = shop.wcs()[op.getWC(i)].workload();
                sample[1][count[1]][1] = shop.wcs()[op.getWC(i)].jobinqueue();
                sample[1][count[1]][2] = shop.wcs()[op.getWC(i)].workload_ratio_morethan_processingtime(op.getProcessingTime(i));
                sample[1][count[1]][3] = shop.wcs()[op.getWC(i)].workload_ratio_more_urgent(job.duedate() - (FlexibleJobShop.now() + job.remainingTime()));
                situation[1][count[1]] = sitcount[1];
            }
            double temp_priority = -shop.wcs()[op.getWC(i)].workload();
            if (temp_priority > max_wc_priority) {
                max_wc_priority = temp_priority;
                select_mode = i;
            }
            count[1]++;
        }
        job.set_route(select_mode);
    }

}
