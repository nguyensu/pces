package problems.rcjs;

import java.util.*;

// A class to determine a schedule of jobs given their sequence
public class Scheduler {
    // Call one of the constructions by default
    // This is to be implemented in future
    Scheduler(Data d, List<Integer> jobs) {

    }

    Scheduler(Data d) {

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
    double Serial(Data d, String rulename) {
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
                double priority = 1; // d.weight.get(j)/d.due.get(j);
                if (rulename == "FIFO") {
                    priority = getStartTime(j, d, end);
                } else if (rulename == "WEDD") {
                    priority = d.weight.get(j)/d.due.get(j);
                }
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
        return tardiness;
    }
}
