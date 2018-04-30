package grader.griesstudent;

import models.Node;

/** An instance contains a Node and its corresponding SFInfo object,
 * meaning the shortest distance to it, its backpointer, and number of gems */
public class NodeInfo {
    Node node;
    SFinfo info;

    /** Constructor: an instance with node n and info in. */
    public NodeInfo(Node n, SFinfo in) {
        node= n;
        info= in;
    }
}