package exec.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import static pacman.game.Constants.*;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.FullGame;
import pacman.game.Game;
import pacman.game.GameView;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
public class Executor
{
    public static void warmUp() {
        long start_time = System.currentTimeMillis();

        while (System.currentTimeMillis()-start_time<50000) {
            for (int i=0; i<1000000; i++) {}
        }
    }



//       static boolean verbose = true;
//       public static void runCompetition(List<CompetitionOptions> options_list, int trials, boolean visual, boolean recorded, String path) {
//            try {
//                File dir = new File(path);
//                try {
//                    new File(path).mkdirs();
//                } catch (SecurityException ex) {
//                    System.err.printf("Cannot create folder %s, operation not permitted\n", path);
//                    return;
//                }
//                Executor exec = new Executor();
//                String date_string = new SimpleDateFormat("yyMMdd-hhmmss").format(new Date());
//                PrintWriter writer = new PrintWriter(String.format(path+"results-%s.txt", date_string));                ;
//                int replay_number = 0;
//                for (CompetitionOptions options: options_list) {
//                    int pacman_delay = options.pacmanDelay();
//                    int ghosts_delay = options.ghostsDelay();
//                    long total_score = 0;
//                    long min_score = Long.MAX_VALUE;
//                    long max_score = 0;
//                    String competition_id = String.format("%s\t%s\t%s\t%s", options.pacmanName(), pacman_delay, options.ghostName(), ghosts_delay);
//                    Game game;
//                    for (int i=1; i<=trials; i++) {
//                        if (recorded) {
//                            String replay_name = String.format(path+"%s-%s-%s_%s-%s_%s.replay", date_string, replay_number++, options.pacmanName(), pacman_delay, options.ghostName(), ghosts_delay);
//                            game = exec.runGameTimedRecorded(new Game(0), options.pacmanController(), options.ghostController(), visual, true, replay_name, pacman_delay, ghosts_delay);
//                        } else {
//                            game = exec.runGameTimed(options.pacmanController(), options.ghostController(), visual, true, pacman_delay, ghosts_delay);
//                        }
//                        String result_string = String.format("%s\t%s\t%s\n", i, competition_id, game.getScore());
//                        writer.write(result_string);
//                        writer.flush();
//                        System.out.print(result_string);
//                        total_score+=game.getScore();
//                        min_score = Math.min(min_score, game.getScore());
//                        max_score = Math.max(max_score, game.getScore());
//                    }
//                    String min_string = String.format("MIN\t%s\t%s\n", competition_id, min_score);
//                    String max_string = String.format("MAX\t%s\t%s\n", competition_id, max_score);
//                    String avg_string = String.format("AVG\t%s\t%s\n", competition_id, total_score/(double)trials);
//                    String stats_string = String.format("%s%s%s", min_string, max_string, avg_string);
//
//                    writer.write(stats_string);
//                    writer.flush();
//                    System.out.print(stats_string);
//                }
//                writer.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }

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
//    public void runExperiment(Controller<MOVE> pacManController, Controller<EnumMap<GHOST,MOVE>> ghostController, int trials)
//    {
//    	double avgScore=0;
//
//    	Random rnd=new Random(0);
//		Game game;
//
//		for(int i=0;i<trials;i++)
//		{
//			game=new Game(rnd.nextLong());
//
//			while(!game.gameOver())
//			{
//		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
//		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
//			}
//
//			avgScore+=game.getScore();
//			System.out.println(i+"\t"+game.getScore());
//		}
//
//		System.out.println(avgScore/trials);
//    }

        public Game runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int pacman_delay,int ghost_delay, boolean dispose_view){
            return runGame(new FullGame(0), pacManController, ghostController, visual, pacman_delay, ghost_delay, dispose_view);
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
	public Game runGame(Game game, Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int pacman_delay,int ghost_delay, boolean dispose_view)
	{

		GameView gv=null;

		if(visual)
			gv=new GameView(game).showGame("runGame");

		while(!game.gameOver()) {
                    long start_time = System.currentTimeMillis();
                    MOVE pacman_move = pacManController.getMove(game, System.currentTimeMillis()+pacman_delay);
                    EnumMap<GHOST,MOVE> ghost_move = ghostController.getMove(game, System.currentTimeMillis()+ghost_delay);

                    game.advanceGame(pacman_move, ghost_move);
//                    game.advanceGame(pacManController.getMove(game.copy(),),ghostController.getMove(game.copy(),-1));

//	        try{Thread.sleep(delay);}catch(Exception e){}

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
		Game game=new FullGame(0);

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
 		Game game=new FullGame(0);

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



    public Game runGameTimedRecorded(Game game, Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,boolean dispose_view, String fileName) {
        return runGameTimedRecorded(game, pacManController, ghostController, visual, dispose_view, fileName, DELAY, DELAY);
    }
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public Game runGameTimedRecorded(Game game, Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,boolean dispose_view, String fileName, int pacman_delay, int ghosts_delay)
	{
		StringBuilder replay=new StringBuilder();

		//Game game=new Game(0);

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

		Game game=new FullGame(0);

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