package grader.griesstudent;

import models.Node;

/** An instance contains information about a node: the previous node
 *  on a shortest path from the start node to this node, the distance
 *  of this node from the start node, and the number of gems on the 
 *  path */
public class SFinfo {
    Node bckPntr; // backpointer on path from start node to this one
    int dist; // distance from start node to this one
    int gems;     // number of gems on the path

    /** Constructor: an instance with distance d from the start node,
     *  backpointer p, and number of gems g*/
    SFinfo(int d, Node p, int g) {
        dist= d;     // Distance from start node to this one.
        bckPntr= p;  // Backpointer on the path (null if start node)
        gems= g;
    }

    /** return a representation of this instance. */
    public String toString() {
        return "dist " + dist + ", bckptr " + bckPntr +
                "gems " + gems;
    }
}
