package pes.tda.mapping;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import pes.tda.algorithm.AdaptiveGNG;

import javax.swing.*;
import javax.swing.border.Border;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * @author Su Nguyen
 * Research Centre for Data Analytics and Cognition
 * La Trobe University
 * <p>
 * This used GraphStream to visualise the GNG network.
 */

public class Visualisation {
    public enum NODE_LABEL_STYLE {AUTO_LABEL, NO_LABEL, USERDDEFINED_LABEL};
    public static NODE_LABEL_STYLE ns = NODE_LABEL_STYLE.USERDDEFINED_LABEL;
    public enum NODE_ACTION {ADD_NEW_NODE, SELECT_NODE, ADD_WEIGHT};
    public static ArrayList<String> selected_nodes = new ArrayList();
    public static NODE_ACTION ACTION_MODE = NODE_ACTION.SELECT_NODE;
    public static boolean display = true;
    public static boolean grayscale = false;
    public static int size_scale = 1;
    public static ArrayList<String> labels = null;
    public static ArrayList<Double> values = null;
    public static ArrayList<Double> progsizes = null;
    public static ArrayList<Integer> sizes = null;
    public static ArrayList<double[]> corrs = null;
    public static String[] reference_names = null;
    public static double[][] reference_corrs = null;
    static MultiGraph g = null;
    static Viewer viewer = null;
    static int count = 0;
    public static int nodeSize = -1;
    public static int textSize = -1;
    public static int markbest = -1;
    public static int ncomponents = -1;
    public static int nnodes = -1;
    static ArrayList<String> controlnodes = new ArrayList<>();
    public static boolean VISVAL = true;
    public static void clearOldData() {
        if (labels != null && !labels.isEmpty()) labels.clear();
        if (values != null && !values.isEmpty()) values.clear();
        if (sizes != null && !sizes.isEmpty()) sizes.clear();
        if (corrs != null && !corrs.isEmpty()) corrs.clear();
        if (progsizes != null && !progsizes.isEmpty()) progsizes.clear();
    }
    static boolean loop = true;
    static boolean to_reset = false;

    public static void addLabels(String[] lbs) {
        if (labels == null) labels = new ArrayList<>();
        for (int i = 0; i < lbs.length; i++) {
            labels.add(lbs[i]);
        }
    }
    public static double[] lastpos = new double[2];


    public static void addValues(double[] vs) {
        if (values == null) values = new ArrayList<>();
        for (int i = 0; i < vs.length; i++) {
            values.add(vs[i]);
        }
        if (VISVAL)
            preprocessVal();
    }

    public static void addSize(int[] sizes) {
        if (Visualisation.sizes == null) Visualisation.sizes = new ArrayList<>();
        for (int i = 0; i < sizes.length; i++) {
            Visualisation.sizes.add(sizes[i]);
        }
    }

    public static void addPSize(double[] psizes) {
        if (Visualisation.progsizes == null) Visualisation.progsizes = new ArrayList<>();
        for (int i = 0; i < psizes.length; i++) {
            Visualisation.progsizes.add(psizes[i]);
        }
        if (!VISVAL)
            preprocessProgsize();
    }

    public static void addCorr(double[][] corr) {
        if (corrs == null) corrs = new ArrayList<>();
        for (int i = 0; i < corr.length; i++) {
            corrs.add(corr[i]);
        }
    }

    static String[] styles = new String[] {
            "shape: circle; fill-color: blue; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: red; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: darkgreen; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: black; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: yellow; size: 10px; text-color:black; text-size: 20px;",
            "shape: circle; fill-color: orange; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: brown; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: grey; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: darkred; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: maroon; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: purple; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: olive; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: navy; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: teal; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: crimson; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: darkblue; size: 10px; text-color:white; text-size: 20px;",
            "shape: circle; fill-color: deeppink; size: 10px; text-color:white; text-size: 20px;",
    };

    public static void setNodeSize(int nodesize, int textsize ) {
        nodeSize = nodesize;
        textSize = textsize;
        String[] s = new String[] {
                "shape: circle; fill-color: blue; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: red; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: darkgreen; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: black; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: green; size:"+nodesize+"px; text-color:black; text-size: "+textsize+"px;",
                "shape: circle; fill-color: orange; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: brown; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: grey; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: darkred; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: maroon; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: purple; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: olive; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: navy; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: teal; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: crimson; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: darkblue; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
                "shape: circle; fill-color: deeppink; size:"+nodesize+"px; text-color:white; text-size: "+textsize+"px;",
        };
        styles = s;
    } 
    public static void main(String argv[]) throws Exception {
    }

