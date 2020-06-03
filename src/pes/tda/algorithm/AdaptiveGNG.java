package pes.tda.algorithm;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This is the adaptive growing neural gas (GNG, a variant of self-organizing map). This algorithm will take program characteristics
 * as inputs and generate a network (or map) that optimally represent the topological relations of programs evolved by GP algorithm.
 * At the moment, the algorithm can be called and the GNG network is updated in each generation. To improve the efficiency of the algorithm,
 * principal component analysis (PCA) is applied.
 */

public class AdaptiveGNG {
    public static void main(String[] args) {
        AdaptiveGNG compute = new AdaptiveGNG(1);
        compute.lambdaGNG = 100; // number of steps before inserting a new node
        compute.stepSize = 20000; // number of steps
        compute.MAX_STEPSIZE = compute.stepSize;
        compute.alphaGNG = 0.5f; // split error between two nodes when insert new node
        compute.betaGNG = 5e-4f; // decay factor E = E*(1-Beta)
        compute.MAX_EDGE_AGE = 88; // max age for edges in in GNG
        compute.maxNodes = 200;
        compute.prepareAlgo(1);
        compute.reset();
        compute.learn();
        System.out.println("Number of nodes is " + compute.nNodes);
        System.out.println("Done!!!");
    }


    Random rnd = new Random(1);
    public AdaptiveGNG(int rndseed) {
        rnd = new Random(rndseed);
    }


    void log(String txt) {

    }
    public void setSize(Dimension d) {
        log("COM: setsize");
    }
    final static private SimpleDateFormat format
            = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
    public synchronized String timeStamp() {
        long lDateTime = new Date().getTime();
        String str;
        str = format.format(new Date())+"."+String.format("%03d",lDateTime%1000);
        return str;
    }

    public double[] delta = null;

    /**
     * The flag for debugging.
     */
    public final boolean DEBUG = false;
    /**
     * The maximum number of elements to draw/calculate for the distributions.
     */
    public final int MAX_COMPLEX = 58;
    /**
     * The maximum number of nodes.
     */
    public final int MAX_NODES = 10000;
    /**
     * The maximum number of edges (3 * maximum number of nodes).
     */
    public final int MAX_EDGES = 6 * MAX_NODES;
    /**
     * The maximum number of startMapping.Voronoi lines (5 * maximum number of nodes).
     */
    public final int MAX_V_LINES = 6 * MAX_NODES;
    /**
     * The maximum stepsize.
     */
    public int MAX_STEPSIZE = 20000;
    /**
     * The sizes of the DiscreteMixture signal set.
     */
    public final int MIXTURE_SIZE = 500;
    /**
     * The maximum number of discrete signals.
     */
    public final int MAX_DISCRETE_SIGNALS = 10000;
    /**
     * The maximum x sizes of the grid array.
     */
    public final int MAX_GRID_X = 10000;
    /**
     * The maximum y sizes of the grid array.
     */
    public final int MAX_GRID_Y = 100;

    /**
     * The factor for the ring-thickness (distribution).
     */
    public final float RING_FACTOR = 0.4f;	// Factor < 1
    /**
     * The current maximum number of nodes.
     */
    public int maxNodes = 100;
    /**
     * The current number of runs to insert a new node (GNG).
     */
    public int lambdaGNG = 600;
    /**
     * The current number of input signals used for adaptation.
     */
    public int sigs = 0;

    /**
     * The temporal backup of a run.
     */
    public int sigsTmp = 0;
    /**
     * The x-position of the actual signal.
     */
    public int SignalX = 0;
    /**
     * The y-position of the actual signal.
     */
    public int SignalY = 0;

    public double[] Signal = null;

    public int id_sig = -1;

    public int dimension = -1;
    /**
     * The actual number of nodes.
     */
    public int nNodes = 0;
    /**
     * The array of the actual used nodes.
     */
    public NodeGNG nodes[] = new NodeGNG[MAX_NODES];
    /**
     * The sorted array of indices of nodes.
     * The indices of the nodes are sorted by their distance from the actual
     * signal. snodes[1] is the index of the nearest node.
     */
    public int snodes[] = new int[MAX_NODES + 1];
    /**
     * The actual number of edges.
     */
    public int nEdges = 0;
    /**
     * The array of the actual used edges.
     */
    public EdgeGNG edges[] = new EdgeGNG[MAX_EDGES];

