package pacman.game;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.Random;
import java.util.Map.Entry;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Ghost;
import pacman.game.internal.Maze;
import pacman.game.internal.Node;
import pacman.game.internal.PacMan;
import pacman.game.internal.PathsCache;

import static pacman.game.Constants.*;

public final class SimplifiedGame implements Game
{

	//pills stored as bitsets for efficient copying
	protected BitSet pills, powerPills;
	//all the game's variables
	protected int mazeIndex, levelCount, currentLevelTime, totalTime, score, ghostEatMultiplier, timeOfLastGlobalReversal;
	protected boolean gameOver, pacmanWasEaten, pillWasEaten, powerPillWasEaten;
	protected EnumMap<GHOST,Boolean> ghostsEaten;
	//the data relating to pacman and the ghosts are stored in respective data structures for clarity
	protected PacMan pacman;
	protected EnumMap<GHOST, Ghost> ghosts;

        protected boolean random_reversal = false;
        private int gameLength = mcts.Constants.DEFAULT_GAME_LENGTH;

	//mazes are only loaded once since they don't change over time
	protected static Maze[] mazes=new Maze[NUM_MAZES];;

	protected Maze currentMaze;

	static
	{
		for(int i=0;i<mazes.length;i++)
			mazes[i]=new Maze(i);
	}

	public static PathsCache[] caches=new PathsCache[NUM_MAZES];

	static
	{
		for(int i=0;i<mazes.length;i++)
		{
			caches[i]=new PathsCache(i);
		}
	}

	protected Random rnd;
	protected long seed;

	/////////////////////////////////////////////////////////////////////////////
	///////////////////  Constructors and initialisers  /////////////////////////
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Instantiates a new game. The seed is used to initialise the pseudo-random
	 * number generator. This way, a game may be replicated exactly by using identical
	 * seeds. Note: in the competition, the games received from the game server are
	 * using different seeds. Otherwise global reversal events would be predictable.
	 *
	 * @param seed The seed for the pseudo-random number generator
	 */
	public SimplifiedGame(long seed)
	{
		this.seed=seed;
		rnd=new Random(seed);

		_init(0);
	}

	/**
	 * Initiates a new game specifying the maze to start with.
	 *
	 * @param seed Seed used for the pseudo-random numbers
	 * @param initialMaze The maze to start the game with
	 */
	public SimplifiedGame(long seed,int initialMaze)
	{
		this.seed=seed;
		rnd=new Random(seed);

		_init(initialMaze);
	}

	/**
	 * Empty constructor used by the copy method.
	 */
	protected SimplifiedGame(){}

	/**
	 * _init.
	 *
	 * @param initialMaze the initial maze
	 */
	protected void _init(int initialMaze)
	{
		mazeIndex=initialMaze;
		score=currentLevelTime=levelCount=totalTime=0;
		ghostEatMultiplier=1;
		gameOver=false;
		timeOfLastGlobalReversal=-1;
		pacmanWasEaten=false;
		pillWasEaten=false;
		powerPillWasEaten=false;

		ghostsEaten=new EnumMap<GHOST,Boolean>(GHOST.class);

		for(GHOST ghost : GHOST.values())
			ghostsEaten.put(ghost,false);

		_setPills(currentMaze=mazes[mazeIndex]);
		_initGhosts();

		pacman=new PacMan(currentMaze.initialPacManNodeIndex,MOVE.LEFT,1,false);
	}

	/**
	 * _new level reset.
	 */
	protected void _newLevelReset()
	{
            gameOver = true;
//		mazeIndex=++mazeIndex%NUM_MAZES;
//		levelCount++;
//		currentMaze=mazes[mazeIndex];
//
//		currentLevelTime=0;
//		ghostEatMultiplier=1;
//
//		_setPills(currentMaze);
//		_levelReset();
	}

	/**
	 * _level reset.
	 */
	protected void _levelReset()
	{
		ghostEatMultiplier=1;

		_initGhosts();

		pacman.currentNodeIndex=currentMaze.initialPacManNodeIndex;
		pacman.lastMoveMade=MOVE.LEFT;
	}

	/**
	 * _set pills.
	 *
	 * @param maze the maze
	 */
	protected void _setPills(Maze maze)
	{
		pills=new BitSet(currentMaze.pillIndices.length);
		pills.set(0,currentMaze.pillIndices.length);
		powerPills=new BitSet(currentMaze.powerPillIndices.length);
		powerPills.set(0,currentMaze.powerPillIndices.length, false);
	}

        public void setShortLairTimes() {
            if (totalTime!=0) {
                System.err.printf("Cannot change lair times because game is already running");
                return;
            }
            int i = 1;
            for (Ghost ghost: ghosts.values()) {
                ghost.lairTime = 5*i;
                i++;
            }
        }

	/**
	 * _init ghosts.
	 */
	protected void _initGhosts()
	{
		ghosts=new EnumMap<GHOST, Ghost>(GHOST.class);

		for(GHOST ghostType : GHOST.values())
			ghosts.put(ghostType,new Ghost(ghostType,currentMaze.lairNodeIndex,0,
					(int)(ghostType.initialLairTime*(Math.pow(LAIR_REDUCTION,levelCount%LEVEL_RESET_REDUCTION))),MOVE.NEUTRAL));
	}

