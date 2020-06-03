/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package problems.dfjss;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytcs and Cognition
 * La Trobe University, Australia
 */
public class Job{
    public double priority;
    //static int countID = 0;
    private int id = -1;
    private ShopLevel shop = null;
    private Machine mc = null;
    private Workcenter wc = null;
    private int currentOperation = 0;
    private int currentMode = 0; //which workcenter (based on the flexibility) to handle the current operation
    private int nextMode = 0; //which workcenter (based on the flexibility) to handle the next operation
    private Operation[] operations;
    private  double marrivetime = -1;
    private double remaintime = 0;
    private double totaltime = 0;
    private double rank = 0;
    private double duedate = -1;
    private double releasetime = -1;
    private double workloadonroute = 0;
    private double systemworkload = 0;
    private double weight = 1;
    public static int countID = 0;
    private static Scheduling scheduler;
    public static Scheduling getScheduler() {
        return scheduler;
    }
    public static void setScheduler(Scheduling sched) {
        scheduler = sched;
    }
    public Job(ShopLevel s, int[][] route, double[][] pr, double release, double w){
        shop = s;
        id = countID++;
        operations = new Operation[route.length];
        for (int i = 0; i < route.length; i++) {
            operations[i] = new Operation(pr[i], route[i], s.id() == 0);
            workloadonroute += s.wcs()[route[i][0]].workload();
            remaintime += operations[i].averagePrtime();
        }
        for (int i = 0; i < s.wcs().length; i++) {
            systemworkload += s.wcs()[i].workload();
        }
        totaltime = remaintime;
        releasetime = release;
        weight = w;
    }

    public int id() {
        return id;
    }
    public double[] jobrecord() {
        return new double[] {workloadonroute, operations.length, totaltime, duedate - releasetime, systemworkload, tardy()};
    }
    public void accummulateRank(double r) {
        rank += r;
    }
    public void assignRank(double r) {
        rank = r;
    }
    public double rank() {
        return rank;
    }
    public void resetRank() {
        rank = 0;
    }
    public double startOperation(){
        mc.occupy(this); operations[currentOperation].setStart(FlexibleJobShop.now());
        return operations[currentOperation].getProcessingTime(currentMode);
    }
    public void endOperation(){
        remaintime -= operations[currentOperation].averagePrtime();
        wc.deduceWorkload(operations[currentOperation].getProcessingTime(currentMode));
        mc.idle();
        currentOperation++;
    }
    public void addworkloadtowc() {
        wc.addWorkload(operations[currentOperation].getProcessingTime(currentMode));
    }
    public Workcenter route(){
        marrivetime = FlexibleJobShop.now();
        if (currentOperation == 0) {
            shop.jobInShop().add(this);
        }
        wc = shop.wcs()[operations[currentOperation].getWC(currentMode)];
        return wc;
    }
    public boolean isDone() {
        return currentOperation >= operations.length;
    }
    public Workcenter getWC(){
        return wc;
    }
    public String toString(){
        return "" + shop.id() + "\t" + id + "\t" + currentOperation+"/"+operations.length;
    }
    public boolean ready() {
        return operations[currentOperation].isReady();
    }
    public double marrivetime() {
        return marrivetime;
    }
    public Workcenter wc(){
        return wc;
    }
    public void assignMachine(Machine m) {
        mc = m;
    }
    public Machine mc() {
        return mc;
    }
    public ShopLevel shop() {
        return shop;
    }
    public int remainingOperation() {
        return operations.length - currentOperation;
    }
    public int remainingOperationToEndProduct() {
        return remainingOperation();
    }
    public double remainingTime() {
        return remaintime;
    }
    public double remainingTime_currentmode() {
        return remaintime - operations[currentOperation].averagePrtime() + operations[currentOperation].getProcessingTime(currentMode);
    }
    public double remainingTime(int from) {
        double r = 0;
        for (int i = from; i < operations.length; i++) {
            r += operations[i].getProcessingTime(currentMode);
        }
        return r;
    }
    public double currentProcesstime() {
        return operations[currentOperation].getProcessingTime(currentMode);
    }
    public double currentOperationDueDate() {
        return operations[currentOperation].getODD();
    }
    public Operation current_operation() {
        return operations[currentOperation];
    }
    public double workloadnextQ() {
        if (currentOperation+1==operations.length) return 0;
        return shop.wcs()[operations[currentOperation+1].getWC(nextMode)].workload();
    }