    /**
     * The flag for playing the sound for a new inserted node.
     */
    public boolean insertedSoundB = false;
    /**
     * The flag for random init. The nodes will be placed only in the specified
     *  distribution or not.
     */
    public boolean rndInitB = false;
    /**
     * stop the algo when max number of nodes is reached
     */
    public boolean autoStopB = true;
    /**
     * The flag for inserting new nodes in GNG.
     *  This variable can be set by the user. If true no new nodes are
     *  inserted.
     */
    public boolean noNewNodesGNGB = false;
    /**
     * The flag for stopping the demo.
     *  This variable can be set by the user. If true no calculation is done.
     */
    private boolean stopB = false;
    /**
     * The flag for any moved nodes (to startMapping the startMapping.Voronoi diagram/Delaunay
     *  triangulation).
     */
    public boolean nodesMovedB = true;
    /**
     * The flag for using utility (GNG-U).
     */
    public boolean GNG_U_B = false;
    /**
     * The flag for changed number of nodes.
     */
    public boolean nNodesChangedB = true;
    /**
     * The current maximum number to delete an old edge (GNG,NGwCHL).
     *  This variable can be set by the user.
     */
    public int MAX_EDGE_AGE = 88;
    /**
     * The current number of calculations done in one step.
     *  This variable can be set by the user. After <TT> stepSize </TT>
     *  calculations the result is displayed.
     */
    public int stepSize = 50;
    /**
     * The actual x sizes of the grid array.
     */
    public int gridWidth = 0;
    /**
     * The actual y sizes of the grid array.
     */
    public int gridHeight = 0;
    /**
     * The value alpha for the GNG algorithm.
     *  This variable can be set by the user.
     */
    public float alphaGNG = 0.5f;
    /**
     * The value beta for the GNG algorithm.
     *  This variable can be set by the user.
     */
    public float betaGNG = 0.0005f;
    /**
     * maximum width of grid
     *
     */
    public int maxYGG = 0;
    /**
     * The utility factor for the GNG-U algorithm.
     *  This variable can be set by the user.
     */
    public float utilityGNG = 3.0f;
    /**
     * The decay factor for utility
     */
    public float decayFactorUtility = 1.0f - betaGNG;
    /**
     * The factor to forget old values.
     */
    public float decayFactor = 1.0f - betaGNG;
    /**
     * This value is displayed in the error graph.
     */
    public float valueGraph = 0.0f;

    public ArrayList<double[]> data = new ArrayList<>();

    public void updateData(double[][] dat) {
        if (data.isEmpty()) {
            data = new ArrayList<double[]>();
            dimension = dat[0].length;
        } else data.clear();
        if (dimension != dat[0].length) System.err.println("Dimension of data is not matched!!!");
        for (int i = 0; i < dat.length; i++) {
            data.add(dat[i]);
        }
    }

    /**
     * Add a node. The new node will be randomly placed within the
     *  given dimension or according to the current distribution.
     *
     * @return           The index of the new node
     */
    public int addNode() {
        if ( (nNodes == MAX_NODES) || (nNodes >= maxNodes) )
            return -1;

        NodeGNG n = new NodeGNG();

        if (rndInitB) {
            // init from rectangle 0.8x0.8+(0.1,0.1)
            n.x = (float) (10 + (200-20) * rnd.nextDouble());
            n.y = (float) (10 + (200-20) * rnd.nextDouble());
        } else {
            // init from current distribution
            getSignal();
            n.feature = new double[dimension];
            for (int d = 0; d < dimension; d++) {
                n.feature[d] = Signal[d];
            }
        }
        //System.out.printf("addNode() and width is %d and rndInit is %s, x=%f, y=%f\n",d.width, rndInitB,n.x,n.y);

        n.nNeighbor = 0;
        nodes[nNodes] = n;
        nNodesChangedB = true;
        return nNodes++;
    }

