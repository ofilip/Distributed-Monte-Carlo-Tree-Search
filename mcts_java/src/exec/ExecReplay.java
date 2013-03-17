package exec;

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
@SuppressWarnings("unused")
public class ExecReplay {
	public static void main(String[] args)	{
            if (args.length!=1) {
                System.out.printf("Usage: %s <replay_file>\n", ExecReplay.class.getSimpleName());
                System.exit(1);
            }
            
            ExecReplay exec = new ExecReplay();
            
            exec.replayGame(args[0], true);
	}

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