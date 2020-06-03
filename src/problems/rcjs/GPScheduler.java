package problems.rcjs;

import pes.core.gp.tgp.Program;
import pes.core.gp.tgp.TGPcore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// A class to determine a schedule of jobs given their sequence
public class GPScheduler {
    // Call one of the constructions by default
    // This is to be implemented in future

    public TGPcore gp;
    public Program scheduling_program;

    GPScheduler() {

    }

    public void setprog(Program prog) {
        scheduling_program = prog;
    }

    // Check if the preceding jobs have been completed
    boolean check_precedence(int job, Data d, List<Integer> jc){
        for(int i=0;i<d.prec_no;i++){
            int job_pre = d.precedences.get(i).get(0);
            int job_post = d.precedences.get(i).get(1);
            if(job==job_post-1 && jc.get(job_pre-1) == 0) return true;
        }
        return false;
    }

    // Get the revised start time of a job
    int getStartTime(int job, Data d, int[] end){
        int t = d.rel.get(job);
        for(int i=0;i<d.prec_no;i++){
            int job_pre = d.precedences.get(i).get(0);
            int job_post = d.precedences.get(i).get(1);
            if(job==job_post-1 && t < end[job_pre-1]) t = end[job_pre-1];
        }
        return t;
    }


    // We can have serial or parallel scheduling
    double Serial(Data d, List<Integer> jobs) {
        // Some testdata structures to use locally
        int[][] schedule = new int[d.machines][d.timeline]; // The full schedule
        int[] start = new int[d.total_jobs]; // Start times of the jobs
        int[] end = new int[d.total_jobs]; // End times of the jobs
        int[] resources_used = new int[d.timeline]; // Resources used at each time point

        for(int i = 0; i < d.total_jobs; i++) {
            start[i] = -1;
            end[i] = -1;
        }
        for(int t = 0; t < d.timeline; t++) resources_used[t] = 0;
        for(int m = 0; m < d.machines; m++) {
            for(int t = 0; t < d.timeline; t++) {
                schedule[m][t] = -1;
            }
        }

        double tardiness = 0.0;
        // we schedule all the jobs exactly in the sequence they are given
        List<Integer> jw = new LinkedList<Integer>(); // Waiting jobs
        List<Integer> jc = new ArrayList<Integer>(); // Completed jobs
        for(int k=0; k<d.total_jobs;k++) jc.add(0);
        // construct schedule
        for(int k=0; k<d.total_jobs;k++){
            int job = jobs.get(k);
            boolean cant_schedule=check_precedence(job,d,jc);
            if(cant_schedule) jw.add(job);
            else{
                boolean jobs_found = true;
                while(jobs_found){
                    jc.set(job,1);
                    int t=getStartTime(job,d,end);
                    int machine = d.job_belongs_to_machine.get(job);
                    boolean valid_block = false;
                    while(t < d.timeline && valid_block==false){
                        if(schedule[machine][t]==-1){
                            boolean block_ok = true;
                            for(int j=t; j < t+d.dur.get(job); j++) {
                                if(schedule[machine][j]!=-1 || resources_used[j]+d.power.get(job) > d.max_power) block_ok=false;
                            }
                            if(block_ok) {
                                valid_block=true;
                                for(int j=t; j < t+d.dur.get(job); j++) {
                                    schedule[machine][j]=job;
                                    resources_used[j]+=d.power.get(job);
                                }
                                start[job] = t;
                                end[job] = t+ d.dur.get(job);
                                if(end[job] > d.due.get(job)) tardiness += (double) (end[job] - d.due.get(job))*d.weight.get(job);
                            }
                        }
                        t++;
                    }
                    boolean still_cant = true;
                    for (Iterator<Integer> it = jw.listIterator(); it.hasNext(); ) {
                        int j = it.next();
                        still_cant = check_precedence(j,d,jc);
                        if(!still_cant) {
                            it.remove();
                            job = j;
                            break;
                        }
                    }
                    if(still_cant)
                        jobs_found=false;
                }
            }
        }
        return tardiness;
    }

