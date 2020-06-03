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
public class Event<ENTITY> implements Comparable<Event> {
    static NumberFormat f = new DecimalFormat("#0.000");
    static String[] ename = {"Arrive", "Visit","Process","Finish"};
    double T = -1;
    int id = -1;
    ENTITY ent = null; 
    public Event(int eventID, double t, ENTITY en){
        id = eventID;
        T = t;
        ent = en;
    }
    public int ID(){
        return id;
    }
    public ENTITY getEntity(){
        return ent;
    }
    public String toString(){
        return ename[id] + "\t" + f.format(T) + "\t" + ent;
    }   
    public double time(){
        return T;
    }    
    @Override
    public int compareTo(Event t) {
        return (t.time() > T)? 0:1;
    }
}
