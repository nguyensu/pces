package problems.rcjs;

import java.io.*;
import java.util.*;

// A testdata class to keep track of all the RCJS testdata we are using

public class Data {
	// Initialise some variables
	int machines;
	int max_power;
	int prec_no;
	int total_jobs;
	int timeline;
	List<Integer> dur;
	List<Integer> rel;
	List<Integer> due;
	List<Integer> id;
	List<Integer> power;
	List<Double> weight;
	List<Double> total_weight_successor;
	List<Double> total_load_successor;
	List<List<Integer> > precedences;
	List<List<Integer> > successors;
	List<Integer> jobs_in_machines;
	List<Integer> number_succesors;
	List<Integer> first_job_in_machine;
	List<Integer> job_belongs_to_machine;
	double baseline;
	double complexity;
	String name = "";
	// Not used here but maybe in future
	double lower_bound;

	// Constructor
	Data(String filename){
		dur = new ArrayList<Integer> ();
		rel = new ArrayList<Integer> ();
		due = new ArrayList<Integer> ();
		id = new ArrayList<Integer> ();
		power = new ArrayList<Integer> ();
		weight = new ArrayList<Double> ();
		total_weight_successor = new ArrayList<Double> ();
		total_load_successor = new ArrayList<Double> ();
		number_succesors = new ArrayList<Integer> ();
		precedences = new ArrayList<List<Integer>> ();
		successors = new ArrayList<List<Integer>> ();
		jobs_in_machines = new ArrayList<Integer> ();
		first_job_in_machine = new ArrayList<Integer> ();
		job_belongs_to_machine = new ArrayList<Integer> ();
		name = filename;
		// Read the testdata
		ReadFile(filename);
		// Compute a time-line
		computeTimeLine();
		Scheduler s = new Scheduler(this);
		double tardinessFIFO = s.Serial(this, "FIFO");
		double tardinessWEDD = s.Serial(this, "WEDD");
		complexity = (max(tardinessFIFO, tardinessWEDD) - min(tardinessFIFO, tardinessWEDD))/ max(tardinessFIFO, tardinessWEDD);
		baseline = tardinessWEDD;
	}

	double max(double a, double b) {
		return a > b? a: b;
	}

	double min(double a, double b) {
		return a < b? a: b;
	}

	// Read the contents of the file
	void ReadFile(String filename) {
		Scanner sc;
		try {
			sc = new Scanner(new File(filename));
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				if(line.contains("machines")) {
					machines = Integer.parseInt(sc.nextLine());
				}
				else if(line.contains("max power")) {
					max_power = Integer.parseInt(sc.nextLine());
				}
				else if(line.contains("Jobs in")) {
					// Extract all the jobs in the machines
					int jc = 0;
					for(int i=0;i<machines;i++){
						int jobs =  Integer.parseInt(sc.nextLine());
						// Extract job specific testdata
						for(int j=0;j<jobs;j++){
							job_belongs_to_machine.add(i);
							id.add(jc);
							if(j==0) first_job_in_machine.add(jc);
							sc.next(); // Read the ID
							rel.add(sc.nextInt());
							dur.add(sc.nextInt());
							due.add(sc.nextInt());
							power.add(sc.nextInt());
							weight.add(sc.nextDouble());
							jc++;
						}
						// Extra characters and header information
						sc.nextLine();
						sc.nextLine();
					}
					total_jobs = id.size();
					// Extract the precedences
					prec_no = Integer.parseInt(sc.nextLine());

					for(int i=0;i<prec_no;i++) {
						List<Integer> t = new ArrayList<Integer> ();
						t.add(sc.nextInt()); // Pre-job
						t.add(sc.nextInt()); // Post-job
						precedences.add(t);
					}
				}
			}
			for (int i = 0; i < this.total_jobs; i++) {
				double totalw_suc = 0;
				double totalload_suc = 0;
				int count = 0;
				List<Integer> successor= new ArrayList();
				for (int j = 0; j < this.precedences.size(); j++) {
					if (this.precedences.get(j).get(0) - 1 == i) {
						totalw_suc += this.weight.get(this.precedences.get(j).get(1) - 1);
						totalload_suc += this.dur.get(this.precedences.get(j).get(1) - 1);
						count ++;
						successor.add(this.precedences.get(j).get(1) - 1);
					}
				}
				this.total_weight_successor.add(totalw_suc);
				this.total_load_successor.add(totalload_suc);
				this.number_succesors.add(count);
				this.successors.add(successor);
			}
//			System.out.println();
		} catch (FileNotFoundException e) {
			System.out.println("The file does not exist");
		}
	}
	// Compute a reasonable time line
	int computeTimeLine(){
		// A more complex variant used if the time lines don't suffice
		/*int time_line=0;
		int max_release=0;
		for(int m = 0; m < machines; m += 2){
			int first_estimate=0;
			int second_estimate=0;
			for(int i = first_job_in_machine[m];i<first_job_in_machine[m]+jobs_in_machines[m];i++) {
				first_estimate+=dur[i];
				if(rel[i]>max_release) max_release=rel[i];
			}
			if(m+1<machines){
				for(int i = first_job_in_machine[m+1];i<first_job_in_machine[m+1]+jobs_in_machines[m+1];i++) first_estimate+=dur[i];
			}
			if(first_estimate>second_estimate) time_line += first_estimate;
			else time_line +=second_estimate;
		}*/

		// Otherwise, this is sufficient
		int time_line = 0;
		for(int i=0;i<total_jobs;i++) time_line+=dur.get(i);
		timeline=time_line;
//		System.out.println("Time line: " + time_line);
		return time_line;
	}

	// Display the testdata if needed
	void displayData() {
		System.out.println("Machines " + machines);
		System.out.println("Power " + max_power);
		for(int i=0;i<total_jobs;i++){
			System.out.println("Job " + (i+1) + ", release " +rel.get(i)
					+ ", duration " +dur.get(i) + ", due " +due.get(i)
					+ ", power " +power.get(i)+ ", weight " +weight.get(i));
		}

		for(int i=0;i<prec_no;i++) {
			System.out.println("Job pre " + precedences.get(i).get(0) + ", job post " + precedences.get(i).get(1));
		}
	}

}
