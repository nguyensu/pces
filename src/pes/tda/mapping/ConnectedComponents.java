package pes.tda.mapping;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.algorithm.FixedArrayList;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.util.Filter;
import org.graphstream.util.FilteredEdgeIterator;
import org.graphstream.util.FilteredNodeIterator;
import org.graphstream.util.Filters;

import java.util.*;

public class ConnectedComponents extends SinkAdapter implements DynamicAlgorithm, Iterable<ConnectedComponents.ConnectedComponent> {
    private HashMap<Node, Integer> connectedComponentsMap;
    protected Graph graph;
    protected int connectedComponents;
    protected HashMap<Integer, Integer> connectedComponentsSize;
    protected FixedArrayList<String> ids;
    protected FixedArrayList<ConnectedComponent> components;
    protected boolean started;
    protected String cutAttribute;
    protected String countAttribute;

    public ConnectedComponents() {
        this((Graph)null);
    }

    public ConnectedComponents(Graph graph) {
        this.connectedComponents = 0;
        this.ids = new FixedArrayList();
        this.components = new FixedArrayList();
        this.started = false;
        this.cutAttribute = null;
        this.countAttribute = null;
        this.ids.add("");
        if(graph != null) {
            this.init(graph);
        }

    }

    public List<Node> getGiantComponent(int idc) {
        if(!this.started) {
            this.compute();
        }
        ArrayList<Node> giant = new ArrayList();
        Iterator it = this.graph.getNodeSet().iterator();

        while(it.hasNext()) {
            Node n = (Node)it.next();
            if(((Integer)this.connectedComponentsMap.get(n)).intValue() == idc) {
                giant.add(n);
            }
        }

        return giant;
    }

    public List<Node> getGiantComponent() {
        if(!this.started) {
            this.compute();
        }

        int maxSize = -2147483648;
        int maxIndex = -1;
        Iterator i$ = this.connectedComponentsSize.keySet().iterator();

        while(i$.hasNext()) {
            Integer c = (Integer)i$.next();
            if(((Integer)this.connectedComponentsSize.get(c)).intValue() > maxSize) {
                maxSize = ((Integer)this.connectedComponentsSize.get(c)).intValue();
                maxIndex = c.intValue();
            }
        }

        if(maxIndex != -1) {
            ArrayList<Node> giant = new ArrayList();
            Iterator it = this.graph.getNodeSet().iterator();

            while(it.hasNext()) {
                Node n = (Node)it.next();
                if(((Integer)this.connectedComponentsMap.get(n)).intValue() == maxIndex) {
                    giant.add(n);
                }
            }

            return giant;
        } else {
            return null;
        }
    }

    public int getConnectedComponentsCount() {
        return this.getConnectedComponentsCount(1);
    }

    public int getConnectedComponentsCount(int sizeThreshold) {
        return this.getConnectedComponentsCount(sizeThreshold, 0);
    }

    public int getConnectedComponentsCount(int sizeThreshold, int sizeCeiling) {
        if(!this.started) {
            this.compute();
        }

        if(sizeThreshold <= 1 && sizeCeiling <= 0) {
            return this.connectedComponents;
        } else {
            int count = 0;
            Iterator i$ = this.connectedComponentsSize.keySet().iterator();

            while(true) {
                Integer c;
                do {
                    do {
                        if(!i$.hasNext()) {
                            return count;
                        }

                        c = (Integer)i$.next();
                    } while(((Integer)this.connectedComponentsSize.get(c)).intValue() < sizeThreshold);
                } while(sizeCeiling > 0 && ((Integer)this.connectedComponentsSize.get(c)).intValue() >= sizeCeiling);

                ++count;
            }
        }
    }

    public Iterator<ConnectedComponent> iterator() {
        while(this.components.size() > this.connectedComponents) {
            this.components.remove(this.components.getLastIndex());
        }

        return this.components.iterator();
    }

    protected int addIdentifier() {
        this.ids.add("");
        return this.ids.getLastIndex();
    }

    protected void removeIdentifier(int identifier) {
        this.ids.remove(identifier);
    }

