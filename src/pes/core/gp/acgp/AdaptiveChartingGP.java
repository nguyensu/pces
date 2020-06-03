/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pes.core.gp.acgp;

import edu.princeton.cs.algs4.IndexMaxPQ;
import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.SET;
import pes.core.gp.tgp.Program;
import pes.core.gp.tgp.TGPcore;
import pes.tda.mapping.Mapper;
import pes.tda.mapping.Visualisation;

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

public class AdaptiveChartingGP extends TGPcore {
    /**
     * get the name of the algorithm
     * @return name of the algorithm
     */
    @Override
    public String name() {
        return "pes";
    }

    /**
     * constructor
     * @param seed random seed
     */
    public AdaptiveChartingGP(long seed, PrintWriter writer) {
        super (seed, writer);
    }
    public static int time_window = 50;

    double[] surrogatefit;
    ArrayList<Integer>[] surrogateSample;
    SET<String> vectorset;

    /**
     * start evolving GP program
     */
    public void evolve() throws IOException, InterruptedException {
//        Visualisation.size_scale = 3;
        Mapper.MAXDATA = POPSIZE*time_window; // very much record all generated programs
        setupSample();
        best_program = null;
        bestfitness = Double.POSITIVE_INFINITY;
        int gen = 0;
        // initialisation
        pop = create_random_pop(POPSIZE, INIT_DEPTH);
        // obtain statistic
        stats(pop, 0);
        newImprove = false;

        // start a generation
        for (gen = 1; gen < GENERATIONS; gen++) {
            surrogatefit = new double[POPSIZE];
            for (int i = 0; i < POPSIZE; i++) {
                surrogatefit[i] = pop[i].fitness;
            }
            surrogateSample = new ArrayList[POPSIZE];
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<Double> fitvals = new ArrayList<>();
            ArrayList<Integer> prog_sizes = new ArrayList<>();

            for (int indivs = 0; indivs < POPSIZE; indivs++) {
                surrogateSample[indivs] = samplingdecision_output(pop[indivs]);
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
                Visualisation.size_scale = 2;
                if (gen%1 == 0) {
                    Mapper.interactive = true; // turn on/off the interactive mode
                } else {
                    Mapper.interactive = false;
                }
                Mapper.mapping(surrogateSample, labels, prog_sizes, fitvals,10000, !Mapper.CROSS_GENERATION || gen==1, false, "mini_nonimprove_"+nonimprove_step+"_fit_"+best_program.fitnessDEEP+"__BLength_"+best_program.total_length()+"__GEN_"+(GEN-1)+".png");
//                break;
            }
            //copy good programs to the next generation
            Program[] newpop = new Program[POPSIZE]; // new population
            double[] newfitness = new double[POPSIZE]; // fitness of programs in the new population
            int startreproduce = 0; //number of programs already in the new population
            vectorset = new SET<String>();
            int TRIALSIZE = POPSIZE*10;
            Program[] trialpop = new Program[TRIALSIZE]; // new population
            double[] approximatefit = new double[TRIALSIZE]; // fitness of programs in the new population
            IndexMinPQ<Double> ranking = new IndexMinPQ<Double>(TRIALSIZE);
            ArrayList<Integer>[] trial_phenotype = new ArrayList[TRIALSIZE];
            for (int indivs = 0; indivs < TRIALSIZE; indivs++) {
                int[] genops = {0, 1, 2};
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
                    default : // Optional
                        System.out.println("Genetic ID" + genid + " does not exist!");
                        System.exit(0);
                }
                trialpop[indivs] = childprog;
                trial_phenotype[indivs] = samplingdecision_output(trialpop[indivs]);
            }
            // predict the fitness of trial programs
            Mapper.gngmodel.resetPool();
            for (int indivs = 0; indivs < TRIALSIZE; indivs++) {
                double control_factor = Mapper.gngmodel.control_factor(trial_phenotype[indivs], trialpop[indivs].total_length());
                approximatefit[indivs] =  (fitness_function_light(trialpop[indivs]))*control_factor;
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

    public double fitness_function_light(Program Prog) {
        ArrayList<Integer> sampleProg = samplingdecision_output(Prog);
        String vectorStr = convert(sampleProg);
        if (vectorset.contains(vectorStr))
            return Double.POSITIVE_INFINITY;
        int nearestIndex = 0;
        double difference = compareRules(sampleProg, surrogateSample[0]);
        for (int i = 1; i < surrogateSample.length; i++) {
            double corr = compareRules(sampleProg, surrogateSample[i]);
            if (difference > corr) {
                difference = corr;
                nearestIndex = i;
            }
        }
        if (difference == 0)
            return Double.POSITIVE_INFINITY;
        vectorset.add(vectorStr);
        return surrogatefit[nearestIndex];

    }

    /**
     * calculate some outputs from a given program
     * @param prog given program to be sampled
     * @return sampling output
     */
    public ArrayList<int[]> samplingdecisionREF_output(char[] prog, int component) {
        ArrayList<int[]> ranks = new ArrayList<int[]>();
        for (int i = 0; i < Program.decisionSituations[component].size(); i++) {
            IndexMaxPQ<Double> sort = new IndexMaxPQ<Double>(targets.length);
            for (int j = 0; j < Program.decisionSituations[component].get(i).length; j++) {
                for (int k = 0; k < Program.sch_terminals[component].length; k++) {
                    input_vec[k] = Program.decisionSituations[component].get(i)[j][k];
                }
                program = prog;
                PC = 0;
                sort.insert(j, run());
            }
            int[] rank = new int[Program.decisionSituations[component].get(i).length];
            for (int j = 0; j < Program.decisionSituations[component].get(i).length; j++) {
                rank[sort.delMax()] = j+1;
            }
            ranks.add(rank);
        }
        return ranks;
    }

    ArrayList<Integer> samplingdecision_output(char[] prog, int component) {
        ArrayList<Integer> ranks = new ArrayList<Integer>();
        int count = 0;
        for (int i = 0; i < Program.decisionSituations[component].size(); i++) {
            double highestPriority = Double.NEGATIVE_INFINITY;
            int topindex = -1;
            for (int j = 0; j < Program.decisionSituations[component].get(i).length; j++) {
                for (int k = 0; k < Program.sch_terminals[component].length; k++) {
                    input_vec[k] = Program.decisionSituations[component].get(i)[j][k];
                }
                program = prog;
                PC = 0;
                double priority = run();
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

    ArrayList<Integer> samplingdecision_output(Program Prog) {
        ArrayList<Integer> ranks = new ArrayList<Integer>();
        for (int component = 0; component < Program.nComponent; component++) {
            ranks.addAll(samplingdecision_output(Prog.prog[component],component));
        }
        return ranks;
    }

    static double compareRules(ArrayList<Integer> a, ArrayList<Integer> b) {
        if (a.size()!=b.size()) {
            System.err.println("dimensions of decision vectors are not matched!!!");
            System.exit(0);
        }
        double similarity = 0;
        for (int i = 0; i < a.size(); i++) {
            similarity += (a.get(i) - b.get(i))*(a.get(i) - b.get(i));
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
