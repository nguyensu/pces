package problems.dfjss;
/**
 *
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 *
 */

import pes.core.gp.tgp.Program;
import pes.core.gp.tgp.TGPcore;

public class GeneticProgrammingScheduling extends Scheduling {
    //GP related
    public static int count;
    public static boolean reference = false;
    public TGPcore gp;
    public char[] dispatcher;
    public char[] router;
    Program temp;
    public void setprog(Program prog) {
        if (reference) return;
        dispatcher = prog.prog[0];
        router = prog.prog[1];
        temp = prog;
    }
    public double prioritise(Job job){
        if (reference) return -job.marrivetime();
        Program.switch_component(0);
        double t = FlexibleJobShop.now();
        gp.input_vec[0] = t - job.marrivetime();
        gp.input_vec[1] = t - job.releasetime();
        gp.input_vec[2] = job.remainingOperation();
        gp.input_vec[3] = job.remainingTime_currentmode();
        gp.input_vec[4] = job.currentProcesstime();
        gp.input_vec[5] = job.duedate() - t;
        gp.input_vec[6] = job.duedate() - (t + gp.input_vec[3]);
        gp.input_vec[7] = job.workloadnextQ_average();
        gp.input_vec[8] = job.nextprocesstime_average();
        gp.input_vec[9] = job.weight();
        gp.program = dispatcher; gp.PC = 0;
        count++;
        return gp.run();
    }

    public void nextroute(Job job, ShopLevel shop) {
        if (!reference) Program.switch_component(1);
        int select_mode = -1;
        double max_wc_priority = Double.NEGATIVE_INFINITY;
        Operation op = job.current_operation();
        for (int i = 0; i < op.nMode(); i++) {
            double temp_priority;
            if (reference) {
                temp_priority = -shop.wcs()[op.getWC(i)].workload();
            } else {
                // prioritize route with GP generated rule
                gp.input_vec[0] = shop.wcs()[op.getWC(i)].workload();
                gp.input_vec[1] = shop.wcs()[op.getWC(i)].jobinqueue();
                gp.input_vec[2] = shop.wcs()[op.getWC(i)].workload_ratio_morethan_processingtime(op.getProcessingTime(i));
                gp.input_vec[3] = shop.wcs()[op.getWC(i)].workload_ratio_more_urgent(job.duedate() - (FlexibleJobShop.now() + job.remainingTime()));
                gp.program = router;
                gp.PC = 0;
                temp_priority = gp.run();
            }
            if (temp_priority > max_wc_priority) {
                max_wc_priority = temp_priority;
                select_mode = i;
            }
        }
        if (select_mode == -1) select_mode = 0;
        job.set_route(select_mode);
    }
}