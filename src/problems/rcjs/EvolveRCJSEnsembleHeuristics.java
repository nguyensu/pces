package problems.rcjs;

import pes.core.gp.tgp.Program;
import pes.core.gp.tgp.TGPcore;
import pes.tda.mapping.Mapper;
import pes.tda.mapping.MapperModel;
import pes.tda.mapping.Visualisation;
import pes.utilites.SmallStatistic;

import java.io.*;
import java.util.*;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is the example of usinng adaptive charting genetic programming for evolving production scheduling heuristics
 * for the dynamic flexible job shop scheduling. Each program has two trees representings dispatching rules and routing
 * rules. The phenotypic characteristics of each program is determined by a decision vector which shows how the evolved rules
 * ranks jobs and machines.
 *
 * Visualisation (and interaction) can be called in the pes.core.gp.acgp.evolve() -- the function Mapper.mapping() will take
 * inputs including the program phenotypic characteristics, labels (if you need to make some annotations to the nodes of GNG),
 * program sizes (number of GP program nodes), and the fitnesses of evolved programs, etc.
 *
 * Mapper.mapping() can be reused for other tasks -- please look at evolutionary featuer synthesis and GP classification in
 * the example folder.
 *
 */

public class EvolveRCJSEnsembleHeuristics extends AdaptiveChartingEnsembleGPrcjs {
    static GPScheduler scheduler = new GPScheduler();
    static List<Data> testdata;
    static List<Data> traindata;
    static String name = "pces_rcjs_p200_g50_randomcme_t5_";
    @Override
    public String name() {return name;}
    static String expName = "";
    static int seed = 1;
    static int batchsize = 1;
    static String phenotype_instance = "";
    static int phenotype_instance_index = -1;
    static List<List<Integer>> rsubsets;
    static Map<String, Double> upperbound;
    static int nRules = 1;
    static int nInstanceTrainGen = 1;
    static int popsize = 500;
    static int ufreq = 1;
    static int maxdepth = 1700;
    public static void main(String[] args) throws IOException, InterruptedException {
        String config = "";
        if (args.length<16) {
            System.err.println("Wrong arguments!");
            System.exit(0);
        } else {
            for (int i = 0; i < args.length; i++) {
                if ("-n".equals(args[i])) {
                    expName = args[++i];
                    config += expName + "\n";
                } else if ("-popsize".equals(args[i])) {
                    popsize = Integer.parseInt(args[++i]);
                    config += "PS = "+popsize + "\n";
                } else if ("-s".equals(args[i])) {
                    seed = Integer.parseInt(args[++i]);
                } else if ("-b".equals(args[i])) {
                    batchsize = Integer.parseInt(args[++i]);
                } else if ("-showgng".equals(args[i])) {
                    Mapper.SHOW_GNG = Integer.parseInt(args[++i]) == 1? true: false;
                } else if ("-nsize".equals(args[i])) {
                    MapperModel.neighbors_size = Double.parseDouble(args[++i]);
                    config += "NS = "+MapperModel.neighbors_size + "\n";
                } else if ("-npc".equals(args[i])) {
                    Mapper.NPC = Integer.parseInt(args[++i]);
                    config += "NPC = "+Mapper.NPC + "\n";
                } else if ("-tw".equals(args[i])) {
                    time_window = Integer.parseInt(args[++i]);
                    config += "TW = "+ time_window + "\n";
                } else if ("-bc".equals(args[i])) {
                    MapperModel.bloat_control = Integer.parseInt(args[++i]);
                    config += "BC = "+MapperModel.bloat_control + "\n";
                } else if ("-pi".equals(args[i])) {
                    phenotype_instance = args[++i];
                    config += "PI = "+phenotype_instance + "\n";
                } else if ("-nr".equals(args[i])) {
                    nRules = Integer.parseInt(args[++i]);
                    config += "NR = "+nRules + "\n";
                } else if ("-ntg".equals(args[i])) {
                    nInstanceTrainGen = Integer.parseInt(args[++i]);
                    config += "NTG = "+nInstanceTrainGen + "\n";
                } else if ("-isize".equals(args[i])) {
                    ISIZE = Integer.parseInt(args[++i]);
                    config += "SIP = "+ISIZE + "\n";
                } else if ("-maxtime".equals(args[i])) {
                    MAXTIME = Integer.parseInt(args[++i]);
                    config += "MT = "+MAXTIME + "\n";
                } else if ("-updatef".equals(args[i])) {
                    ufreq = Integer.parseInt(args[++i]);
                    config += "UF = "+ufreq + "\n";
                } else if ("-maxdepth".equals(args[i])) {
                    maxdepth = Integer.parseInt(args[++i]);
                    config += "MD = "+maxdepth + "\n";
                }
            }
        }
        read_traindatafiles("src/problems/rcjs/traindata/");
        read_testdatafiles("src/problems/rcjs/testdata/");
        Visualisation.color_ratio = 0.2;
        Mapper.theta = 1000;
        for (int i = 0; i < batchsize; i++) {
            long runningTime = System.currentTimeMillis();
            String iconfig = config + "SEED = " + (seed+i);
            String filename = iconfig.replace("\n","").replace(" = ","").replace(" ", "");
            String outname = filename; //"results/" + filename;
            System.out.println(iconfig);
            EvolveRCJSEnsembleHeuristics gp = new EvolveRCJSEnsembleHeuristics(seed+i, new PrintWriter(outname + ".stat", "UTF-8"));
            EvolveRCJSEnsembleHeuristics.args = args;
            EvolveRCJSEnsembleHeuristics.subtitle = iconfig;
            gp.turn_running_stat();
            gp.setMaxDepth(maxdepth);
            gp.setPopSize(popsize);
            gp.setMaxGeneration(5000);
            gp.setTourSize(5);
            gp.evolve();
            PrintWriter writer = new PrintWriter(outname + ".out", "UTF-8");
            runningTime = (System.currentTimeMillis()-runningTime)/1000;
            gp.report += "\nRunning time = " + runningTime;
            writer.println("Best fitness:" + gp.bestfitness);
            writer.println("Test performance: \n" + gp.report);
            writer.close();
            System.out.println(gp.report);
        }
    }

