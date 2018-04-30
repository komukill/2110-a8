package grader;

import controllers.Controller;
import models.Model;
import models.Model.Phase;
import views.View;

/**
 * An instance is a view that immediately starts the game and runs updates on
 * its own thread when initialized.
 */
public class GraderView implements View {

	private Controller ctrlr;
	private boolean running;
	private Thread thread = new Thread() {
		@Override
		public void run() {
			running = true;
			while (running) {
				ctrlr.update();
			}
		}
	};

	@Override
	public void init(Controller c, Model m) {
		ctrlr = c;
		c.start();
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void endGame(int score) {
		running = false;
	}

	@Override
	public void beginStage(Phase s) {}

	@Override
	public void endStage(Phase s) {}

	@Override
	public void outprint(String s) {}

	@Override
	public void errprint(String s) {}
}