    /**
     * Add a node. The new node will be placed between the
     *  given nodes which must be connected. The existing edge is splitted.
     *  The new node gets the average of the interesting values of
     *  the two given nodes.
     *
     * @param n1         The index of a node
     * @param n2         The index of a node
     * @return           The index of the new node
     */
    public int insertNode(int n1, int n2) {
        if ( (nNodes == MAX_NODES) || (nNodes >= maxNodes) )
            return -1;
        if ( (n1 < 0) || (n2 < 0) )
            return -1;
        NodeGNG n = new NodeGNG();
        if (dimension!=-1) {
            delta = new double[dimension];
        }
        float dx =0 , dy = 0;
        if (dimension==-1) {
            dx = (nodes[n1].x - nodes[n2].x) / 2.0f;
            dy = (nodes[n1].y - nodes[n2].y) / 2.0f;
        } else {
            for (int d = 0; d < dimension; d++) {
                delta[d] =  (nodes[n1].feature[d] - nodes[n2].feature[d]) / 2.0;
            }
        }
        // reduce errors of neighbor nodes of the new unit
        nodes[n1].error *= (1.0f - alphaGNG);
        nodes[n2].error *= (1.0f - alphaGNG);

        // interpolate error from neighbors
        n.error = (nodes[n1].error + nodes[n2].error)/2.0f;
        // interpolate utility from neighbors
        n.utility = (nodes[n1].utility + nodes[n2].utility)/2.0f;
        // interpolate coordinates
        if (dimension==-1) {
            n.x = nodes[n1].x - dx;
            n.y = nodes[n1].y - dy;
        } else {
            n.feature = new double[dimension];
            for (int d = 0; d < dimension; d++) {
                n.feature[d] = nodes[n1].feature[d] - delta[d];
            }
        }
        n.isMostRecentlyInserted = true;
        nodes[nNodes] = n;
        deleteEdge(n1, n2);
        addEdge(n1, nNodes); //n1<->new
        addEdge(n2, nNodes); //n2<->new
        nNodesChangedB = true;

        return nNodes++;
    }


    /**
     * Delete the given node.
     *
     * @param n          The index of a node
     */
    public void deleteNode(int n) {
        NodeGNG node = nodes[n];
        int num = node.numNeighbors();
        int i;

        for (i = 0; i < num; i++)
            deleteEdge(n, node.neighbor(0));

        nNodesChangedB = true;
        nNodes--;
        nodes[n] = nodes[nNodes];
        nodes[nNodes] = null;

        // Now rename all occurances of nodes[nnodes] to nodes[n]
        for (i = 0 ; i < nNodes ; i++)
            nodes[i].replaceNeighbor(nNodes, n);
        for (i = 0 ; i < nEdges ; i++)
            edges[i].replace(nNodes, n);

        return;
    }

    /**
     * Connect two nodes or reset the age of their edge.
     *
     * @param from          The index of the first node
     * @param to            The index of the second node
     */
    public void addEdge(int from, int to) {
        if (nNodes < 2)
            return;

        if (nodes[from].isNeighbor(to)) {
            // Find edge(from,to) and reset age
            int i = findEdge(from, to);

            if (i != -1)
                edges[i].age = 0;
            return;
        }

        if (nEdges == MAX_EDGES)
            return;

        if ( (nodes[from].moreNeighbors()) && (nodes[to].moreNeighbors()) ) {
            nodes[to].addNeighbor(from);
            nodes[from].addNeighbor(to);
        } else
            return;

        // Add new edge
        EdgeGNG e = new EdgeGNG();
        e.from = from;
        e.to = to;
        edges[nEdges] = e;
        nEdges++;
    }

    /**
     * Disconnect two nodes.
     *
     * @param from          The index of the first node
     * @param to            The index of the second node
     */
    public void deleteEdge(int from, int to) {
        int i = findEdge(from, to);
        if (i != -1) {
            nodes[edges[i].from].deleteNeighbor(edges[i].to);
            nodes[edges[i].to].deleteNeighbor(edges[i].from);
            nEdges--;
            edges[i] = edges[nEdges];
            edges[nEdges] = null;
        }
        return;
    }

