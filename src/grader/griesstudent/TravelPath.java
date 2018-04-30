package grader.griesstudent;

import java.util.List;

import models.Node;

/** An instance contains a path of nodes, the time it takes to
 * travel the path, and the speed of the ship on the final node. */
public class TravelPath {
     List<Node> path;  // the path
     double travelTime; // the time it takes to travel the path
     double speed; // speed of ship upon reaching the last node.
     
     /** Constructor: an instance with path p, trave time t. and final
      * speed sp. */
     public TravelPath(List<Node> p, double t, double sp) {
         path= p;
         travelTime= t;
         speed= sp;
     }

}
