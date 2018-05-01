package student;

import controllers.Spaceship;
import models.Edge;
import models.Node;
import models.NodeStatus;

import controllers.SearchPhase;
import controllers.RescuePhase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/** An instance implements the methods needed to complete the mission. */
public class MySpaceship implements Spaceship {

	HashSet<Integer> visited = new HashSet<Integer>(); //set of visited planets
	boolean found = false;

	/** The spaceship is on the location given by parameter state.
	 * Move the spaceship to Planet X and then return (with the spaceship is on
	 * Planet X). This completes the first phase of the mission.
	 * 
	 * If the spaceship continues to move after reaching Planet X, rather than
	 * returning, it will not count. If you return from this procedure while
	 * not on Planet X, it will count as a failure.
	 *
	 * There is no limit to how many steps you can take, but your score is
	 * directly related to how long it takes you to find Planet X.
	 *
	 * At every step, you know only the current planet's ID, the IDs of
	 * neighboring planets, and the strength of the signal from Planet X at
	 * each planet.
	 *
	 * In this rescuePhase,
	 * (1) In order to get information about the current state, use functions
	 * currentID(), neighbors(), and signal().
	 *
	 * (2) Use method onPlanetX() to know if you are on Planet X.
	 *
	 * (3) Use method moveTo(int id) to move to a neighboring planet with the
	 * given ID. Doing this will change state to reflect your new position.
	 */
	@Override
	public void search(SearchPhase state) {
		// TODO: Find the missing spaceship
		signalSearch(state);
	}
	
	/* This is a helper method for search() 
	 * 
	 * Return when state is on Planet X and variable found is true;
	 * 
	 * The spaceship is on the location given by parameter state.
	 * Move the spaceship to Planet X and then return (with the spaceship 
	 * is on Planet X).
	 * 
	 * Visit every planet reachable along paths of unvisited planets from 
	 * here using dfs algorithm. End with the spaceship on PlanetX.
	 * Precondition: this state's planet is unvisited.
	 */
	public void basicSearch(SearchPhase state) {
		int u = state.currentID();
		visited.add(u);
		if (state.onPlanetX()) {
			found = true;
			return;
		}
		for(NodeStatus n: state.neighbors()) {
			if (!visited.contains(n.id()) && !found) {
				state.moveTo(n.id());
				basicSearch(state);
				if (!found) state.moveTo(u);
			}
		}
	}
	
	/* This is a helper method for search() 
	 * 
	 * Return when state is on Planet X and variable found is true;
	 * 
	 * The spaceship is on the location given by parameter state.
	 * Move the spaceship to Planet X and then return (with the spaceship 
	 * is on Planet X).
	 * 
	 * Visit every planet reachable along paths of unvisited planets from 
	 * here using dfs algorithm by always choosing the neighbor with 
	 * the strongest signal. End with the spaceship on PlanetX.
	 * Precondition: this state's planet is unvisited.
	 */
	public void signalSearch(SearchPhase state) {
		int u = state.currentID();
		visited.add(u);
		if (state.onPlanetX()) {
			found = true;
			return;
		}
		
		NodeStatus[] ne = state.neighbors();
		Arrays.sort(ne);
		
		for(int i = ne.length-1; i>=0; i--) {
			if (!visited.contains(ne[i].id()) && !found) {
				state.moveTo(ne[i].id());
				signalSearch(state);
				if (!found) state.moveTo(u);
			}
		}
	}

	

	/** The spaceship is on the location given by state. Get back to Earth
	 * without running out of fuel and return while on Earth. Your ship can
	 * determine how much fuel it has left via method fuelRemaining().
	 * 
	 * In addition, each Planet has some gems. Passing over a Planet will
	 * automatically collect any gems it carries, which will increase your
	 * score; your objective is to return to earth successfully with as many
	 * gems as possible.
	 * 
	 * You now have access to the entire underlying graph, which can be accessed
	 * through parameter state. Functions currentNode() and earth() return Node
	 * objects of interest, and nodes() returns a collection of all nodes on the
	 * graph.
	 *
	 * Note: Use moveTo() to move to a destination node adjacent to your current
	 * node. */
	@Override
	public void rescue(RescuePhase state) {
		// TODO: Complete the rescue mission and collect gems
		rescue3(state);
	}
	