    private static void read_traindatafiles(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);
        traindata = new ArrayList<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
//                System.out.println(listOfFiles[i].getName());
                traindata.add(new Data(path + listOfFiles[i].getName()));
                traindata.get(i).name = listOfFiles[i].getName();
                if (traindata.get(i).name.equals(phenotype_instance)) {
                    phenotype_instance_index = i;
                }
            }
        }
    }
    private static void read_testdatafiles(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);
        testdata = new ArrayList<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                testdata.add(new Data(path + listOfFiles[i].getName()));
                testdata.get(i).name = listOfFiles[i].getName();
//                if (testdata.get(i).name.equals(phenotype_instance)) {
//                    phenotype_instance_index = i;
//                }
            }
        }
        upperbound = new HashMap<String, Double>();
        upperbound.put("3testS5.txt", 505.0);
        upperbound.put("3testS23.txt", 149.1);
        upperbound.put("3testS53.txt", 69.4);
        upperbound.put("4testS28.txt", 23.8);
        upperbound.put("4testS42.txt", 66.7);
        upperbound.put("4testS61.txt", 46.0);
        upperbound.put("5testS7.txt", 252.9);
        upperbound.put("5testS21.txt", 168.6);
        upperbound.put("5testS62.txt", 250.7);
        upperbound.put("6testS10.txt", 861.4);
        upperbound.put("6testS28.txt", 228.5);
        upperbound.put("6testS58.txt", 234.2);
        upperbound.put("7testS5.txt", 438.7);
        upperbound.put("7testS23.txt", 562.8);
        upperbound.put("7testS47.txt", 439.4);
        upperbound.put("8testS3.txt", 631.8);
        upperbound.put("8testS53.txt", 449.2);
        upperbound.put("8testS77.txt", 1237.2);
        upperbound.put("9testS20.txt", 930.2);
        upperbound.put("9testS47.txt", 1233.1);
        upperbound.put("9testS62.txt", 1460.7);
        upperbound.put("10testS7.txt", 2538.2);
        upperbound.put("10testS13.txt", 2191.8);
        upperbound.put("10testS31.txt", 2191.8);
        upperbound.put("11testS21.txt", 1017.1);
        upperbound.put("11testS56.txt", 1790.1);
        upperbound.put("11testS63.txt", 2021.9);
        upperbound.put("12testS14.txt", 1766.4);
        upperbound.put("12testS36.txt", 2968.9);
        upperbound.put("12testS80.txt", 2457.6);
        upperbound.put("15testS2.txt", 3928.0);
        upperbound.put("15testS3.txt", 4327.3);
        upperbound.put("15testS5.txt", 3490.2);
        upperbound.put("20testS2.txt", 8344.9);
        upperbound.put("20testS5.txt", 14533.3);
        upperbound.put("20testS6.txt", 7438.5);
    }

    public EvolveRCJSEnsembleHeuristics(long seed, PrintWriter writer) throws FileNotFoundException, UnsupportedEncodingException {
        super(seed, writer);
    }

    public void define_terminal_function() {
        Program.gp = this;
        Program.nComponent = nRules;
        Program.sch_terminals = new String[Program.nComponent][];
        Program.inputs = new double[Program.nComponent][];
        Program.functions = new int[Program.nComponent][];
        for (int k = 0; k < Program.nComponent; k++) {
            Program.sch_terminals[k] = new String[] {"PR", "W", "R", "SL", "TPS", "TWS", "NS"}; //, "RT", "ASU"};
            Program.functions[k] = new int[] {TGPcore.ADD, TGPcore.SUB, TGPcore.MUL, TGPcore.DIV, TGPcore.MIN, TGPcore.MAX};
        }
    }

    public void setup_fitness() {
        varnumber =  Program.sch_terminals.length;
        randomnumber = 100;
        minrandom = 0;
        maxrandom = 1;
        if (varnumber + randomnumber >= FSET_START) {
            System.out.println("too many variables and constants");
        }
        // ,,,,
        int subset_size = nInstanceTrainGen;
        rsubsets = new ArrayList<>();
        for (int gen = 0; gen < 5000; gen++) {
            List<Integer> rsubset = new ArrayList<Integer>();
            while (rsubset.size() < subset_size) {
                int random = this.rd.nextInt(traindata.size());
                if (!rsubset.contains(random)) {
                    rsubset.add(random);
                }
            }
            rsubsets.add(rsubset);
        }
//        System.out.println(rsubsets.get(0).toString());
    }

    public double[] get_phenotype(Program Prog) {
        scheduler.gp = this;
        scheduler.setprog(Prog);
        for (int k = 0; k < Program.nComponent; k++) {
            scheduler.Serial(traindata.get(phenotype_instance_index), true, k);
        }
        return Prog.phenotype;
    }

    public double fitness_function(Program Prog) {
        update_progress();
        scheduler.gp = this;
        scheduler.setprog(Prog);
        SmallStatistic result = new SmallStatistic();
        for (int instance_gen: rsubsets.get(GEN)) {
            double mintwt = Double.POSITIVE_INFINITY;
            for (int k = 0; k < Program.nComponent; k++) {
                double twt = scheduler.Serial(traindata.get(instance_gen), false, k );
                mintwt = Math.min(mintwt, twt);
            }
            result.add(mintwt/ traindata.get(instance_gen).baseline);
        }
        return result.getAverage();
    }

    private void update_progress() {
        start_eval_at++;
        int percentage = (100*start_eval_at/POPSIZE);
        if (past_percent!=percentage && percentage%2==0) {
            System.out.print(".");
            past_percent = percentage;
            if (percentage%10==0) System.out.print(percentage + "%");
        }
    }

    public double fitness_function(Program Prog, int n) {
        return -1;
    }


    Program[] bestpop;
    double bestpopfit = Double.POSITIVE_INFINITY;
    public double fitness_function_deep(Program Prog) {
        if (GEN>0 && GEN%ufreq==0) {
            scheduler.gp = this;
            SmallStatistic result = new SmallStatistic();
            for (int i = 0; i < testdata.size(); i++) {
                double mintwt = Double.POSITIVE_INFINITY;
                for (int j = 0; j < POPSIZE; j++) {
                    scheduler.setprog(pop[j]);
                    for (int k = 0; k < Program.nComponent; k++) {
                        double twt = scheduler.Serial(testdata.get(i), false, k);
                        mintwt = Math.min(mintwt, twt);
                    }
                }
//                System.out.println(mintwt);
                result.add((mintwt - upperbound.get(testdata.get(i).name)) / upperbound.get(testdata.get(i).name));
            }
            double popfit = result.getAverage();
            if (popfit < bestfitness) {
                bestpop = Program.clone(pop);
            }
            System.out.println(" ==> Validation performance =" + popfit);
            Program.gendeeofut = popfit;
            favgpop = popfit;
            return popfit;
        } else {
            return 99;
        }
    }

    String report = "";
    @Override
    public double post_evolution() {
        scheduler.gp = this;
        SmallStatistic result = new SmallStatistic();

        for (int i = 0; i < testdata.size(); i++) {
            double mintwt = Double.POSITIVE_INFINITY;
            for (int j = 0; j < POPSIZE; j++) {
                scheduler.setprog(bestpop[j]);
                for (int k = 0; k < Program.nComponent; k++) {
                    double twt = scheduler.Serial(testdata.get(i), false, k );
                    mintwt = Math.min(mintwt, twt);
                }
            }
            report += testdata.get(i).name + ": " + mintwt + "\n";
            result.add((mintwt - upperbound.get(testdata.get(i).name))/upperbound.get(testdata.get(i).name));
        }

        report += "Average relative performace = " + result.getAverage();
        return result.getAverage();
    }


}
