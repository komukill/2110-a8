package grader.griesstudent;

import controllers.Spaceship;
import models.Edge;
import models.Node;
import models.NodeStatus;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import controllers.SearchPhase;
import controllers.RescuePhase;

/** An instance implements the methods needed to complete the mission. */
public class MySpaceship implements Spaceship { 

    // return state fields. Initialized when returnToEarth called
    RescuePhase returnState; // Parameter of system's call to returnToEarth
    Node Earth;   // Node earth

    /** The spaceship is on the location given by parameter state.
     * Move the spaceship to Planet X and then return, while the spaceship is on
     * Planet X. This completes the first phase of the mission.
     * 
     * If the spaceship continues to move after reaching planet X, rather than
     * returning, it will not count. Returning from this procedure while
     * not on Planet X counts as a failure.
     * <p>
     * There is no limit to how many steps you can take, but your score is
     * directly related to how long it takes you to find Planet X.
     * <p>
     * At every step, you know only the current planet's ID, the IDs of
     * neighboring planets, and the strength of the ping from Planet X at
     * each planet.
     * <p>
     * In this rescueStage,<br>
     * (1) In order to get information about the current state, use functions
     * getLocation(), neighbors(), getPing(), and foundSpaceship().
     * <p>
     * (2) You know you are on Planet X when foundSpaceship() is true.
     * <p>
     * (3) Use function moveTo(long id) to move to a neighboring planet
     * by its ID. Doing this will change state to reflect your new position.
     *
     * @param state
     * an interface to view and manipulate the current state */
    @Override
    public void search(SearchPhase state) {
        
        // TODO : Find the missing spaceship
        dfsOptimized(state, new HashSet<>());
        //dfs(state, new HashSet<>());
        return;
    }

    /** The ship is on a planet p (say) given by field state. visited is the
     * set of unique ids of nodes that have been visited.
     * Fly to all planets reachable along unvisited path from p.
     * If Planet X is reached, return while on that planet.
     * If the flying does not find planet X, return with the ship on
     * planet p.
     * 
     * Precondition: p has no been visited.
     */
    public void dfs(SearchPhase state, HashSet<Integer> visited) {
        if (state.onPlanetX()) return;

        Integer p= state.currentID();
        visited.add(p);
        NodeStatus[] nebs= state.neighbors();
        for (NodeStatus ns : nebs) {
            int neighborId= ns.id();
            if (!visited.contains(neighborId)) {
                state.moveTo(neighborId);
                dfs(state, visited);
                if (state.onPlanetX()) return;
                state.moveTo(p);
            }
        }
    }

    /** The ship is on the planet p (say) given by field state. visited is the
     * set of unique ids of nodes that have been visited.
     * Fly to all planets reachable along unvisited path from p.
     * If Planet X is reached, return while on that planet.
     * If the flying does not find planet X, return with the ship on
     * planet p.
     * 
     * Precondition: p has not been visited.
     */
    public void dfsOptimized(SearchPhase state, HashSet<Integer> visited) {
        /** This version is optimized in that it processes the neighbors of
         * a node in decreasing order of ping value ---the higher the ping value
         * the close a neighbor is to Planet X.
         */
        if (state.onPlanetX()) return;

        Integer p= state.currentID();
        visited.add(p);
        NodeStatus[] nebArray= state.neighbors();
        Arrays.sort(nebArray);
        for (int k= nebArray.length-1; 0 <= k; k= k-1) {
            NodeStatus ns= nebArray[k];
            int neighborId= ns.id();
            if (!visited.contains(neighborId)) {
                state.moveTo(neighborId);
                dfsOptimized(state, visited);
                if (state.onPlanetX()) return;
                state.moveTo(p);
            }
        }
    }


    /** Return to Earth while collecting as many gems as possible.
     * The rescued spaceship has information on the entire galaxy, so you
     * now have access to the entire underlying graph. This can be accessed
     * through ReturnStage. getCurrentNode() and getEarth() will return Node
     * objects of interest, and getNodes() gives you a Set of all nodes
     * in the graph. 
     *
     * You must return from this function while on Earth. Returning from the
     * wrong location will be considered a failed run.
     *
     * You must make it back to Earth before running out of fuel.
     * state.getDistanceLeft() will tell you how far you can travel with your  
     * remaining fuel stores.
     * 
     * You can increase your score by collecting more gems on your way back to 
     * Earth. You should look for ways to optimize your return. The information 
     * from the rescued ship includes information on where gems are located. 
     * getNumGems() will give you the number of gems on a node. You will 
     * automatically collect any remaining gems when you move to a planet during 
     * the rescue stage.  */
    @Override
    public void rescue(RescuePhase state) {
        returnState= state;
        Earth= returnState.earth();
        // removed for autograder
//        Set<Node> nodes= state.getNodes();
//        System.out.println("Number of nodes: " + nodes.size());
        getManyGems();
    }