    public void setCutAttribute(String cutAttribute) {
        this.cutAttribute = cutAttribute;
        this.compute();
    }

    public void setCountAttribute(String countAttribute) {
        this.removeMarks();
        this.countAttribute = countAttribute;
        this.remapMarks();
    }

    protected void removeMarks() {
        Iterator nodes = this.graph.getNodeIterator();

        while(nodes.hasNext()) {
            Node node = (Node)nodes.next();
            if(this.countAttribute == null) {
                node.removeAttribute(this.countAttribute);
            }
        }

    }

    protected void remapMarks() {
        if(this.countAttribute != null && this.connectedComponentsMap != null) {
            Iterator nodes = this.graph.getNodeIterator();

            while(nodes.hasNext()) {
                Node v = (Node)nodes.next();
                int id = ((Integer)this.connectedComponentsMap.get(v)).intValue();
                v.addAttribute(this.countAttribute, new Object[]{Integer.valueOf(id - 1)});
            }
        }

    }

    public void init(Graph graph) {
        if(this.graph != null) {
            this.graph.removeSink(this);
        }

        this.graph = graph;
        this.graph.addSink(this);
    }

    public void compute() {
        this.connectedComponents = 0;
        this.started = true;
        this.ids.clear();
        this.ids.add("");
        this.components.add(new ConnectedComponent(Integer.valueOf(0)));
        this.connectedComponentsMap = new HashMap();
        this.connectedComponentsSize = new HashMap();
        Iterator nodes = this.graph.getNodeIterator();

        while(nodes.hasNext()) {
            this.connectedComponentsMap.put((Node) nodes.next(), Integer.valueOf(0));
        }

        nodes = this.graph.getNodeIterator();

        while(nodes.hasNext()) {
            Node v = (Node)nodes.next();
            if(((Integer)this.connectedComponentsMap.get(v)).intValue() == 0) {
                ++this.connectedComponents;
                int newIdentifier = this.addIdentifier();
                int size = this.computeConnectedComponent(v, newIdentifier, (Edge)null);
                if(size > 0) {
                    this.components.add(new ConnectedComponent(Integer.valueOf(newIdentifier)));
                }

                this.connectedComponentsSize.put(Integer.valueOf(newIdentifier), Integer.valueOf(size));
            }
        }

        this.remapMarks();
    }

    public void terminate() {
        if(this.graph != null) {
            this.graph.removeSink(this);
            this.graph = null;
            this.started = false;
            this.connectedComponents = 0;
            this.connectedComponentsSize.clear();
        }

    }

    private int computeConnectedComponent(Node v, int id, Edge exception) {
        int size = 0;
        LinkedList<Node> open = new LinkedList();
        open.add(v);

        label34:
        while(!open.isEmpty()) {
            Node n = (Node)open.remove();
            this.connectedComponentsMap.put(n, Integer.valueOf(id));
            ++size;
            this.markNode(n, id);
            Iterator edges = n.getEdgeIterator();

            while(true) {
                Edge e;
                do {
                    do {
                        if(!edges.hasNext()) {
                            continue label34;
                        }

                        e = (Edge)edges.next();
                    } while(e == exception);
                } while(this.cutAttribute != null && e.hasAttribute(this.cutAttribute));

                Node n2 = e.getOpposite(n);
                if(((Integer)this.connectedComponentsMap.get(n2)).intValue() != id) {
                    open.add(n2);
                    this.connectedComponentsMap.put(n2, Integer.valueOf(id));
                    this.markNode(n2, id);
                }
            }
        }

        return size;
    }

    protected void markNode(Node node, int id) {
        if(this.countAttribute != null) {
            node.addAttribute(this.countAttribute, new Object[]{Integer.valueOf(id - 1)});
        }

    }

