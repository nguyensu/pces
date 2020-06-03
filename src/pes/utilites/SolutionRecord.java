package pes.utilites;

/**
 * Created by nguyensu on 1/05/14.
 */
public class SolutionRecord implements Comparable<SolutionRecord> {
    private final char[] solution;
    public char[] soluion = null;
    public double obj;
    public SolutionRecord(char[] prog, double o) {
        solution = prog.clone();
        obj = o;
    }
    @Override
    public String toString() {
        return "obj=" + obj + "; length="+solution.length;
    }

    @Override
    public int compareTo(SolutionRecord o) {
        if (obj > o.obj) return 1;
        else if (obj == o.obj) return  0;
        else return -1;
    }

    public boolean dominate(SolutionRecord s) {
        return paretoDominates(new double[] {obj, solution.length}, new double[] {s.obj, s.solution.length});
    }

    public boolean equal(SolutionRecord s) {
        if (obj==s.obj&&solution.length==s.solution.length) return true;
        return false;
    }

    public static boolean paretoDominates(double[] objectives, double[] other_objectives)
    {
        boolean abeatsb = false;
        for (int x = 0; x < objectives.length; x++)
        {
            if (objectives[x] < other_objectives[x])
                abeatsb = true;
            else if (objectives[x] > other_objectives[x])
                return false;
        }
        return abeatsb;
    }

    public static boolean paretoWeakDominates(double[] objectives, double[] other_objectives)
    {
        boolean abeatsb = false;
        for (int x = 0; x < objectives.length; x++)
        {
            if (objectives[x] <= other_objectives[x])
                abeatsb = true;
            else if (objectives[x] > other_objectives[x])
                return false;
        }
        return abeatsb;
    }
}