    /** Get back to Earth on the shortest path using field returnStage. */
    public void goQuickly() {
        Node start= returnState.currentNode();
        HashMap<Node, SFinfo> map= MinPathGries.minPath(start, Earth, Integer.MAX_VALUE);
        List<Node> path= MinPathGries.buildPath(Earth, map);
        int extraDistance= returnState.fuelRemaining() - map.get(Earth).dist;
        traverse(path, extraDistance);
    }

    /** Get back to Earth, trying to get as many gems as possible,
     * Using field returnState. */
    public void getManyGems() {
        int maxInSettledset= 40;
        //Iteratively find the best node to travel to without running out
        // of fuel on the way back to Earth and traverse the best path to
        // that node.
        PathInfo pl= getBestHeap(maxInSettledset);
        //PathInfo pl= getBest(maxInSettledset);
        while (pl != null) {
            HashMap<Node, SFinfo> map= MinPathGries.minPath(pl.node, Earth, Integer.MAX_VALUE);
            
            int distanceLimit= returnState.fuelRemaining();
            int distBackToEarth= map.get(Earth).dist; 
            //traverse(pl.path, distanceLimit - distBackToEarth - pl.length);
            traverse(pl.path, 0);
            pl= getBest(maxInSettledset);
        }
        
        goQuickly(); // traverse shortest path to Earth
    }
    
    /** The spaceship is at the current planet. Return the path and length to
     * the best path in terms of gems/distance for which a return to
     * to Earth is possible without running out of fuel --or null if
     * there is no such path. In doing this, find shortest path to
     * at most maxNodes nodes. Uses field returnState. */
    public PathInfo getBestHeap(int maxNodes) {
        Node nn= returnState.currentNode();
        int distanceLimit= returnState.fuelRemaining();
        HashMap<Node, SFinfo> map= MinPathGries.minPath(nn, null, maxNodes);
        
        // Make a heap of the nodes in map.
        // Priority is -(number of gems on node)/(distance to to node) 
       Heap<Node> heap= new Heap<>();
       for (Node node : map.keySet()) {
           double dist= map.get(node).dist;
           heap.insert(node, dist == 0 ? 0 : -node.gems()/dist);
       }
        
//        // Make an array of NodeInfos of the nodes in map.
//        NodeInfo[] nodeInfos= new NodeInfo[map.size()];
//        int k= 0;
//        for (Node node : map.keySet()) {
//            nodeInfos[k]= new NodeInfo(node, map.get(node));
//            k= k+1;
//        }

        // Sort nodeInfos by gems/distance along shortest path to nodes,
        // highest first.
//        Arrays.sort(nodeInfos,  (b, c) ->
//        b.info.dist == 0 ? 1 :
//            c.info.dist == 0 ? -1 :
//                b.info.gems/b.info.dist  < c.info.gems/c.info.dist ? 1  :
//                    b.info.gems/b.info.dist  > c.info.gems/c.info.dist ? -1  : 0);

        // invariant: nodes that have been processed have no gems on paths
        // to them or can't be traveled to (and then to Earth) without running out of fuel.
        while (heap.size() > 0) {
            Node node= heap.poll();
            if (node.gems() <= 0) continue;
            int dist= map.get(node).dist;
            if (dist >= distanceLimit) continue;
            HashMap<Node, SFinfo> toEarth= MinPathGries.minPath(node, Earth, Integer.MAX_VALUE);
            if (dist + toEarth.get(Earth).dist >= distanceLimit) continue;
            
            return new PathInfo(MinPathGries.buildPath(node, map), node, dist);
        }
      return null;
    }

    /** The spaceship is at the current planet. Return the path and length to
     * the best path in terms of gems/distance for which a return to
     * to Earth is possible without running out of fuel --or null if
     * there is no such path. In doing this, find shortest path to
     * at most maxNodes nodes. Uses field returnState. */
    public PathInfo getBest(int maxNodes) {
        Node nn= returnState.currentNode();
        int distanceLimit= returnState.fuelRemaining();
        HashMap<Node, SFinfo> map= MinPathGries.minPath(nn, null, maxNodes);

        // Make an array of NodeInfos of the nodes in map.
        NodeInfo[] nodeInfos= new NodeInfo[map.size()];
        int k= 0;
        for (Node node : map.keySet()) {
            nodeInfos[k]= new NodeInfo(node, map.get(node));
            k= k+1;
        }

        // Sort nodeInfos by gems/distance along shortest path to nodes,
        // highest first.
        Arrays.sort(nodeInfos,  (b, c) ->
        b.info.dist == 0 ? 1 :
            c.info.dist == 0 ? -1 :
                b.info.gems/b.info.dist  < c.info.gems/c.info.dist ? 1  :
                    b.info.gems/b.info.dist  > c.info.gems/c.info.dist ? -1  : 0);

        // invariant: nodes that have been processed have no gems on paths
        // to them or can't be traveled to (and then to Earth) without running out of fuel.
        for (NodeInfo nodeInfo : nodeInfos) {
            if (nodeInfo.info.gems <= 0) continue;
            if (nodeInfo.info.dist >= distanceLimit) continue;
            HashMap<Node, SFinfo> toEarth= MinPathGries.minPath(nodeInfo.node, Earth, Integer.MAX_VALUE);
            if (nodeInfo.info.dist + toEarth.get(Earth).dist >= distanceLimit) continue;
            
            return new PathInfo(MinPathGries.buildPath(nodeInfo.node, map), nodeInfo.node, nodeInfo.info.dist);
        }
      return null;
    }

