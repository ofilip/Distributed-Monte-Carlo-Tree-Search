/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.game;

import java.util.EnumMap;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Maze;


public interface Game {

    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////////  Game-engine   //////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /**
     * Central method that advances the game state using the moves supplied by
     * the controllers. It first updates Ms Pac-Man, then the ghosts and then
     * the general game logic.
     *
     * @param pacManMove The move supplied by the Ms Pac-Man controller
     * @param ghostMoves The moves supplied by the ghosts controller
     */
    void advanceGame(MOVE pacManMove, EnumMap<GHOST, MOVE> ghostMoves);

    void advanceGameWithForcedReverse(MOVE pacManMove, EnumMap<GHOST, MOVE> ghostMoves);

    void advanceGameWithPowerPillReverseOnly(MOVE pacManMove, EnumMap<GHOST, MOVE> ghostMoves);

    void advanceGameWithoutReverse(MOVE pacManMove, EnumMap<GHOST, MOVE> ghostMoves);

    /**
     * Returns an exact copy of the game. This may be used for forward searches
     * such as minimax. The copying is relatively efficient.
     *
     * @return the game
     */
    Game copy();

    /**
     * If in lair (getLairTime(-)>0) or if not at junction.
     *
     * @param ghostType the ghost type
     * @return true, if successful
     */
    boolean doesGhostRequireAction(GHOST ghostType);

    /**
     * Checks whether the game is over or not: all lives are lost or 16 levels have been
     * played. The variable is set by the methods _feast() and _checkLevelState().
     *
     * @return true, if successful
     */
    boolean gameOver();

    /**
     * Gets the A* path considering previous moves made (i.e., opposing actions are ignored)
     *
     * @param fromNodeIndex The node index from which to move (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @return The A* path
     *
     * @deprecated use getShortestPath() instead.
     */
    @Deprecated
    int[] getAStarPath(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade);

    /**
     * returns the indices of all active pills in the mazes[gs.curMaze]
     *
     * @return the active pills indices
     */
    int[] getActivePillsIndices();

    /**
     * returns the indices of all active power pills in the mazes[gs.curMaze]
     *
     * @return the active power pills indices
     */
    int[] getActivePowerPillsIndices();

    /**
     * Gets the approximate next move away from a target not considering directions opposing the last move made.
     *
     * @param fromNodeIndex The node index from which to move (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @param distanceMeasure The distance measure required (Manhattan, Euclidean or Straight line)
     * @return The approximate next move towards target (chosen greedily)
     */
    MOVE getApproximateNextMoveAwayFromTarget(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade, DM distanceMeasure);

    /**
     * Gets the approximate next move towards target not considering directions opposing the last move made.
     *
     * @param fromNodeIndex The node index from which to move (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @param distanceMeasure The distance measure required (Manhattan, Euclidean or Straight line)
     * @return The approximate next move towards target (chosen greedily)
     */
    MOVE getApproximateNextMoveTowardsTarget(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade, DM distanceMeasure);

    /**
     * Gets the approximate shortest path taking into account the last move made (i.e., no reversals).
     * This is approximate only as the path is computed greedily. A more accurate path can be obtained
     * using A* which is slightly more costly.
     *
     * @param fromNodeIndex The node index from where to start (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @return the shortest path from start to target
     *
     * @deprecated use getShortestPath() instead.
     */
    @Deprecated
    int[] getApproximateShortestPath(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade);

    /**
     * Similar to getApproximateShortestPath but returns the distance of the path only. It is slightly
     * more efficient.
     *
     * @param fromNodeIndex The node index from where to start (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @return the exact distance of the path
     *
     * @deprecated use getShortestPathDistance() instead.
     */
    @Deprecated
    int getApproximateShortestPathDistance(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade);

    /**
     * Gets the closest node index from node index.
     *
     * @param fromNodeIndex the from node index
     * @param targetNodeIndices the target node indices
     * @param distanceMeasure the distance measure
     * @return the closest node index from node index
     */
    int getClosestNodeIndexFromNodeIndex(int fromNodeIndex, int[] targetNodeIndices, DM distanceMeasure);

    /**
     * Returns the current level.
     *
     * @return The current level
     */
    int getCurrentLevel();

    /**
     * Returns the time of the current level (important with respect to LEVEL_LIMIT).
     *
     * @return the current level time
     */
    int getCurrentLevelTime();

    /**
     * Returns the current maze of the game.
     *
     * @return The current maze.
     */
    Maze getCurrentMaze();

    /**
     * Gets the distance.
     *
     * @param fromNodeIndex the from node index
     * @param toNodeIndex the to node index
     * @param distanceMeasure the distance measure
     * @return the distance
     */
    double getDistance(int fromNodeIndex, int toNodeIndex, DM distanceMeasure);