	/**
	 * Gets the game state as a string: all variables are written to a string in a pre-determined
	 * order. The string may later be used to recreate a game state using the setGameState() method.
	 *
	 * Variables not included: enableGlobalReversals
	 *
	 * @return The game state as a string
	 */
    @Override
	public String getGameState()
	{
		StringBuilder sb=new StringBuilder();

		sb.append(mazeIndex+","+totalTime+","+score+","+currentLevelTime+","+levelCount+","
				+pacman.currentNodeIndex+","+pacman.lastMoveMade+","+pacman.numberOfLivesRemaining+","+pacman.hasReceivedExtraLife+",");

		for(Ghost ghost : ghosts.values())
			sb.append(ghost.currentNodeIndex+","+ghost.edibleTime+","+ghost.lairTime+","+ghost.lastMoveMade+",");

		for(int i=0;i<currentMaze.pillIndices.length;i++)
			if(pills.get(i))
				sb.append("1");
			else
				sb.append("0");

		sb.append(",");

		for(int i=0;i<currentMaze.powerPillIndices.length;i++)
			if(powerPills.get(i))
				sb.append("1");
			else
				sb.append("0");

		sb.append(",");
		sb.append(timeOfLastGlobalReversal);
		sb.append(",");
		sb.append(pacmanWasEaten);
		sb.append(",");

		for(GHOST ghost : GHOST.values())
		{
			sb.append(ghostsEaten.get(ghost));
			sb.append(",");
		}

		sb.append(pillWasEaten);
		sb.append(",");
		sb.append(powerPillWasEaten);

		return sb.toString();
	}

	/**
	 * Sets the game state from a string: the inverse of getGameState(). It reconstructs
	 * all the game's variables from the string.
	 *
	 * @param gameState The game state represented as a string
	 */
    @Override
	public void setGameState(String gameState)
	{
		String[] values=gameState.split(",");

		int index=0;

		mazeIndex=Integer.parseInt(values[index++]);
		totalTime=Integer.parseInt(values[index++]);
		score=Integer.parseInt(values[index++]);
		currentLevelTime=Integer.parseInt(values[index++]);
		levelCount=Integer.parseInt(values[index++]);

		pacman=new PacMan(Integer.parseInt(values[index++]),MOVE.valueOf(values[index++]),
				Integer.parseInt(values[index++]),Boolean.parseBoolean(values[index++]));

		ghosts=new EnumMap<GHOST, Ghost>(GHOST.class);

		for(GHOST ghostType : GHOST.values())
			ghosts.put(ghostType,new Ghost(ghostType,Integer.parseInt(values[index++]),Integer.parseInt(values[index++]),
					Integer.parseInt(values[index++]),MOVE.valueOf(values[index++])));

		_setPills(currentMaze=mazes[mazeIndex]);

		for(int i=0;i<values[index].length();i++)
			if(values[index].charAt(i)=='1')
				pills.set(i);
			else
				pills.clear(i);

		index++;

		for(int i=0;i<values[index].length();i++)
			if(values[index].charAt(i)=='1')
				powerPills.set(i);
			else
				powerPills.clear(i);

		timeOfLastGlobalReversal=Integer.parseInt(values[++index]);
		pacmanWasEaten=Boolean.parseBoolean(values[++index]);

		ghostsEaten=new EnumMap<GHOST,Boolean>(GHOST.class);

		for(GHOST ghost : GHOST.values())
			ghostsEaten.put(ghost,Boolean.parseBoolean(values[++index]));

		pillWasEaten=Boolean.parseBoolean(values[++index]);
		powerPillWasEaten=Boolean.parseBoolean(values[++index]);
	}

	/**
	 * Returns an exact copy of the game. This may be used for forward searches
	 * such as minimax. The copying is relatively efficient.
	 *
	 * @return the game
	 */
    @Override
	public SimplifiedGame copy()
	{
		SimplifiedGame copy=new SimplifiedGame();

		copy.seed=seed;
		copy.rnd=new Random(seed);
		copy.currentMaze=currentMaze;
		copy.pills=(BitSet)pills.clone();
		copy.powerPills=(BitSet)powerPills.clone();
		copy.mazeIndex=mazeIndex;
		copy.levelCount=levelCount;
		copy.currentLevelTime=currentLevelTime;
		copy.totalTime=totalTime;
		copy.score=score;
		copy.ghostEatMultiplier=ghostEatMultiplier;
		copy.gameOver=gameOver;
		copy.timeOfLastGlobalReversal=timeOfLastGlobalReversal;
		copy.pacmanWasEaten=pacmanWasEaten;
		copy.pillWasEaten=pillWasEaten;
		copy.powerPillWasEaten=powerPillWasEaten;
		copy.pacman=pacman.copy();

		copy.ghostsEaten=new EnumMap<GHOST,Boolean>(GHOST.class);
		copy.ghosts=new EnumMap<GHOST,Ghost>(GHOST.class);

		for(GHOST ghostType : GHOST.values())
		{
			copy.ghosts.put(ghostType,ghosts.get(ghostType).copy());
			copy.ghostsEaten.put(ghostType,ghostsEaten.get(ghostType));
		}

		return copy;
	}

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
    @Override
	public void advanceGame(MOVE pacManMove,EnumMap<GHOST,MOVE> ghostMoves)
	{
		updatePacMan(pacManMove);
		updateGhosts(ghostMoves);
		updateGame();
	}

    @Override
	public void advanceGameWithoutReverse(MOVE pacManMove,EnumMap<GHOST,MOVE> ghostMoves)
	{
		updatePacMan(pacManMove);
		updateGhostsWithoutReverse(ghostMoves);
		updateGame();
	}

    @Override
	public void advanceGameWithForcedReverse(MOVE pacManMove,EnumMap<GHOST,MOVE> ghostMoves)
	{
		updatePacMan(pacManMove);
		updateGhostsWithForcedReverse(ghostMoves);
		updateGame();
	}

    @Override
	public void advanceGameWithPowerPillReverseOnly(MOVE pacManMove,EnumMap<GHOST,MOVE> ghostMoves)
	{
            //advanceGame(pacManMove, ghostMoves);
//            random_reversal = false;
            updatePacMan(pacManMove);
            updateGhosts(ghostMoves);
            updateGame();
//            random_reversal = true;
//		updatePacMan(pacManMove);
//
//		if(powerPillWasEaten)
//			updateGhostsWithForcedReverse(ghostMoves);
//		else
//			updateGhostsWithoutReverse(ghostMoves);
//
//		updateGame();
	}

