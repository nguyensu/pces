package problems.rcjs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// The controller function for the RCJS problem
public class Controller {
	public static void main(String[] args) {
		String path = "src/problems/rcjs/testdata/";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		List<String> filenames = new ArrayList<>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				filenames.add(listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
			}
		}

		for (String iname: filenames) {
			if (!iname.equals("4testS28.txt")) {
				continue;
			}
			System.out.println("Reading file: " + path + iname);
			// Read in the testdata
			Data d = new Data(path + iname);
			// Define a sequence of jobs
			// In the test case, 4testS28.txt there are 41 jobs
			List<Integer> jobs = new ArrayList<Integer>();
			for (int i = 0; i < 41; i++) {
				jobs.add(i);
			}
			//d.displayData();
			// Schedule the jobs using the scheduling heuristic
			Scheduler s = new Scheduler(d, jobs);
			double tardiness = s.Serial(d, "FIFO");
			System.out.println(tardiness);
		}
	}
}
