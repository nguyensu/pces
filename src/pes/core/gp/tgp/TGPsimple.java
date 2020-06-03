/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pes.core.gp.tgp;

import edu.princeton.cs.algs4.IndexMaxPQ;
import edu.princeton.cs.algs4.SET;
import pes.tda.mapping.Mapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is a simple genetic programming algorithm using tree-based representation.
 */
public class TGPsimple extends TGPcore {
    /**
     * get the name of the algorithm
     *
     * @return name of the algorithm
     */
    @Override
    public String name() {
        return "tgpsimple";
    }

    /**
     * constructor
     *
     * @param seed random seed
     */
    public TGPsimple(long seed, PrintWriter writer) {
        super(seed, writer);
    }

    public static ArrayList<Integer>[] benchmarkSample;
    public static String[] benchmarkName = null;
    public static int time_window = 50;

    double[] surrogatefit;
    ArrayList<Integer>[] surrogateSample;
    SET<String> vectorset;

    /**
     * start evolving GP program
     */
    public void evolve() throws IOException, InterruptedException {
        Mapper.MAXDATA = POPSIZE * 10000;
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
            surrogateSample = new ArrayList[POPSIZE];
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<Double> showvals = new ArrayList<>();
            ArrayList<Integer> prog_sizes = new ArrayList<>();

            for (int indivs = 0; indivs < POPSIZE; indivs++) {
                surrogateSample[indivs] = samplingdecision_output(pop[indivs]);
                if (Mapper.CROSS_GENERATION) {
//                    labels.add(String.valueOf(gen-1));
                    labels.add("");
                    showvals.add((double) (pop[indivs].fitness));
                    prog_sizes.add(pop[indivs].total_length());
                } else {
                    //labels.add(pop[indivs].toString());
                    labels.add("");
                    showvals.add(pop[indivs].fitness);
                    prog_sizes.add(pop[indivs].total_length());
                }
            }
            if (gen % 1 == 0 || gen == 1) {
                Mapper.interactive = false;
                Mapper.mapping(surrogateSample, labels, prog_sizes, showvals, 1000, !Mapper.CROSS_GENERATION || gen == 1, false, "simplegp_nonimprove_" + nonimprove_step + "_fit_" + best_program.fitnessDEEP + "__BLength_" + best_program.total_length() + "__GEN_" + (GEN - 1) + ".png");
            }

            //copy good programs to the next generation
            Program[] newpop = new Program[POPSIZE]; // new population
            double[] newfitness = new double[POPSIZE]; // fitness of programs in the new population
            int startreproduce = 0; //number of programs already in the new population
            int TRIALSIZE = POPSIZE;
            Program[] trialpop = new Program[TRIALSIZE]; // new population

            for (int indivs = 0; indivs < TRIALSIZE; indivs++) {
                double r = rd.nextDouble();
                if (r < CROSSOVER_PROB) { //crossover
                    // select two parent programs
                    Program[] childprog = Program.crossover(pop, TSIZE, rd);
                    for (int i = 0; i < 2; i++) {
                        if (i == 1) {
                            if (indivs == TRIALSIZE - 1) break;
                            else {
                                indivs++;
                            }
                        }
                        trialpop[indivs] = childprog[i];
                    }
                } else if (r < CROSSOVER_PROB + MUTATION_PROB) { //mutation
                    Program childProgram = Program.mutation(pop, TSIZE, rd);
                    trialpop[indivs] = childProgram;
                } else { //reproduction
                    Program newind;
                    int parent = tournament(TSIZE);
                    newind = pop[parent].copy();
                    trialpop[indivs] = newind;
                    continue;
                }
            }
            // choose individuals with best approximate fitness to the next generation
            for (int i = startreproduce; i < POPSIZE; i++) {
                newpop[i] = trialpop[i];
                newpop[i].fitness = fitness_function(trialpop[i]);
            }
            pop = newpop;
            //obtain statistic for this generation
            stats(pop, gen);
        }
        log.close();
        //perform a post evolution process such as testing ...
        post_evolution();
        System.out.print("PROBLEM *NOT* SOLVED\n ******************************************\n");
        System.out.println("Best program so far: ");
        // print out the best obtained program
        System.out.println(best_program);
        ;
        System.out.println("\n Best_fitness = " + bestfitness);
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
     *
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
                rank[sort.delMax()] = j + 1;
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
            ranks.addAll(samplingdecision_output(Prog.prog[component], component));
        }
        return ranks;
    }

    static double compareRules(ArrayList<Integer> a, ArrayList<Integer> b) {
        if (a.size() != b.size()) {
            System.err.println("dimensions of decision vectors are not matched!!!");
            System.exit(0);
        }
        double similarity = 0;
        for (int i = 0; i < a.size(); i++) {
            similarity += (a.get(i) - b.get(i)) * (a.get(i) - b.get(i));
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

    }

    @Override
    public double fitness_function(Program Prog, int seed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