    /** Get back to Earth, trying to get as many gems as possible,
     * using field returnState. */
    public void getManyGemsPerDistance() {
        /* First sort the planets in reverse order of number of gems per distance.
         * Then, iteratively, find the planet with the most gems per distance that
         * can be visited and still get back to back to Earth in time, and
         * fly to that planet.  */
        Node[] nodes= orderNodes();

        // invariant: nodes that have been processed have no gems or can't
        // be traveled to (and then to Earth) without running out of fuel.
        for (Node node : nodes) {
            if (node.gems() > 0) {
                Node nn= returnState.currentNode();
                int distanceLimit= returnState.fuelRemaining();

                HashMap<Node, SFinfo> map= MinPathGries.minPath(nn, null, 50);
            }
        }
        goQuickly();
    }

    /** Construct the shortest travel-time path from node first to
     * node last, followed by the shortest path from node to Earth.
     * If that path has distance less than maxDistance,
     * return the path to last together with the sum of the
     * two path lengths.  Uses field returnState.
     */
    public List<Node> path(Node last, HashMap<Node, SFinfo> map, int maxDistance) {
        int p1= map.get(last).dist;

        HashMap<Node, SFinfo> map2= MinPathGries.minPath(last, Earth, Integer.MAX_VALUE);
        int p2= map2.get(Earth).dist;


        if (p1 + p2 >= maxDistance) return null;
        return MinPathGries.buildPath(last, map);
    }

    /** Precondition. the ship is on the first Node of path, which
     * is given by state.
     * Move the ship along the path, ending on the last node of path.
     * Neighboring nodes of the path may be visited to pick up their gems,
     * as long as the detours do not exceed extraDist. Don't detour from
     * the last node of the path.
     * Return extraDist - (distance traveled on detours).
     * Using field returnState */
    public int traverse(List<Node> path, int extraDist) {
        
        Node[] nodeArray= new Node[0];
        nodeArray= path.toArray(nodeArray);

        //invariant: ship is standing on nodeArray[k]
        int k= 0;
        for (k= 0; k < nodeArray.length-1; k= k+1) {
            extraDist= detour(nodeArray[k+1], extraDist);
            returnState.moveTo(nodeArray[k+1]);
        }
        return extraDist;
    }

    /** Detour to neighbors of the current node that
     *    (1) are not next, are not Earth,
     *    (2) have gems, and
     *    (3) can be visited (and back) in distance less than maxDistance.
     * Return maxDistance - (distance traveled by detours)
     * 
     * Note: Experience shows that using detour does NOT help.
     * We recommend that maxDistance be 0 except for the final
     * move to Earth. */
    public int detour(Node next, int maxDistance) {
        Node n= returnState.currentNode();
        Set<Edge> edges=  n.exits();
        for (Edge edge : edges) {
            Node adj= edge.getOther(n);
            if (adj != next  &&  adj != Earth  &&  adj.gems() > 0  &&
                    2*edge.length < maxDistance) {
                returnState.moveTo(adj);
                returnState.moveTo(n);
                maxDistance= maxDistance - 2*edge.length;
            }
        }

        return maxDistance;
    }

    /** Return the nodes given by state, in decreasing order of the
     * number of gems. Use field returnState*/
    public Node[] orderNodes() {
        Iterable<Node> nodeIt=returnState.nodes();
        // Store in n the number of nodes
        int n= 0;
        for (Node node : nodeIt) {
            n= n+1;
        }

        Node[] nodes= new Node[n];

        // Put the nodes into nodes
        int k= 0;
        for (Node node : nodeIt) {
            nodes[k]= node;
            k= k+1;
        }

        Arrays.sort(nodes, (a, b) -> b.gems() - a.gems());
        return nodes;
    }

    /** Return the number of gems on each node of nodes, separated by ", "
     * and delimited by "[" and "]". */
    public String gemList(Node[] nodes) {
        String res= "[";
        for (Node n : nodes) {
            if (res.length() > 1) res= res + ", ";
            res= res + n.gems();
        }
        return res + "]";
    }

    


}