    public double workloadnextQ_average() {
        if (currentOperation+1==operations.length) return 0;
        double total = 0;
        for (int k = 0; k < operations[currentOperation+1].nMode(); k++) {
            total += shop.wcs()[operations[currentOperation+1].getWC(k)].workload();
        }
        return total/operations[currentOperation+1].nMode();
    }

    public double nJobnextQ() {
        if (currentOperation+1==operations.length) return 0;
        return shop.wcs()[operations[currentOperation+1].getWC(nextMode)].jobinqueue();
    }

    public double avgWorkloadCurrentQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].workload()/shop.wcs()[operations[currentOperation].getWC(currentMode)].jobinqueue();
    }

    public double numberJobinQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].jobinqueue();
    }

    public double minProcessingTimeinQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].minProcessingTime();
    }

    public double maxProcessingTimeinQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].maxProcessingTime();
    }

    public double minDueDateinQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].minDueDate();
    }

    public double maxDueDateinQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].maxDueDate();
    }

    public double maxWeightinQ() {
        return shop.wcs()[operations[currentOperation].getWC(currentMode)].maxWeight();
    }

    public double aheadWorkload(double alpha) {
        if (currentOperation+1 == operations.length||alpha==0) return 0;
        int lastop = currentOperation;
        double aworkload = 0;
        for (int i = 1; i <= alpha; i++) {
            if (currentOperation+i==operations.length) break;
            aworkload += shop.wcs()[operations[currentOperation+i].getWC(nextMode)].workload() + operations[currentOperation+i].getProcessingTime(nextMode);
            lastop = currentOperation+i;
        }
        lastop++;
        double lalpha = alpha - (int) alpha;
        if (lalpha > 0&&lastop!=operations.length) {
            aworkload += lalpha*(shop.wcs()[operations[lastop].getWC(nextMode)].workload() + operations[lastop].getProcessingTime(nextMode));
        }
        return aworkload;
    }

    public double workloadOnRoute() {
        if (currentOperation+1==operations.length) return 0;
        double tworkload = 0;
        for (int i = currentOperation; i < operations.length; i++) {
            tworkload += shop.wcs()[operations[i].getWC(nextMode)].workload();
        }
        return tworkload;
    }

    public double nextprocesstime() {
        if (currentOperation+1==operations.length) return 0;
        return operations[currentOperation+1].getProcessingTime(nextMode);
    }

    public double nextprocesstime_average() {
        if (currentOperation+1==operations.length) return 0;
        double total = 0;
        for (int k = 0; k < operations[currentOperation+1].nMode(); k++) {
            total += operations[currentOperation+1].getProcessingTime(k);
        }
        return total/operations[currentOperation+1].nMode();
    }

    public double totaltime() {
        return totaltime;
    }
    public int noperation(){
        return operations.length;
    }
    public double duedate() {
        return duedate;
    }
    public void setduedate(double allowance) {
        duedate = releasetime + allowance*totaltime;
    }
    public Operation[] operations() {
        return operations;
    }

    public double releasetime(){
        return releasetime;
    }
    public static double max(double a, double b){
        if (a>b) return a;
        else return b;
    }
    public double leadtime(){
        return FlexibleJobShop.now() - releasetime;
    }
    public double weight() {
        return weight;
    }
    public int[] remainRoute() {
        int[] route;
        if (current_operation().getStart() == -1) {
            route = new int[operations.length - currentOperation + 1];
        } else {
            route = new int[operations.length - currentOperation];
        }
        for (int i = currentOperation; i < operations.length; i++) {
            if (current_operation().getStart() == -1) continue;
            route[i - currentOperation] = operations[i].getWC(0);
        }
        return route;
    }

    public void set_route(int index) {
        currentMode = index;
    }
    public double tardy(){
        return Utilities.maxPlus(FlexibleJobShop.now() - duedate);
    }
}
