
package grader.griesstudent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Edge;
import models.Node;

/** This class contains Dijkstra's shortest-path algorithm and some other methods. */
public class MinPath {

    /** Return the shortest path from first to last ---or the empty list
     * if a path does not exist.
     * Note: The empty list is NOT "null"; it is a list with 0 elements. */
    public static List<Node> minPath(Node first, Node last) {
        /* TODO Read note A7 FAQs on the course piazza for ALL details. */
        Heap<Node> F= new Heap<Node>(); // As in lecture slides

        // map contains an entry for each node in S or F. Thus, |map| = |S| + |F|.
        // For each such node, the value part in map contains the shortest known
        // distance to the node and the node's backpointer on that shortest path.
        HashMap<Node, SFinfo> map= new HashMap<Node, SFinfo>();

        F.insert(first, 0);
        map.put(first, new SFinfo(0, null));
        // inv: See Piazza note 1008 (Fall 2017), together with the def of F and map
        while (F.size() != 0) {
            Node f= F.poll();
            if (f == last) return buildPath(last, map);
            int fDist= map.get(f).distance;
            
            for (Edge e : f.exits()) {// for each neighbor w of f
                Node w= e.getOther(f);
                int newWdist= fDist + e.length;
                SFinfo wInfo= map.get(w);
                if (wInfo == null) { //if w not in S or F
                    map.put(w, new SFinfo(newWdist, f));
                    F.insert(w, newWdist);
                } else if (newWdist < wInfo.distance) {
                    wInfo.distance= newWdist;
                    wInfo.bckPntr= f;
                    F.changePriority(w, newWdist);
                }
            }
        }

        // no path from start to end
        return new LinkedList<Node>();
    }


    /** Return the path from the first node to node last.
     *  Precondition: info contains all the necessary information about
     *  the path. */
    public static List<Node> buildPath(Node last, HashMap<Node, SFinfo> info) {
        List<Node> path= new LinkedList<Node>();
        Node p= last;
        // invariant: All the nodes from p's successor to the end are in
        //            path, in reverse order.
        while (p != null) {
            path.add(0, p);
            p= info.get(p).bckPntr;
        }
        return path;
    }

    /** Return the sum of the weights of the edges on path path. */
    public static int pathWeight(List<Node> path) {
        if (path.size() == 0) return 0;
        synchronized(path) {
            Iterator<Node> iter= path.iterator();
            Node p= iter.next();  // First node on path
            int s= 0;
            // invariant: s = sum of weights of edges from start to p
            while (iter.hasNext()) {
                Node q= iter.next();
                s= s + p.getEdge(q).length;
                p= q;
            }
            return s;
        }
    }

    /** An instance contains information about a node: the previous node
     *  on a shortest path from the start node to this node and the distance
     *  of this node from the start node. */
    private static class SFinfo {
        private Node bckPntr; // backpointer on path from start node to this one
        private int distance; // distance from start node to this one

        /** Constructor: an instance with distance d from the start node and
         *  backpointer p.*/
        private SFinfo(int d, Node p) {
            distance= d;     // Distance from start node to this one.
            bckPntr= p;  // Backpointer on the path (null if start node)
        }

        /** return a representation of this instance. */
        public String toString() {
            return "dist " + distance + ", bckptr " + bckPntr;
        }
    }
}
