package grader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import controllers.SearchPhase;
import controllers.RescuePhase;
import controllers.Spaceship;
import models.Node;
import models.NodeStatus;

/** An instance is a bare-minimum solution of the Planet X game. */
public class BasicSpaceship implements Spaceship {

	@Override
	public void search(SearchPhase state) {
		dfsWalk(new HashSet<Integer>(), state);
	}

	/**
	 * Iff Planet X is reachable from the current Node without passing through
	 * visited Nodes, move to Planet X and return true, marking all passed Nodes
	 * as visited. Otherwise, return false and don't move, marking all Nodes in
	 * the path as visited.
	 */
	private boolean dfsWalk(Set<Integer> visited, SearchPhase state) {
		if (state.onPlanetX())
			return true;
		int current = state.currentID();
		visited.add(current);
		for (NodeStatus n : state.neighbors()) {
			int next = n.id();
			if (!visited.contains(next)) {
				state.moveTo(next);
				if (dfsWalk(visited, state))
					return true;
				state.moveTo(current);
			}
		}

		return false;
	}

	@Override
	public void rescue(RescuePhase state) {
		List<Node> l = MinPath.minPath(state.currentNode(), state.earth());
		l.remove(0);
		for (Node n : l)
			state.moveTo(n);
	}
}
