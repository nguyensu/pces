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
public class SchedulingTest extends Scheduling {
    public String drule = "EDD";
    public String rrule = "LWT";
    public double u = 0.9;
    public double ahead = 1;
    public void setRule(String r) {
        rrule = r;
    }
    public void setUtilisation(double uu) {
        u = uu;
    }
    public static boolean reference = false;
    public SchedulingTest(){

    }
    public SchedulingTest(String rr, String dr){
        rrule = rr;
        drule = dr;
    }
    @Override
    public double prioritise(Job job){
        double t = FlexibleJobShop.now();
        double rRJ = t - job.marrivetime();
        double rrJ = t - job.releasetime();
        double RO = job.remainingOperation();
        double RT = job.remainingTime_currentmode();
        double PR = job.currentProcesstime();
        double rDD = job.duedate() - t;
        double SJ = job.duedate() - (t + RT);
        double WINQ = job.workloadnextQ_average();
        double NPT = job.nextprocesstime_average();
        double W = job.weight();
        
        double workload = job.wc().workload();
        double jobinQ = job.wc().jobinqueue();
        
        if (reference) return - job.marrivetime();
        else {
            if (drule == "FIFO" ) {
                return - job.marrivetime();
            } else if (drule == "EDD" ){
                return - job.duedate();
            } else if (drule == "WSPT" ){
                return W/PR;
            } else if (drule == "SJ" ){
                return - SJ;
            }  else if (drule == "WATC" ){
                return (W/PR)*Math.exp(-maxPlus((SJ-(2)*(RT-PR))/(2*workload/jobinQ)));
            }  else if (drule == "SPT" ){
                return - PR;
            }
            return -1;
        }
    }

    public void nextroute(Job job, ShopLevel shop) {
        int select_mode = -1;
        double max_wc_priority = Double.NEGATIVE_INFINITY;
        Operation op = job.current_operation();
        for (int i = 0; i < op.nMode(); i++) {
            //sch_terminals[1] = new String[] {"WLN", "NON","rPT","rSLACK"};
            double WLN = shop.wcs()[op.getWC(i)].workload();
            double NON = shop.wcs()[op.getWC(i)].jobinqueue();
            double rPT = shop.wcs()[op.getWC(i)].workload_ratio_morethan_processingtime(op.getProcessingTime(i));
            double rSLACK = shop.wcs()[op.getWC(i)].workload_ratio_more_urgent(job.duedate() - (FlexibleJobShop.now() + job.remainingTime()));
            double temp_priority = 0;
            if (reference) temp_priority = -shop.wcs()[op.getWC(i)].workload();
            else {
                if (rrule == "LWT") {
                    temp_priority = -shop.wcs()[op.getWC(i)].workload();
                }
            }
            if (temp_priority > max_wc_priority) {
                max_wc_priority = temp_priority;
                select_mode = i;
            }
        }
        job.set_route(select_mode);
    }

    public static double div(double a, double b){
        if (b==0) return 1;
        else return a/b;
    }

    public static double max(double a, double b){
        if (a>b) return a;
        else return b;
    }
    public static double Abs(double a){
        return Math.abs(a);
    }
    private static double maxPlus(double a){
        if (a>0) return a;
        else return 0;
    }
    public static double min(double a, double b){
        if (a<b) return a;
        else return b;
    }
    public static double IF(double a, double b, double c){
        if (a>=0){
            return b;
        }else{
            return c;
        }
    }
    public static double pd(double a, double b) {
        if (b==0) return 1;
        else return a/b;
    }


}

