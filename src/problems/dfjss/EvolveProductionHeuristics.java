package problems.dfjss;

import edu.princeton.cs.algs4.IndexMinPQ;
import pes.core.gp.acgp.AdaptiveChartingGP;
import pes.core.gp.tgp.Program;
import pes.core.gp.tgp.TGPcore;
import pes.tda.mapping.Mapper;
import pes.tda.mapping.MapperModel;
import pes.utilites.Miscellaneous;
import pes.utilites.SmallStatistic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

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

public class EvolveProductionHeuristics extends AdaptiveChartingGP {
    static GeneticProgrammingScheduling scheduler = new GeneticProgrammingScheduling();
    // some parameter used in evolving dispatching rules for job shop, ignore them if you work on other problems
    public static ShopParameter shopparam;
    static String name = "pces_fjss_p200_g50_randomcme_t5_";
    @Override
    public String name() {
            return name;
        }
    static String expName = "";
    static String obj = "twt";
    static int warmup = 500;
    static int lengthsim = 5000;
    static double u = 0.80;
    static double all = 1;
    static int flexibility = 2;
    static int seed = 1;
    static int batchsize = 1;
    double reffit_abs;
    double reffitdeep_abs;
    double refsimtime_abs;
    double refsimtimedeep_abs;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length<16) {
            System.err.println("Wrong arguments!");
            System.exit(0);
        } else {
            for (int i = 0; i < args.length; i++) {
                if ("-n".equals(args[i])) {
                    expName = args[++i];
                } else if ("-o".equals(args[i])) {
                    obj = args[++i];
                } else if ("-u".equals(args[i])) {
                    u = Double.parseDouble(args[++i]);
                } else if ("-a".equals(args[i])) {
                    all = Double.parseDouble(args[++i]);
                } else if ("-f".equals(args[i])) {
                    flexibility = Integer.parseInt(args[++i]);
                } else if ("-s".equals(args[i])) {
                    seed = Integer.parseInt(args[++i]);
                } else if ("-b".equals(args[i])) {
                    batchsize = Integer.parseInt(args[++i]);
                } else if ("-showgng".equals(args[i])) {
                    Mapper.SHOW_GNG = Integer.parseInt(args[++i]) == 1? true: false;
                } else if ("-nsize".equals(args[i])) {
                    MapperModel.neighbors_size = Double.parseDouble(args[++i]);
                } else if ("-npc".equals(args[i])) {
                    Mapper.NPC = Integer.parseInt(args[++i]);
                } else if ("-tw".equals(args[i])) {
                    time_window = Integer.parseInt(args[++i]);
                } else if ("-bc".equals(args[i])) {
                    MapperModel.bloat_control = Integer.parseInt(args[++i]);
                }
            }
        }
        MapperModel.neighbors_size = 0.03;