    /**
     * Delete an edge.
     *
     * @param edgeNr          The index of the edge
     */
    public void deleteEdge(int edgeNr) {
        nodes[edges[edgeNr].from].deleteNeighbor(edges[edgeNr].to);
        nodes[edges[edgeNr].to].deleteNeighbor(edges[edgeNr].from);
        nEdges--;
        edges[edgeNr] = edges[nEdges];
        edges[nEdges] = null;
    }

    /**
     * Find an edge. Find the edge between the two given nodes.
     *
     * @param from          The index of the first node
     * @param to            The index of the second node
     * @return              The index of the found edge or -1
     */
    public int findEdge(int from, int to) {

        for (int i = 0; i < nEdges; i++)
            if (( (edges[i].from == from) && (edges[i].to == to) ) ||
                    ( (edges[i].from == to) && (edges[i].to == from) ) )
                return i;
        return -1;
    }

    /**
     * All edges starting from the given node are aged.
     *  Too old edges are deleted.
     *
     * @param node          The index of a node
     */
    public void ageEdgesOfNode(int node) {
        // TODO: this is inefficient for large number of edges, perhaps keep local list of edges per node
        for (int i = nEdges - 1; i > -1; i--) {
            if ( (edges[i].from == node) || (edges[i].to == node) )
                edges[i].age++;
            if (edges[i].age > MAX_EDGE_AGE)
                deleteEdge(i);
        }
    }

    /**
     * Find neighbor with the highest error.
     *
     * @param master          The index of a node
     * @return                The index of a node
     */
    public int maximumErrorNeighbor(int master) {
        float ws = Float.MIN_VALUE;
        int wn = -1;
        int n = -1;
        int num = nodes[master].numNeighbors();
        for (int i = 0; i < num; i++) {
            n = nodes[master].neighbor(i);
            if (ws < nodes[n].error) {
                ws = nodes[n].error;
                wn = n;
            }
        }

        return wn;
    }

    public Point2D.Double circlePoint() {
        Point2D.Double origin = new Point2D.Double(0,0);
        Point2D.Double signal = new Point2D.Double(0,0);
        do {
            signal.x = rnd.nextDouble()-0.5;
            signal.y = rnd.nextDouble()-0.5;
        } while (origin.distanceSq(signal) > 0.25 );
        // assertion: signal is on circle around origin with diameter 1, radius 0.5
        //System.out.printf("%f %f \n",signal.getX(),signal.getY());
        return signal;
    }

    /**
     * Generate a signal for the given distribution.
     *  The result goes into the global variables <TT> SignalX </TT>
     *  and <TT> SignalY </TT>.
     *
     */
    public void getSignal() {
        int idx = (int) (rnd.nextDouble()*data.size());
        Signal = data.get(idx);
    }