	/**
	 * Updates the state of Ms Pac-Man given the move returned by the controller.
	 *
	 * @param pacManMove The move supplied by the Ms Pac-Man controller
	 */
    @Override
	public void updatePacMan(MOVE pacManMove)
	{
		_updatePacMan(pacManMove);					//move pac-man
		_eatPill();									//eat a pill
		_eatPowerPill();							//eat a power pill
	}

	/**
	 * Updates the states of the ghosts given the moves returned by the controller.
	 *
	 * @param ghostMoves The moves supplied by the ghosts controller
	 */
    @Override
	public void updateGhosts(EnumMap<GHOST,MOVE> ghostMoves)
	{
		ghostMoves=_completeGhostMoves(ghostMoves);

		if(!_reverseGhosts(ghostMoves,false))
			_updateGhosts(ghostMoves);
	}

    @Override
	public void updateGhostsWithoutReverse(EnumMap<GHOST,MOVE> ghostMoves)
	{
		ghostMoves=_completeGhostMoves(ghostMoves);
		_updateGhosts(ghostMoves);
	}

    @Override
	public void updateGhostsWithForcedReverse(EnumMap<GHOST,MOVE> ghostMoves)
	{
		ghostMoves=_completeGhostMoves(ghostMoves);
		_reverseGhosts(ghostMoves,true);
	}

	/**
	 * Updates the game once the individual characters have been updated: check if anyone
	 * can eat anyone else. Then update the lair times and check if Ms Pac-Man should be
	 * awarded the extra live. Then update the time and see if the level or game is over.
	 */
    @Override
	public void updateGame()
	{
		_feast();									//ghosts eat pac-man or vice versa
		_updateLairTimes();
		//_updatePacManExtraLife();

		totalTime++;
		currentLevelTime++;

		_checkLevelState();							//check if level/game is over
	}

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
    @Override
	public void updateGame(boolean feast,boolean updateLairTimes,boolean updateExtraLife,boolean updateTotalTime,boolean updateLevelTime)
	{
		if(feast) 			_feast();				//ghosts eat pac-man or vice versa
		if(updateLairTimes) _updateLairTimes();
		//if(updateExtraLife) _updatePacManExtraLife();

		if(updateTotalTime) totalTime++;
		if(updateLevelTime) currentLevelTime++;

		_checkLevelState();							//check if level/game is over
	}

	/**
	 * _update lair times.
	 */
	protected void _updateLairTimes()
	{
		for(Ghost ghost : ghosts.values())
			if(ghost.lairTime>0)
				if(--ghost.lairTime==0)
					ghost.currentNodeIndex=currentMaze.initialGhostNodeIndex;
	}

	/**
	 * _update pac man extra life.
	 */
//	protected void _updatePacManExtraLife()
//	{
//		if(!pacman.hasReceivedExtraLife && score>=EXTRA_LIFE_SCORE)	//award 1 extra life at 10000 points
//		{
//			pacman.hasReceivedExtraLife=true;
//			pacman.numberOfLivesRemaining++;
//		}
//	}

	/**
	 * _update pac man.
	 *
	 * @param move the move
	 */
	protected void _updatePacMan(MOVE move)
	{
		pacman.lastMoveMade=_correctPacManDir(move);
		pacman.currentNodeIndex=pacman.lastMoveMade == MOVE.NEUTRAL ? pacman.currentNodeIndex :
			currentMaze.graph[pacman.currentNodeIndex].neighbourhood.get(pacman.lastMoveMade);
	}

	/**
	 * _correct pac man dir.
	 *
	 * @param direction the direction
	 * @return the mOVE
	 */
	protected MOVE _correctPacManDir(MOVE direction)
	{
		Node node=currentMaze.graph[pacman.currentNodeIndex];

		//direction is correct, return it
		if(node.neighbourhood.containsKey(direction))
			return direction;
		else
		{
			//try to use previous direction (i.e., continue in the same direction)
			if(node.neighbourhood.containsKey(pacman.lastMoveMade))
				return pacman.lastMoveMade;
			//else stay put
			else
				return MOVE.NEUTRAL;
		}
	}

	/**
	 * _update ghosts.
	 *
	 * @param moves the moves
	 */
	protected void _updateGhosts(EnumMap<GHOST,MOVE> moves)
	{
		for(Entry<GHOST,MOVE> entry : moves.entrySet())
		{
			Ghost ghost=ghosts.get(entry.getKey());

			if(ghost.lairTime==0)
			{
				if(ghost.edibleTime==0 || ghost.edibleTime%GHOST_SPEED_REDUCTION!=0)
				{
					ghost.lastMoveMade=_checkGhostDir(ghost,entry.getValue());
					moves.put(entry.getKey(), ghost.lastMoveMade);
					ghost.currentNodeIndex=currentMaze.graph[ghost.currentNodeIndex].neighbourhood.get(ghost.lastMoveMade);
				}
			}
		}
	}

	protected EnumMap<GHOST,MOVE> _completeGhostMoves(EnumMap<GHOST,MOVE> moves)
	{
		if(moves==null)
		{
			moves=new EnumMap<GHOST,MOVE>(GHOST.class);

			for(GHOST ghostType : GHOST.values())
				moves.put(ghostType,ghosts.get(ghostType).lastMoveMade);
		}

		if(moves.size()<NUM_GHOSTS)
			for(GHOST ghostType : GHOST.values())
				if(!moves.containsKey(ghostType))
					moves.put(ghostType,MOVE.NEUTRAL);

		return moves;
	}

	/**
	 * _check ghost dir.
	 *
	 * @param ghost the ghost
	 * @param direction the direction
	 * @return the mOVE
	 */
	protected MOVE _checkGhostDir(Ghost ghost,MOVE direction)
	{
		//Gets the neighbours of the node with the node that would correspond to reverse removed
		Node node=currentMaze.graph[ghost.currentNodeIndex];

		//The direction is possible and not opposite to the previous direction of that ghost
		if(node.neighbourhood.containsKey(direction) && direction!=ghost.lastMoveMade.opposite())
			return direction;
		else
		{
			if(node.neighbourhood.containsKey(ghost.lastMoveMade))
				return ghost.lastMoveMade;
			else
			{
				MOVE[] moves=node.allPossibleMoves.get(ghost.lastMoveMade);
				return moves[rnd.nextInt(moves.length)];
			}
		}
	}

