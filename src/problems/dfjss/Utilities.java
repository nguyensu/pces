/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package problems.dfjss;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class Utilities {
    static NumberFormat formatter3 = new DecimalFormat("#0.000");
    static NumberFormat formatter2 = new DecimalFormat("#0.00");
       /*
     * get maxPlus
     */
    public static double maxPlus(double a){
        if (a>0) return a;
        else return 0;
    }
    public static String f2(double x) {
        return formatter2.format(x);
    }
    public static String f3(double x) {
        return formatter3.format(x);
    }
}