    /**
     * Build a minimum-heap.
     *
     * @param i          The start of the intervall
     * @param k          The end of the intervall
     */
    public void reheap_min(int i, int k) {
        int j = i;
        int son;

        while (2*j <= k) {
            if (2*j+1 <= k)
                if (nodes[snodes[2*j]].sqrDist < nodes[snodes[2*j+1]].sqrDist)
                    son = 2*j;
                else
                    son = 2*j + 1;
            else
                son = 2*j;

            if (nodes[snodes[j]].sqrDist > nodes[snodes[son]].sqrDist) {
                int exchange = snodes[j];
                snodes[j] = snodes[son];
                snodes[son] = exchange;
                j = son;
            } else
                return;
        }
    }
    /**
     * Do the learning. An input signal is generated for the given distribution
     *  and forwarded to the switched algorithm.
     *  Available Algorithms (abbrev):
     *   Growing Neural Gas (GNG),
     *   Growing Neural Gas w. Utility (GNG-U),
     *   Hard Competitive Learning (HCL),
     *   Neural Gas (NG),
     *   Neural Gas with Competitive Hebbian Learning (NGwCHL) and
     *   Competitive Hebbian Learning (CHL).
     *   LBG (LBG).
     *   LBG with Utility (LBG-U).
     *   Growing Grid (GG).
     *   Self-Organizing Map (SOM).
     *
     */
    public synchronized void learn() {
        //
        // learning is done for stepSize steps
        //
        int curr1stIdx, curr2ndIdx;
        int i, j, k, l, m;
        int x, y;
        int numError, minUtilityNode;
        int numNb;
        int toDelete;
        float dx, dy;
        if (dimension!=-1) {
            delta = new double[dimension];
        }
        float dstSgmExp;
        float bestSqrDist, nextBestDist;
        float h_l = 0.0f;
        float l_t = 0.0f;
        float maxError, minUtility;
        NodeGNG curr1st, curr2nd, n_i, node;
//        dimension = 2;
        SignalX = 200/2;
        SignalY = 200/2;
        Signal = new double[] {SignalX,SignalY};
        valueGraph = 0.0f;
        if (stopB)
            return;

        // do stepSize adaption steps using random signals
        for (k = 0; k < stepSize; k++) {
//            System.out.println(k + " " + SignalX + " " + SignalY);
            sigs++;
//			if (sigs%100==0) {
//				//
//				try {
//					Thread.sleep(delay);
//				} catch (InterruptedException e) {
//					break;
//				}
//			}

            if (true) { // neither LBG nor LBG-U
                //
                // generate random signal and determine winner etc.
                //
                curr1stIdx = 0;
                curr2ndIdx = 0;
                curr1st = nodes[curr1stIdx];
                curr2nd = nodes[curr2ndIdx];
                numError = 0;
                minUtilityNode = 0;
                maxError = 0.0f;
                minUtility = Float.MAX_VALUE;
                bestSqrDist = Float.MAX_VALUE;
                nextBestDist = Float.MAX_VALUE;
                toDelete = -1;

                // Get a random signal out of the selected distribution
                getSignal();
                // Save the signals

                // Locate the nearest node (winner) and the second-nearest (runner-up)
                for (i = 0 ; i < nNodes ; i++) {
                    n_i = nodes[i];
                    n_i.isWinner = n_i.isSecond = n_i.hasMoved = false;

                    if ((!noNewNodesGNGB) && ((sigs % lambdaGNG) == 0))
                        n_i.isMostRecentlyInserted = false;

                    // Mark node without neighbors (one each run is enough)
                    if (n_i.numNeighbors() == 0)
                        toDelete = i;

                    // Calculate squared distance to input signal
                    n_i.sqrDist = 0;
                    if (dimension==-1) {
                        n_i.sqrDist =
                                (n_i.x - SignalX) * (n_i.x - SignalX) +
                                        (n_i.y - SignalY) * (n_i.y - SignalY);
                    } else {
                        for (int d = 0; d < dimension; d++) {
                            n_i.sqrDist += (n_i.feature[d] - Signal[d])*(n_i.feature[d] - Signal[d]);
                        }
                    }
                    // Decay error and utility
                    n_i.error *= decayFactor;
                    n_i.utility *= decayFactorUtility;
                    n_i.tau *= decayFactor;

                    // Keep track of current first and second winner
                    if (n_i.sqrDist <= bestSqrDist) { // changed to <= to handle strange cases with all nodes in one position
                        curr2nd = curr1st;
                        curr2ndIdx = curr1stIdx;
                        curr1st = n_i;
                        curr1stIdx = i;
                        nextBestDist = bestSqrDist;
                        bestSqrDist = n_i.sqrDist;
                    }

                    // Calculate node with maximal Error
                    if (n_i.error > maxError) {
                        maxError = n_i.error;
                        numError = i;
                    }

                    // Calculate node with mininimum utility (GNG-U)
                    if (n_i.utility < minUtility) {
                        minUtility = n_i.utility;
                        minUtilityNode = i;
                    }
                }
                //
                // assertion: winner should be determined here
                //
                if (Float.isNaN(curr1st.x)){ // hack
                    System.out.printf("learn(): Float.isNaN(pick.x) stopping .....\n");
                    return;
                }
                valueGraph += bestSqrDist;

                // Mark winner for teach-mode
                curr1st.isWinner = true;
                curr1st.nwins++;
                float epsilonGNG = 1.0f/curr1st.nwins;
                float epsilonGNG2 = 0.01f/curr1st.nwins;
                // do adaptation ("learning") according to current model
                //
// Find second-closest node (continued)
                if (curr1stIdx == curr2ndIdx) {
                    curr2ndIdx++;
                    nextBestDist = Float.MAX_VALUE;
                    curr2nd = nodes[curr2ndIdx];
                }
                for (i = curr1stIdx + 1 ; i < nNodes ; i++) {
                    //n_i = nodes[i];
                    if (nodes[i].sqrDist < nextBestDist) {
                        curr2nd = nodes[i];
                        curr2ndIdx = i;
                        nextBestDist = nodes[i].sqrDist;
                    }
                }
                // Mark second for teach-mode
                curr2nd.isSecond = true;

                // Adaptation of Winner:
                if (dimension==-1) {
                    dx = epsilonGNG * (SignalX - curr1st.x);
                    dy = epsilonGNG * (SignalY - curr1st.y);
                    curr1st.adapt(dx, dy);
                } else {
                    for (int d = 0; d < dimension; d++) {
                        delta[d] = epsilonGNG * (Signal[d] - curr1st.feature[d]);
                    }
                    curr1st.adapt(delta);
                }

                numNb = curr1st.numNeighbors();

                // Adaptation of Neighbors:
                int nn;
                for (i = 0; i < numNb; i++) {
                    nn = curr1st.neighbor(i);

                    nodes[nn].hasMoved = true;
                    if (dimension==-1) {
                        dx = epsilonGNG2 * (SignalX - nodes[nn].x);
                        dy = epsilonGNG2 * (SignalY - nodes[nn].y);
                        nodes[nn].adapt(dx, dy);
                    } else {
                        for (int d = 0; d < dimension; d++) {
                            delta[d] = epsilonGNG2 * (Signal[d] - curr1st.feature[d]);
                        }
                        nodes[nn].adapt(delta);
                    }
                }

                // Accumulate square error
                curr1st.error += bestSqrDist;

                // Accumulate utility
                curr1st.utility += (nextBestDist - bestSqrDist);

                // Connect the two winning nodes
                addEdge(curr1stIdx, curr2ndIdx);

                // Calculate the age of the connected edges and delete too old edges
                ageEdgesOfNode(curr1stIdx);

                // Check inserting node and insert if necessary
                if ( (sigs % lambdaGNG) == 0 ) {
                    if (!noNewNodesGNGB) {
                        if (autoStopB) {
                            if (nNodes >= maxNodes || (GNG_U_B && (sigs > Double.POSITIVE_INFINITY))) {
//                                        System.out.println("# signals = " + sigs);
                                break;
                            }
                        }
//                                System.out.println("New node is inserted!!!");
                        insertedSoundB =
                                ( -1 != insertNode(numError, maximumErrorNeighbor(numError)) );
                    }
                }

                // Delete Node without Neighbors (not GNG-U)
                if ((toDelete != -1) && (nNodes > 2) && !GNG_U_B )
                {
                    deleteNode(toDelete);
//                            System.out.println("natural delete");
                }
                // Delete Node with very low utility
                else {
                    if ( maxError > minUtility * utilityGNG) {
                        if (GNG_U_B && (nNodes > 2)) {
                            deleteNode(minUtilityNode);
//                                    System.out.println("utility delete " + maxError + "/" + minUtility);
                        }
                    } else if (  (nNodes > 2) && (nNodes > maxNodes) ) {
                        // relevant if maxnodes is lowered during the simulation
                        deleteNode(minUtilityNode);
                    }
                }
            }
            if (stopB==true)
                break;

        } // loop over stepSize

    }

    public void prepareAlgo(int dim){
        dimension = dim;
//        log("PREPARE ALGO");
        int i;
        // Reset values
        sigs = 0;
        nNodes = 0;
        nEdges = 0;
        nodesMovedB = true;
        id_sig = 0;
        // set algo to use
        // Set specific algorithm parameters
        GNG_U_B=true;
        // set algo to GNG in both cases!
        if (maxNodes != 1)
            addNode();
    }

    public void reset() {
        int i;
        // Reset values
        sigs = 0;
        nNodes = 0;
        nEdges = 0;
        noNewNodesGNGB = false;
        nodesMovedB = true;
        addNode(); // node 1
        if (maxNodes != 1)
            addNode(); // node 2
    }

}