//        MapperModel.neighbors_size = 0.5;
        for (int i = 0; i < batchsize; i++) {
            long runningTime = System.currentTimeMillis();
            String outname = "results/" + expName + name + "GNGns" + MapperModel.neighbors_size + "npc" + Mapper.NPC + "tw" + time_window + "bc" + MapperModel.bloat_control + "O" + obj + "U" + u + "A" + all+ "F" + flexibility + "S" + (seed+i);
            EvolveProductionHeuristics gp = new EvolveProductionHeuristics(seed+i, new PrintWriter(outname + ".stat", "UTF-8"));
            EvolveProductionHeuristics.args = args;
            gp.turn_running_stat();
            gp.setMaxDepth(1700);
            gp.setPopSize(200);
            gp.setMaxGeneration(50);
            gp.setTourSize(5);
            gp.setCrossoverRate(0.5);
            gp.setMutationRate(0.5);
            gp.evolve();
            PrintWriter writer = new PrintWriter(outname + ".out", "UTF-8");
            runningTime = (System.currentTimeMillis()-runningTime)/1000;
            gp.report += "\nRunning time = " + runningTime;
            writer.println("Best fitness:" + gp.bestfitness);
            writer.println("Test performance: \n" + gp.report);
            writer.close();
//        gp.trainwriter.close();
            System.out.println(gp.report);
        }
    }

    public EvolveProductionHeuristics(long seed, PrintWriter writer) throws FileNotFoundException, UnsupportedEncodingException {
        super(seed, writer);
    }

    public void define_terminal_function() {
        Program.gp = this;
        Program.sch_terminals[0] = new String[] {"rRJ", "rrJ", "RO", "RT", "PR", "rDD", "SJ", "WINQ", "NPT", "W"};
        Program.sch_terminals[1] = new String[] {"WLN", "NON","rPT","rSLACK"};
        Program.functions[0] = new int[] {TGPcore.ADD, TGPcore.SUB, TGPcore.MUL, TGPcore.DIV, TGPcore.MIN, TGPcore.MAX};
        Program.functions[1] = new int[] {TGPcore.ADD, TGPcore.SUB, TGPcore.MUL, TGPcore.DIV, TGPcore.MIN, TGPcore.MAX};
    }

    public void setup_fitness() {
        varnumber = 10;
        randomnumber = 100;
        minrandom = 0;
        maxrandom = 1;
        if ("twt".equals(obj)) {
            FlexibleJobShop.withWeight = true;
        }
        if (varnumber + randomnumber >= FSET_START) {
            System.out.println("too many variables and constants");
        }
        // TODO code application logic here
        double uti = u;
        int nlevel = 1;
        ShopLevel.ShopType[] LV1 = {ShopLevel.ShopType.PJS};

        int[] nwc = {10};
        int[][] nmc = {{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}};
        double[] lowUti = {uti};
        double[] highUti = {uti};
        String[] processdist = {"erlang"};
        double[] processdist_param = {1};
        int[] flex = {flexibility};
        double[] meantime = {1};
        int[] lowop = {1}; int[] upop = {nwc[0]};
        double allowance = all;
        shopparam = new ShopParameter(scheduler, nlevel, LV1, nwc, nmc, lowUti, highUti, processdist, processdist_param, flex, meantime, lowop, upop, true, allowance);
        // get reference rule performance
        GeneticProgrammingScheduling.reference = true;
        reffit_abs = reffitdeep_abs = 1.0; refsimtime_abs = refsimtimedeep_abs = Double.POSITIVE_INFINITY;
        reffit_abs = fitness_function((Program) null);
        refsimtime_abs = simtime;
        reffitdeep_abs = fitness_function_deep(null);
        refsimtimedeep_abs = simtime;
        GeneticProgrammingScheduling.reference = false;
    }

    double simtime = -1;
    public double fitness_function(Program Prog) {
        start_eval_at++;
        int percentage = (100*start_eval_at/POPSIZE);
        if (past_percent!=percentage && percentage%2==0) {
            System.out.print(".");
            past_percent = percentage;
            if (percentage%10==0) System.out.print(percentage + "%");
        }

        scheduler.gp = this;
        scheduler.setprog(Prog);
        SmallStatistic result = new SmallStatistic();
        SmallStatistic simtime_stop = new SmallStatistic();
        int rep = 1;
        for (int i = 0; i < rep; i++) {
            FlexibleJobShop.simetimeref = refsimtime_abs;
            FlexibleJobShop shop = new FlexibleJobShop(shopparam, lengthsim, warmup, Miscellaneous.FitSeed[GEN]);
            simtime_stop.add(FlexibleJobShop.now());
            if (FlexibleJobShop.unstable) {
                result.add(Double.POSITIVE_INFINITY);
            } else {
                result.add(shop.getObjective(obj));
            }
        }
        simtime = simtime_stop.getAverage();
        return result.getAverage()/reffit_abs;
    }

    public double fitness_function(Program Prog, int n) {
        return -1;
    }
    public double fitness_function_deep(Program Prog) {
        scheduler.gp = this;
        scheduler.setprog(Prog);
        SmallStatistic result = new SmallStatistic();
        SmallStatistic simtime_stop = new SmallStatistic();
        int rep = 50;
        detaildeep = new double[rep];
        for (int i = 0; i < rep; i++) {
            FlexibleJobShop.simetimeref = refsimtimedeep_abs;
            FlexibleJobShop shop = new FlexibleJobShop(shopparam, lengthsim, warmup, Miscellaneous.ExpSeed[i]);
            detaildeep[i] = shop.getObjective(obj);
            simtime_stop.add(FlexibleJobShop.now());
            if (FlexibleJobShop.unstable) {
                result.add(Double.POSITIVE_INFINITY);
            } else {
                result.add(shop.getObjective(obj));
            }
            result.add(detaildeep[i]); // no need (redundant)
        }
        simtime = simtime_stop.getAverage();
        return result.getAverage()/reffitdeep_abs;
    }

    String report = "";
    @Override
    public double post_evolution() {
        scheduler.gp = this;
        scheduler.setprog(best_program);
        int rep = 50;
        double[][] testScene = {{0.95,3},{0.95,5},{0.95,7},{0.85,3},{0.85,5},{0.85,7}};
        FlexibleJobShop.simetimeref = Double.POSITIVE_INFINITY;
        for (int s = 0; s < testScene.length; s++) {
            SmallStatistic result = new SmallStatistic();
            String scenout = "";
            shopparam.setUtilisation(testScene[s][0]);
            shopparam.setFlexibility((int) testScene[s][1]);
            for (int i = 0; i < rep; i++) {
                FlexibleJobShop shop = new FlexibleJobShop(shopparam, lengthsim, warmup, Miscellaneous.SimSeed[i]);
                result.add(shop.getObjective(obj));
                scenout+= shop.getObjective(obj) + "," ;
            }
            scenout = "Testing performance: \n" + result.toString() + "\n[" + scenout + "]\n";
            report += "Scenario with U = " + testScene[s][0] + " and Flexibility = " + testScene[s][1] + "\n"+ scenout;
        }
        report += "\n" + best_program;
        report += "\n" + best_program.toStringInt();
        return 0;
    }

    Random rnd = new Random(911);

    public void setupSample() {
        setupSampleJuergen();
    }
    void setupSampleJuergen() {
        SchedulingSampleReference sche = new SchedulingSampleReference();
        shopparam.assignScheduler(sche);
        FlexibleJobShop shop = new FlexibleJobShop(shopparam, 10000, 500, 111);

        for (int component = 0; component < Program.nComponent; component++) {
            targets = SchedulingSampleReference.sample[component];
            double[] sit = SchedulingSampleReference.situation[component];
            // count the number of situations
            ArrayList<double[][]> situations = new ArrayList<double[][]>();
            int nSitutations = 0; double tempt = 1; int tempCount = 0;
            IndexMinPQ<Double> sam = new IndexMinPQ<Double>(targets.length);
            for (int i = 0; i < targets.length; i++) {
                if (sit[i] == 0 ) break;
                if (sit[i] != tempt) {
                    if (i!=0) {
                        double[][] situation = new double[tempCount][];
                        for (int j = 0; j < tempCount; j++) {
                            situation[j] = targets[i-j-1];
                        }
                        situations.add(situation);
                        sam.insert(nSitutations, rnd.nextDouble());
                    }
                    nSitutations++;
                    tempt = sit[i];
                    tempCount = 1;
                } else {
                    tempCount++;
                }
            }
            Program.decisionSituations[component] = new ArrayList<double[][]>();
            int Nsample = 100;
            int scount = 0;
            do {
                double[][] situation = situations.get(sam.delMin());
                if (situation.length != 3) continue;
                else Program.decisionSituations[component].add(situation);
                scount++;
                maxDistance += (situation.length-1)*(situation.length-1);
            } while (scount<Nsample);
            char[] refprog = {SUB,ZERO,ADD,ADD,ADD, 4,4,7,8};
            if (component==1) {
                char[] refprog_route = {SUB,ZERO,0};
                refprog = refprog_route;
            }
            Program.refranks[component] = samplingdecisionREF_output(refprog,component);
        }
        String[] benchmarkrules = {"WSPT-LWT", "EDD-LWT", "FIFO-LWT", "SL-LWT"};
        Mapper.benchmarkSample = new ArrayList[benchmarkrules.length];
        for (int i = 0; i < benchmarkrules.length; i++) {
            ArrayList<Integer> pheno = samplingdecision_output(benchmarkrules[i]);
            Mapper.benchmarkSample[i] = new ArrayList<>();
            for (int j = 0; j < pheno.size(); j++) {
                Mapper.benchmarkSample[i].add(j, Double.valueOf(pheno.get(j)));
            }
        }
        Mapper.benchmarkName = benchmarkrules;
        maxDistance = Math.sqrt(maxDistance);
        shopparam.assignScheduler(scheduler);
    }

    ArrayList<Integer> samplingdecision_output(String rname, int component) {
        ArrayList<Integer> ranks = new ArrayList<Integer>();
        int count = 0;
        for (int i = 0; i < Program.decisionSituations[component].size(); i++) {
            double highestPriority = Double.NEGATIVE_INFINITY;
            int topindex = -1;
            for (int j = 0; j < Program.decisionSituations[component].get(i).length; j++) {
                double[] inputdat = new double[Program.sch_terminals[component].length];
                for (int k = 0; k < Program.sch_terminals[component].length; k++) {
                    inputdat[k] = Program.decisionSituations[component].get(i)[j][k];
                }
                double priority = 0;
                if (component==0) {
                    priority = SchedulingSampleReference.benchmark_sequencing(inputdat, rname);
                } else {
                    priority = SchedulingSampleReference.benchmark_routing(inputdat, rname);
                }
                count++;
                if (highestPriority < priority) {
                    highestPriority = priority;
                    topindex = j;
                }
            }
            ranks.add(Program.refranks[component].get(i)[topindex]);
        }
        return ranks;
    }

    ArrayList<Integer> samplingdecision_output(String rname) {
        ArrayList<Integer> ranks = new ArrayList<Integer>();
        for (int component = 0; component < Program.nComponent; component++) {
            ranks.addAll(samplingdecision_output(rname, component));
        }
        return ranks;
    }
}