	/**
	 * _eat pill.
	 */
	protected void _eatPill()
	{
		pillWasEaten=false;

		int pillIndex=currentMaze.graph[pacman.currentNodeIndex].pillIndex;

		if(pillIndex>=0 && pills.get(pillIndex))
		{
			score+=PILL;
			pills.clear(pillIndex);
			pillWasEaten=true;
		}
	}

	/**
	 * _eat power pill.
	 */
	protected void _eatPowerPill()
	{
		powerPillWasEaten=false;

		int powerPillIndex=currentMaze.graph[pacman.currentNodeIndex].powerPillIndex;

		if(powerPillIndex>=0 && powerPills.get(powerPillIndex))
		{
			score+=POWER_PILL;
			ghostEatMultiplier=1;
			powerPills.clear(powerPillIndex);

			int newEdibleTime=(int)(EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,levelCount%LEVEL_RESET_REDUCTION)));

			for(Ghost ghost : ghosts.values())
				if(ghost.lairTime==0)
					ghost.edibleTime=newEdibleTime;
				else
					ghost.edibleTime=0;

			powerPillWasEaten=true;
		}
	}

	protected boolean _reverseGhosts(EnumMap<GHOST,MOVE> moves,boolean force)
	{
		boolean reversed=false;
		boolean globalReverse=false;

		if(random_reversal&&Math.random()<GHOST_REVERSAL)
			globalReverse=true;

		for(Entry<GHOST,MOVE> entry : moves.entrySet())
		{
			Ghost ghost=ghosts.get(entry.getKey());

			if(currentLevelTime>1 && ghost.lairTime==0 && ghost.lastMoveMade!=MOVE.NEUTRAL)
			{
				if(force || (powerPillWasEaten || globalReverse))
				{
					ghost.lastMoveMade=ghost.lastMoveMade.opposite();
					ghost.currentNodeIndex=currentMaze.graph[ghost.currentNodeIndex].neighbourhood.get(ghost.lastMoveMade);
					reversed=true;
					timeOfLastGlobalReversal = totalTime;
				}
			}
		}

		return reversed;
	}

	/**
	 * _feast.
	 */
	protected void _feast()
	{
		pacmanWasEaten=false;

		for(GHOST ghost : GHOST.values())
			ghostsEaten.put(ghost,false);

		for(Ghost ghost : ghosts.values())
		{
			int distance=getShortestPathDistance(pacman.currentNodeIndex, ghost.currentNodeIndex);

			if(distance<=EAT_DISTANCE && distance!=-1)
			{
				if(ghost.edibleTime>0)									//pac-man eats ghost
				{
					score+=GHOST_EAT_SCORE*ghostEatMultiplier;
					ghostEatMultiplier*=2;
					ghost.edibleTime=0;
					ghost.lairTime=(int)(COMMON_LAIR_TIME*(Math.pow(LAIR_REDUCTION,levelCount%LEVEL_RESET_REDUCTION)));
					ghost.currentNodeIndex=currentMaze.lairNodeIndex;
					ghost.lastMoveMade=MOVE.NEUTRAL;

					ghostsEaten.put(ghost.type, true);
				}
				else													//ghost eats pac-man
				{
					pacman.numberOfLivesRemaining--;
					pacmanWasEaten=true;

					if(pacman.numberOfLivesRemaining<=0)
						gameOver=true;
					else
						_levelReset();

					return;
				}
			}
		}

		for(Ghost ghost : ghosts.values())
			if(ghost.edibleTime>0)
				ghost.edibleTime--;
	}

	/**
	 * _check level state.
	 */
	protected void _checkLevelState()
	{
		//put a cap on the total time a game can be played for
                if (totalTime+1>gameLength) {
                    gameOver = true;
                    score = 2200;
                }
//		if(totalTime+1>MAX_TIME)
//		{
//			gameOver=true;
//			score+=pacman.numberOfLivesRemaining*AWARD_LIFE_LEFT;
//		}
		//if all pills have been eaten or the time is up...
		else if((pills.isEmpty() && powerPills.isEmpty()) || currentLevelTime>=LEVEL_LIMIT)
			_newLevelReset();
	}

	/////////////////////////////////////////////////////////////////////////////
	///////////////////  Query Methods (return only)  ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns whether pacman was eaten in the last time step
	 *
	 * @return whether Ms Pac-Man was eaten.
	 */
    @Override
	public boolean wasPacManEaten()
	{
		return pacmanWasEaten;
	}

	/**
	 * Returns whether a ghost was eaten in the last time step
	 *
	 * @return whether a ghost was eaten.
	 */
    @Override
	public boolean wasGhostEaten(GHOST ghost)
	{
		return ghostsEaten.get(ghost);
	}

    @Override
	public int getNumGhostsEaten()
	{
		int count=0;

		for(GHOST ghost : GHOST.values())
			if(ghostsEaten.get(ghost))
				count++;

		return count;
	}

	/**
	 * Returns whether a pill was eaten in the last time step
	 *
	 * @return whether a pill was eaten.
	 */
    @Override
	public boolean wasPillEaten()
	{
		return pillWasEaten;
	}

	/**
	 * Returns whether a power pill was eaten in the last time step
	 *
	 * @return whether a power pill was eaten.
	 */
    @Override
	public boolean wasPowerPillEaten()
	{
		return powerPillWasEaten;
	}

	/**
	 * Returns the time when the last global reversal event took place.
	 *
	 * @return time the last global reversal event took place (not including power pill reversals)
	 */
    @Override
	public int getTimeOfLastGlobalReversal()
	{
		return timeOfLastGlobalReversal;
	}

	/**
	 * Checks whether the game is over or not: all lives are lost or 16 levels have been
	 * played. The variable is set by the methods _feast() and _checkLevelState().
	 *
	 * @return true, if successful
	 */
    @Override
	public boolean gameOver()
	{
		return gameOver;
	}

	/**
	 * Returns the current maze of the game.
	 *
	 * @return The current maze.
	 */
    @Override
	public Maze getCurrentMaze()
	{
		return currentMaze;
	}

	/**
	 * Returns the x coordinate of the specified node.
	 *
	 * @param nodeIndex the node index
	 * @return the node x cood
	 */
    @Override
	public int getNodeXCood(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].x;
	}

	/**
	 * Returns the y coordinate of the specified node.
	 *
	 * @param nodeIndex The node index
	 * @return The node's y coordinate
	 */
    @Override
	public int getNodeYCood(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].y;
	}

	/**
	 * Gets the index of the current maze.
	 *
	 * @return The maze index
	 */
    @Override
	public int getMazeIndex()
	{
		return mazeIndex;
	}

	/**
	 * Returns the current level.
	 *
	 * @return The current level
	 */
    @Override
	public int getCurrentLevel()
	{
		return levelCount;
	}

	/**
	 * Returns the number of nodes in the current maze.
	 *
	 * @return number of nodes in the current maze.
	 */
    @Override
	public int getNumberOfNodes()
	{
		return currentMaze.graph.length;
	}

	/**
	 * Returns the current value awarded for eating a ghost.
	 *
	 * @return the current value awarded for eating a ghost.
	 */
    @Override
	public int getGhostCurrentEdibleScore()
	{
		return GHOST_EAT_SCORE*ghostEatMultiplier;
	}

	/**
	 * Returns the node index where ghosts start in the maze once leaving
	 * the lair.
	 *
	 * @return the node index where ghosts start after leaving the lair.
	 */
    @Override
	public int getGhostInitialNodeIndex()
	{
		return currentMaze.initialGhostNodeIndex;
	}

	/**
	 * Whether the pill specified is still there or has been eaten.
	 *
	 * @param pillIndex The pill index
	 * @return true, if is pill still available
	 */
    @Override
	public boolean isPillStillAvailable(int pillIndex)
	{
		return pills.get(pillIndex);
	}

	/**
	 * Whether the power pill specified is still there or has been eaten.
	 *
	 * @param powerPillIndex The power pill index
	 * @return true, if is power pill still available
	 */
    @Override
	public boolean isPowerPillStillAvailable(int powerPillIndex)
	{
		return powerPills.get(powerPillIndex);
	}

	/**
	 * Returns the pill index of the node specified. This can be -1 if there
	 * is no pill at the specified node.
	 *
	 * @param nodeIndex The Index of the node.
	 * @return a number corresponding to the pill index (or -1 if node has no pill)
	 */
    @Override
	public int getPillIndex(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].pillIndex;
	}

	/**
	 * Returns the power pill index of the node specified. This can be -1 if there
	 * is no power pill at the specified node.
	 *
	 * @param nodeIndex The Index of the node.
	 * @return a number corresponding to the power pill index (or -1 if node has no pill)
	 */
    @Override
	public int getPowerPillIndex(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].powerPillIndex;
	}

	/**
	 * Returns the array of node indices that are junctions (3 or more neighbours).
	 *
	 * @return the junction indices
	 */
    @Override
	public int[] getJunctionIndices()
	{
		return currentMaze.junctionIndices;
	}

	/**
	 * Returns the indices to all the nodes that have pills.
	 *
	 * @return the pill indices
	 */
    @Override
	public int[] getPillIndices()
	{
		return currentMaze.pillIndices;
	}

	/**
	 * Returns the indices to all the nodes that have power pills.
	 *
	 * @return the power pill indices
	 */
    @Override
	public int[] getPowerPillIndices()
	{
		return currentMaze.powerPillIndices;
	}

	/**
	 * Current node index of Ms Pac-Man.
	 *
	 * @return the pacman current node index
	 */
    @Override
	public int getPacmanCurrentNodeIndex()
	{
		return pacman.currentNodeIndex;
	}

	/**
	 * Current node index of Ms Pac-Man.
	 *
	 * @return the pacman last move made
	 */
    @Override
	public MOVE getPacmanLastMoveMade()
	{
		return pacman.lastMoveMade;
	}

	/**
	 * Lives that remain for Ms Pac-Man.
	 *
	 * @return the number of lives remaining
	 */
    @Override
	public int getPacmanNumberOfLivesRemaining()
	{
		return pacman.numberOfLivesRemaining;
	}

	/**
	 * Current node at which the specified ghost resides.
	 *
	 * @param ghostType the ghost type
	 * @return the ghost current node index
	 */
    @Override
	public int getGhostCurrentNodeIndex(GHOST ghostType)
	{
		return ghosts.get(ghostType).currentNodeIndex;
	}

	/**
	 * Current direction of the specified ghost.
	 *
	 * @param ghostType the ghost type
	 * @return the ghost last move made
	 */
    @Override
	public MOVE getGhostLastMoveMade(GHOST ghostType)
	{
		return ghosts.get(ghostType).lastMoveMade;
	}

	/**
	 * Returns the edible time for the specified ghost.
	 *
	 * @param ghostType the ghost type
	 * @return the ghost edible time
	 */
    @Override
	public int getGhostEdibleTime(GHOST ghostType)
	{
		return ghosts.get(ghostType).edibleTime;
	}

	/**
	 * Simpler check to see if a ghost is edible.
	 *
	 * @param ghostType the ghost type
	 * @return true, if is ghost edible
	 */
    @Override
	public boolean isGhostEdible(GHOST ghostType)
	{
		return ghosts.get(ghostType).edibleTime>0;
	}

	/**
	 * Returns the score of the game.
	 *
	 * @return the score
	 */
    @Override
	public int getScore()
	{
		return score;
	}

	/**
	 * Returns the time of the current level (important with respect to LEVEL_LIMIT).
	 *
	 * @return the current level time
	 */
    @Override
	public int getCurrentLevelTime()
	{
		return currentLevelTime;
	}

	/**
	 * Total time the game has been played for (at most LEVEL_LIMIT*MAX_LEVELS).
	 *
	 * @return the total time
	 */
    @Override
	public int getTotalTime()
	{
		return totalTime;
	}

	/**
	 * Total number of pills in the mazes[gs.curMaze]
	 *
	 * @return the number of pills
	 */
    @Override
	public int getNumberOfPills()
	{
		return currentMaze.pillIndices.length;
	}

	/**
	 * Total number of power pills in the mazes[gs.curMaze]
	 *
	 * @return the number of power pills
	 */
    @Override
	public int getNumberOfPowerPills()
	{
		return currentMaze.powerPillIndices.length;
	}

	/**
	 * Total number of pills in the mazes[gs.curMaze]
	 *
	 * @return the number of active pills
	 */
    @Override
	public int getNumberOfActivePills()
	{
		return pills.cardinality();
	}

	/**
	 * Total number of power pills in the mazes[gs.curMaze]
	 *
	 * @return the number of active power pills
	 */
    @Override
	public int getNumberOfActivePowerPills()
	{
		return powerPills.cardinality();
	}

	/**
	 * Time left that the specified ghost will spend in the lair.
	 *
	 * @param ghostType the ghost type
	 * @return the ghost lair time
	 */
    @Override
	public int getGhostLairTime(GHOST ghostType)
	{
		return ghosts.get(ghostType).lairTime;
	}

	/**
	 * returns the indices of all active pills in the mazes[gs.curMaze]
	 *
	 * @return the active pills indices
	 */
    @Override
	public int[] getActivePillsIndices()
	{
		int[] indices=new int[pills.cardinality()];

		int index=0;

		for(int i=0;i<currentMaze.pillIndices.length;i++)
			if(pills.get(i))
				indices[index++]=currentMaze.pillIndices[i];

		return indices;
	}

	/**
	 * returns the indices of all active power pills in the mazes[gs.curMaze]
	 *
	 * @return the active power pills indices
	 */
    @Override
	public int[] getActivePowerPillsIndices()
	{
		int[] indices=new int[powerPills.cardinality()];

		int index=0;

		for(int i=0;i<currentMaze.powerPillIndices.length;i++)
			if(powerPills.get(i))
				indices[index++]=currentMaze.powerPillIndices[i];

		return indices;
	}

	/**
	 * If in lair (getLairTime(-)>0) or if not at junction.
	 *
	 * @param ghostType the ghost type
	 * @return true, if successful
	 */
    @Override
	public boolean doesGhostRequireAction(GHOST ghostType)
	{
		//inlcude neutral here for the unique case where the ghost just left the lair
		return ((isJunction(ghosts.get(ghostType).currentNodeIndex) || (ghosts.get(ghostType).lastMoveMade==MOVE.NEUTRAL) && ghosts.get(ghostType).currentNodeIndex==currentMaze.initialGhostNodeIndex)
				&& (ghosts.get(ghostType).edibleTime==0 || ghosts.get(ghostType).edibleTime%GHOST_SPEED_REDUCTION!=0));
	}

	/**
	 * Checks if the node specified by the nodeIndex is a junction.
	 *
	 * @param nodeIndex the node index
	 * @return true, if is junction
	 */
    @Override
	public boolean isJunction(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].numNeighbouringNodes>2;
	}

	/**
	 * Gets the possible moves from the node index specified.
	 *
	 * @param nodeIndex The current node index
	 * @return The set of possible moves
	 */
    @Override
	public MOVE[] getPossibleMoves(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].allPossibleMoves.get(MOVE.NEUTRAL);
	}

	/**
	 * Gets the possible moves except the one that corresponds to the reverse of the move supplied.
	 *
	 * @param nodeIndex The current node index
	 * @param lastModeMade The last mode made (possible moves will exclude the reverse)
	 * @return The set of possible moves
	 */
    @Override
	public MOVE[] getPossibleMoves(int nodeIndex,MOVE lastModeMade)
	{
		return currentMaze.graph[nodeIndex].allPossibleMoves.get(lastModeMade);
	}

	/**
	 * Gets the neighbouring nodes from the current node index.
	 *
	 * @param nodeIndex The current node index
	 * @return The set of neighbouring nodes
	 */
    @Override
	public int[] getNeighbouringNodes(int nodeIndex)
	{
		return currentMaze.graph[nodeIndex].allNeighbouringNodes.get(MOVE.NEUTRAL);
	}

	/**
	 * Gets the neighbouring nodes from the current node index excluding the node
	 * that corresponds to the opposite of the last move made which is given as an argument.
	 *
	 * @param nodeIndex The current node index
	 * @param lastModeMade The last mode made
	 * @return The set of neighbouring nodes except the one that is opposite of the last move made
	 */
    @Override
	public int[] getNeighbouringNodes(int nodeIndex,MOVE lastModeMade)
	{
		return currentMaze.graph[nodeIndex].allNeighbouringNodes.get(lastModeMade);
	}

   /**
    * Given a node index and a move to be made, it returns the node index the move takes one to.
    * If there is no neighbour in that direction, the method returns -1.
    *
    * @param nodeIndex The current node index
	* @param moveToBeMade The move to be made
	* @return The node index of the node the move takes one to
    */
    @Override
    public int getNeighbour(int nodeIndex, MOVE moveToBeMade)
    {
    	Integer neighbour=currentMaze.graph[nodeIndex].neighbourhood.get(moveToBeMade);

    	return neighbour==null ? -1 : neighbour;
    }

	/**
	 * Method that returns the direction to take given a node index and an index of a neighbouring
	 * node. Returns null if the neighbour is invalid.
	 *
	 * @param currentNodeIndex The current node index.
	 * @param neighbourNodeIndex The direct neighbour (node index) of the current node.
	 * @return the move to make to reach direct neighbour
	 */
    @Override
	public MOVE getMoveToMakeToReachDirectNeighbour(int currentNodeIndex,int neighbourNodeIndex)
	{
		for(MOVE move : MOVE.values())
		{
			if(currentMaze.graph[currentNodeIndex].neighbourhood.containsKey(move)
					&& currentMaze.graph[currentNodeIndex].neighbourhood.get(move)==neighbourNodeIndex)
			{
				return move;
			}
		}

		return null;
	}

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
    @Override
	public int getShortestPathDistance(int fromNodeIndex,int toNodeIndex)
	{
		if(fromNodeIndex==toNodeIndex)
			return 0;
		else if(fromNodeIndex<toNodeIndex)
			return currentMaze.shortestPathDistances[((toNodeIndex*(toNodeIndex+1))/2)+fromNodeIndex];
		else
			return currentMaze.shortestPathDistances[((fromNodeIndex*(fromNodeIndex+1))/2)+toNodeIndex];
	}

	/**
	 * Returns the EUCLEDIAN distance between two nodes in the current mazes[gs.curMaze].
	 *
	 * @param fromNodeIndex the from node index
	 * @param toNodeIndex the to node index
	 * @return the euclidean distance
	 */
    @Override
	public double getEuclideanDistance(int fromNodeIndex,int toNodeIndex)
	{
		return Math.sqrt(Math.pow(currentMaze.graph[fromNodeIndex].x-currentMaze.graph[toNodeIndex].x,2)+Math.pow(currentMaze.graph[fromNodeIndex].y-currentMaze.graph[toNodeIndex].y,2));
	}

	/**
	 * Returns the MANHATTAN distance between two nodes in the current mazes[gs.curMaze].
	 *
	 * @param fromNodeIndex the from node index
	 * @param toNodeIndex the to node index
	 * @return the manhattan distance
	 */
    @Override
	public int getManhattanDistance(int fromNodeIndex,int toNodeIndex)
	{
		return (int)(Math.abs(currentMaze.graph[fromNodeIndex].x-currentMaze.graph[toNodeIndex].x)+Math.abs(currentMaze.graph[fromNodeIndex].y-currentMaze.graph[toNodeIndex].y));
	}

	/**
	 * Gets the distance.
	 *
	 * @param fromNodeIndex the from node index
	 * @param toNodeIndex the to node index
	 * @param distanceMeasure the distance measure
	 * @return the distance
	 */
    @Override
	public double getDistance(int fromNodeIndex,int toNodeIndex,DM distanceMeasure)
	{
		switch(distanceMeasure)
		{
			case PATH: return getShortestPathDistance(fromNodeIndex,toNodeIndex);
			case EUCLID: return getEuclideanDistance(fromNodeIndex,toNodeIndex);
			case MANHATTAN: return getManhattanDistance(fromNodeIndex,toNodeIndex);
		}

		return -1;
	}

	/**
	 * Returns the distance between two nodes taking reversals into account.
	 *
	 * @param fromNodeIndex the index of the originating node
	 * @param toNodeIndex the index of the target node
	 * @param lastMoveMade the last move made
	 * @param distanceMeasure the distance measure to be used
	 * @return the distance between two nodes.
	 */
    @Override
	public double getDistance(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade,DM distanceMeasure)
	{
		switch(distanceMeasure)
		{
			case PATH: return getApproximateShortestPathDistance(fromNodeIndex,toNodeIndex,lastMoveMade);
			case EUCLID: return getEuclideanDistance(fromNodeIndex,toNodeIndex);
			case MANHATTAN: return getManhattanDistance(fromNodeIndex,toNodeIndex);
		}

		return -1;
	}

	/**
	 * Gets the closest node index from node index.
	 *
	 * @param fromNodeIndex the from node index
	 * @param targetNodeIndices the target node indices
	 * @param distanceMeasure the distance measure
	 * @return the closest node index from node index
	 */
    @Override
	public int getClosestNodeIndexFromNodeIndex(int fromNodeIndex,int[] targetNodeIndices,DM distanceMeasure)
	{
		double minDistance=Integer.MAX_VALUE;
		int target=-1;

		for(int i=0;i<targetNodeIndices.length;i++)
		{
			double distance=0;

			distance=getDistance(targetNodeIndices[i],fromNodeIndex,distanceMeasure);

			if(distance<minDistance)
			{
				minDistance=distance;
				target=targetNodeIndices[i];
			}
		}

		return target;
	}

	/**
	 * Gets the farthest node index from node index.
	 *
	 * @param fromNodeIndex the from node index
	 * @param targetNodeIndices the target node indices
	 * @param distanceMeasure the distance measure
	 * @return the farthest node index from node index
	 */
    @Override
	public int getFarthestNodeIndexFromNodeIndex(int fromNodeIndex,int[] targetNodeIndices,DM distanceMeasure)
	{
		double maxDistance=Integer.MIN_VALUE;
		int target=-1;

		for(int i=0;i<targetNodeIndices.length;i++)
		{
			double distance=0;

			distance=getDistance(targetNodeIndices[i],fromNodeIndex,distanceMeasure);

			if(distance>maxDistance)
			{
				maxDistance=distance;
				target=targetNodeIndices[i];
			}
		}

		return target;
	}

	/**
	 * Gets the next move towards target.
	 *
	 * @param fromNodeIndex the from node index
	 * @param toNodeIndex the to node index
	 * @param distanceMeasure the distance measure
	 * @return the next move towards target
	 */
    @Override
	public MOVE getNextMoveTowardsTarget(int fromNodeIndex,int toNodeIndex,DM distanceMeasure)
	{
		MOVE move=null;

		double minDistance=Integer.MAX_VALUE;

		for(Entry<MOVE,Integer> entry : currentMaze.graph[fromNodeIndex].neighbourhood.entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);

			if(distance<minDistance)
			{
				minDistance=distance;
				move=entry.getKey();
			}
		}

		return move;
	}

	/**
	 * Gets the next move away from target.
	 *
	 * @param fromNodeIndex the from node index
	 * @param toNodeIndex the to node index
	 * @param distanceMeasure the distance measure
	 * @return the next move away from target
	 */
    @Override
	public MOVE getNextMoveAwayFromTarget(int fromNodeIndex,int toNodeIndex,DM distanceMeasure)
	{
		MOVE move=null;

		double maxDistance=Integer.MIN_VALUE;

		for(Entry<MOVE,Integer> entry : currentMaze.graph[fromNodeIndex].neighbourhood.entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);

			if(distance>maxDistance)
			{
				maxDistance=distance;
				move=entry.getKey();
			}
		}

		return move;
	}

	/**
	 * Gets the approximate next move towards target not considering directions opposing the last move made.
	 *
	 * @param fromNodeIndex The node index from which to move (i.e., current position)
	 * @param toNodeIndex The target node index
	 * @param lastMoveMade The last move made
	 * @param distanceMeasure The distance measure required (Manhattan, Euclidean or Straight line)
	 * @return The approximate next move towards target (chosen greedily)
	 */
    @Override
	public MOVE getApproximateNextMoveTowardsTarget(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade, DM distanceMeasure)
	{
		MOVE move=null;

		double minDistance=Integer.MAX_VALUE;

		for(Entry<MOVE,Integer> entry : currentMaze.graph[fromNodeIndex].allNeighbourhoods.get(lastMoveMade).entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);

			if(distance<minDistance)
			{
				minDistance=distance;
				move=entry.getKey();
			}
		}

		return move;
	}

	/**
	 * Gets the approximate next move away from a target not considering directions opposing the last move made.
	 *
	 * @param fromNodeIndex The node index from which to move (i.e., current position)
	 * @param toNodeIndex The target node index
	 * @param lastMoveMade The last move made
	 * @param distanceMeasure The distance measure required (Manhattan, Euclidean or Straight line)
	 * @return The approximate next move towards target (chosen greedily)
	 */
    @Override
	public MOVE getApproximateNextMoveAwayFromTarget(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade, DM distanceMeasure)
	{
		MOVE move=null;

		double maxDistance=Integer.MIN_VALUE;

		for(Entry<MOVE,Integer> entry : currentMaze.graph[fromNodeIndex].allNeighbourhoods.get(lastMoveMade).entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,distanceMeasure);

			if(distance>maxDistance)
			{
				maxDistance=distance;
				move=entry.getKey();
			}
		}

		return move;
	}

	/**
	 * Gets the exact next move towards target taking into account reversals. This uses the pre-computed paths.
	 *
	 * @param fromNodeIndex The node index from which to move (i.e., current position)
	 * @param toNodeIndex The target node index
	 * @param lastMoveMade The last move made
	 * @param distanceMeasure the distance measure to be used
	 * @return the next move towards target
	 */
    @Override
	public MOVE getNextMoveTowardsTarget(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade, DM distanceMeasure)
	{
		MOVE move=null;

		double minDistance=Integer.MAX_VALUE;

		for(Entry<MOVE,Integer> entry : currentMaze.graph[fromNodeIndex].allNeighbourhoods.get(lastMoveMade).entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,lastMoveMade,distanceMeasure);

			if(distance<minDistance)
			{
				minDistance=distance;
				move=entry.getKey();
			}
		}

		return move;
	}

	/**
	 * Gets the exact next move away from target taking into account reversals. This uses the pre-computed paths.
	 *
	 * @param fromNodeIndex The node index from which to move (i.e., current position)
	 * @param toNodeIndex The target node index
	 * @param lastMoveMade The last move made
	 * @param distanceMeasure the distance measure to be used
	 * @return the next move away from target
	 */
    @Override
	public MOVE getNextMoveAwayFromTarget(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade, DM distanceMeasure)
	{
		MOVE move=null;

		double maxDistance=Integer.MIN_VALUE;

		for(Entry<MOVE,Integer> entry : currentMaze.graph[fromNodeIndex].allNeighbourhoods.get(lastMoveMade).entrySet())
		{
			double distance=getDistance(entry.getValue(),toNodeIndex,lastMoveMade,distanceMeasure);

			if(distance>maxDistance)
			{
				maxDistance=distance;
				move=entry.getKey();
			}
		}

		return move;
	}

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
    @Override
	public int[] getAStarPath(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		return getShortestPath(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

	/**
	 * Gets the shortest path from node A to node B as specified by their indices.
	 *
	 * @param fromNodeIndex The node index from where to start (i.e., current position)
	 * @param toNodeIndex The target node index
	 * @return the shortest path from start to target
	 */
    @Override
	public int[] getShortestPath(int fromNodeIndex,int toNodeIndex)
	{
		return caches[mazeIndex].getPathFromA2B(fromNodeIndex,toNodeIndex);
	}

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
    @Override
	public int[] getApproximateShortestPath(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		return getShortestPath(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

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
    @Override
	public int[] getShortestPath(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		if(currentMaze.graph[fromNodeIndex].neighbourhood.size()==0)//lair
			return new int[0];

		return caches[mazeIndex].getPathFromA2B(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

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
    @Override
	public int getApproximateShortestPathDistance(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		return getShortestPathDistance(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

	/**
	 * Similar to getShortestPath but returns the distance of the path only. It is slightly
	 * more efficient.
	 *
	 * @param fromNodeIndex The node index from where to start (i.e., current position)
	 * @param toNodeIndex The target node index
	 * @param lastMoveMade The last move made
	 * @return the exact distance of the path
	 */
    @Override
	public int getShortestPathDistance(int fromNodeIndex,int toNodeIndex,MOVE lastMoveMade)
	{
		if(currentMaze.graph[fromNodeIndex].neighbourhood.size()==0)//lair
			return 0;

		return caches[mazeIndex].getPathDistanceFromA2B(fromNodeIndex,toNodeIndex,lastMoveMade);
	}

    /**
     * @return the gameLength
     */
    public int getGameLength() {
        return gameLength;
    }

    /**
     * @param gameLength the gameLength to set
     */
    public void setGameLength(int gameLength) {
        this.gameLength = gameLength;
    }


}