    /**
     * Returns the distance between two nodes taking reversals into account.
     *
     * @param fromNodeIndex the index of the originating node
     * @param toNodeIndex the index of the target node
     * @param lastMoveMade the last move made
     * @param distanceMeasure the distance measure to be used
     * @return the distance between two nodes.
     */
    double getDistance(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade, DM distanceMeasure);

    /**
     * Returns the EUCLEDIAN distance between two nodes in the current mazes[gs.curMaze].
     *
     * @param fromNodeIndex the from node index
     * @param toNodeIndex the to node index
     * @return the euclidean distance
     */
    double getEuclideanDistance(int fromNodeIndex, int toNodeIndex);

    /**
     * Gets the farthest node index from node index.
     *
     * @param fromNodeIndex the from node index
     * @param targetNodeIndices the target node indices
     * @param distanceMeasure the distance measure
     * @return the farthest node index from node index
     */
    int getFarthestNodeIndexFromNodeIndex(int fromNodeIndex, int[] targetNodeIndices, DM distanceMeasure);

    /**
     * Gets the game state as a string: all variables are written to a string in a pre-determined
     * order. The string may later be used to recreate a game state using the setGameState() method.
     *
     * Variables not included: enableGlobalReversals
     *
     * @return The game state as a string
     */
    String getGameState();

    /**
     * Returns the current value awarded for eating a ghost.
     *
     * @return the current value awarded for eating a ghost.
     */
    int getGhostCurrentEdibleScore();

    /**
     * Current node at which the specified ghost resides.
     *
     * @param ghostType the ghost type
     * @return the ghost current node index
     */
    int getGhostCurrentNodeIndex(GHOST ghostType);

    /**
     * Returns the edible time for the specified ghost.
     *
     * @param ghostType the ghost type
     * @return the ghost edible time
     */
    int getGhostEdibleTime(GHOST ghostType);

    /**
     * Returns the node index where ghosts start in the maze once leaving
     * the lair.
     *
     * @return the node index where ghosts start after leaving the lair.
     */
    int getGhostInitialNodeIndex();

    /**
     * Time left that the specified ghost will spend in the lair.
     *
     * @param ghostType the ghost type
     * @return the ghost lair time
     */
    int getGhostLairTime(GHOST ghostType);

    /**
     * Current direction of the specified ghost.
     *
     * @param ghostType the ghost type
     * @return the ghost last move made
     */
    MOVE getGhostLastMoveMade(GHOST ghostType);

    /**
     * Returns the array of node indices that are junctions (3 or more neighbours).
     *
     * @return the junction indices
     */
    int[] getJunctionIndices();

    /**
     * Returns the MANHATTAN distance between two nodes in the current mazes[gs.curMaze].
     *
     * @param fromNodeIndex the from node index
     * @param toNodeIndex the to node index
     * @return the manhattan distance
     */
    int getManhattanDistance(int fromNodeIndex, int toNodeIndex);

    /**
     * Gets the index of the current maze.
     *
     * @return The maze index
     */
    int getMazeIndex();

    /**
     * Method that returns the direction to take given a node index and an index of a neighbouring
     * node. Returns null if the neighbour is invalid.
     *
     * @param currentNodeIndex The current node index.
     * @param neighbourNodeIndex The direct neighbour (node index) of the current node.
     * @return the move to make to reach direct neighbour
     */
    MOVE getMoveToMakeToReachDirectNeighbour(int currentNodeIndex, int neighbourNodeIndex);

    /**
     * Given a node index and a move to be made, it returns the node index the move takes one to.
     * If there is no neighbour in that direction, the method returns -1.
     *
     * @param nodeIndex The current node index
     * @param moveToBeMade The move to be made
     * @return The node index of the node the move takes one to
     */
    int getNeighbour(int nodeIndex, MOVE moveToBeMade);

    /**
     * Gets the neighbouring nodes from the current node index.
     *
     * @param nodeIndex The current node index
     * @return The set of neighbouring nodes
     */
    int[] getNeighbouringNodes(int nodeIndex);

    /**
     * Gets the neighbouring nodes from the current node index excluding the node
     * that corresponds to the opposite of the last move made which is given as an argument.
     *
     * @param nodeIndex The current node index
     * @param lastModeMade The last mode made
     * @return The set of neighbouring nodes except the one that is opposite of the last move made
     */
    int[] getNeighbouringNodes(int nodeIndex, MOVE lastModeMade);

