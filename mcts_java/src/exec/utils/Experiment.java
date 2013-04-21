package exec.utils;

import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.game.*;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

class ControllerThread<M> extends Thread {
    private Controller<M> controller;
    private M move = null;
    private Game game;
    private long timeDue;

    public ControllerThread(Controller<M> controller, Game game, long timeDue) {
        this.controller = controller;
        this.game = game.copy();
        this.timeDue = timeDue;
    }

    public void run() {
        move = controller.getMove(game, timeDue);
    }

    public M getMove() {
        return move;
    }
}

public class Experiment {
    private Game game = new SimplifiedGame(System.currentTimeMillis());
    private Controller<MOVE> pacmanController;
    private Controller<EnumMap<GHOST,MOVE>> ghostController;
    private boolean visual = false;
    private String visualTitle = "Experiment";
    private boolean disposeView = true;
    private int pacmanDelay = 40;
    private int ghostDelay = 40;
    private boolean multithreaded = false;

    public Experiment() {
    }

    public Game execute() {
        assert(pacmanController!=null);
        assert(ghostController!=null);

        GameView gv = isVisual()? new GameView(getGame()).showGame(getVisualTitle()): null;


        while (!game.gameOver()) {
            MOVE pacmanMove;
            EnumMap<GHOST,MOVE> ghostMove;
            if (multithreaded) {
                ControllerThread<MOVE> pacmanThread = new ControllerThread<MOVE>(pacmanController, game, System.currentTimeMillis()+getPacmanDelay());
                ControllerThread<EnumMap<GHOST,MOVE>> ghostThread = new ControllerThread<EnumMap<GHOST,MOVE>>(ghostController, game, System.currentTimeMillis()+getGhostDelay());

                pacmanThread.start();
                ghostThread.start();
                try {
                    pacmanThread.join();
                    ghostThread.join();
                } catch (InterruptedException ex) {
                    pacmanThread.interrupt();
                    ghostThread.interrupt();
                }

                pacmanMove = pacmanThread.getMove();
                ghostMove = ghostThread.getMove();
            } else {
                pacmanMove = getPacmanController().getMove(getGame(), System.currentTimeMillis()+getPacmanDelay());
                ghostMove = getGhostController().getMove(getGame(), System.currentTimeMillis()+getGhostDelay());
            }
            getGame().advanceGame(pacmanMove, ghostMove);
            if (isVisual()) {
                gv.repaint();
            }
        }

        if (isVisual()&&isDisposeView()) {
            gv.getFrame().dispose();
        }

        return getGame();
    }


    /**
     * @return the game
     */
    public Game getGame() {
        return game;
    }

    /**
     * @param game the game to set
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * @return the pacmanController
     */
    public Controller<MOVE> getPacmanController() {
        return pacmanController;
    }

    /**
     * @param pacmanController the pacmanController to set
     */
    public void setPacmanController(Controller<MOVE> pacmanController) {
        this.pacmanController = pacmanController;
    }

    /**
     * @return the ghostController
     */
    public Controller<EnumMap<GHOST,MOVE>> getGhostController() {
        return ghostController;
    }

    /**
     * @param ghostController the ghostController to set
     */
    public void setGhostController(Controller<EnumMap<GHOST,MOVE>> ghostController) {
        this.ghostController = ghostController;
    }

    /**
     * @return the visual
     */
    public boolean isVisual() {
        return visual;
    }

    /**
     * @param visual the visual to set
     */
    public void setVisual(boolean visual) {
        this.visual = visual;
    }

    public boolean isMultithreaded() { return multithreaded; }
    public void setMultithreaded(boolean multithreaded) { this.multithreaded = multithreaded; }

    /**
     * @return the visualTitle
     */
    public String getVisualTitle() {
        return visualTitle;
    }

    /**
     * @param visualTitle the visualTitle to set
     */
    public void setVisualTitle(String visualTitle) {
        this.visualTitle = visualTitle;
    }

    /**
     * @return the disposeView
     */
    public boolean isDisposeView() {
        return disposeView;
    }

    /**
     * @param disposeView the disposeView to set
     */
    public void setDisposeView(boolean disposeView) {
        this.disposeView = disposeView;
    }

    /**
     * @return the pacmanDelay
     */
    public int getPacmanDelay() {
        return pacmanDelay;
    }

    /**
     * @param pacmanDelay the pacmanDelay to set
     */
    public void setPacmanDelay(int pacmanDelay) {
        this.pacmanDelay = pacmanDelay;
    }

    /**
     * @return the ghostDelay
     */
    public int getGhostDelay() {
        return ghostDelay;
    }

    /**
     * @param ghostDelay the ghostDelay to set
     */
    public void setGhostDelay(int ghostDelay) {
        this.ghostDelay = ghostDelay;
    }
}