    public void edgeAdded(String graphId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
        if(!this.started && this.graph != null) {
            this.compute();
        } else if(this.started) {
            Edge edge = this.graph.getEdge(edgeId);
            if(edge != null && !((Integer)this.connectedComponentsMap.get(edge.getNode0())).equals(this.connectedComponentsMap.get(edge.getNode1()))) {
                --this.connectedComponents;
                int id0 = ((Integer)this.connectedComponentsMap.get(edge.getNode0())).intValue();
                int id1 = ((Integer)this.connectedComponentsMap.get(edge.getNode1())).intValue();
                this.computeConnectedComponent(edge.getNode1(), id0, edge);
                this.removeIdentifier(id1);
                this.connectedComponentsSize.put(Integer.valueOf(id0), Integer.valueOf(((Integer)this.connectedComponentsSize.get(Integer.valueOf(id0))).intValue() + ((Integer)this.connectedComponentsSize.get(Integer.valueOf(id1))).intValue()));
                this.connectedComponentsSize.remove(Integer.valueOf(id1));
            }
        }

    }

    public void nodeAdded(String graphId, long timeId, String nodeId) {
        if(!this.started && this.graph != null) {
            this.compute();
        } else if(this.started) {
            Node node = this.graph.getNode(nodeId);
            if(node != null) {
                ++this.connectedComponents;
                int id = this.addIdentifier();
                this.connectedComponentsMap.put(node, Integer.valueOf(id));
                this.markNode(node, id);
                this.connectedComponentsSize.put(Integer.valueOf(id), Integer.valueOf(1));
            }
        }

    }

    public void edgeRemoved(String graphId, long timeId, String edgeId) {
        if(!this.started && this.graph != null) {
            this.compute();
        }

        if(this.started) {
            Edge edge = this.graph.getEdge(edgeId);
            if(edge != null) {
                int id = this.addIdentifier();
                int oldId = ((Integer)this.connectedComponentsMap.get(edge.getNode0())).intValue();
                int oldSize = ((Integer)this.connectedComponentsSize.get(Integer.valueOf(oldId))).intValue();
                int newSize = this.computeConnectedComponent(edge.getNode0(), id, edge);
                if(!((Integer)this.connectedComponentsMap.get(edge.getNode0())).equals(this.connectedComponentsMap.get(edge.getNode1()))) {
                    if(newSize > 0) {
                        this.connectedComponentsSize.put(Integer.valueOf(id), Integer.valueOf(newSize));
                        ++this.connectedComponents;
                    }

                    if(oldSize - newSize > 0) {
                        this.connectedComponentsSize.put(Integer.valueOf(oldId), Integer.valueOf(oldSize - newSize));
                    } else {
                        this.connectedComponentsSize.remove(Integer.valueOf(oldId));
                        --this.connectedComponents;
                    }
                } else {
                    this.removeIdentifier(oldId);
                    this.connectedComponentsSize.put(Integer.valueOf(id), this.connectedComponentsSize.get(Integer.valueOf(oldId)));
                    this.connectedComponentsSize.remove(Integer.valueOf(oldId));
                }
            }
        }

    }

    public void nodeRemoved(String graphId, long timeId, String nodeId) {
        if(!this.started && this.graph != null) {
            this.compute();
        }

        if(this.started) {
            Node node = this.graph.getNode(nodeId);
            if(node != null) {
                this.connectedComponentsSize.remove(this.connectedComponentsMap.get(node));
                --this.connectedComponents;
                this.removeIdentifier(((Integer)this.connectedComponentsMap.get(node)).intValue());
            }
        }

    }

    public void graphCleared(String graphId, long timeId) {
        if(this.started) {
            this.connectedComponents = 0;
            this.ids.clear();
            this.ids.add("");
            this.components.clear();
            this.components.add(new ConnectedComponent(Integer.valueOf(0)));
            this.connectedComponentsMap.clear();
            this.connectedComponentsSize.clear();
        }

    }

