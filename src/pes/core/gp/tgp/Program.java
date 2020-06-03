package pes.core.gp.tgp;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is a template for evolved programs with multiple trees. This template
 * can be reused to generate programs for different applications.
 */

public class Program implements Comparable<Program>{
    public static TGPcore gp;
    public static int nComponent = 2;
    public static double gendeeofut = -1;
    public static String[][] sch_terminals = new String[nComponent][];
    public static double[][] inputs = new double[nComponent][];
    public static int[][] functions = new int[nComponent][];;
    public static ArrayList<double[][]>[] decisionSituations = new ArrayList[nComponent];
    public static ArrayList<int[]>[] refranks = new ArrayList[nComponent];
    public char[][] prog = new char[nComponent][];
    public double fitness = -1;
    public double afit = 0;
    public double fitnessDEEP = -1;
    public double fit_est = 0;
    public double age = 0;
    public double[] phenotype = null;
    public double[] dominance = null;

    public static Program[] crossover_multitree(Program[] pop, int TSIZE, double rate, Random rd) {
        Program[] childProgram = new Program[2];
        for (int i = 0; i < 2; i++) {
            childProgram[i] = new Program();
        }
        // select two parent programs
        int parent1 = gp.tournament(TSIZE);
        int parent2 = gp.tournament(TSIZE);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[][] childinds = new char[2][];
            if (rd.nextDouble() < rate) {
                do {
                    childinds = gp.crossover_subtree(pop[parent1].prog[k], pop[parent2].prog[k]);
                } while (gp.find_depth(childinds[0]) > gp.MAXDEPTH || gp.find_depth(childinds[1]) > gp.MAXDEPTH);
            } else {
                childinds[0] = pop[parent1].prog[k].clone();
                childinds[1] = pop[parent2].prog[k].clone();
            }
            for (int i = 0; i < 2; i++) {
                childProgram[i].prog[k] = childinds[i];
            }
        }
        return childProgram;
    }

    public static Program[] crossover(Program[] pop, int TSIZE, Random rd) {
        Program[] childProgram = new Program[2];
        for (int i = 0; i < 2; i++) {
            childProgram[i] = new Program();
        }
        // select two parent programs
        int parent1 = gp.tournament(TSIZE);
        int parent2 = gp.tournament(TSIZE);
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[][] childinds = new char[2][];
            if (k==selectComponent) {
                do {
                    childinds = gp.crossover_subtree(pop[parent1].prog[k], pop[parent2].prog[k]);
                } while (gp.find_depth(childinds[0]) > gp.MAXDEPTH || gp.find_depth(childinds[1]) > gp.MAXDEPTH);
            } else {
                childinds[0] = pop[parent1].prog[k].clone();
                childinds[1] = pop[parent2].prog[k].clone();
            }
            for (int i = 0; i < 2; i++) {
                childProgram[i].prog[k] = childinds[i];
            }
        }
        return childProgram;
    }

    public static Program[] crossover_noalign_dominance(Program[] pop, int TSIZE, int ntrials, Random rd) {
        Program[] childProgram = new Program[2];
        for (int i = 0; i < 2; i++) {
            childProgram[i] = new Program();
        }
        // select two parent programs
        int parent1 = gp.tournament(TSIZE);
        int parent2 = gp.tournament(TSIZE);
        int selectComponent1 = 0; int selectComponent2 = 0;
        for (int k = 0; k < nComponent; k++) {
            childProgram[0].prog[k] = pop[parent1].prog[k].clone();
            childProgram[1].prog[k] = pop[parent2].prog[k].clone();
        }
        for (int i = 0; i < ntrials; i++) {
            selectComponent1 = argmin_stochastic1(pop[parent1].dominance, rd);
            selectComponent2 = argmax_stochastic1(pop[parent2].dominance, rd); //rd.nextInt(nComponent);
            char[][] childinds = new char[2][];
            do {
                childinds = gp.crossover_subtree(pop[parent1].prog[selectComponent1], pop[parent2].prog[selectComponent2]);
            } while (gp.find_depth(childinds[0]) > gp.MAXDEPTH || gp.find_depth(childinds[1]) > gp.MAXDEPTH);
            childProgram[0].prog[selectComponent1] = childinds[0];
            childProgram[1].prog[selectComponent2] = childinds[1];
        }
        return childProgram;
    }

    public static Program[] crossover_noalign(Program[] pop, int TSIZE, int ntrials, Random rd) {
        Program[] childProgram = new Program[2];
        for (int i = 0; i < 2; i++) {
            childProgram[i] = new Program();
        }
        // select two parent programs
        int parent1 = gp.tournament(TSIZE);
        int parent2 = gp.tournament(TSIZE);
        int selectComponent1 = 0; int selectComponent2 = 0;
        for (int k = 0; k < nComponent; k++) {
            childProgram[0].prog[k] = pop[parent1].prog[k].clone();
            childProgram[1].prog[k] = pop[parent2].prog[k].clone();
        }
        for (int i = 0; i < ntrials; i++) {
            selectComponent1 = rd.nextInt(nComponent);
            selectComponent2 = rd.nextInt(nComponent);
            char[][] childinds = new char[2][];
            do {
                childinds = gp.crossover_subtree(pop[parent1].prog[selectComponent1], pop[parent2].prog[selectComponent2]);
            } while (gp.find_depth(childinds[0]) > gp.MAXDEPTH || gp.find_depth(childinds[1]) > gp.MAXDEPTH);
            childProgram[0].prog[selectComponent1] = childinds[0];
            childProgram[1].prog[selectComponent2] = childinds[1];
        }
        return childProgram;
    }

    public static Program[] crossover(Program bestprog, Program[] pop, int TSIZE, Random rd) {
        Program[] childProgram = new Program[2];
        for (int i = 0; i < 2; i++) {
            childProgram[i] = new Program();
        }
        // select two parent programs
        int parent1 = gp.tournament(TSIZE);
        int parent2 = gp.tournament(TSIZE);
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[][] childinds = new char[2][];
            if (k==selectComponent) {
                do {
                    childinds = gp.crossover_subtree(bestprog.prog[k], pop[parent2].prog[k]);
                } while (gp.find_depth(childinds[0]) > gp.MAXDEPTH || gp.find_depth(childinds[1]) > gp.MAXDEPTH);
            } else {
                childinds[0] = pop[parent1].prog[k].clone();
                childinds[1] = pop[parent2].prog[k].clone();
            }
            for (int i = 0; i < 2; i++) {
                childProgram[i].prog[k] = childinds[i];
            }
        }
        return childProgram;
    }

    public static Program[] crossover(Program[] parents, Random rd) {
        Program[] childProgram = new Program[2];
        for (int i = 0; i < 2; i++) {
            childProgram[i] = new Program();
        }
        // select two parent programs
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[][] childinds = new char[2][];
            if (k==selectComponent) {
                do {
                    childinds = gp.crossover_subtree(parents[0].prog[k], parents[1].prog[k]);
                } while (gp.find_depth(childinds[0]) > gp.MAXDEPTH || gp.find_depth(childinds[1]) > gp.MAXDEPTH);
            } else {
                childinds[0] = parents[0].prog[k].clone();
                childinds[1] = parents[1].prog[k].clone();
            }
            for (int i = 0; i < 2; i++) {
                childProgram[i].prog[k] = childinds[i];
            }
        }
        return childProgram;
    }

    public static Program mutation_dominance(Program[] pop, int TSIZE, Random rd) {
        // select two parent programs
        int parent = gp.tournament(TSIZE);
        Program childProgram = new Program();
        int selectComponent = 0;
        selectComponent = argmin_stochastic1(pop[parent].dominance, rd);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[] childind;
            if (k==selectComponent) {
                do {
                    childind = gp.mutation_subtree(pop[parent].prog[k]);
                }   while (gp.find_depth(childind) > gp.MAXDEPTH);
            } else {
                childind = pop[parent].prog[k].clone();
            }
            childProgram.prog[k] = childind;
        }
        return childProgram;
    }

    public static Program mutation(Program[] pop, int TSIZE, Random rd) {
        // select two parent programs
        int parent = gp.tournament(TSIZE);
        Program childProgram = new Program();
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[] childind;
            if (k==selectComponent) {
                do {
                    childind = gp.mutation_subtree(pop[parent].prog[k]);
                }   while (gp.find_depth(childind) > gp.MAXDEPTH);
            } else {
                childind = pop[parent].prog[k].clone();
            }
            childProgram.prog[k] = childind;
        }
        return childProgram;
    }

    public static Program random_extraction(Program[] pop, int TSIZE, Random rd) {
        // select two parent programs
        int parent = gp.tournament(TSIZE);
        Program childProgram = new Program();
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[] childind;
            if (k==selectComponent) {
                int rpos =  gp.select_random_point(pop[parent].prog[k]);
                childind = gp.extractSubtree(pop[parent].prog[k], rpos);
            } else {
                childind = pop[parent].prog[k].clone();
            }
            childProgram.prog[k] = childind;
        }
        return childProgram;
    }

    public static Program mutation(Program bestprog, Program[] pop, double[] fitness, int TSIZE, Random rd) {
        // select two parent programs
        int parent = gp.tournament(TSIZE);
        Program childProgram = new Program();
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[] childind;
            if (k==selectComponent) {
                do {
                    childind = gp.mutation_subtree(bestprog.prog[k]);
                }   while (gp.find_depth(childind) > gp.MAXDEPTH);
            } else {
                childind = pop[parent].prog[k].clone();
            }
            childProgram.prog[k] = childind;
        }
        return childProgram;
    }

    public static Program mutation(Program parentprog, Random rd) {
        // select two parent programs
        Program childProgram = new Program();
        int selectComponent = 0;
        selectComponent = rd.nextInt(nComponent);
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            char[] childind;
            if (k==selectComponent) {
                do {
                    childind = gp.mutation_subtree(parentprog.prog[k]);
                }   while (gp.find_depth(childind) > gp.MAXDEPTH);
            } else {
                childind = parentprog.prog[k].clone();
            }
            childProgram.prog[k] = childind;
        }
        return childProgram;
    }

    public static void switch_component(int k) {
        gp.input_vec = Program.inputs[k];
        gp.function_set = Program.functions[k];
        gp.varnumber = Program.sch_terminals[k].length;
        gp.sch_terminal = Program.sch_terminals[k];
    }

    Program copy() {
        Program newprog = new Program();
        for (int i = 0; i < prog.length; i++) {
            newprog.prog[i] = prog[i].clone();
        }
        newprog.fitness = fitness;
        newprog.fitnessDEEP = fitnessDEEP;
        newprog.age = age;
        return newprog;
    }

    boolean isBetter(Program program) {
        if (fitness < program.fitness) return true;
        return false;
    }

    boolean isBetterDEEP(Program program) {
        if (fitnessDEEP < program.fitnessDEEP) return true;
        return false;
    }

    public String toString() {
        String all = fitness + " ~~ ";
        all += "age = " + age + " ~~ ";
        all += "est = " + fit_est + " ~~ ";
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            all += gp.print_indiv_readable(prog[k],0) + ";";
        }
        return all;
    }
    public String toStringInt() {
        String all = "";
        for (int k = 0; k < nComponent; k++) {
            switch_component(k);
            all += gp.print_program_integer(prog[k]) + ";";
        }
        return all;
    }

    public int total_length() {
        int total = 0;
        for (int k = 0; k < nComponent; k++) {
            total += prog[k].length;
        }
        return total;
    }

    @Override
    public int compareTo(Program o) {
        double fit = o.afit;
        if (afit < fit) return -1;
        else if (afit == fit) return 0;
        else return 1;
    }

    public Program clone() {
        Program newprogram = new Program();
        for (int i = 0; i < nComponent; i++) {
            newprogram.prog[i] = prog[i].clone();
        }
        return newprogram;
    }

    public static Program[] clone(Program[] oldpop) {
        Program[] newpop = new Program[oldpop.length];
        for (int i = 0; i < oldpop.length; i++) {
            newpop[i] = oldpop[i].clone();
        }
        return newpop;
    }

    public static int argmin_stochastic1(double[] x, Random rd) {
        int argi = 0;
        double min = x[0];
        for (int i = 1; i < x.length; i++) {
            if (min > x[i]) {
                min = x[i];
                argi = i;
            } else if (min == x[i]) {
                if (rd.nextBoolean()) {
                    argi = i;
                }
            }
        }
        return argi;
    }

    public static int argmax_stochastic1(double[] x, Random rd) {
        int argi = 0;
        double max = x[0];
        for (int i = 1; i < x.length; i++) {
            if (max < x[i]) {
                max = x[i];
                argi = i;
            } else if (max == x[i]) {
                if (rd.nextBoolean()) {
                    argi = i;
                }
            }
        }
        return argi;
    }

}