    public static void generateGraph(AdaptiveGNG gng, String[] lbs, double[] vals, double[] psize, int[] nsize, double[][] ncorr, boolean accum, boolean interactive, boolean fixpos, boolean writeDot, boolean takescreenshot, String outputname) throws IOException, InterruptedException {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        controlnodes.add("Select"); controlnodes.add("Unselect"); controlnodes.add("+");
        controlnodes.add("-");controlnodes.add("Expand");controlnodes.add("Clear");
        controlnodes.add("Switch"); controlnodes.add("Interactive");

        selected_nodes.clear();
        loop = true;
        if (g == null) {
            count = 0;
            g = new MultiGraph("test");
            if (display) {
                if (!fixpos) viewer = g.display(false);
                else {
                    viewer = g.display(true);
                }
            }
        } else {
            g.clear();
        }
        if (!accum) {
            clearOldData();
            count = 0;
        }
        ViewerPipe fromViewer = null;
        if (interactive) {
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
            fromViewer = viewer.newViewerPipe();
            add_listener_to_graph(viewer, fromViewer);
        }
        addLabels(lbs);
        addValues(vals);
        addSize(nsize);
        addCorr(ncorr);
        addPSize(psize);
//        viewer.getDefaultView().setToolTipText(outputname);
        Border boder = BorderFactory.createTitledBorder(outputname);
        if (display) viewer.getDefaultView().setBorder(boder);
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.quality");
        g.addAttribute("ui.stylesheet","graph {fill-color:white;}");

        for (int id = count; id < count+gng.nNodes; id++) {
            if (true) {
                g.addNode(String.valueOf(id)).addAttribute("x", corrs.get(id)[0]);
                g.getNode(String.valueOf(id)).addAttribute("y", corrs.get(id)[1]);
                g.getNode(String.valueOf(id)).addAttribute("layout.frozen");
            } else {
                g.addNode(String.valueOf(id)).addAttribute("id", id);
            }

        }

        for (int i = 0; i < gng.nEdges; i++) {
            int from = count + gng.edges[i].from;
            int to = count + gng.edges[i].to;
            g.addEdge(from+"_"+to, String.valueOf(from), String.valueOf(to), false);
        }

        count += gng.nNodes;

        for (Node node: g) {
            switch (ns){
                case NO_LABEL:
                    break;
                case AUTO_LABEL:
                    node.addAttribute("ui.label", node.getId());
                    break;
                case USERDDEFINED_LABEL:
                    node.addAttribute("ui.label", labels.get(Integer.parseInt(node.getId())));
                    break;
                default:
                    break;
            }
            if (values!=null) node.addAttribute("fitness", values.get(Integer.parseInt(node.getId())));
            if (progsizes!=null) node.addAttribute("progsize", progsizes.get(Integer.parseInt(node.getId())));
            if (sizes !=null) node.addAttribute("nsize", size_scale* sizes.get(Integer.parseInt(node.getId())));
            node.addAttribute("preference", 1);
            node.addAttribute("newpos", 0);
        }

        ConnectedComponents cc = new ConnectedComponents();

        cc.init(g);
        cc.compute();

        Iterator it = cc.iterator();

        for (int c = 0; c < cc.getConnectedComponentsCount(); c++) {
            for (Node node: cc.getGiantComponent(c+1)) {
                if (!labels.get(Integer.parseInt(node.getId())).equals("#0")) {
                    node.addAttribute("ui.style", styles[c%styles.length]);
                } else {
                    node.addAttribute("ui.style", styles[c%styles.length]);
//                    node.addAttribute("ui.style", "shape: diamond; size: "+1.5*nodeSize+"px; text-size: "+1.5*textSize+"px;");
                }
                if (values != null) {
                    double val = node.getAttribute("fitness");
                    if (!VISVAL) {
                        val = node.getAttribute("progsize");
                    }
                    if (!grayscale) {
                        node.addAttribute("ui.style", "fill-color:" + getHeatColor(val) + "; text-color:black;");
                    } else {
                        node.addAttribute("ui.style", "fill-color:" + getGrayscaleColor(val) + "; text-color:black;");
                    }
                }
                if (sizes != null) {
                    int val = node.getAttribute("nsize");
                    node.addAttribute("ui.style", "size:" + val + "px; text-size: "+(int)(val*0.9)+"px;");
                }
            }
        }

        System.out.printf("%d connected component(s) and %d nodes in this graph, so far.%n",
                cc.getConnectedComponentsCount(), gng.nNodes);
        ncomponents = cc.getConnectedComponentsCount();
        nnodes = gng.nNodes;
        System.out.flush();

        // add reference points
        if (reference_names != null) {
            for (int i = 0; i < reference_names.length; i++) {
                g.addNode(reference_names[i]).addAttribute("x", reference_corrs[i][0]);
                g.getNode(reference_names[i]).addAttribute("y", reference_corrs[i][1]);
                g.getNode(reference_names[i]).addAttribute("ui.style", "stroke-mode: plain; stroke-width: 7; stroke-color: gray;" +
                        "shape: cross; fill-mode: none; fill-color: gray; text-color:black; size:" + 20*size_scale+
                        "px; text-size: 30px; text-style: bold; z-index: 5;");
                g.getNode(reference_names[i]).addAttribute("ui.label", reference_names[i]);
                controlnodes.add(reference_names[i]);
            }
        }

        if (interactive) {
            addControlNodes();
            while (loop) {
                fromViewer.pump(); // or fromViewer.blockingPump(); in the nightly builds
            }
        }
        if (writeDot) {
            FileSinkDOT fs = new FileSinkDOT();
            fs.writeAll(g, "gg.dot");
            Process p = Runtime.getRuntime().exec("\"c:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe\" -Ksfdp -Tps gg.dot -o \"out.ps\"");
            p.waitFor();
            Runtime.getRuntime().exec("C:\\Users\\nguye\\AppData\\Local\\Apps\\Evince-2.32.0\\bin\\evince.exe \"out.ps\"");
        }
        if (takescreenshot) {
            g.addAttribute("ui.quality");
//            Thread.sleep(10000);
            if (outputname==null) {
                g.addAttribute("ui.screenshot", "gnggraph.png");
                System.out.println("Saving the graph as gnggraph.png");
            }
            else {
                g.addAttribute("ui.screenshot", "visout\\" + outputname);
                System.out.println("Saving the graph as " + outputname);
            }
        }

        if (interactive) {
            for (Node node : g) {
                if (controlnodes.contains(node.getId())) {
                    continue;
                }
                int id = Integer.parseInt(node.getId());
                int preference = node.getAttribute("preference");
                if (preference > 1)
                    System.out.println("Node #" + node.getId() + ": x=" + gng.nodes[id].feature[0] + "; y=" + gng.nodes[id].feature[1] + " ; weight=" + gng.nodes[id].preference);
                gng.nodes[id].preference = preference;
                double[] features = {node.getAttribute("x"), node.getAttribute("y")};
                gng.nodes[id].feature = features;
                if (preference > 1)
                    System.out.println("---> #" + node.getId() + ": x=" + features[0] + "; y=" + features[1] + " ; weight=" + preference);
            }
            if (!Mapper.interactive || to_reset) {
                g = null;
                to_reset = !to_reset;
            }
        }
    }

