package pes.core.gp.tgp;

/**
 *
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 *
 * This is the core of our gp engine.
 * The program is a represented as a flattened tree.
 * This is an extention of TinyGP developed by Riccardo Poli.
 */

import pes.tda.mapping.Visualisation;
import pes.utilites.StopwatchCPU;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public abstract class TGPcore {
    public static String[] sch_terminal = {}; //variable names for evolving dispatching rules
    public boolean newImprove = false;
    // some parameter/variables used in symbolic regression (can be ignored)
    public static double[][] targets; // training set of supervised learning tasks
    int fitnesscount = 0; // number of fitness evaluation
    // gp status
    public int GEN = 0; // current generation
    char[][] inits;
    // population and fitness
    public Program[] pop;
    public int start_eval_at = 0;
    public int past_percent = -1;
    // definition of function set
    public static final int ADD = 20210,
            SUB = 20211,
            MUL = 20212,
            DIV = 20213,
            MIN = 20214,
            MAX = 20215,
            NMIN = 20216,
            NMAX = 20217,
            ReLU = 20218;
    public int FSET_START = ADD,
            FSET_END = MAX;
    public int[] function_set = {ADD,SUB,MUL,DIV,MIN,MAX};
    int nBranch = 5; //number of branch in NMIN and NMAX
    char NONE;
    public char ZERO;
    char ONE;
    char TWO;
    // input vector (store the input features and random constant ...)
    public double[] input_vec = new double[FSET_START];
    // the minimum and maximum of random generated constant
    public double minrandom;
    public double maxrandom;
    // a temporal program
    public char[] program;
    // best program found by the run
    public Program best_program = null;
    public char[] ref_program = null;
    // best fitness obtained by the run
    public double bestfitness = Double.POSITIVE_INFINITY;
    // the position of the program being evaluated
    public int PC;
    // number of active terms
    int nTerms;
    // number of variables (features), number of fitness cases, number of random number
    public int varnumber;
    public int randomnumber;
    // the best and average fitness in the current generation
    public double fbestpop = 0.0, favgpop = 0.0;
    // index of best program in the population
    int bestpopgen = -1;
    // random seed
    long seedgp;
    // average length of program
    double avg_len;
    // log
    public PrintWriter log;
    // maximum depth of evolved programs
    int MAXDEPTH = 5;
    // number of non-improved steps
    public int nonimprove_step = 0;
    // GP search parameters
    int
            MAX_LEN = 10000; // maximum length of evolved programs
    public int POPSIZE = 1000; // population sizes
    public int INIT_DEPTH = 2; //initial depth of program
    public int GENERATIONS = 50; // number of generations
    public int TSIZE = 5 // tournament sizes
                    ;
    double pr_terminal_select = 0.1; // selection probability of terminal nodes
    double pr_nonterminal_select = 0.9; // selection probability of nonterminal nodes
    public double
            CROSSOVER_PROB = 0.9, // crossover probability
            MUTATION_PROB = 0.05; // mutation probability
    boolean show_run_stat = false; // whether to show the stat information from each generation
    int maxdepthsearch = -1; // the depth of a program (used in traverse function)
    ArrayList<Character> terms; int[][] tracking;
    public static String[] args; String humanReadableProgram = "";
    double[][] sampling_set; // sampling set used to measure the similarity of programs
    public double[] detaildeep; //detailed performance when performing deep evaluation
    public static Random rd = new Random(); // random number generator
    public double maxDistance = 0;
    public StopwatchCPU timer;


    /**
     * construction of pes
     * @param s random seed
     */
    public TGPcore(long s, PrintWriter writer) {
        define_terminal_function();
        log = writer;
        fitnesscount = 0;
        seedgp = s;
        if (seedgp >= 0) {
            rd.setSeed(seedgp);
        }
        setup_fitness();
        for (int k = 0; k < Program.nComponent; k++) {
            for (int i = 0; i < FSET_START; i++) {
                input_vec[i] = (maxrandom - minrandom) * rd.nextDouble() + minrandom;
            }
            Program.inputs[k] = input_vec.clone();
        }
        NONE = (char) (varnumber + ++randomnumber); input_vec[varnumber+randomnumber] = -1;
        ZERO = (char) (varnumber + ++randomnumber); input_vec[varnumber+randomnumber] = 0;
        ONE = (char) (varnumber + ++randomnumber); input_vec[varnumber+randomnumber] = 1;
        TWO = (char) (varnumber + ++randomnumber); input_vec[varnumber+randomnumber] = 2;
    }

    /**
     * evolution start here
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract void evolve() throws IOException, InterruptedException;

    /**
     * generate a random program with a predefined maximum depth
     * @param depth maximum depth
     * @return genreated program
     */
    char[] create_random_indiv(int depth) {
        char[] programBuffer = new char[MAX_LEN]; //used in program initilisation
        char[] ind;
        int len;
        boolean fullgen = rd.nextBoolean();
        if (fullgen) len = full(programBuffer, 0, MAX_LEN, depth);
        else len = grow(programBuffer, 0, MAX_LEN, depth);

        while (len < 0) {
            if (fullgen) len = full(programBuffer, 0, MAX_LEN, depth);
            else len = grow(programBuffer, 0, MAX_LEN, depth);
        }
        ind = new char[len];

        System.arraycopy(programBuffer, 0, ind, 0, len);
        return (ind);
    }

    /**
     * evaluate the program
     * @return the output of the program given the inputs input_vec
     */
    public double run() { /* Interpreter */
        char primitive = program[PC++];
        if (primitive < FSET_START) {
            return (input_vec[primitive]);
        }
        switch (primitive) {
            case ADD:
                return (run() + run());
            case SUB:
                return (run() - run());
            case MUL:
                return (run() * run());
            case DIV: {
                double num = run(), den = run();
                if (Math.abs(den) == 0) {
                    return (1);
                } else {
                    return (num / den);
                }
            }
            case MIN : {
                double a = run(), b = run();
                if ( a > b )
                    return( b );
                else
                    return( a );
            }
            case MAX : {
                double a = run(), b = run();
                if ( a < b )
                    return( b );
                else
                    return( a );
            }
            case NMIN : {
                double min = Double.POSITIVE_INFINITY;
                for (int i = 0; i < nBranch; i++) {
                    min = Math.min(min, run());
                }
                return min;
            }
            case NMAX : {
                double max = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < nBranch; i++) {
                    max = Math.max(max, run());
                }
                return max;
            }
            case ReLU : {
                double a = run();
                if (a>0) return 1;
                else return 0;
            }
        }
        return (0.0); // should never get here
    }

    /**
     * generate a random population
     * @param n number of program to be generated
     * @param depth maximum depth of generated program
     * @return obtained generated programs
     */
    public Program[] create_random_pop(int n, int depth) {
        Program[] pop = new Program[n];
        int i;
        for (i = 0; i < n; i++) {
            pop[i] = new Program();
            for (int k = 0; k < Program.nComponent; k++) {
                input_vec = Program.inputs[k];
                function_set = Program.functions[k];
                varnumber = Program.sch_terminals[k].length;
                sch_terminal = Program.sch_terminals[k];
                pop[i].prog[k] = create_random_indiv(depth);
            }
        }

        for (i = 0; i < n; i++) {
            pop[i].fitness = fitness_function(pop[i]);
        }
        return (pop);
    }



    /**
     * Create a random program with n components
     * @return
     */
    Program create_random_program() {
        Program newprog = new Program();
        for (int k = 0; k < Program.nComponent; k++) {
            input_vec = Program.inputs[k];
            function_set = Program.functions[k];
            varnumber = Program.sch_terminals[k].length;
            sch_terminal = Program.sch_terminals[k];
            newprog.prog[k] = create_random_indiv(INIT_DEPTH);
        }
        return newprog;
    }

    /**
     * find the depth of a subprogram (or subtree)
     * @param prog subprogram
     * @param pos the root node of the subtree
     * @param depth the current depth
     * @return the depth of the subprogram (or subtree)
     */
    int traverse_depth(char[] prog, int pos, int depth) {
        if (prog[pos] < FSET_START) {
            maxdepthsearch = Math.max(maxdepthsearch, depth);
            depth++;
            return ++pos;
        }

        switch (prog[pos]) {
            case ReLU:
                depth++;
                return (traverse_depth(prog, ++pos, depth));
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MIN:
            case MAX:
                depth++;
                int len = traverse_depth(prog, ++pos,depth);
                return (traverse_depth(prog, len, depth));
            case NMIN:
            case NMAX:
                depth++;
                int tlen = ++pos;
                for (int i = 0; i < nBranch; i++) {
                    tlen = traverse_depth(prog, tlen , depth);
                }
                return tlen;
        }
        return (0); // should never get here
    }

    /**
     * find the depth of a program
     * @param prog program
     * @return
     */
    int find_depth(char[] prog) {
        maxdepthsearch = -1;
        traverse_depth(prog, 0, 0);
        return maxdepthsearch;
    }

    /**
     * find the depth of a subtree
     * @param prog subtree
     * @return
     */
    int find_depth(char[] prog, int pos) {
        maxdepthsearch = -1;
        traverse_depth(prog, pos, 0);
        return maxdepthsearch;
    }

    /**
     * identify the type of nodes in the program tree
     * @param tlist list of terminal nodes
     * @param ntlist list of nonterminal nodes
     * @param prog program
     * @param pos the root node of the subtree
     * @return length of the program
     */
    int traverse_type(ArrayList<Integer> tlist, ArrayList<Integer> ntlist, char[] prog, int pos) {
        if (prog[pos] < FSET_START) {
            tlist.add(pos);
            return (++pos);
        }
        switch (prog[pos]) {
            case ReLU:
                ntlist.add(pos);
                return traverse_type(tlist, ntlist, prog, ++pos);
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MIN:
            case MAX:
                ntlist.add(pos);
                return (traverse_type(tlist, ntlist, prog, traverse_type(tlist, ntlist, prog, ++pos)));
            case NMIN:
            case NMAX:
                ntlist.add(pos);
                int tlen = ++pos;
                for (int i = 0; i < nBranch; i++) {
                    tlen = traverse_type(tlist, ntlist, prog, tlen);
                }
                return tlen;
        }
        return (0); // should never get here
    }

    /**
     * identify the type of nodes in the program tree
     * @param prog program
     * @param pos the root node of the subtree
     * @return length of the program
     */
    int tracking(char[] prog, int pos) {
        if (pos==0) tracking = new int[prog.length][3];
        if (prog[pos] < FSET_START) {
            return (++pos);
        }
        switch (prog[pos]) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MIN:
            case MAX:
                int currentpos = pos;
                tracking[currentpos][0] = pos+1;
                tracking[currentpos][1] = tracking(prog, ++pos);
                tracking[currentpos][2] = tracking(prog, tracking[currentpos][1]);
                return (tracking[currentpos][2]);
        }
        return (0); // should never get here
    }

    /**
     * reconfirm the length of the program (just in case there are something when wrong with the genetic operator
     * @param prog program
     * @param pos the root node of the subtree
     * @return length of the subtree
     */
    int traverse(char[] prog, int pos) {
        if (prog[pos] < FSET_START) {
            return (++pos);
        }

        switch (prog[pos]) {
            case ReLU:
                return traverse(prog, ++pos);
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MIN:
            case MAX:
                return (traverse(prog, traverse(prog, ++pos)));
            case NMIN:
            case NMAX:
                int tlen = ++pos;
                for (int i = 0; i < nBranch; i++) {
                    tlen = traverse(prog, tlen);
                }
                return tlen;
        }
        return (0); // should never get here
    }

    /**
     * default_setup the dataset .. for fitness evaluation
     * this is only called once
     */
    public abstract void setup_fitness();

    /**
     * a light fitness evaluation (approximate fitness)
     * @param Prog program
     * @return approximated fitness value
     */
    public abstract double fitness_function_light(Program Prog) ;

    /**
     * fitness from full evaluation (in validation step)
     * @param Prog program
     * @return reliable fitness value
     */
    public abstract double fitness_function_deep(Program Prog) ;

    /**
     * evaluate the fitness funtion
     * @param Prog program
     * @return fitness
     */
    public abstract double fitness_function(Program Prog) ;

    public abstract double[] fitness_function_deep_mo(Program Prog) ;

    /**
     * evaluate the fitness funtion
     * @param Prog program
     * @return fitness
     */
    public abstract double[] fitness_function_mo(Program Prog) ;

    /**
     * get the fitness for a particular case (or simulation replication with seed)
     * @param Prog program
     * @param seed case/example or random seed
     * @return fitness value
     */
    public abstract double fitness_function(Program Prog, int seed) ;


    /**
     * evaluate fitness of the group of program
     * @param Prog group of programs
     * @return average fitness of that group
     */
    public abstract double fitness_function(Program[] Prog) ;

    /**
     * what need to be done at the end of each generation (i.e. perform local search)
     * @return
     */
    public abstract double finalise_generation() ;

    /**
     * what need to be done at the end of the evolution (i.e testing)
     * @return
     */
    public abstract double post_evolution() ;

    /**
     * grow initialisation method
     * @param program program to be randomly generated
     * @param pos current position of program
     * @param max maximum length of a program
     * @param depth reverse depth of the program (0 if the maximum depth is reached)
     * @return -1 if there is something wrong
     */
    int grow(char[] program, int pos, int max, int depth) {
        char prim = (char) rd.nextInt(2);
        int one_child;
        if (pos >= max) {
            return (-1);
        }
        if (prim == 0 || depth == 0) {
            prim = (char) rd.nextInt(varnumber + 1);
            if (prim == varnumber) {
                prim = (char) (varnumber + rd.nextInt(randomnumber));
            }
            program[pos] = prim;
            return (pos + 1);
        } else {
            prim = (char) (function_set[rd.nextInt(function_set.length)]);
            switch (prim) {
                case ReLU:
                    program[pos] = prim;
                    return grow(program, pos + 1, max, depth - 1);
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MIN:
                case MAX:
                    program[pos] = prim;
                    one_child = grow(program, pos + 1, max, depth - 1);
                    if (one_child < 0) {
                        return (-1);
                    }
                    return (grow(program, one_child, max, depth - 1));
                case NMIN:
                case NMAX:
                    program[pos] = prim;
                    one_child = pos + 1;
                    for (int i = 0; i < nBranch; i++) {
                        one_child = grow(program, one_child, max, depth - 1);
                    }
                    return one_child;
            }
        }
        return (0); // should never get here
    }

    /**
     * full initialisation method
     * @param program program to be randomly generated
     * @param pos current position of program
     * @param max maximum length of a program
     * @param depth reverse depth of the program (0 if the maximum depth is reached)
     * @return -1 if there is something wrong
     */
    int full(char[] program, int pos, int max, int depth) {
        char prim = (char) rd.nextInt(2);
        int one_child;

        if (pos >= max) {
            return (-1);
        }

        if (depth == 0) {
            prim = (char) rd.nextInt(varnumber + 1);
            if (prim == varnumber) {
                prim = (char) (varnumber + rd.nextInt(randomnumber));
            }
            program[pos] = prim;
            return (pos + 1);
        } else {
            prim = (char) (function_set[rd.nextInt(function_set.length)]);
            switch (prim) {
                case ReLU:
                    program[pos] = prim;
                    return full(program, pos + 1, max, depth - 1);
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MIN:
                case MAX:
                    program[pos] = prim;
                    one_child = full(program, pos + 1, max, depth - 1);
                    if (one_child < 0) {
                        return (-1);
                    }
                    return (full(program, one_child, max, depth - 1));
                case NMIN:
                case NMAX:
                    program[pos] = prim;
                    one_child = pos + 1;
                    for (int i = 0; i < nBranch; i++) {
                        one_child = grow(program, one_child, max, depth - 1);
                    }
                    return one_child;
            }
        }
        return (0); // should never get here
    }

    /**
     * print the human-readable program
     * @param prog program to be printed
     * @param pos position of program to be printed
     * @return human readable form
     */
    String print_indiv_readable(char[] prog, int pos) {
        humanReadableProgram="";
        print_indiv(prog, pos);
        return humanReadableProgram;
    }

    /**
     * print the human-readable program
     * @param prog program to be printed
     * @param pos position of program to be printed
     * @return program position
     */
    int print_indiv(char[] prog, int pos) {
        int a1 = 0, a2;
        if (prog[pos] < FSET_START) {
            if (prog[pos] < varnumber) {
                humanReadableProgram+=( (sch_terminal[prog[pos]] )+ " ");
//                System.out.print("X" + (prog[pos] + 1) + " ");
            } else {
                humanReadableProgram+=(input_vec[prog[pos]]);
            }
            return (++pos);
        }
        switch (prog[pos]) {
            case ReLU:
                a1 = ++pos;
                humanReadableProgram+=("relu(");
                break;
            case ADD:
                humanReadableProgram+=("(");
                a1 = print_indiv(prog, ++pos);
                humanReadableProgram+=(" + ");
                break;
            case SUB:
                humanReadableProgram+=("(");
                a1 = print_indiv(prog, ++pos);
                humanReadableProgram+=(" - ");
                break;
            case MUL:
                humanReadableProgram+=("(");
                a1 = print_indiv(prog, ++pos);
                humanReadableProgram+=(" * ");
                break;
            case DIV:
                humanReadableProgram+=( "div (");
                a1=print_indiv( prog, ++pos );
                humanReadableProgram+=( " , ");
                break;
            case MIN:
                humanReadableProgram+=( "min (");
                a1=print_indiv( prog, ++pos );
                humanReadableProgram+=( " , ");
                break;
            case MAX:
                humanReadableProgram+=( "max (");
                a1=print_indiv( prog, ++pos );
                humanReadableProgram+=( " , ");
                break;
            case NMIN:
                humanReadableProgram+=( "nmin (" + nBranch + " , ");
                a1 = ++pos;
                for (int i = 0; i < nBranch - 1; i++) {
                    a1 = print_indiv( prog, a1);
                    humanReadableProgram+=( " , ");
                }
                break;
            case NMAX:
                humanReadableProgram+=( "nmax (" + nBranch + " , ");
                a1 = ++pos;
                for (int i = 0; i < nBranch - 1; i++) {
                    a1 = print_indiv( prog, a1);
                    humanReadableProgram+=( " , ");
                }
                break;
        }
        a2 = print_indiv(prog, a1);
        humanReadableProgram+=( ")");
        return (a2);
    }

    /**
     * statistic of a generation
     * @param pop population
     * @param gen current generation
     */
    public void stats(Program[] pop, int gen) {
        start_eval_at = 0;
        past_percent = -1;
        int i, best = rd.nextInt(POPSIZE);
        int node_count = 0;
        fbestpop = pop[best].fitness;
        favgpop = 0.0;
//        double avgFeature = 0;

        for (i = 0; i < POPSIZE; i++) {
            for (int k = 0; k < Program.nComponent; k++) {
                node_count += traverse(pop[i].prog[k], 0);
            }
            favgpop += pop[i].fitness;
            if (pop[i].fitness < fbestpop) {
                best = i;
                fbestpop = pop[i].fitness;
            }
        }
        avg_len = (double) node_count / POPSIZE;
        favgpop /= POPSIZE; //avgFeature /= POPSIZE;
        bestpopgen = best;
        double fbestpopdeep = fitness_function_deep(pop[best]);
        if (best_program==null || (fbestpopdeep < bestfitness)||((fbestpopdeep == bestfitness)&&(pop[best].total_length() < best_program.total_length()))) {
            bestfitness = fbestpopdeep;
            best_program = pop[best].copy();
            best_program.fitnessDEEP = fbestpopdeep;
            newImprove = true;
            nonimprove_step = 0;
        } else
        {
            nonimprove_step++;
        }
        if (show_run_stat) {
            System.out.print("\nGeneration=" + gen + " Avg Fitness=" + (favgpop)
                    + " Best Fitness=" + (fbestpop) + " Best Fitness (DEEP) = " + (bestfitness) + " Avg Size=" + avg_len
//                    + "\nMaskBest = " + arrayToString(best_program.mask)
//                    + "-- GlobalMask = " + arrayToString(gpcross_phenosur_noduplicate.globalMask)
//                    + "-- AvgFeature = " + avgFeature
                    + "\nBest Individual: ");
            if (timer == null) {
                log.println(String.format("%3d %10.5f %10.5f %10.5f %10.5f %d %d %d", gen, favgpop, fbestpop, avg_len, bestfitness, best_program.total_length(), Visualisation.ncomponents, Visualisation.nnodes));
            } else {
                log.println(String.format("%3d %10.5f %10.5f %10.5f %10.5f %10.5f %d %d %d", gen, timer.elapsedTime(), favgpop, fbestpop, avg_len, bestfitness, best_program.total_length(), Visualisation.ncomponents, Visualisation.nnodes));
            }
            log.flush();
            System.out.println(best_program.toString());
            System.out.print("\n");
        }
        System.out.flush();
        GEN++;
    }


    int countFeature(int[] mask) {
        int count = 0;
        for (int i = 0; i < mask.length; i++) {
            if (mask[i] == 1) count++;
        }
        return count;
    }

    /**
     * tournament selection (program with smaller fitness is preferred)
     * @param tsize tournament sizes
     * @return return the index of winner program
     */
    int tournament(int tsize) {
        int best = rd.nextInt(pop.length), i, competitor;
        double fbest = +1.0e34;

        for (i = 0; i < tsize; i++) {
            competitor = rd.nextInt(pop.length);
            if (pop[competitor].fitness < fbest) {
                fbest = pop[competitor].fitness;
                best = competitor;
            }
        }
        return (best);
    }

    /**
     * tournament selection (program with smaller fitness is preferred)
     * @param fitness fitness of programs
     * @param tsize tournament sizes
     * @param from index of the starting individuals to be sampled
     * @param range the range in which individuals to be sampled
     * @return return the index of winner program
     */
    int tournament(double[] fitness, int tsize, int from, int range) {
        int best = from + rd.nextInt(range), i, competitor;
        double fbest = +1.0e34;
        for (i = 0; i < tsize; i++) {
            competitor = rd.nextInt(POPSIZE);
            if (fitness[competitor] < fbest) {
                fbest = fitness[competitor];
                best = competitor;
            }
        }
        return (best);
    }

    /**
     * negative tournament selection (program with larger fitness is preferred)
     * @param fitness fitness of programs
     * @param tsize tournament sizes
     * @return return the index of winner program
     */
    int negative_tournament(double[] fitness, int tsize) {
        int worst = rd.nextInt(POPSIZE), i, competitor;
        double fworst = -1e34;

        for (i = 0; i < tsize; i++) {
            competitor = rd.nextInt(POPSIZE);
            if (fitness[competitor] > fworst) {
                fworst = fitness[competitor];
                worst = competitor;
            }
        }
        return (worst);
    }

    /**
     * subtree crossover
     * @param parent1 parent 1
     * @param parent2 parent 2
     * @return two generated child programs
     */
    char[][] crossover_subtree(char[] parent1, char[] parent2) {
        int xo1start, xo1end, xo2start, xo2end;
        char[][] offspring = new char[2][];
        int len1 = traverse(parent1, 0);
        int len2 = traverse(parent2, 0);
        int[] lenoff = new int[2];

        xo1start = select_random_point(parent1);
        xo1end = traverse(parent1, xo1start);

        xo2start = select_random_point(parent2);
        xo2end = traverse(parent2, xo2start);

        lenoff[0] = xo1start + (xo2end - xo2start) + (len1 - xo1end);
        lenoff[1] = xo2start + (xo1end - xo1start) + (len2 - xo2end);

        offspring[0] = new char[lenoff[0]];
        offspring[1] = new char[lenoff[1]];

        System.arraycopy(parent1, 0, offspring[0], 0, xo1start);
        System.arraycopy(parent2, xo2start, offspring[0], xo1start,
                (xo2end - xo2start));
        System.arraycopy(parent1, xo1end, offspring[0],
                xo1start + (xo2end - xo2start),
                (len1 - xo1end));

        System.arraycopy(parent2, 0, offspring[1], 0, xo2start);
        System.arraycopy(parent1, xo1start, offspring[1], xo2start,
                (xo1end - xo1start));
        System.arraycopy(parent2, xo2end, offspring[1],
                xo2start + (xo1end - xo1start),
                (len2 - xo2end));


        return (offspring);
    }

    /**
     * subtree crossover to produce one child program
     * @param parent1 parent 1
     * @param parent2 parent 2
     * @return generated program
     */
    char[] crossover_subtree_one(char[] parent1, char[] parent2) {
        int xo1start, xo1end, xo2start, xo2end;
        char[] offspring;
        int len1 = traverse(parent1, 0);
        int len2 = traverse(parent2, 0);
        int lenoff;

        xo1start = select_random_point(parent1);
        xo1end = traverse(parent1, xo1start);

        xo2start = select_random_point(parent2);
        xo2end = traverse(parent2, xo2start);

        lenoff = xo1start + (xo2end - xo2start) + (len1 - xo1end);

        offspring = new char[lenoff];

        System.arraycopy(parent1, 0, offspring, 0, xo1start);
        System.arraycopy(parent2, xo2start, offspring, xo1start,
                (xo2end - xo2start));
        System.arraycopy(parent1, xo1end, offspring,
                xo1start + (xo2end - xo2start),
                (len1 - xo1end));

        return (offspring);
    }

    /**
     * select a random node of the program
     * @param prog program
     * @return index of selected node
     */
    public int select_random_point(char[] prog) {
        ArrayList<Integer> tlist = new ArrayList<Integer>();
        ArrayList<Integer> ntlist = new ArrayList<Integer>();
        traverse_type(tlist, ntlist,  prog, 0);
        if (rd.nextDouble()<pr_nonterminal_select) {
            if (ntlist.isEmpty()) return 0;
            else {
                return ntlist.get(rd.nextInt(ntlist.size()));
            }
        } else {
            return tlist.get(rd.nextInt(tlist.size()));
        }
    }

    /**
     * point mutation
     * @param parent parent program
     * @param pmut mutation rate (probability that a node will be mutated)
     * @return mutated program
     */
    char[] mutation(char[] parent, double pmut) {
        int len = traverse(parent, 0), i;
        int mutsite;
        char[] parentcopy = new char[len];
        System.arraycopy(parent, 0, parentcopy, 0, len);
        for (i = 0; i < len; i++) {
            if (rd.nextDouble() < pmut) {
                mutsite = i;
                if (parentcopy[mutsite] < FSET_START) {
                    parentcopy[mutsite] = (char) rd.nextInt(varnumber + randomnumber);
                } else {
                    switch (parentcopy[mutsite]) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                        case MIN:
                        case MAX:
                            parentcopy[mutsite] =
                                    (char) (rd.nextInt(FSET_END - FSET_START + 1)
                                            + FSET_START);
                    }
                }
            }
        }
        return (parentcopy);
    }

    /**
     * subtree mutation
     * @param parent parent program
     * @return mutated program
     */
    char[] mutation_subtree(char[] parent) {
        char[] subsol = create_random_indiv(2);
        int xo1start, xo1end;
        char [] newsol;
        int len1 = traverse( parent, 0 );
        int len2 = traverse( subsol, 0 );
        int lenoff;
        xo1start =  select_random_point(parent);
        xo1end = traverse( parent, xo1start );
        lenoff = xo1start + (len2) + (len1-xo1end);
        newsol = new char[lenoff];
        System.arraycopy( parent, 0, newsol, 0, xo1start );
        System.arraycopy( subsol, 0, newsol, xo1start,len2 );
        System.arraycopy( parent, xo1end, newsol, xo1start + len2, (len1-xo1end) );
        return( newsol );
    }

    /**
     * subtree mutation
     * @param parent parent program
     * @return mutated program
     */
    char[] mutation_relusubtree(char[] parent) {
        int xo1start, xo1end;
        char [] newsol;
        int len1 = traverse( parent, 0 );
        int lenoff;
        xo1start =  select_random_point(parent);
        char[] extract = extractSubtree(parent, xo1start);
        char[] subsol = new char[1+extract.length];
        subsol[0] = ReLU;
        System.arraycopy( extract, 0, subsol, 1, extract.length );
        int len2 = traverse( subsol, 0 );
        xo1end = traverse( parent, xo1start );
        lenoff = xo1start + (len2) + (len1-xo1end);
        newsol = new char[lenoff];
        System.arraycopy( parent, 0, newsol, 0, xo1start );
        System.arraycopy( subsol, 0, newsol, xo1start,len2 );
        System.arraycopy( parent, xo1end, newsol, xo1start + len2, (len1-xo1end) );
        return( newsol );
    }

    /**
     * subtree mutation with a restricted depth of the ramdom subtree
     * @param parent parent program
     * @param depth depth of random subtree
     * @return mutated program
     */
    char[] mutation_subtree(char[] parent, int depth) {
        char[] subsol = create_random_indiv(depth);
        int xo1start, xo1end;
        char [] newsol;
        int len1 = traverse( parent, 0 );
        int len2 = traverse( subsol, 0 );
        int lenoff;

        xo1start =  select_random_point(parent);
        xo1end = traverse( parent, xo1start );

//        System.out.println(xo1end - xo1start);

        lenoff = xo1start + (len2) + (len1-xo1end);

        newsol = new char[lenoff];

        System.arraycopy( parent, 0, newsol, 0, xo1start );
        System.arraycopy( subsol, 0, newsol, xo1start,len2 );
        System.arraycopy( parent, xo1end, newsol,
                xo1start + len2,
                (len1-xo1end) );
        return( newsol );
    }


    /**
     * turn on/off the statistic log
     */
    public void turn_running_stat() {
        show_run_stat = true;
    }

    /**
     * set the population sizes
     * @param ps population sizes
     */
    public void setPopSize(int ps) {
        POPSIZE = ps;
    }

    /**
     * set the maximum length of the evolved program
     * @param ml maximum length
     */
    public void setMaxLength(int ml) {
        MAX_LEN = ml;
    }

    /**
     * set the maximum depth of evolved programs
     * @param md maximum depth
     */
    public void setMaxDepth(int md) {
        MAXDEPTH = md;
    }

    /**
     * set the depth of inital generated programs
     * @param id
     */
    public void setInitDepth(int id) {
        INIT_DEPTH = id;
    }

    /**
     * set the maximum generation for one GP run
     * @param mg maximum generation
     */
    public void setMaxGeneration(int mg) {
        GENERATIONS = mg;
    }

    /**
     * set the tournament selection sizes
     * @param ts
     */
    public void setTourSize(int ts) {
        TSIZE = ts;
    }

    /**
     * set the crossover probability
     * @param cr crossover probability
     */
    public void setCrossoverRate(double cr) {
        CROSSOVER_PROB = cr;
    }

    /**
     * set the mutation probability
     * @param mr mutation probability
     */
    public void setMutationRate(double mr) {
        MUTATION_PROB = mr;
    }

    /**
     * print the parameter of GP
     */
    void print_parms() {
        System.out.print("-- TINY GP (Java version) --\n");
        System.out.print("SEED=" + seedgp + "\nMAX_LEN=" + MAX_LEN
                + "\nPOPSIZE=" + POPSIZE + "\nDEPTH=" + INIT_DEPTH
                + "\nCROSSOVER_PROB=" + CROSSOVER_PROB
                + "\nMIN_RANDOM=" + minrandom
                + "\nMAX_RANDOM=" + maxrandom
                + "\nGENERATIONS=" + GENERATIONS
                + "\nTSIZE=" + TSIZE
                + "\n----------------------------------\n");
    }

    /**
     * print the array of integer representing the evolved program
     * @param sol program
     * @return integer array
     */
    public String print_program_integer(char[] sol) {
        String sp = "";
        sp = ("int[] intprogram = {");
        for (int i = 0; i < sol.length; i++) {
            sp += ((int)sol[i] + ",");
        }
        sp += ("};");
        return sp;
    }

    /**
     * get the current input vector
     * @return
     */
    public double[] X() {
        return input_vec;
    }

    /**
     * convert from integer array back to flattened tree
     * @param x integer array
     * @return flattened tree
     */
    char[] convert(int[] x) {
        char[] p = new char[x.length];
        for (int i = 0; i < p.length; i++) {
            p[i] = (char) x[i];
        }
        return p;
    }

    /**
     * check random number and variable allocation
     */
    void checkAllocation() {
        if (varnumber + randomnumber >= FSET_START) {
            System.err.println("allocation error! inputs and functions overlapped!");
        }
    }

    /**
     * get allocation of a new constant
     * @return allocation of new constant (in array input_vec)
     */
    int newConstant() {
        return varnumber + ++randomnumber;
    }

    /**
     * get the sample set
     */
    public abstract void setupSample();

    /**
     * define terminal and function sets
     */
    public abstract void define_terminal_function();

    /**
     * get the name of the algorithm
     * @return
     */
    public abstract String name();

    /**
     * extract a subtree from an original tree
     * @param prog program
     * @param pos position to extract
     * @return extracted subtree
     */
    char[] extractSubtree(char[] prog, int pos) {
        int xostart =  pos;
        int xoend = traverse( prog, xostart );
        char[] newind = new char[xoend - xostart];
        System.arraycopy( prog, xostart, newind, 0, xoend - xostart);
        return newind;
    }

    /**
     * check if a node is a terminal or not
     * @param c node
     * @return true if it is a terminal; otherwise false
     */
    boolean isTerminal(char c) {
        if (c < FSET_START) {
            return true;
        }
        return false;
    }

    /**
     * check if a node is a constant
     * @param c node
     * @return true if it is a constant; otherwise false
     */
    boolean isConstant(char c) {
        if (c >= varnumber && c < FSET_START) {
            return true;
        }
        return false;
    }

    /**
     * calculate some outputs from a given program
     * @param sample sample feature vectors
     * @param prog given program to be sampled
     * @return sampling output
     */
    double[] sampling_output(double[][] sample, char[] prog) {
        double[] soutput = new double[sample.length];
        for (int i = 0; i < soutput.length; i++) {
            for (int j = 0; j < varnumber; j++) {
                input_vec[j] = sample[i][j];
            }
            program = prog;
            PC = 0;
            soutput[i] = -run();
        }
        return soutput;
    }

    String arrayToString(int[] x) {
        String s = "[";
        for (int i = 0; i < x.length; i++) {
            s += x[i] + ",";
        }
        s += "]";
        return s;
    }

    public String convert(ArrayList<Integer> list) {
        String x = "";
        for (int i = 0; i < list.size(); i++) {
            x += list.get(i);
        }
        return x;
    }
    public String convert(char[][] list) {
        String x = "";
        for (int i = 0; i < list.length; i++) {
            for (int j = 0; j < list[i].length; j++) {
                x += list[i];
            }
        }
        return x;
    }
    public String convert(double[] list) {
        String x = "";
        for (int i = 0; i < list.length; i++) {
            x += (int)list[i];
        }
        return x;
    }
}
