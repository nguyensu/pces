/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package problems.rcjs;

import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.SET;
import pes.core.gp.tgp.Program;
import pes.core.gp.tgp.TGPcore;
import pes.tda.mapping.Mapper;
import pes.utilites.StopwatchCPU;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is the adaptive charting genetic algorithm (Su Nguyen et al, GECCO'2018). The algorithm combines the surrogate-assisted
 * genetic programming and a visualisation components to control the diversity and size of evolved programs.
 */

public class PCES_grow_rcjs extends TGPcore {
    /**
     * get the name of the algorithm
     * @return name of the algorithm
     */
    @Override
    public String name() {
        return "pes";
    }
    public static String subtitle;
    /**
     * constructor
     * @param seed random seed
     */
    public PCES_grow_rcjs(long seed, PrintWriter writer) {
        super (seed, writer);
    }
    public static int time_window = 50;
    public static int ISIZE = 10;
    public static boolean guidedopt = true;
    public static double fitness_weight = 1.0;

    double[] surrogatefit;
    double[][] surrogateSample;
    SET<String> vectorset;
    static long MAXTIME = 60;

    /**
     * start evolving GP program
     */
    public void evolve() throws IOException, InterruptedException {
        System.out.println("Start evolving ...");
//        Visualisation.size_scale = 3;
        timer = new StopwatchCPU();
        Mapper.MAXDATA = POPSIZE*time_window; // very much record all generated programs
        best_program = null;
        bestfitness = Double.POSITIVE_INFINITY;
        int gen = 0;
        // initialisation
        pop = create_random_pop(POPSIZE, INIT_DEPTH);

        // obtain statistic
        stats(pop, 0);
        newImprove = false;
        for (int i = 0; i < POPSIZE; i++) {
            get_phenotype(pop[i]);
        }
        // start a generation
        for (gen = 1; gen < GENERATIONS; gen++) {
            surrogatefit = new double[POPSIZE];
            for (int i = 0; i < POPSIZE; i++) {
                surrogatefit[i] = pop[i].fitness;
            }
            surrogateSample = new double[POPSIZE][];
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<Double> fitvals = new ArrayList<>();
            ArrayList<Integer> prog_sizes = new ArrayList<>();
            int allnodecount = 0;
            for (int indivs = 0; indivs < POPSIZE; indivs++) {
                surrogateSample[indivs] = pop[indivs].phenotype;
                allnodecount += pop[indivs].total_length();
                if (Mapper.CROSS_GENERATION) {
                    labels.add("");
                    fitvals.add(pop[indivs].fitness);
                    prog_sizes.add(pop[indivs].total_length());
                } else {
                    labels.add("");
                    fitvals.add(pop[indivs].fitness);
                    prog_sizes.add(pop[indivs].total_length());
                }
            }
            if (gen%1==0||gen==1) {
                Mapper.interactive = false; // turn on/off the interactive mode
                Mapper.mapping(surrogateSample, labels, prog_sizes, fitvals,(GEN==1)? 500: 500, !Mapper.CROSS_GENERATION || gen==1, false,  "--nonimprove"+nonimprove_step+"_fit_"+best_program.fitnessDEEP+"__Totalsize_"+allnodecount+"__GEN_"+(GEN-1)+".png\n"+subtitle);
//                break;
            }
            //copy good programs to the next generation
            Program[] newpop = new Program[POPSIZE]; // new population
            double[] newfitness = new double[POPSIZE]; // fitness of programs in the new population
            int startreproduce = 0; //number of programs already in the new population
            vectorset = new SET<String>();
            int TRIALSIZE = POPSIZE*ISIZE;
            Program[] trialpop = new Program[TRIALSIZE]; // new population
//            trialpop[0] = best_program;
            double[] approximatefit = new double[TRIALSIZE]; // fitness of programs in the new population
            IndexMinPQ<Double> ranking = new IndexMinPQ<Double>(TRIALSIZE);
            for (int indivs = 0; indivs < TRIALSIZE; indivs++) {
                int[] genops = {4,5};
                if (Program.nComponent == 1 | !guidedopt) {
                    genops = new int[] {0, 1};
                }
                if (guidedopt) {
                    genops = new int[] {4, 5};
                }
                int genid = genops[rd.nextInt(genops.length)];
                Program childprog = null;
                switch (genid) {
                    case 0: // crossover
                        childprog = Program.crossover(pop,TSIZE,rd)[0];
                        break;
                    case 1: // mutation
                        childprog = Program.mutation(pop,TSIZE,rd);
                        break;
                    case 2: // random extraction
                        childprog = Program.random_extraction(pop,TSIZE,rd);
                        break;
                    case 3: // crossover random trees
                        childprog = Program.crossover_noalign(pop,TSIZE,5,rd)[0];
                        break;
                    case 4: // crossover random trees
                        childprog = Program.crossover_noalign_dominance(pop,TSIZE,5,rd)[0];
                        break;
                    case 5: // crossover random trees
                        childprog = Program.mutation_dominance(pop,TSIZE,rd);
                        break;
                    default : // Optional
                        System.out.println("Genetic ID" + genid + " does not exist!");
                        System.exit(0);
                }
                trialpop[indivs] = childprog;
                get_phenotype(trialpop[indivs]);
            }

            // predict the fitness of trial programs
            Mapper.gngmodel.resetPool();
            for (int indivs = 0; indivs < TRIALSIZE; indivs++) {
                double lambda = fitness_weight;
                double surrogate_near_gen = fitness_function_light(trialpop[indivs]);
                double surrogate_gng = Mapper.gngmodel.surrogate_gng(trialpop[indivs].phenotype);
                double density_gng = 1; //Mapper.gngmodel.density_gng(trialpop[indivs].phenotype);
                approximatefit[indivs] = density_gng*(lambda*surrogate_near_gen + (1-lambda)*surrogate_gng)*duplicate(trialpop[indivs]);
                ranking.insert(indivs, approximatefit[indivs]);
            }
            // choose individuals with best approximate fitness to the next generation
            for (int i = startreproduce; i < POPSIZE; i++) {
                int topindex = ranking.delMin();
                newpop[i] = trialpop[topindex];
                newpop[i].fitness = fitness_function(trialpop[topindex]);
            }
            pop = newpop;
            //obtain statistic for this generation
            stats( pop, gen);
            if (timer.elapsedTime() > MAXTIME) {
                break;
            }
        }
//        best_program = pop[bestpopgen].copy();
        log.close();
        //perform a post evolution process such as testing ...
        post_evolution();
        System.out.print("PROBLEM *NOT* SOLVED\n ******************************************\n");
        System.out.println("Best program so far: ");
        // print out the best obtained program
        System.out.println(best_program);; System.out.println("\n Best_fitness = " + bestfitness);
        System.out.println(best_program.toStringInt());
    }

    public double duplicate(Program Prog) {
        String vectorStr = convert(Prog.phenotype);
        if (vectorset.contains(vectorStr))
            return Double.POSITIVE_INFINITY;
        vectorset.add(vectorStr);
        return 1;
    }

    public double fitness_function_light(Program Prog) {
//        String vectorStr = convert(Prog.phenotype);
//        if (vectorset.contains(vectorStr))
//            return Double.POSITIVE_INFINITY;
//        vectorset.add(vectorStr);
        int nearestIndex = 0;
        double difference = compareRules(Prog.phenotype, surrogateSample[0]);
        for (int i = 1; i < surrogateSample.length; i++) {
            double corr = compareRules(Prog.phenotype, surrogateSample[i]);
            if (difference > corr) {
                difference = corr;
                nearestIndex = i;
            }
        }
        return surrogatefit[nearestIndex];
    }

    static double compareRules(double[] a, double[] b) {
        if (a.length!=b.length) {
            System.err.println("dimensions of decision vectors are not matched!!!");
            System.exit(0);
        }
        double similarity = 0;
        for (int i = 0; i < a.length; i++) {
            similarity += (a[i] - b[i])*(a[i] - b[i]);
        }
        return Math.sqrt(similarity);
    }

    @Override
    public void setup_fitness() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double fitness_function(Program Prog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] fitness_function_deep_mo(Program Prog) {
        return new double[0];
    }

    public double[] get_phenotype(Program Prog) {
        return new double[0];
    }

    @Override
    public double[] fitness_function_mo(Program Prog) {
        return new double[0];
    }

    @Override
    public double fitness_function_deep(Program Prog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public double fitness_function(Program[] Prog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public double post_evolution() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public double finalise_generation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setupSample() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void define_terminal_function() {
            throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double fitness_function(Program Prog, int seed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