	/** The spaceship is on the location given by state. Get back to Earth
	 * without running out of fuel and return while on Earth. 
	 * 
	 * Uses Dijkstra's shortest path algorithm to determine the shortest path
	 * back to earth. Follows that path.
	 */
	public void rescue1(RescuePhase state) {
		List<Node> path = Paths.minPath(state.currentNode(), state.earth());
		
		for(Node n: path) {
			if (n != state.currentNode()) state.moveTo(n);
		}
	}

	/** The spaceship is on the location given by state. Get back to Earth
	 * without running out of fuel and return while on Earth. 
	 * 
	 * Uses Dijkstra's shortest path algorithm to determine the shortest path
	 * back to earth for each neighbor of the current planet, follow the path
	 * of the neighbor that has the highest gems unless the remaining fuel is
	 * less than the fuel needed to follow the path.
	 * 
	 */
	public void rescue2(RescuePhase state) {
		
		if (state.currentNode() == state.earth()) return;
		
		Heap<Node> maxGemHeap = new Heap<Node>(false);
		
		for(Edge e: state.currentNode().getExits()) {
			Node n = e.getOther(state.currentNode());
			List<Node> path = Paths.minPath(n, state.earth());
			//fuel back to earth
			int estFuel= 0;
			int estGems = 0;
			for(int i = 0; i < path.size()-1; i++) {
				estFuel = estFuel + (path.get(i).getEdge(path.get(i+1))).length;
				estGems = estGems + (path.get(i).gems());
			}
			if (estFuel + e.length <= state.fuelRemaining()) {
				maxGemHeap.add(n, n.gems());
			}
		}

		Node best = maxGemHeap.poll();
		state.moveTo(best);
		rescue2(state);
	}
	
	
	// do this with something not Dijkstra
	public void rescue3(RescuePhase state) {
		
		HashMap<Node, Integer> toEarth = new HashMap<Node, Integer>();
		HashMap<Node, Integer> fromX = new HashMap<Node, Integer>();
		
		for(Node n: state.nodes()) {
			List<Node> path = Paths.minPath(n, state.earth());
			int estGems = 0;
			for(Node p:path) {
				estGems += p.gems();
			}
			toEarth.put(n, estGems);
		}
		
		for(Node n: state.nodes()) {
			List<Node> path = Paths.minPath(state.currentNode(),n);
			int estGems = 0;
			for(Node p:path) {
				estGems += p.gems();
			}
			fromX.put(n, estGems);
		}
		
		Heap<Node> connect = new Heap<Node>(false);
		
		for(Node n:state.nodes()) {
			int totalGems = toEarth.get(n) + fromX.get(n);
			connect.add(n, totalGems);
		}
		
		Node best = connect.poll();
		List<Node> pathFromX = Paths.minPath(state.currentNode(),best);
		List<Node> pathToEarth = Paths.minPath(best, state.earth());
		List<Node> path = pathFromX.subList(1, pathFromX.size()-1);
		path.addAll(pathToEarth);
		
		int estFuel= 0;
		for(int i = 0; i < path.size()-1; i++) {
			estFuel = estFuel + (path.get(i).getEdge(path.get(i+1))).length;
		}
		
		while(estFuel > state.fuelRemaining()) {
			best = connect.poll();
			pathFromX = Paths.minPath(state.currentNode(),best);
			pathToEarth = Paths.minPath(best, state.earth());
			path = pathFromX.subList(1, pathFromX.size()-1);
			path.addAll(pathToEarth);
			estFuel= 0;
			for(int i = 0; i < path.size()-1; i++) {
				estFuel = estFuel + (path.get(i).getEdge(path.get(i+1))).length;
			}
		}
		
		for(Node n:path) {
			state.moveTo(n);
		}
	}
	
	//change above to dfs + other
	
	//fishbone 
	
	//visit planets with most gems every time. 
	
}