    // We can have serial or parallel scheduling
    public double Serial(Data d, boolean update_phenotype, int kindex) {
        // Some testdata structures to use locally
        if (update_phenotype) scheduling_program.phenotype = new double[d.total_jobs];

        int[][] schedule = new int[d.machines][d.timeline]; // The full schedule
        int[] start = new int[d.total_jobs]; // Start times of the jobs
        int[] end = new int[d.total_jobs]; // End times of the jobs
        int[] resources_used = new int[d.timeline]; // Resources used at each time point

        for(int i = 0; i < d.total_jobs; i++) {
            start[i] = -1;
            end[i] = -1;
        }
        for(int t = 0; t < d.timeline; t++) resources_used[t] = 0;
        for(int m = 0; m < d.machines; m++) {
            for(int t = 0; t < d.timeline; t++) {
                schedule[m][t] = -1;
            }
        }

        double tardiness = 0.0;
        // we schedule all the jobs exactly in the sequence they are given
        List<Integer> jw = new LinkedList<Integer>(); // Waiting jobs
        List<Integer> jr = new LinkedList<Integer>(); // Remaining jobs
        List<Integer> jc = new ArrayList<Integer>(); // Completed jobs
        for(int k=0; k<d.total_jobs;k++) {
            jc.add(0);
            jr.add(k);
        }
        for (int k = 0; k < jr.size(); k++) {
            int job = jr.get(k);
            boolean cant_schedule=check_precedence(job,d,jc);
            if(!cant_schedule) jw.add(job);
        }
        jr.removeAll(jw);
        // construct schedule
        while (!jw.isEmpty()){
            Integer job = -1;
            double highest_priority = Double.NEGATIVE_INFINITY;
            for (int j: jw) {
                int earliest_start =getStartTime(j,d,end);
                gp.input_vec[0] = d.dur.get(j);
                gp.input_vec[1] = d.weight.get(j);
                gp.input_vec[2] = d.power.get(j);
                gp.input_vec[3] = d.due.get(j) - (earliest_start + d.dur.get(j));
                gp.input_vec[4] = d.total_load_successor.get(j);
                gp.input_vec[5] = d.total_weight_successor.get(j);
                gp.input_vec[6] = d.number_succesors.get(j);
                double resource_tightness = 0;
                for (int tick = earliest_start; tick < earliest_start+d.dur.get(j); tick++) {
                    if (tick > d.timeline - 1) return Double.POSITIVE_INFINITY;
                    resource_tightness = Math.max(0, d.max_power - (resources_used[tick]+d.power.get(j)));
                }
                gp.input_vec[7] = resource_tightness/d.dur.get(j);
                double average_succesor_uggency = 0;
                for (int sc: d.successors.get(j)) {
                    average_succesor_uggency += d.due.get(sc) - (earliest_start + d.dur.get(j) + d.dur.get(sc));
                }
                gp.input_vec[8] = (gp.input_vec[3] + average_succesor_uggency) / (d.number_succesors.get(j) + 1);
                gp.program = scheduling_program.prog[kindex]; gp.PC = 0;
                double priority =  gp.run();
//                double priority = 1.0/d.due.get(j);

                if (highest_priority < priority) {
                    highest_priority = priority;
                    job = j;
                }
            }
            jc.set(job,1);
            int t=getStartTime(job,d,end);
            int machine = d.job_belongs_to_machine.get(job);
            boolean valid_block = false;
            while(t < d.timeline && valid_block==false){
                if(schedule[machine][t]==-1){
                    boolean block_ok = true;
                    for(int j=t; j < t+d.dur.get(job); j++) {
                        if (j > schedule[machine].length - 1)
                            return  Double.POSITIVE_INFINITY;
                        if(schedule[machine][j]!=-1 || resources_used[j]+d.power.get(job) > d.max_power) block_ok=false;
                    }
                    if(block_ok) {
                        valid_block=true;
                        for(int j=t; j < t+d.dur.get(job); j++) {
                            schedule[machine][j]=job;
                            resources_used[j]+=d.power.get(job);
                        }
                        start[job] = t;
                        if (update_phenotype) scheduling_program.phenotype[job] = t;
                        end[job] = t+ d.dur.get(job);
                        if(end[job] > d.due.get(job)) tardiness += (double) (end[job] - d.due.get(job))*d.weight.get(job);
                    }
                }
                t++;
            }
            // add more more jobs into job list
            List<Integer> jw_new = new LinkedList<Integer>();
            for (int j: jr) {
                boolean cant_schedule=check_precedence(j,d,jc);
                if(!cant_schedule) {
                    jw_new.add(j);
                    jw.add(j);
                }
            }
            jw.remove(job);
            jr.removeAll(jw_new);

        }
        if (update_phenotype) {
            double avgflowtime = 0;
            for(int i = 0; i < d.total_jobs; i++) {
                avgflowtime += end[i];
            }
            avgflowtime /= d.total_jobs;
            return avgflowtime;
        }
        return tardiness;
    }
}