    public void edgeAttributeAdded(String graphId, long timeId, String edgeId, String attribute, Object value) {
        if(this.cutAttribute != null && attribute.equals(this.cutAttribute)) {
            if(!this.started && this.graph != null) {
                this.compute();
            }

            Edge edge = this.graph.getEdge(edgeId);
            int id = this.addIdentifier();
            int oldId = ((Integer)this.connectedComponentsMap.get(edge.getNode0())).intValue();
            int oldSize = ((Integer)this.connectedComponentsSize.get(Integer.valueOf(oldId))).intValue();
            int newSize = this.computeConnectedComponent(edge.getNode0(), id, edge);
            if(!((Integer)this.connectedComponentsMap.get(edge.getNode0())).equals(this.connectedComponentsMap.get(edge.getNode1()))) {
                if(newSize > 0) {
                    this.connectedComponentsSize.put(Integer.valueOf(id), Integer.valueOf(newSize));
                    ++this.connectedComponents;
                }

                if(oldSize - newSize > 0) {
                    this.connectedComponentsSize.put(Integer.valueOf(oldId), Integer.valueOf(oldSize - newSize));
                } else {
                    this.connectedComponentsSize.remove(Integer.valueOf(oldId));
                    --this.connectedComponents;
                }
            } else {
                this.removeIdentifier(oldId);
                this.connectedComponentsSize.put(Integer.valueOf(id), this.connectedComponentsSize.get(Integer.valueOf(oldId)));
                this.connectedComponentsSize.remove(Integer.valueOf(oldId));
            }
        }

    }

    public void edgeAttributeRemoved(String graphId, long timeId, String edgeId, String attribute) {
        if(this.cutAttribute != null && attribute.equals(this.cutAttribute)) {
            if(!this.started && this.graph != null) {
                this.compute();
            }

            Edge edge = this.graph.getEdge(edgeId);
            if(!((Integer)this.connectedComponentsMap.get(edge.getNode0())).equals(this.connectedComponentsMap.get(edge.getNode1()))) {
                --this.connectedComponents;
                int id0 = ((Integer)this.connectedComponentsMap.get(edge.getNode0())).intValue();
                int id1 = ((Integer)this.connectedComponentsMap.get(edge.getNode1())).intValue();
                this.computeConnectedComponent(edge.getNode1(), id0, edge);
                this.removeIdentifier(id1);
                this.connectedComponentsSize.put(Integer.valueOf(id0), Integer.valueOf(((Integer)this.connectedComponentsSize.get(Integer.valueOf(id0))).intValue() + ((Integer)this.connectedComponentsSize.get(Integer.valueOf(id1))).intValue()));
                this.connectedComponentsSize.remove(Integer.valueOf(id1));
            }
        }

    }

    private static class EdgeFilter implements Filter<Edge> {
        Filter<Node> f;

        public EdgeFilter(Filter<Node> f) {
            this.f = f;
        }

        public boolean isAvailable(Edge e) {
            return this.f.isAvailable(e.getNode0()) && this.f.isAvailable(e.getNode1());
        }
    }

    public class ConnectedComponent implements Iterable<Node> {
        public final Integer id;
        Filter<Node> nodeFilter;
        Filter<Edge> edgeFilter;
        Iterable<Edge> eachEdge;

        public ConnectedComponent(Integer id) {
            this.id = id;
            this.nodeFilter = null;
            this.edgeFilter = null;
            this.eachEdge = null;
        }

        public Iterator<Node> iterator() {
            if(this.nodeFilter == null) {
                this.nodeFilter = Filters.byAttributeFilter(ConnectedComponents.this.countAttribute, this.id);
            }

            return new FilteredNodeIterator(ConnectedComponents.this.graph, this.nodeFilter);
        }

        public Iterable<Node> getEachNode() {
            return this;
        }

        public Iterable<Edge> getEachEdge() {
            if(this.eachEdge == null) {
                this.eachEdge = new Iterable<Edge>() {
                    public Iterator<Edge> iterator() {
                        return ConnectedComponent.this.getEdgeIterator();
                    }
                };
            }

            return this.eachEdge;
        }

        public Iterator<Edge> getEdgeIterator() {
            if(this.edgeFilter == null) {
                if(this.nodeFilter == null) {
                    this.nodeFilter = Filters.byAttributeFilter(ConnectedComponents.this.countAttribute, this.id);
                }

                this.edgeFilter = new EdgeFilter(this.nodeFilter);
            }

            return new FilteredEdgeIterator(ConnectedComponents.this.graph, this.edgeFilter);
        }
    }
}