    /**
     * Gets the next move away from target.
     *
     * @param fromNodeIndex the from node index
     * @param toNodeIndex the to node index
     * @param distanceMeasure the distance measure
     * @return the next move away from target
     */
    MOVE getNextMoveAwayFromTarget(int fromNodeIndex, int toNodeIndex, DM distanceMeasure);

    /**
     * Gets the exact next move away from target taking into account reversals. This uses the pre-computed paths.
     *
     * @param fromNodeIndex The node index from which to move (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @param distanceMeasure the distance measure to be used
     * @return the next move away from target
     */
    MOVE getNextMoveAwayFromTarget(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade, DM distanceMeasure);

    /**
     * Gets the next move towards target.
     *
     * @param fromNodeIndex the from node index
     * @param toNodeIndex the to node index
     * @param distanceMeasure the distance measure
     * @return the next move towards target
     */
    MOVE getNextMoveTowardsTarget(int fromNodeIndex, int toNodeIndex, DM distanceMeasure);

    /**
     * Gets the exact next move towards target taking into account reversals. This uses the pre-computed paths.
     *
     * @param fromNodeIndex The node index from which to move (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @param distanceMeasure the distance measure to be used
     * @return the next move towards target
     */
    MOVE getNextMoveTowardsTarget(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade, DM distanceMeasure);

    /**
     * Returns the x coordinate of the specified node.
     *
     * @param nodeIndex the node index
     * @return the node x cood
     */
    int getNodeXCood(int nodeIndex);

    /**
     * Returns the y coordinate of the specified node.
     *
     * @param nodeIndex The node index
     * @return The node's y coordinate
     */
    int getNodeYCood(int nodeIndex);

    int getNumGhostsEaten();

    /**
     * Total number of pills in the mazes[gs.curMaze]
     *
     * @return the number of active pills
     */
    int getNumberOfActivePills();

    /**
     * Total number of power pills in the mazes[gs.curMaze]
     *
     * @return the number of active power pills
     */
    int getNumberOfActivePowerPills();

    /**
     * Returns the number of nodes in the current maze.
     *
     * @return number of nodes in the current maze.
     */
    int getNumberOfNodes();

    /**
     * Total number of pills in the mazes[gs.curMaze]
     *
     * @return the number of pills
     */
    int getNumberOfPills();

    /**
     * Total number of power pills in the mazes[gs.curMaze]
     *
     * @return the number of power pills
     */
    int getNumberOfPowerPills();

    /**
     * Current node index of Ms Pac-Man.
     *
     * @return the pacman current node index
     */
    int getPacmanCurrentNodeIndex();

    /**
     * Current node index of Ms Pac-Man.
     *
     * @return the pacman last move made
     */
    MOVE getPacmanLastMoveMade();

    /**
     * Lives that remain for Ms Pac-Man.
     *
     * @return the number of lives remaining
     */
    int getPacmanNumberOfLivesRemaining();

    /**
     * Returns the pill index of the node specified. This can be -1 if there
     * is no pill at the specified node.
     *
     * @param nodeIndex The Index of the node.
     * @return a number corresponding to the pill index (or -1 if node has no pill)
     */
    int getPillIndex(int nodeIndex);

    /**
     * Returns the indices to all the nodes that have pills.
     *
     * @return the pill indices
     */
    int[] getPillIndices();

    /**
     * Gets the possible moves from the node index specified.
     *
     * @param nodeIndex The current node index
     * @return The set of possible moves
     */
    MOVE[] getPossibleMoves(int nodeIndex);

    /**
     * Gets the possible moves except the one that corresponds to the reverse of the move supplied.
     *
     * @param nodeIndex The current node index
     * @param lastModeMade The last mode made (possible moves will exclude the reverse)
     * @return The set of possible moves
     */
    MOVE[] getPossibleMoves(int nodeIndex, MOVE lastModeMade);

    /**
     * Returns the power pill index of the node specified. This can be -1 if there
     * is no power pill at the specified node.
     *
     * @param nodeIndex The Index of the node.
     * @return a number corresponding to the power pill index (or -1 if node has no pill)
     */
    int getPowerPillIndex(int nodeIndex);

    /**
     * Returns the indices to all the nodes that have power pills.
     *
     * @return the power pill indices
     */
    int[] getPowerPillIndices();

    /**
     * Returns the score of the game.
     *
     * @return the score
     */
    int getScore();

    /**
     * Gets the shortest path from node A to node B as specified by their indices.
     *
     * @param fromNodeIndex The node index from where to start (i.e., current position)
     * @param toNodeIndex The target node index
     * @return the shortest path from start to target
     */
    int[] getShortestPath(int fromNodeIndex, int toNodeIndex);

