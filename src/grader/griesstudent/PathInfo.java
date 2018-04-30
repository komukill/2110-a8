package grader.griesstudent;

import java.util.List;

import models.Node;

/** An instance contains a non-empty path, its last node n,
 *  and a length.
 * The length MAY be the length of the path.
 * It MAY be the length of the path + shortest distance to Earth */
class PathInfo {
    List<Node> path; // The path
    Node node;        // Last node on the path
    int length;   // The length

    /** Constructor: an instance with path p, last node n, and length len */
    public PathInfo(List<Node> p, Node n, int len) {
        path= p;
        node= n;
        length= len;
    }
}