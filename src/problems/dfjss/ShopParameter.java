/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package problems.dfjss;

import java.util.Arrays;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class ShopParameter {
    int nlevel;
    ShopLevel.ShopType[] LV1 = {ShopLevel.ShopType.PJS, ShopLevel.ShopType.PJS};
    int[] nwc = {6,6};
    int[][] nmc = {{1,1,1,1,1,1},{1,1,1,1,1,1}};
    double[] lowUti = {0.8, 0.8};
    double[] highUti = {0.9, 0.9};
    String[] processdist = {"erlang", "erlang"};
    double[] processdist_param = {1, 1};
    int[] flex = {1,1};
    double[] meantime = {1,1};
    int[] lowop = {1,1};
    int[] upop = {5,5};
    boolean revisit;
    double allowance = 4;
    Scheduling scheduler;
    public ShopParameter(Scheduling sched, int n, ShopLevel.ShopType[] st, int[] nw, int[][] nm, double[] lu, double[] hu, String[] pd, double[] pp, int[] f , double[] mean, int[] l, int[] u, boolean re, double all) {
        nlevel = n;
        LV1 = st;
        nwc = nw;
        nmc = nm;
        lowUti = lu;
        highUti = hu;
        processdist = pd;
        processdist_param = pp;
        meantime = mean;
        lowop = l;
        upop = u;
        flex = f;
        scheduler = sched;
        revisit = re;
        allowance = all;
    }
    public void setUtilisation(double u) {
        Arrays.fill(lowUti,u);
        Arrays.fill(highUti,u);
    }
    public void setAllowance(double a) {
        allowance = a;
    }
    public void setFlexibility(int f) {
        Arrays.fill(flex,f);
    }
    public void assignScheduler(Scheduling sche) {
        scheduler = sche;
    }
    public void assignCommonUtilisation(double u) {
        Arrays.fill(lowUti, u);
        Arrays.fill(highUti, u);
    }
}