    /**
     * Gets the shortest path taking into account the last move made (i.e., no reversals).
     * This is approximate only as the path is computed greedily. A more accurate path can be obtained
     * using A* which is slightly more costly.
     *
     * @param fromNodeIndex The node index from where to start (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @return the shortest path from start to target
     */
    int[] getShortestPath(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade);

    /////////////////////////////////////////////////////////////////////////////
    ///////////////////  Helper Methods (computational)  ////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the PATH distance from any node to any other node.
     *
     * @param fromNodeIndex the from node index
     * @param toNodeIndex the to node index
     * @return the shortest path distance
     */
    int getShortestPathDistance(int fromNodeIndex, int toNodeIndex);

    /**
     * Similar to getShortestPath but returns the distance of the path only. It is slightly
     * more efficient.
     *
     * @param fromNodeIndex The node index from where to start (i.e., current position)
     * @param toNodeIndex The target node index
     * @param lastMoveMade The last move made
     * @return the exact distance of the path
     */
    int getShortestPathDistance(int fromNodeIndex, int toNodeIndex, MOVE lastMoveMade);

    /**
     * Returns the time when the last global reversal event took place.
     *
     * @return time the last global reversal event took place (not including power pill reversals)
     */
    int getTimeOfLastGlobalReversal();

    /**
     * Total time the game has been played for (at most LEVEL_LIMIT*MAX_LEVELS).
     *
     * @return the total time
     */
    int getTotalTime();

    /**
     * Simpler check to see if a ghost is edible.
     *
     * @param ghostType the ghost type
     * @return true, if is ghost edible
     */
    boolean isGhostEdible(GHOST ghostType);

    /**
     * Checks if the node specified by the nodeIndex is a junction.
     *
     * @param nodeIndex the node index
     * @return true, if is junction
     */
    boolean isJunction(int nodeIndex);

    /**
     * Whether the pill specified is still there or has been eaten.
     *
     * @param pillIndex The pill index
     * @return true, if is pill still available
     */
    boolean isPillStillAvailable(int pillIndex);

    /**
     * Whether the power pill specified is still there or has been eaten.
     *
     * @param powerPillIndex The power pill index
     * @return true, if is power pill still available
     */
    boolean isPowerPillStillAvailable(int powerPillIndex);

    /**
     * Sets the game state from a string: the inverse of getGameState(). It reconstructs
     * all the game's variables from the string.
     *
     * @param gameState The game state represented as a string
     */
    void setGameState(String gameState);

    /**
     * Updates the game once the individual characters have been updated: check if anyone
     * can eat anyone else. Then update the lair times and check if Ms Pac-Man should be
     * awarded the extra live. Then update the time and see if the level or game is over.
     */
    void updateGame();

    /**
     * This method is for specific purposes such as searching a tree in a specific manner. It has to be used cautiously as it might
     * create an unstable game state and may cause the game to crash.
     *
     * @param feast Whether or not to enable feasting
     * @param updateLairTimes Whether or not to update the lair times
     * @param updateExtraLife Whether or not to update the extra life
     * @param updateTotalTime Whether or not to update the total time
     * @param updateLevelTime Whether or not to update the level time
     */
    void updateGame(boolean feast, boolean updateLairTimes, boolean updateExtraLife, boolean updateTotalTime, boolean updateLevelTime);

    /**
     * Updates the states of the ghosts given the moves returned by the controller.
     *
     * @param ghostMoves The moves supplied by the ghosts controller
     */
    void updateGhosts(EnumMap<GHOST, MOVE> ghostMoves);

    void updateGhostsWithForcedReverse(EnumMap<GHOST, MOVE> ghostMoves);

    void updateGhostsWithoutReverse(EnumMap<GHOST, MOVE> ghostMoves);

    /**
     * Updates the state of Ms Pac-Man given the move returned by the controller.
     *
     * @param pacManMove The move supplied by the Ms Pac-Man controller
     */
    void updatePacMan(MOVE pacManMove);

    /**
     * Returns whether a ghost was eaten in the last time step
     *
     * @return whether a ghost was eaten.
     */
    boolean wasGhostEaten(GHOST ghost);

    /////////////////////////////////////////////////////////////////////////////
    ///////////////////  Query Methods (return only)  ///////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /**
     * Returns whether pacman was eaten in the last time step
     *
     * @return whether Ms Pac-Man was eaten.
     */
    boolean wasPacManEaten();

    /**
     * Returns whether a pill was eaten in the last time step
     *
     * @return whether a pill was eaten.
     */
    boolean wasPillEaten();

    /**
     * Returns whether a power pill was eaten in the last time step
     *
     * @return whether a power pill was eaten.
     */
    boolean wasPowerPillEaten();

}
