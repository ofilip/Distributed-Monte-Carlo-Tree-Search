package pacman;

import communication.DummyMessage;
import communication.Message;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;
import java.util.Set;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.IndependentGhostAgent;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.*;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.ghosts.MCTSGhosts;
import pacman.entries.pacman.*;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class MyExecutor
{
       static abstract class CompetitionOptions {
           private int pacman_delay;
           private int ghosts_delay;
           private String pacman_name;
           private String ghost_name;
           public CompetitionOptions(String pacman_name, int pacman_delay, String ghost_name, int ghosts_delay) {
               this.pacman_delay = pacman_delay;
               this.ghosts_delay = ghosts_delay;
               this.pacman_name = pacman_name;
               this.ghost_name = ghost_name;
           }
           public abstract Controller<MOVE> pacmanController();
           public abstract Controller<EnumMap<GHOST,MOVE>> ghostController();
           public int pacmanDelay() {
               return pacman_delay;
           }
           public int ghostsDelay() {
               return ghosts_delay;
           }
           public String pacmanName() { return pacman_name; }
           public String ghostName() { return ghost_name; }
       }
    
        public static void runCompetition(List<CompetitionOptions> options_list, int trials, boolean visual, boolean recorded) {
            try {
                MyExecutor exec = new MyExecutor();
                String date_string = new SimpleDateFormat("yyMMdd-hhmmss").format(new Date());
                PrintWriter writer = new PrintWriter(String.format("results-%s.txt", date_string));                
                int replay_number = 0;
                for (CompetitionOptions options: options_list) {
                    int pacman_delay = options.pacmanDelay();
                    int ghosts_delay = options.ghostsDelay();
                    long total_score = 0;
                    long min_score = Long.MAX_VALUE;
                    long max_score = 0;
                    String competition_id = String.format("%s\t%s\t%s\t%s", options.pacmanName(), pacman_delay, options.ghostName(), ghosts_delay);
                    Game game;
                    for (int i=1; i<=trials; i++) {
                        if (recorded) {
                            String replay_name = String.format("%s-%s-%s_%s-%s_%s.replay", date_string, replay_number++, options.pacmanName(), pacman_delay, options.ghostName(), ghosts_delay);
                            game = exec.runGameTimedRecorded(options.pacmanController(), options.ghostController(), visual, true, replay_name, pacman_delay, ghosts_delay);
                        } else {
                            game = exec.runGameTimed(options.pacmanController(), options.ghostController(), visual, true, pacman_delay, ghosts_delay);
                        }
                        String result_string = String.format("%s\t%s\t%s\n", i, competition_id, game.getScore());
                        writer.write(result_string);
                        writer.flush();
                        System.out.print(result_string);
                        total_score+=game.getScore();
                        min_score = Math.min(min_score, game.getScore());
                        max_score = Math.max(max_score, game.getScore());
                    }
                    String min_string = String.format("MIN\t%s\t%s\n", competition_id, min_score);
                    String max_string = String.format("MAX\t%s\t%s\n", competition_id, max_score);
                    String avg_string = String.format("AVG\t%s\t%s\n", competition_id, total_score/(double)trials);
                    String stats_string = String.format("%s%s%s", min_string, max_string, avg_string);
                                        
                    writer.write(stats_string);
                    writer.flush();
                    System.out.print(stats_string);
                }
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        final static CompetitionOptions STARTER_VS_LEGACY = new CompetitionOptions("StarterPacMan", 40, "Legacy", 40) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new Legacy();}};
        final static CompetitionOptions STARTER_VS_LEGACY2THERECKONING = new CompetitionOptions("StarterPacMan", 40, "Legacy2TheReckoning", 40) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new Legacy2TheReckoning();};};
        final static CompetitionOptions STARTER_VS_MCTS80 = new CompetitionOptions("StarterPacMan", 40, "MCTSGhosts(80,0.5)", 40) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new MCTSGhosts(80, 0.5, false);};};
        final static CompetitionOptions STARTER_VS_MCTS80_200 = new CompetitionOptions("StarterPacMan", 40, "MCTSGhosts(80,0.5)", 200) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new MCTSGhosts(80, 0.5, false);};};
        final static CompetitionOptions STARTER_VS_MCTS80_800 = new CompetitionOptions("StarterPacMan", 40, "MCTSGhosts(80,0.5)", 800) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new MCTSGhosts(80, 0.5, false);};};
        final static CompetitionOptions STARTER_VS_MCTS200_800 = new CompetitionOptions("StarterPacMan", 40, "MCTSGhosts(200,0.5)", 800) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new MCTSGhosts(200, 0.5, false);};};
        final static CompetitionOptions MCTS120_800_VS_MCTS200_800 = new CompetitionOptions("MCTSPacman(120,0.5)", 800, "MCTSGhosts(200,0.5)", 800) {                
                @Override public Controller<MOVE> pacmanController() {return new MCTSPacman(200, 0.5, false);}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {return new MCTSGhosts(200, 0.5, false);};};
        final static CompetitionOptions STARTER_VS_INDEPENDENT_GHOSTS80_200 = new CompetitionOptions("StarterPacMan", 40, "IndependentGhostAgents(80,0.5)", 200) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {
                    return new DistributedMCTSController<IndependentGhostAgent, DummyMessage>(1, true)
                                .addGhostAgent(new IndependentGhostAgent(GHOST.BLINKY, 80, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.PINKY, 80, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.INKY, 80, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.SUE, 80, 0.5));
                };};    
        final static CompetitionOptions STARTER_VS_INDEPENDENT_GHOSTS80_800 = new CompetitionOptions("StarterPacMan", 40, "IndependentGhostAgents(80,0.5)", 800) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {
                    return new DistributedMCTSController<IndependentGhostAgent, DummyMessage>(1, true)
                                .addGhostAgent(new IndependentGhostAgent(GHOST.BLINKY, 80, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.PINKY, 80, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.INKY, 80, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.SUE, 80, 0.5));
                };};    
        final static CompetitionOptions STARTER_VS_INDEPENDENT_GHOSTS200_800 = new CompetitionOptions("StarterPacMan", 40, "IndependentGhostAgents(80,0.5)", 800) {                
                @Override public Controller<MOVE> pacmanController() {return new StarterPacMan();}
                @Override public Controller<EnumMap<GHOST, MOVE>> ghostController() {
                    return new DistributedMCTSController<IndependentGhostAgent, DummyMessage>(1, true)
                                .addGhostAgent(new IndependentGhostAgent(GHOST.BLINKY, 200, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.PINKY, 200, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.INKY, 200, 0.5))
                                .addGhostAgent(new IndependentGhostAgent(GHOST.SUE, 200, 0.5));
                };};    
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args)	{
            List<CompetitionOptions> options_list = new ArrayList<CompetitionOptions>();
//            options_list.add(STARTER_VS_LEGACY);
//            options_list.add(STARTER_VS_LEGACY2THERECKONING);
//            options_list.add(STARTER_VS_MCTS80);
//            options_list.add(STARTER_VS_MCTS80_200);
//            options_list.add(STARTER_VS_MCTS80_800);
//            options_list.add(STARTER_VS_MCTS200_800);
//            options_list.add(MCTS120_800_VS_MCTS200_800);
            options_list.add(STARTER_VS_INDEPENDENT_GHOSTS80_200);
            options_list.add(STARTER_VS_INDEPENDENT_GHOSTS80_800);
            options_list.add(STARTER_VS_INDEPENDENT_GHOSTS200_800);            
            options_list.add(STARTER_VS_INDEPENDENT_GHOSTS80_200);
            options_list.add(STARTER_VS_INDEPENDENT_GHOSTS80_800);
            options_list.add(STARTER_VS_INDEPENDENT_GHOSTS200_800);
            
            runCompetition(options_list, 10, true, true);
	}
	
    /**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public void runExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
    	double avgScore=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			avgScore+=game.getScore();
			System.out.println(i+"\t"+game.getScore());
		}
		
		System.out.println(avgScore/trials);
    }
	
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 */
	public Game runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay, boolean dispose_view)
	{
		Game game=new Game(0);

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame("runGame");
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
                if (dispose_view) {
                    gv.getFrame().dispose();
                }
                return game;
        }
        
        
     public Game runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual, boolean dispose_view) {
         return runGameTimed(pacManController, ghostController, visual, dispose_view, DELAY, DELAY);
     }
	
	/**
     * Run the game with time limit (asynchronous mode). This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
     */
    public Game runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual, boolean dispose_view, int pacman_delay, int ghosts_delay)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+pacman_delay);
			ghostController.update(game.copy(),System.currentTimeMillis()+ghosts_delay);

			try
			{
				Thread.sleep(Math.max(pacman_delay, ghosts_delay));
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

                    game.advanceGame(pacManController.getMove(),ghostController.getMove());	   

                    if(visual)
                            gv.repaint();
		}
                
                if (visual&&dispose_view) {
                    gv.getFrame().dispose();
                }
		
		pacManController.terminate();
		ghostController.terminate();
                return game;
	}

    public Game runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual)
    {
        return runGameTimedSpeedOptimised(pacManController, ghostController, fixedTime, visual, DELAY);
    }
    /**
     * Run the game in asynchronous mode but proceed as soon as both controllers replied. The time limit still applies so 
     * so the game will proceed after 40ms regardless of whether the controllers managed to calculate a turn.
     *     
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param fixedTime Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual Indicates whether or not to use visuals
     */
    public Game runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual, int delay)
 	{
 		Game game=new Game(0);
 		
 		GameView gv=null;
 		
 		if(visual)
 			gv=new GameView(game).showGame();
 		
 		if(pacManController instanceof HumanController)
 			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
 				
 		new Thread(pacManController).start();
 		new Thread(ghostController).start();
 		
 		while(!game.gameOver())
 		{
 			pacManController.update(game.copy(),System.currentTimeMillis()+delay);
 			ghostController.update(game.copy(),System.currentTimeMillis()+delay);

 			try
			{
				int waited=delay/INTERVAL_WAIT;
				
				for(int j=0;j<delay/INTERVAL_WAIT;j++)
				{
					Thread.sleep(INTERVAL_WAIT);
					
					if(pacManController.hasComputed() && ghostController.hasComputed())
					{
						waited=j;
						break;
					}
				}
				
				if(fixedTime)
					Thread.sleep(((delay/INTERVAL_WAIT)-waited)*INTERVAL_WAIT);
				
				game.advanceGame(pacManController.getMove(),ghostController.getMove());	
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
 	        
                        if(visual)
                                gv.repaint();
 		}
 		
 		pacManController.terminate();
 		ghostController.terminate();
                return game;
 	}
    
    
    
    public Game runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,boolean dispose_view, String fileName) {
        return runGameTimedRecorded(pacManController, ghostController, visual, dispose_view, fileName, DELAY, DELAY);
    }
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public Game runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,boolean dispose_view, String fileName, int pacman_delay, int ghosts_delay)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof HumanController)
				gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
		}		
		
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+pacman_delay);
			ghostController.update(game.copy(),System.currentTimeMillis()+ghosts_delay);

			try
			{
				Thread.sleep(Math.max(pacman_delay, ghosts_delay));
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        replay.append(game.getGameState()+"\n");
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		saveToFile(replay.toString(),fileName,false);
                if (visual&&dispose_view) {
                    gv.getFrame().dispose();
                }
                return game;
	}
	
	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName,boolean visual)
	{
		ArrayList<String> timeSteps=loadReplay(fileName);
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		for(int j=0;j<timeSteps.size();j++)
		{			
			game.setGameState(timeSteps.get(j));

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	        if(visual)
	        	gv.repaint();
		}
	}
	
	//save file for replays
    public static void saveToFile(String data,String name,boolean append)
    {
        try 
        {
            FileOutputStream outS=new FileOutputStream(name,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }  

    //load a replay
    private static ArrayList<String> loadReplay(String fileName)
	{
    	ArrayList<String> replay=new ArrayList<String>();
		
        try
        {         	
        	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));	 
            String input=br.readLine();		
            
            while(input!=null)
            {
            	if(!input.equals(""))
            		replay.add(input);

            	input=br.readLine();	
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        return replay;
	}
}