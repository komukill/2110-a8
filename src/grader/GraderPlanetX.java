package grader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import controllers.PlanetX;
import controllers.Spaceship;
import models.Board;
import models.Controllable.SolutionFailedException;
import models.PlanetXModel;

public class GraderPlanetX extends PlanetX {

	protected static ExecutorService executor = // used to time out solutions
		Executors.newCachedThreadPool(r -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		});
	protected Future<?> future; // future from the executor

	protected boolean timedOut; // true iff a solution times out

	public static final long TIMEOUT_LENGTH = 15;
	public static final TimeUnit TIMEOUT_UNITS = TimeUnit.SECONDS;

	/** Constructor: a PlanetX game with the given seed and solution. */
	public GraderPlanetX(long s, Spaceship sp) {
		super(s, sp, new GraderView());
	}

	@Override
	protected void init(long s, Spaceship sp) {
		seed = s;
		spaceship = sp;
		Board b = new Board.BoardBuilder().size(WIDTH, HEIGHT).seed(s)
			.nodeBounds(MIN_NODES, MAX_NODES).gemBounds(MIN_GEMS, MAX_GEMS).build();
		model = new PlanetXModel(b);
	}

	@Override
	public void start() {
		started = true;
		future = executor.submit(thread);
	}

	/**
	 * Return the number of nodes in this particular game.
	 */
	public int getNumNodes() {
		return model.nodes().size();
	}

	/**
	 * Run this solution's rescue, only, and return the resulting score.
	 * Running this multiple times will not reset the score. The solution is
	 * allowed to run for TIMEOUT_LENGTH seconds before forcibly timing out.
	 */
	public int gradeRescue() {
		model.setShipLocation(model.earth());
		return timeoutHelper(() -> search(), TIMEOUT_LENGTH);
	}

	/**
	 * Run this solution's rescue, only, and return the resulting score.
	 * Running this multiple times will not reset the score. The solution is
	 * allowed to run for timeoutSeconds before forcibly timing out.
	 */
	public int gradeRescue(long timeoutSeconds) {
		model.setShipLocation(model.earth());
		return timeoutHelper(() -> search(), timeoutSeconds);
	}

	/**
	 * Run this solution's return, only, and return the resulting score.
	 * Running this multiple times will not reset the score. The solution is
	 * allowed to run for TIMEOUT_LENGTH seconds before timing out.
	 */
	public int gradeReturn() {
		model.setShipLocation(model.planetX());
		return timeoutHelper(() -> rescue(), TIMEOUT_LENGTH);
	}

	/**
	 * Run this solution's return, only, and return the resulting score.
	 * Running this multiple times will not reset the score. The solution is
	 * allowed to run for timeoutSeconds before forcibly timing out.
	 */
	public int gradeReturn(long timeoutSeconds) {
		model.setShipLocation(model.planetX());
		return timeoutHelper(() -> rescue(), timeoutSeconds);
	}

	/**
	 * Return true iff this solution timed out.
	 */
	public boolean isTimedOut() {
		return timedOut;
	}

	@FunctionalInterface
	private static interface PhaseProcedure {
		public void execute() throws SolutionFailedException;
	}

	/**
	 * Run the given function fnc and then end the game in a new ModelThread
	 * using a GraderView, returning the score. This will time out after 15
	 * seconds and set timedOut to be true.
	 */
	private int timeoutHelper(PhaseProcedure p, long timeoutLength) {
		thread = new ModelThread() {
			@Override
			public void run() {
				try {
					p.execute();
					view.endGame(model.score());
				} catch (SolutionFailedException e) {
					view.endGame(0);
				}
			}
		};
		view.init(this, model);
		try {
			future.get(timeoutLength, TIMEOUT_UNITS);
		} catch (TimeoutException e) {
			timedOut = true;
			view.endGame(0);
			thread.kill();
		} catch (Exception e) {}
		return model.score();
	}
}