    private static void addControlNodes() {
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        for (Node node: g) {
            double x = node.getAttribute("x");
            double y = node.getAttribute("y");
            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);
            minX = Math.min(x, minX);
            minY = Math.min(y, minY);
        }
        double offset = 0.1;
        // add control nodes
        Node node = g.addNode("Interactive");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: blue; text-color:white; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.40);
        if (Mapper.interactive) {
            node.addAttribute("ui.label", "Ion");
        } else node.addAttribute("ui.label", "Ioff");

        node = g.addNode("Select");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: gray; text-color:black; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.35);
        node.addAttribute("ui.label", "S");

        node = g.addNode("Unselect");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: maroon; text-color:white; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.30);
        node.addAttribute("ui.label", "U");

        node = g.addNode("Expand");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: purple; text-color:white; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.25);
        node.addAttribute("ui.label", "E");

        node = g.addNode("+");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: green; text-color:black; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.20);
        node.addAttribute("ui.label", "+");

        node = g.addNode("-");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: red; text-color:white; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.15);
        node.addAttribute("ui.label", "-");

        node = g.addNode("Clear");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: white; text-color:black; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.10);
        node.addAttribute("ui.label", "C");

        node = g.addNode("Switch");
        node.addAttribute("ui.style", "stroke-mode: plain; shape: box; fill-color: yellow; text-color:black; size:" + 50*size_scale+ "px; text-size: 30px;");
        node.addAttribute("xy", minX - (maxX-minX)*offset, minY + (maxY-minY)*0.05);
        node.addAttribute("ui.label", "F-S");
    }

    private static void add_listener_to_graph(Viewer viewer, ViewerPipe fromViewer) {
        fromViewer.addViewerListener(new ViewerListener() {
            @Override
            public void viewClosed(String s) {
                loop = false;
                to_reset = true;
            }

            @Override
            public void buttonPushed(String id) {
                System.out.println("Button pushed on node "+id);
                switch (id) {
                    case "Interactive":
                        Node inode = g.getNode(id);
                        Mapper.interactive = !Mapper.interactive;
                        if (Mapper.interactive) {
                            inode.addAttribute("ui.label", "Ion");
                        } else inode.addAttribute("ui.label", "Ioff");
                        return;
                    case "Select":
                        ACTION_MODE = NODE_ACTION.SELECT_NODE;
                        System.out.println("Activate Select Mode!");
                        return;
                    case "Expand":
                        if (!selected_nodes.isEmpty()) {
                            System.out.println("Expand Selection!");
                        } else {
                            System.out.println("No Selected Nodes!");
                            return;
                        }
                        ArrayList<String> new_selected_nodes = new ArrayList();
                        for (String nn: selected_nodes) {
                            Iterator<Node> it = g.getNode(nn).getNeighborNodeIterator();
                            while (it.hasNext()) {
                                Node neighbour = it.next();
//                                System.out.println(neighbour);
                                if (!selected_nodes.contains(neighbour.toString())) {
                                    new_selected_nodes.add(neighbour.toString());
                                }
                            }
                        }
//                        System.out.println(new_selected_nodes.toString());
                        for (String nn: new_selected_nodes) {
                            Node node = g.getNode(nn);
                            int pref = node.getAttribute("preference");
                            node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4 * pref + "; stroke-color: blue;");
                        }
                        selected_nodes.addAll(new_selected_nodes);
                        return;
                    case "+":
                        for (String nn: selected_nodes) {
                            Node node = g.getNode(nn);
                            int pref = node.getAttribute("preference");
                            pref = Math.min(pref + 1, 5);
                            node.addAttribute("preference", pref);
                            node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4*pref + ";");
                        }
                        return;
                    case "-":
                        for (String nn: selected_nodes) {
                            Node node = g.getNode(nn);
                            int pref = node.getAttribute("preference");
                            pref = Math.max(pref - 1, 1);
                            node.addAttribute("preference", pref);
                            node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4*pref + ";");
                        }
                        return;
                    case "Unselect":
                        for (String nn: selected_nodes) {
                            Node node = g.getNode(nn);
                            int pref = node.getAttribute("preference");
                            if (pref == 1) {
                                node.addAttribute("ui.style", "stroke-mode: none;");
                            } else {
                                node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4*pref + "; stroke-color: teal;");
                            }
                        }
                        selected_nodes.clear();
                        return;
                    case "Clear":
                        for (Node node: g) {
                            if (!controlnodes.contains(node.getId())) {
                                node.addAttribute("preference", 1);
                                node.addAttribute("ui.style", "stroke-mode: none;");
                            }
                        }
                        selected_nodes.clear();
                        return;
                    case "Switch":
                        VISVAL = !VISVAL;
                        if (VISVAL) {
                            g.getNode("Switch").addAttribute("ui.label", "F-S");
                            preprocessVal();
                        } else {
                            g.getNode("Switch").addAttribute("ui.label", "S-F");
                            preprocessProgsize();
                        }
                        for (Node node: g) {
                            if (controlnodes.contains(node.getId())) continue;
                            int pref = node.getAttribute("preference");
                            if (pref == 1) {
                                node.addAttribute("ui.style", "stroke-mode: none;");
                            } else {
                                node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4*pref + "; stroke-color: teal;");
                            }
                            double val = values.get(Integer.parseInt(node.getId()));
                            if (!VISVAL) {
                                val = progsizes.get(Integer.parseInt(node.getId()));
                            }
//                            System.out.println(val);
                            if (!grayscale) {
                                node.addAttribute("ui.style", "fill-color:" + getHeatColor(val) + "; text-color:black;");
                            } else {
                                node.addAttribute("ui.style", "fill-color:" + getGrayscaleColor(val) + "; text-color:black;");
                            }
                        }
                        selected_nodes.clear();
                        return;
                    default:
                }

                GraphicNode n = viewer.getGraphicGraph().getNode(id);
                double[] newpos = {n.getX(),  n.getY()};
                lastpos = newpos;
                System.out.println("x: " + n.getX() + "; y: " + n.getY());
            }

            @Override
            public void buttonReleased(String id) {
                if (!controlnodes.contains(id)) {
                    Node node = g.getNode(id);
                    int pref = node.getAttribute("preference");
                    boolean justselect = false;
                    if (!selected_nodes.contains(id)) {
                        selected_nodes.add(id);
                        node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4*pref + "; stroke-color: blue;");
                        justselect = true;
                    }
                    System.out.println("Button released on node " + id);
                    GraphicNode n = viewer.getGraphicGraph().getNode(id);
                    double[] newpos = {n.getX(), n.getY()};
                    if ((lastpos[0] == newpos[0]) && (lastpos[1] == newpos[1])) {
                        if (!justselect && selected_nodes.contains(id)) {
                            selected_nodes.remove(id);
                            if (pref == 1) {
                                node.addAttribute("ui.style", "stroke-mode: none;");
                            } else {
                                node.addAttribute("ui.style", "stroke-mode: dots; stroke-width: " + 4*pref + "; stroke-color: teal;");
                            }
                        }
                        System.out.println("Node is not moved!");
                    } else {
                        System.out.println("Node is moved to new position!");
                        double[] diff = {newpos[0] - lastpos[0], newpos[1] - lastpos[1]};
                        System.out.println(" -- Diff " + diff[0] + ";;" + diff[1]);
                        node = g.getNode(id);
                        node.addAttribute("newpos", 1);
                        node.setAttribute("x", n.getX());
                        node.setAttribute("y", n.getY());
                        for (String nn: selected_nodes) {
                            if (!nn.equals(id)) {
                                GraphicNode ng = viewer.getGraphicGraph().getNode(nn);
                                node = g.getNode(nn);
//                                node.addAttribute("xy", , ng.getY() + diff[1]);
                                node.setAttribute("x", ng.getX() + diff[0]);
                                node.setAttribute("y", ng.getY() + diff[1]);
                                System.out.println("Other node #" + nn + "x: " + ng.getX() + "; y: " + ng.getY());
                            }
                        }
                    }
                    lastpos = null;
                    System.out.println("x: " + n.getX() + "; y: " + n.getY());
                }
            }
        });
        fromViewer.addSink(g);
    }

    public static double minval = 0;
    public static double maxval = 2;
    public static double midval = 1;

    public static void preprocessVal() {
        minval = Double.POSITIVE_INFINITY;
        maxval = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.size(); i++) {
            minval = Math.min(minval, values.get(i));
            if (!values.get(i).isInfinite()) maxval = Math.max(maxval, values.get(i));
        }
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).isInfinite())
                values.set(i, maxval);
        }
        midval = minval + color_ratio*(maxval - minval);
    }


    public static double color_ratio = 0.1;
    public static void preprocessProgsize() {
        minval = Double.POSITIVE_INFINITY;
        maxval = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < progsizes.size(); i++) {
            minval = Math.min(minval, progsizes.get(i));
            if (!progsizes.get(i).isInfinite()) maxval = Math.max(maxval, progsizes.get(i));
        }
        for (int i = 0; i < progsizes.size(); i++) {
            if (progsizes.get(i).isInfinite())
                progsizes.set(i, maxval);
        }
        midval = minval + color_ratio*(maxval - minval);
    }

    public static String getHeatColor(double val) {
        if (val < midval) {
            int col = (int) (255*(1-(midval - val)/(midval-minval)));
            return "rgb("+col+",255,0)";
        } else {
            int col = (int) (255*(1-(val-midval)/(maxval-midval)));
            return "rgb(255,"+col+",0)";
        }
    }

    public static String getGrayscaleColor(double val) {
        int col = (int) (120*(maxval - val)/(maxval-minval) + 100);
        return "rgb("+col+","+col+","+col+")";
    }

}
