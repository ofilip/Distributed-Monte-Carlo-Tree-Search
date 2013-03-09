package pacman;

import communication.messages.Message;
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
import mcts.distributed.agents.DummyGhostAgent;
import mcts.distributed.agents.JointActionExchangingAgent;
import mcts.distributed.controller_generators.DummyGhostsGenerator;
import mcts.distributed.controller_generators.JointActionExchangingGhostsGenerator;
import mcts.distributed.controller_generators.RootExchangingGhostsGenerator;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.*;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.ghosts.MCTSGhosts;
import pacman.entries.pacman.*;
import pacman.entries.pacman.generators.StarterPacManGenerator;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;
import utils.VerboseLevel;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class MyExecutor
{
    
       static boolean verbose = true;
       public static void runCompetition(List<CompetitionOptions> options_list, int trials, boolean visual, boolean recorded) {
            try {
                MyExecutor exec = new MyExecutor();
                String date_string = new SimpleDateFormat("yyMMdd-hhmmss").format(new Date());
                PrintWriter writer = new PrintWriter(String.format("d:\\pacman_results\\results-%s.txt", date_string));                ;
                int replay_number = 0;
                for (CompetitionOptions options: options_list) {
                    int pacman_delay = options.pacmanDelay();
                    int ghosts_delay = options.ghostsDelay();
                    long total_score = 0;
                    long min_score = Long.MAX_VALUE;
                    long max_score = 0;
                    String competition_id = String.format("d:\\pacman_results\\%s\t%s\t%s\t%s", options.pacmanName(), pacman_delay, options.ghostName(), ghosts_delay);
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
               
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args)	{
//            MyExecutor exec = new MyExecutor();
//            GhostControllerGenerator gen = new RootExchangingGhostsGenerator(200, 0.7, 10000, VerboseLevel.QUIET);
            //GhostControllerGenerator gen = new JointActionExchangingGhostsGenerator(200, 0.7, 10000, 10, VerboseLevel.DEBUGGING);
            //exec.runGameTimed(new StarterPacMan(), gen.ghostController(), true, true, 40, 1000);
            //exec.runGameTimed(new StarterPacMan(), new MCTSGhosts(200, 0.7, true), true, true, 40, 200);
            final int simulation_depth = 120;
            final double ucb_coef = 0.7;
            final long channel_transmission_speed = 10000;
            GhostControllerGenerator gen_dummy = new DummyGhostsGenerator(simulation_depth, ucb_coef);
            GhostControllerGenerator gen_action_exchange = new JointActionExchangingGhostsGenerator(simulation_depth, ucb_coef, channel_transmission_speed, 5);
            GhostControllerGenerator gen_root_exchange = new RootExchangingGhostsGenerator(simulation_depth, ucb_coef, channel_transmission_speed);
            
            List<CompetitionOptions> options_list = new ArrayList<CompetitionOptions>();
            
            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_dummy, 400));
            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_action_exchange, 400));
            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_root_exchange, 400));
            
            
            
//            for (int ghosts_simulation_depth: new int[]{200}) {
//                for (int ghosts_delay: new int[]{80, 200}) {
//                    for (PacmanControllerGenerator pacman_generator: new PacmanControllerGenerator[]{new StarterPacManGenerator()}) {
//                        options_list.add(new CompetitionOptions(pacman_generator, 40, 
//                                            new DummyGhostsGenerator(ghosts_simulation_depth, 0.7), ghosts_delay));
//                        options_list.add(new CompetitionOptions(pacman_generator, 40, 
//                                            new JointActionExchangingGhostsGenerator(ghosts_simulation_depth, 0.7, 1), ghosts_delay));
//                    }
//                }
//                
//            }
            
            runCompetition(options_list, 10, false, false);
            runCompetition(options_list, 10, false, false);
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
    public void runExperiment(Controller<MOVE> pacManController, Controller<EnumMap<GHOST,MOVE>> ghostController, int trials)
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