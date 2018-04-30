package grader.griesstudent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import models.Edge;
import models.Node;

/** This class contains Dijkstra's shortest-path algorithm and some other methods. */
public class MinPathGries {

    /** Return the shortest path info from first to last ---or the empty list
     * if a path does not exist. If path is null, return all shortest paths.
     * Return partial results when the size of the settled set reaches sizeS.
     * Note: The empty list is NOT "null"; it is a list with 0 elements. */
    public static HashMap<Node, SFinfo> minPath(Node first, Node last, int sizeS) {
        /* TODO Read note A7 FAQs on the course piazza for ALL details. */
        Heap<Node> F= new Heap<Node>(); // As in lecture slides

        // map contains an entry for each node in S or F. Thus, |map| = |S| + |F|.
        // For each such node, the value part in map contains the shortest known
        // distance to the node and the node's backpointer on that shortest path.
        HashMap<Node, SFinfo> map= new HashMap<Node, SFinfo>();

        // The number of nodes in the settled set
        int settledSize;

        F.insert(first, 0);
        map.put(first, new SFinfo(0, null, first.gems()));
        settledSize= 0;

        // inv: See Piazza note 1008 (Fall 2017), together with the def
        // of F and map and settledSize
        while (F.size() != 0) {
            Node f= F.poll();
            settledSize= settledSize + 1;
            if (f == last  || settledSize >= sizeS) {
                return map;
            }

            SFinfo finfo= map.get(f);

            for (Edge e : f.exits()) {// for each neighbor w of f
                Node w= e.getOther(f);
                int newWdist= finfo.dist + (int) e.length;
                SFinfo wInfo= map.get(w);

                if (wInfo == null) { //if w not in S or F
                    map.put(w, new SFinfo(newWdist, f, finfo.gems + w.gems()));
                    F.insert(w, newWdist);
                } else if (newWdist < wInfo.dist) {
                    wInfo.dist= newWdist;
                    wInfo.bckPntr= f;
                    wInfo.gems= finfo.gems + w.gems();
                    F.changePriority(w, newWdist);
                }
            }
        }

        // no path from start to end
        return map;
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
    
    /** Return the shortest distance from first to last. It
     * is known to exist.*/
    public static List<Node> minPath(Node first, Node last) {
        HashMap<Node, SFinfo> map= MinPathGries.minPath(first, last, Integer.MAX_VALUE);
        return MinPathGries.buildPath(last, map);
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



    
}
