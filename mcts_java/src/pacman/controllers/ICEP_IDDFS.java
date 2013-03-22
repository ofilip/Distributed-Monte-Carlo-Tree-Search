package pacman.controllers;


import pacman.controllers.Controller;
import pacman.game.Game;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import static pacman.game.Constants.*;

import java.awt.Color;
import pacman.controllers.Controller;
import pacman.game.GameView;

public class ICEP_IDDFS extends Controller<MOVE>
{
	Game 	game;

	boolean POWER_PILL_SWITCH = true;
	boolean STATIC_PILL_SWitcH = true;
	boolean PILL_SWITCH = true;
	boolean SEPARATE_GHOST_SWITCH = true;
	boolean REVERSAL_SWITCH = true;
	boolean SYSTEM_MESSAGE = false;
	boolean DIFFICULT_PASSAGE_SWITCH = true;
	int IDDFS_IV_MIN = 70;
	int IDDFS_LV_MIN = 70;
	int POWER_PILL_LIMIT_MIN = 20;



	int IDDFS_IV_MAX = 80;
	int IDDFS_LV_MAX = 300;
	int POWER_PILL_LIMIT_MAX = 100;
	int IDDFS_IV;
	int IDDFS_LV;
	int POWER_PILL_LIMIT_DISTANCE;


	int distc;
	int pcutoff = 1000;
	int timecutoff = 35;

	int preDir=-1;
	int preDist=pcutoff;
	int preTime=0;



	int switchflg=0;
	int nearestDist=1000;



	int curpDist;
	int countt;
	int thcurpNode;
	int[][] node;
	int[][] dist;
	int[][] dir;
	int[] junctionList;
	int junctionNum;
	int gdir;
	int blockNode;
	int[] pac_dist;

	int[] junctionSet;
	int[][][] threshold;
	int[][] pth;
	int[] max_dist;
	int hk;
	int cut;
	int uk;

	int setPill;
	int[][] pill;
	int[] visited;
	int ub;

	int pcount;
	int last_node1;
	int last_node2;
	int last_dir1;
	int last_dir2;
	int last_dist1;
	int last_dist2;

	int search_last_dist;
	boolean separate;


	boolean chance_PP;
	boolean edible_PP;

	long startTime;
	long endTime;

	private MOVE d(int d){
		if(d==0)return MOVE.UP;
		else if(d==1)return MOVE.RIGHT;
		else if(d==2)return MOVE.DOWN;
		else return MOVE.LEFT;
	}

	private int d(MOVE d){
		if(d==MOVE.UP)return 0;
		else if(d==MOVE.RIGHT)return 1;
		else if(d==MOVE.DOWN)return 2;
		else return 3;
	}

	private GHOST g(int g){
		if(g==0)return GHOST.BLINKY;
		else if(g==1)return GHOST.PINKY;
		else if(g==2)return GHOST.INKY;
		else return GHOST.SUE;
	}

	private MOVE getReverse(MOVE move){
		if(move==MOVE.UP)return MOVE.DOWN;
		else if(move==MOVE.DOWN)return MOVE.UP;
		else if(move==MOVE.LEFT)return MOVE.RIGHT;
		else return MOVE.LEFT;
	}

	private int getReverse(int move){
		if(move == 0)return 2;
		else if(move == 1)return 3;
		else if(move == 2)return 0;
		else return 1;
	}


	public MOVE getMove(Game game, long timeDue)
	{
		startTime = java.lang.System.nanoTime();

		this.game = game;

		int pacLoc = game.getPacmanCurrentNodeIndex();
		int count=0;
		boolean[] flg = new boolean[4];
		boolean thr = false;
		boolean ci = false;
		junctionSet = new int[game.getNumberOfNodes()];
		max_dist = new int[5];

		if(game.getCurrentLevel()%4!=2){
			POWER_PILL_LIMIT_DISTANCE = POWER_PILL_LIMIT_MIN;
			IDDFS_LV = IDDFS_LV_MIN;
			IDDFS_IV = IDDFS_IV_MIN;
		}
		else if(game.getCurrentLevel()%4==2/* || (game.getCurrentLevel()<2 && game.getPacmanNumberOfLivesRemaining()==0)*/){
			POWER_PILL_LIMIT_DISTANCE = POWER_PILL_LIMIT_MAX;
		//	IDDFS_LV = IDDFS_LV_MAX;
		//	IDDFS_IV = IDDFS_IV_MAX;
		}

		int plus = 0;
		for(int g=0;g<4;g++){
			flg[g] = false;
			thr = false;
			if(game.getGhostLairTime(g(g))==0 && !game.isJunction(game.getGhostCurrentNodeIndex(g(g)))){
				for(int p:game.getActivePowerPillsIndices()){
					if(p==game.getGhostCurrentNodeIndex(g(g))){
						thr = true;
						break;
					}
				}
				for(int gg=0;gg<g;gg++){
					if(game.getGhostCurrentNodeIndex(g(g))==game.getGhostCurrentNodeIndex(g(gg))){
						thr = true;
						break;
					}
				}
				if(!thr){
					flg[g] = true;
					count++;
				}
			}
			if(game.getGhostLairTime(g(g))>0){
				ci = true;
			}
		}
		int[] ghostLoc = new int[count];
		int gc = 0;
		for(int g=0;g<4;g++)
			if(flg[g]){
				ghostLoc[gc] = game.getGhostCurrentNodeIndex(g(g));
				flg[gc] = true;
				gc++;
			}
		if(!game.isJunction(pacLoc)){
			plus++;
		}
		if(ci){
			for(int g=0;g<4;g++){
				if(game.getGhostCurrentNodeIndex(g(g)) == game.getGhostInitialNodeIndex()){
					ci = false;
					break;
				}
			}
			if(ci)
				plus++;
		}

		int[] powerLoc = game.getActivePowerPillsIndices();

		junctionList = new int[game.getJunctionIndices().length+count+powerLoc.length + plus];

		for(int i=0;i<game.getJunctionIndices().length;i++){
			junctionList[i] = game.getJunctionIndices()[i];
		}
		if(ci){
			for(int i=0;i<junctionList.length;i++)
				if(game.getGhostInitialNodeIndex() < junctionList[i] || junctionList[i]==0){
					for(int j=junctionList.length-1;j>i;j--){
						junctionList[j] = junctionList[j-1];
					}
					junctionList[i] = game.getGhostInitialNodeIndex();
					break;
				}
		}
		if(!game.isJunction(pacLoc))
			for(int i=0;i<junctionList.length;i++)
				if(pacLoc < junctionList[i] || junctionList[i]==0){
					for(int j=junctionList.length-1;j>i;j--){
						junctionList[j] = junctionList[j-1];
					}
					junctionList[i] = pacLoc;
					break;
				}

		for(int g=0;g<gc;g++){
			for(int i=0;i<junctionList.length;i++){
				if(flg[g] && (ghostLoc[g] < junctionList[i] || junctionList[i]==0)){
					for(int j=junctionList.length-1;j>i;j--){
						junctionList[j] = junctionList[j-1];
					}
					junctionList[i] = ghostLoc[g];
					break;
				}
			}
		}
		for(int p=0; p<game.getActivePowerPillsIndices().length; p++){
			for(int i=0;i<junctionList.length;i++){
				if(powerLoc[p] < junctionList[i] || junctionList[i]==0){
					for(int j=junctionList.length-1;j>i;j--){
						junctionList[j] = junctionList[j-1];
					}
					junctionList[i] = powerLoc[p];
					break;
				}
			}
		}

		junctionNum = junctionList.length;
		threshold = new int[junctionNum][6][junctionNum];
		pth = new int[junctionNum][junctionNum];
		node = new int[junctionNum][4];
		dist = new int[junctionNum][4];
		dir = new int[junctionNum][4];
		pill = new int[junctionNum][4];

		pillDist = new int[4];

		for(int i=0;i<junctionNum;i++){
			junctionSet[junctionList[i]] = i;
			for(int d=0;d<4;d++){
				node[i][d] = -1;
			}
		}

		createNode();
		createThreshold();

		/*
		for(int i=0;i<junctionNum;i++){
			for(int d=0;d<4;d++){
				if(node[i][d]!=-1 && dist[i][d]==0)System.out.println("!");
			}
		}*/

		pcount = 0;
		int node_i=-1;
		int dir_d=-1;
		for(int i=0;i<junctionNum;i++){
			for(int d=0;d<4;d++){
				if(node[i][d]!=-1){

					int p = game.getPowerPillIndex(junctionList[i]);
					int p2 = game.getPowerPillIndex(junctionList[node[i][d]]);
					boolean jn = game.isJunction(junctionList[node[i][d]]);
					if(jn && pill[i][d]>0 && !(p != -1 && game.isPowerPillStillAvailable(p)) && !(p2 != -1 && game.isPowerPillStillAvailable(p2))){
						pcount++;
						node_i = node[i][d];
						dir_d = getReverse(dir[i][d]);
					}
				}
			}
		}
		pcount++;
		pcount /= 2;

		last_node1 = -1;
		last_dir1 = -1;

		last_node2 = -1;
		last_dir2 = -1;

		last_dist1 = pcutoff;
		last_dist2 = pcutoff;
		//System.out.println(pcount);
		if(pcount==1){
			last_node1 = node_i;
			last_dir1 = dir_d;

			int nn = game.getNeighbour(junctionList[node_i], d(dir_d));
			/*
			int dir_f = -1;
			for(int d=0;d<4;d++){
				if(game.getNeighbour(nn, d(d))!=-1 && getReverse(d)!=dir_d){
					dir_f = d;
				}
			}
			*/


			distc = 1;
			last_node2 = junctionSet[nextJunction(nn,1,dir_d)];
			last_dir2 = getReverse(gdir);
			int rdist = distc;

			setPill = 0;
			int p = game.getPillIndex(nn);
			if(p!=-1 && game.isPillStillAvailable(p)){
				setPill = 1;
			}
			nn = game.getNeighbour(junctionList[last_node2], d(last_dir2));
			npNextJunction(nn,1,last_dir2);


			last_dist1 = rdist - setPill;
			last_dist2 = rdist - pill[node_i][dir_d];
			/*
			GameView.addPoints(game, new Color(0.75f, 0, 0), junctionList[last_node1]);
			GameView.addPoints(game, new Color(0.75f, 0, 0), junctionList[last_node2]);
			*/
		}
		int fd = 0;
		int c=0;
		int[] gn = new int[2];
		boolean pc = false;
		if(!game.isJunction(game.getPacmanCurrentNodeIndex())){
			for(int d=0;d<4;d++){
				if(game.getNeighbour(game.getPacmanCurrentNodeIndex(), d(d))!=-1){
					distc = 0;
					int pn = pNextJunction(game.getNeighbour(game.getPacmanCurrentNodeIndex(),d(d)),0,d);
					fd += distc;
					gn[c] = junctionSet[pn];
					c++;
					if(pill[junctionSet[game.getPacmanCurrentNodeIndex()]][d]>0){
						pc = true;
					}
				}
			}
		}
		/*
		else {
			for(int d=0;d<4;d++){
				if(game.getNeighbour(game.getPacmanCurrentNodeIndex(), d(d))!=-1){
					if(pill[junctionSet[game.getPacmanCurrentNodeIndex()]][d]>0)pc = true;
				}
			}
		}*/

		/*
		if(pth[gn[0]][gn[1]] < fd && pc){
			chance_PP = false;
		}*/
		/*
		if(pc && game.getCurrentLevel()%4 != 2){
			chance_PP = false;
		}
		 */
		if((pth[gn[0]][gn[1]] < fd) && pc && game.getCurrentLevel()%4 != 2){
			chance_PP = false;
		}
		else if(game.isJunction(game.getPacmanCurrentNodeIndex()) && game.getCurrentLevel()%4 != 2){
			for(int d=0;d<4;d++){
				int nn = game.getNeighbour(game.getPacmanCurrentNodeIndex(), d(d));
				if(nn != -1){
					distc = 1;
					int pn = nextJunction(nn,1,d);
					if(distc > pth[junctionSet[game.getPacmanCurrentNodeIndex()]][junctionSet[pn]] &&
							pill[junctionSet[game.getPacmanCurrentNodeIndex()]][d]>0){
						chance_PP = false;
						break;
					}
					else chance_PP = true;
				}
			}
		}
		else chance_PP = true;

		//chance_PP = true;

		/*
		if(((game.getCurrentLevel()==0 && game.getPacmanNumberOfLivesRemaining()==1 && pth[gn[0]][gn[1]] < fd)
				|| (pth[gn[0]][gn[1]] + 30 < fd && game.getCurrentLevel()%4 == 0)) && pc && DIFFICULT_PASSAGE_SWITCH){
			edible_PP = false;
		}*/

		if(pth[gn[0]][gn[1]] + 30 < fd && game.getCurrentLevel()%4 == 0 && pc && DIFFICULT_PASSAGE_SWITCH){
			edible_PP = false;
		}
		/*
		if(pcount>1 && pth[gn[0]][gn[1]] + 30 < fd && game.getCurrentLevel()%4 == 0 && pc && DIFFICULT_PASSAGE_SWITCH){
			edible_PP = false;
		}*/
		else edible_PP = true;


		if(!SEPARATE_GHOST_SWITCH || (pcount<=1 && game.getActivePowerPillsIndices().length==0)){
			separate = false;
		}
		else {
			separate = true;
		}


		int dir;
		countt=0;
		dir = simulate();

		endTime = java.lang.System.nanoTime();
		preDir = dir;
		return d(dir);
	}


	private int simulate(){


		if(game.getCurrentLevelTime()==0){
			switchflg = 0;
			nearestDist = pcutoff;
		}

		if(game.wasPowerPillEaten() || game.getTimeOfLastGlobalReversal()>=game.getTotalTime()-40){
		//	System.out.println(game.getTimeOfLastGlobalReversal());
			switchflg = 0;
			nearestDist = pcutoff;
		}

		int pacNode;
		int[] ghostNode = new int[4];
		int[] ghostNextNode = new int[4];
		int[] ghostDir = new int[4];
		int[] ghostDist = new int[4];
		int direc=-1;

		int[] edibleTime = new int[4];
		pacNode = junctionSet[game.getPacmanCurrentNodeIndex()];
		int[] pdist = new int[4];

		for(int g=0; g<4; g++){
			edibleTime[g] = game.getGhostEdibleTime(g(g));
			if(game.getGhostLairTime(g(g))>0){
				ghostNode[g] = junctionSet[game.getGhostInitialNodeIndex()];
				ghostDist[g] = game.getGhostLairTime(g(g));
				ghostDir[g] = 5;
				ghostNextNode[g] = ghostNode[g];
			}
			else if(!game.isJunction(game.getGhostCurrentNodeIndex(g(g)))){
				ghostNode[g] = junctionSet[game.getGhostCurrentNodeIndex(g(g))];
				for(int d=0;d<4;d++){
					if(d!=d(getReverse(game.getGhostLastMoveMade(g(g)))) && node[ghostNode[g]][d]!=-1){
						ghostNextNode[g] = node[ghostNode[g]][d];
						ghostDist[g] = dist[ghostNode[g]][d];
						ghostDir[g] = dir[ghostNode[g]][d];
					}
				}
			}
			else {
				ghostNode[g] = junctionSet[game.getGhostCurrentNodeIndex(g(g))];
				ghostNextNode[g] = ghostNode[g];
				ghostDist[g] = 0;
				ghostDir[g] = d(game.getGhostLastMoveMade(g(g)));
			}
		}

		int[] pac_future = new int[4];
		int[] save_num = new int[4];
		int[] pac_dist = new int[4];
		int[][] reachable = new int[4][junctionNum];
		boolean[] powerP = new boolean[4];
		int[] needNum = new int[4];

		int[] tt=new int[4];
		boolean[] nRev = new boolean[4];
		int[] min_sg = new int[4];
		boolean spflg = false;

		int ngd=1000;
		for(int g=0;g<4;g++){
			if(!game.isGhostEdible(g(g)) && game.getGhostLairTime(g(g)) == 0 && ngd > threshold[ghostNode[g]][d(game.getGhostLastMoveMade(g(g)))][pacNode])ngd = threshold[ghostNode[g]][d(game.getGhostLastMoveMade(g(g)))][pacNode];
		}


		for(int d=0; d<4; d++){
			if(node[pacNode][d]!=-1){
				pac_future[d] = node[pacNode][d];
				pac_dist[d] = dist[pacNode][d];
			}
			else continue;

			boolean[] srnk = new boolean[4];
			boolean flggg = true;
			for(int g=0;g<4;g++){
				if(pac_future[d]==ghostNode[g]){
					if((pacNode == ghostNextNode[g] || (pac_future[d] == ghostNextNode[g] && dir[pacNode][d] != ghostDir[g]) ) && (pac_dist[d]*2/3 - game.getGhostEdibleTime(g(g))>=EAT_DISTANCE-2))flggg = false;
					if(game.getGhostEdibleTime(g(g))==0 && (pac_future[d] == ghostNextNode[g] && dir[pacNode][d] == ghostDir[g])){
						srnk[g] = true;
					}
				}
			}

			boolean flg = true;
			for(int g=0;g<4;g++){
				int p2dist = pac_dist[d];
				int g2dist = threshold[ghostNextNode[g]][ghostDir[g]][pac_future[d]]+ghostDist[g];
				if(!srnk[g] && ( ((g2dist+edibleTime[g]/2) - p2dist <= EAT_DISTANCE && g2dist > p2dist/2) ||
							((g2dist + p2dist)*2/3 >= edibleTime[g] && g2dist <= p2dist/2) ) ){
					flg = false;
					break;
				}
			}

			hk = d;
			max_dist[hk] = pac_dist[d];
			if(max_dist[d]>88)max_dist[d] /= 16;

			if(flg && flggg){
				reachable[d][pac_future[d]] = pac_dist[d];
				curpDist = 0;
				thcurpNode = pacNode;

				thwalk(pac_future[d],dir[pacNode][d],reachable[d],pac_dist[d],ghostNode, ghostNextNode,ghostDir,ghostDist,edibleTime,1,srnk,pacNode,pac_future[d]);

				if(reachable[d][pacNode]>0)nRev[d] = true;
				else nRev[d] = false;
			}
			save_num[d]=0;

			pdist[d] = 1000;
			for(int i=0; i<junctionNum; i++){
				if(reachable[d][i]>0){
					save_num[d]++;
					int pp = game.getPowerPillIndex(junctionList[i]);
					if (pp != -1 && game.isPowerPillStillAvailable(pp)){
						if(pdist[d]>reachable[d][i])pdist[d] = reachable[d][i];
						powerP[d] = true;
					}
				}
			}
			if(save_num[d]<=0) save_num[d] = 1;
			tt[d] = save_num[d];
		//	save_num[d] *= max_dist[d];


			if(/*preDir != getReverse(d) && */STATIC_PILL_SWitcH/* && game.getCurrentLevel()%4 != 2 && !(game.getCurrentLevel()==0 && game.getPacmanNumberOfLivesRemaining()!=2)*/){
				int pp = game.getPowerPillIndex(junctionList[pac_future[d]]);
				if(pill[pacNode][d]>0 && !(pp!=-1 && game.isPowerPillStillAvailable(pp))){
					needNum[d] = pill[pacNode][d];
				}
				else {
					ub = 350;
					visited = new int[junctionNum];
					pillWalk(node[pacNode][d],dist[pacNode][d],reachable[d]);
					needNum[d] = ub;
				}
				/*if(needNum[d]>IDDFS_IV)*/
				save_num[d] += (350-needNum[d])*50;
			}


			min_sg[d] = pcutoff;
			if(/*preDir != getReverse(d) && */separate){
				int min = 1000;
				for(int g=0;g<4;g++){
					if(min > threshold[ghostNode[g]][d(game.getGhostLastMoveMade(g(g)))][pac_future[d]] - pac_dist[d])
						min = threshold[ghostNode[g]][d(game.getGhostLastMoveMade(g(g)))][pac_future[d]] - pac_dist[d];
				}
			//	if(min <= IDDFS_IV)
				min_sg[d] = min;
				if(min <= 30)spflg = true;
			//	save_num[d] += 100000*min;
			}

			//if(preDir == d)save_num[d] += 10000000;
		}
		if(spflg)
			for(int d=0;d<4;d++)
				save_num[d] += 100000*min_sg[d];
		//else System.out.println("spflg");


		int[] aNode = new int[5];
		int[] aNextNode = new int[5];
		int[] aDir = new int[5];
		int[] distList = new int[5];
		int[] distType = new int[5];
		boolean fitch = false;
		int[] aDist = new int[5];
		int[] rDist = new int[5];


		int[] edip = new int[4];

		for(int d=0;d<4;d++){
			aNode[4] = pacNode;
			aNextNode[4] = pac_future[d];
			aDir[4] = dir[pacNode][d];
			distList[4] = pac_dist[d];
			distType[4] = 4;
			aDist[4] = distList[4];

			for(int g=0; g<4; g++){
				aNode[g] = ghostNode[g];
				aNextNode[g] = ghostNextNode[g];
				aDir[g] = ghostDir[g];
				distList[g] = ghostDist[g];
				distType[g] = g;
				aDist[g] = distList[g];
			}
			edip[d] = 1000;
			if(node[aNode[4]][d]!=-1)edip[d] = eat(aNode, aNextNode, aDir, aDist, reachable[d]);
		}
		int min=1000;
		int did=-1;
		for(int d=0;d<4;d++){
			if(edip[d]==1000 /*|| edip[d]==0*/)continue;
			if(min > edip[d]){
				min = edip[d];
				did = d;
			}
		}
		if(did!=-1 && edible_PP){
			return did;
		}


		int[] sup = new int[4];
		int temp;
		for(int i=0; i<4; i++)
			sup[i] = i;
		for(int d=0; d<4; d++){
			for(int e=d+1; e<4; e++){
				if(save_num[d]>save_num[e]){
					temp = save_num[d];
					save_num[d] = save_num[e];
					save_num[e] = temp;

					temp = sup[d];
					sup[d] = sup[e];
					sup[e] = temp;
				}
			}
		}


		boolean checkLair=false;
		boolean checkEdible = false;
		for(int g=0;g<4;g++){
			if(game.getGhostEdibleTime(g(g))>0)checkEdible=true;
			if(game.getGhostLairTime(g(g))>0)checkLair=true;
		}
		boolean ppk = false;
		for(int d=0;d<4;d++){
			if(powerP[d]){
				ppk = true;
				break;
			}
		}
		boolean powerPill = false;
		int levelEdibleTime = (int)(EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel())));
		if(ppk && !checkLair && !checkEdible){
			powerPill = powerPillEat();
			if(LEVEL_LIMIT - ((levelEdibleTime+200)*game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime())powerPill = true;
		}

		boolean[] route = new boolean[4];
		for(int d=0;d<4;d++){
			if(save_num[d]!=0){
				route[sup[d]] = true;
				save_num[d] = tt[sup[d]];
			}
		}




		if(!game.isJunction(pacNode)){
			if(powerPill && switchflg == 0 && nearestDist > EAT_DISTANCE + 2){
				switchflg = 1;
			}
			int minf = pcutoff;
			int saveGnum = -1;
			for(int g=0;g<4;g++){
				if(minf > threshold[ghostNode[g]][d(game.getGhostLastMoveMade(g(g)))][pacNode]){
					minf = threshold[ghostNode[g]][d(game.getGhostLastMoveMade(g(g)))][pacNode];
					saveGnum = g;
				}
			}
			if(minf > nearestDist && switchflg == 1){
				switchflg = 2;
			}
			else {
				nearestDist = minf;
			}

			int[] reachClose = new int[junctionNum];
			boolean[] srnkClose = new boolean[4];
			boolean isReach = false;
			if(switchflg == 1 && nearestDist > EAT_DISTANCE + 2){
			//if(powerPill && nearestDist > EAT_DISTANCE + 2){
				thwalk_getClose(pacNode,-1,reachClose,2,ghostNode, ghostNextNode,ghostDir,ghostDist,edibleTime,srnkClose);
				for(int i : game.getActivePowerPillsIndices()){
					if(reachClose[junctionSet[i]]>0){
						isReach = true;
						break;
					}
				}

				boolean ok = true;
				if(isReach){
					minf = pcutoff;
					int saveDir=-1;
					for(int d=0;d<4;d++){
						if(node[pacNode][d]!=-1){
							if(minf > threshold[node[pacNode][d]][d][ghostNode[saveGnum]]){
								minf = threshold[node[pacNode][d]][d][ghostNode[saveGnum]];
								saveDir = d;
							}
							int p = game.getPowerPillIndex(junctionList[node[pacNode][d]]);
							if(p!=-1 && game.isPowerPillStillAvailable(p))ok = false;
						}

					}
					if(ok)return saveDir;
				}
			}
		}
	//	System.out.println(switchflg);




		if(powerPill){
			int minp = 10000000;
			int num = 10;
			for(int d=0;d<4;d++){
				if(powerP[d]){
					if(minp > pdist[d]){
						minp = pdist[d];
						num = d;
					}
				}
			}
			if(num!=10 && POWER_PILL_SWITCH && chance_PP)return num;
		}

		int co=0;
		int cutoff=0;
		int ub = 30;
		boolean[] sPD = new boolean[4];

		for(int d=0;d<4;d++){
			pillDist[sup[d]] = pcutoff;
			if(pill[pacNode][sup[d]]>0){
				pillDist[sup[d]] = pill[pacNode][sup[d]]-pac_dist[sup[d]];
				sPD[sup[d]] = true;
				if(reachable[sup[d]][pac_future[sup[d]]]==0)pillDist[sup[d]]+=20;
			}
		}

		for(int turn=0;turn<ub;turn++){
			//for(int d=3; d>=0; d--){
			for(int d=0; d<4; d++){
				if(save_num[d]!=0 && route[sup[d]]){
					for(int g=0; g<4; g++){
						aNode[g] = ghostNode[g];
						aNextNode[g] = ghostNextNode[g];
						aDir[g] = ghostDir[g];
						distList[g] = ghostDist[g];
						distType[g] = g;
						aDist[g] = distList[g];
						rDist[g] = aDist[g];

						int ext;
						if(aDist[g]<edibleTime[g]/2)ext = aDist[g];
						ext = edibleTime[g]/2;
						aDist[g]+=ext;
						distList[g]+=ext;
					}

					uk = sup[d];
					/*
					pillDist[sup[d]] = pcutoff;
					if(pill[pacNode][sup[d]]>0){
						pillDist[sup[d]] = pill[pacNode][sup[d]];
					}*/


					aNode[4] = pacNode;
					aNextNode[4] = pac_future[sup[d]];
					aDir[4] = dir[pacNode][sup[d]];
					distList[4] = pac_dist[sup[d]];
					distType[4] = 4;
					aDist[4] = distList[4];
					rDist[4] = aDist[4];


					int ppp = game.getPowerPillIndex(junctionList[aNextNode[4]]);
					if (/*rDist[4] < POWER_PILL_LIMIT_DISTANCE &&*/ ppp != -1 && game.isPowerPillStillAvailable(ppp)){
						route[sup[d]]=false;
						continue;
					}

					for(int f=0; f<5; f++){
						for(int e=f+1; e<5; e++){
							if(distList[f]>distList[e] || (distList[f]==distList[e] && distType[e]==4)){
								temp = distList[f];
								distList[f] = distList[e];
								distList[e] = temp;

								temp = distType[f];
								distType[f] = distType[e];
								distType[e] = temp;
							}
						}
					}


					if(game.getCurrentLevel()%4!=2){
						POWER_PILL_LIMIT_DISTANCE = POWER_PILL_LIMIT_MIN;
						IDDFS_LV = IDDFS_LV_MIN;
					}
					else {
						POWER_PILL_LIMIT_DISTANCE = POWER_PILL_LIMIT_MAX;
						IDDFS_LV = IDDFS_LV_MAX;
					}
					/*
					if(needNum[sup[d]] > IDDFS_LV)
						POWER_PILL_LIMIT_DISTANCE = needNum[sup[d]];
					*/

					hk = 4;
					cutoff = IDDFS_IV + turn*20;
					cut = save_num[d];
					lpf = false;

					//search_last_node
					if(pcount==1){
						if(aNode[4]==last_node1 && sup[d] == last_dir1){
							search_last_dist = last_dist1;
							lpf = true;
						}
						else if(aNode[4]==last_node2 && sup[d] == last_dir2){
							search_last_dist = last_dist2;
							lpf = true;
						}
					}
					revflg = -1;
					fitch = dfs(aNode, aNextNode, aDir, aDist, rDist, edibleTime, save_num[d], distList, distType, pac_dist[sup[d]], ngd, true, cutoff);
					if(fitch){
						direc = sup[d];
					}
					else route[sup[d]]=false;
				}
				endTime = java.lang.System.nanoTime();
				if((endTime-startTime) / 1000000 >= timecutoff)break;

				/*
				if(needNum[sup[d]] > IDDFS_LV){
					//System.out.println("t");
					IDDFS_LV = needNum[sup[d]];
				}*/
				/*
				if(pillDist[uk]==1000){
					System.out.println("t");
					IDDFS_LV += 20;
				}*/
				if(needNum[sup[d]] > IDDFS_LV){
					//System.out.println("t");
					IDDFS_LV = needNum[sup[d]];
				}
			}
			/*
			if(game.getCurrentLevel()>3){
				for(int i=0;i<4;i++){
					if(pillDist[sup[i]]<1000 && save_num[i]!=0 && route[sup[i]]){
						IDDFS_LV-=20;
					//	System.out.println("t");
						break;
					}
				}
				IDDFS_LV+=20;
			}*/

			boolean lv_flg = true;
			for(int d=0;d<4;d++){
				if(pillDist[sup[d]]<1000 && route[sup[d]]){
					lv_flg = false;
					break;
				}
			}
			if(lv_flg){
			//	IDDFS_LV+=20;
			//	System.out.println("tt");
			}

			co=0;
			int sep=-1;
			for(int i=0;i<4;i++){
				if(route[i]){
					sep = i;
					co++;
				}
			}

			if(co==1){
				direc = sep;
				break;
			}
			else if(co==0)break;
			endTime = java.lang.System.nanoTime();
			if((endTime-startTime) / 1000000 >= timecutoff || cutoff >= IDDFS_LV){
		//	if((endTime-startTime) / 1000000 >= timecutoff && cutoff == IDDFS_IV_MIN && game.getCurrentLevelTime()>100)System.out.println(cutoff);
		//		if(cutoff==IDDFS_IV)System.out.println("not enogh time!");
			//	if(cutoff==IDDFS_IP)System.out.println("80");
				int[] psup = new int[4];
				for(int d=0;d<4;d++){
					psup[d] = d;
				}
				for(int i=0;i<4;i++){
					for(int j=i+1;j<4;j++){
						if(pillDist[i] > pillDist[j]){
							int tempe = pillDist[i];
							pillDist[i] = pillDist[j];
							pillDist[j] = tempe;

							tempe = psup[i];
							psup[i] = psup[j];
							psup[j] = tempe;

						}
						/*
						else if(pillDist[i] == pillDist[j]){
							if(psup[d])
						}*/
					}
				}
				if(pillDist[0]<1000 && PILL_SWITCH){
					for(int d=0;d<4;d++){
						if(route[psup[d]]){
							//System.out.println(psup[d]);
							return psup[d];
						}
					}
				}
				else {
					for(int d=3;d>=0;d--){
						if(route[sup[d]]){
							return sup[d];
						}
					}
				}
				/*
				for(int d=3;d>=0;d--){
					if(route[sup[d]])return sup[d];
				}
				*/
			}

		}
		for(int d=0;d<4;d++){
			if(powerP[d])max_dist[d] += 200000;
		}

		if(co==0){
			int mx=0;
			int save_dd=0;
			for(int i=0;i<4;i++){
				if(max_dist[i]>mx && max_dist[i]!=1000){
					mx = max_dist[i];
					save_dd = i;
				}
			}
			direc = save_dd;
		}

		return direc;
	}

	private boolean powerPillEat(){
		int cutoff=4;

		if(((LEVEL_LIMIT/6)*(7-game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime())  || (pcount <= 1))
			cutoff = 1;
		else if(((LEVEL_LIMIT/6)*(6-game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime()) ||
				(LEVEL_LIMIT/6)*(6-game.getActivePowerPillsIndices().length) <= game.getTotalTime()+LEVEL_LIMIT - MAX_TIME ||
				(pcount <= game.getActivePowerPillsIndices().length+2 && game.getCurrentLevel()%4!=2) ||
				(game.getPacmanNumberOfLivesRemaining()<=2 && game.getCurrentLevel()==0))
				//(game.getPacmanNumberOfLivesRemaining()==1 && game.getCurrentLevel()<2))
			cutoff = 2;
		else if(((LEVEL_LIMIT/6)*(5-game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime()) ||
				(LEVEL_LIMIT/6)*(5-game.getActivePowerPillsIndices().length) <= game.getTotalTime()+LEVEL_LIMIT - MAX_TIME ||
				(pcount <= game.getActivePowerPillsIndices().length+8 && game.getCurrentLevel()%4!=2) ||
				(game.getPacmanNumberOfLivesRemaining()==1 && game.getCurrentLevel()<2))
			cutoff = 3;

		int[] p2gdist = new int[4];
		int[] g2jdist = new int[4];
		int[] reverseNode = new int[4];
		int[] sup = new int[4];

		boolean[] check = new boolean[4];
		int cNode = game.getPacmanCurrentNodeIndex();


		for(int g=0;g<4;g++){
			distc = 0;
			reverseNode[g] = junctionSet[nextJunction(game.getGhostCurrentNodeIndex(g(g)),0,d(getReverse(game.getGhostLastMoveMade(g(g)))))];
			g2jdist[g] = distc;
		//	sup[g] = g;
		}
		for(int gg=0;gg<4;gg++){
			for(int g=0;g<4;g++){
				if(!check[g])p2gdist[g] = pth[junctionSet[cNode]][junctionSet[game.getGhostCurrentNodeIndex(g(g))]];
				else p2gdist[g] = pcutoff;
			}
			int min = pcutoff;
			int saveG = -1;
			for(int g=0;g<4;g++){
				if(min > p2gdist[g]){
					min = p2gdist[g];
					saveG = g;
				}
			}
			check[saveG] = true;
			sup[gg] = saveG;
			cNode = game.getGhostCurrentNodeIndex(g(saveG));
		}


		int totalDist=0;
		int curNode = junctionSet[game.getPacmanCurrentNodeIndex()];
		for(int g=0;g<cutoff;g++){
			boolean jcheck = false;
			if(totalDist + pth[curNode][reverseNode[sup[g]]] - 2*g2jdist[sup[g]] <= EAT_DISTANCE){
				jcheck = true;
			}
			else {
				jcheck = false;
			}

			if(jcheck){
				totalDist += pth[curNode][junctionSet[game.getGhostCurrentNodeIndex(g(sup[g]))]];
				curNode = junctionSet[game.getGhostCurrentNodeIndex(g(sup[g]))];
			}
			else {
				int extra = (totalDist + pth[curNode][reverseNode[sup[g]]] - 2*g2jdist[sup[g]] - EAT_DISTANCE);
				if((g<cutoff-1 && reverseNode[sup[g]] != reverseNode[sup[g+1]]) || g == cutoff-1){
					totalDist += pth[curNode][reverseNode[sup[g]]] + 2*extra - EAT_DISTANCE;
					curNode = reverseNode[sup[g]];
				}
				else {
					totalDist += extra - EAT_DISTANCE;
				}
			}
		}
		int edibleTime = (int)(EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel()%LEVEL_RESET_REDUCTION)));
		if(totalDist*2/3 - edibleTime <= EAT_DISTANCE){
			return true;
		}

		return false;
	}


	/*
	private boolean powerPillEat(){
		int cutoff=4;

		if(((LEVEL_LIMIT/6)*(7-game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime())  || (pcount <= 1))
			cutoff = 1;
		else if(((LEVEL_LIMIT/6)*(6-game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime()) ||
				(LEVEL_LIMIT/6)*(6-game.getActivePowerPillsIndices().length) <= game.getTotalTime()+LEVEL_LIMIT - MAX_TIME || (pcount <= game.getActivePowerPillsIndices().length+2) ||
				(game.getPacmanNumberOfLivesRemaining()==1 && game.getCurrentLevel()<2 ))
			cutoff = 2;
		else if(((LEVEL_LIMIT/6)*(5-game.getActivePowerPillsIndices().length) <= game.getCurrentLevelTime()) ||
				(LEVEL_LIMIT/6)*(5-game.getActivePowerPillsIndices().length) <= game.getTotalTime()+LEVEL_LIMIT - MAX_TIME || (pcount <= game.getActivePowerPillsIndices().length+15))
			cutoff = 3;

		int[] p2gdist = new int[4];
		int[] g2jdist = new int[4];
		int[] reverseNode = new int[4];
		int[] sup = new int[4];

		boolean[] check = new boolean[4];
		int cNode = game.getPacmanCurrentNodeIndex();


		for(int g=0;g<4;g++){
			distc = 0;
			reverseNode[g] = junctionSet[nextJunction(game.getGhostCurrentNodeIndex(g(g)),0,d(getReverse(game.getGhostLastMoveMade(g(g)))))];
			g2jdist[g] = distc;
		//	sup[g] = g;
		}
		for(int gg=0;gg<4;gg++){
			for(int g=0;g<4;g++){
				if(!check[g])p2gdist[g] = pth[junctionSet[cNode]][junctionSet[game.getGhostCurrentNodeIndex(g(g))]];
				else p2gdist[g] = pcutoff;
			}
			int min = pcutoff;
			int saveG = -1;
			for(int g=0;g<4;g++){
				if(min > p2gdist[g]){
					min = p2gdist[g];
					saveG = g;
				}
			}
			check[saveG] = true;
			sup[gg] = saveG;
			cNode = game.getGhostCurrentNodeIndex(g(saveG));
		}


		int totalDist=0;
		int curNode = junctionSet[game.getPacmanCurrentNodeIndex()];
		for(int g=0;g<cutoff;g++){
			boolean jcheck = false;
			if(totalDist + pth[curNode][reverseNode[sup[g]]] - 2*g2jdist[sup[g]] <= EAT_DISTANCE){
				jcheck = true;
			}
			else {
				jcheck = false;
			}

			if(jcheck){
				totalDist += pth[curNode][junctionSet[game.getGhostCurrentNodeIndex(g(sup[g]))]];
				curNode = junctionSet[game.getGhostCurrentNodeIndex(g(sup[g]))];
			}
			else {
				int extra = (totalDist + pth[curNode][reverseNode[sup[g]]] - 2*g2jdist[sup[g]] - EAT_DISTANCE);
				if((g<cutoff-1 && reverseNode[sup[g]] != reverseNode[sup[g+1]]) || g == cutoff-1){
					totalDist += pth[curNode][reverseNode[sup[g]]] + 2*extra - EAT_DISTANCE;
					curNode = reverseNode[sup[g]];
				}
				else {
					totalDist += extra - EAT_DISTANCE;
				}
			}
		}
		int edibleTime = (int)(EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel()%LEVEL_RESET_REDUCTION)));
		if(totalDist*2/3 - edibleTime <= EAT_DISTANCE){
			return true;
		}

		return false;
	}*/


	private int eat(int[] aNode, int[] aNextNode, int[] aDir, int[] aDist, int[] reachable){
		int[] gdl = new int[4];
		boolean[] gflg = new boolean[4];
		int[] minf = new int[4];
		int[] minb = new int[4];
		for(int g=0;g<4;g++){
			gdl[g] = 1000;
			if(game.getGhostEdibleTime(g(g))>0/* && reachable[aNode[g]]>0*/){
				int fjunction,bjunction;
				fjunction = pNextJunction(game.getGhostCurrentNodeIndex(g(g)), 0, d(game.getGhostLastMoveMade(g(g))));
				bjunction = pNextJunction(game.getGhostCurrentNodeIndex(g(g)), 0, d(getReverse(game.getGhostLastMoveMade(g(g)))));
				for(int gg=0;gg<4;gg++){
					if(g!=gg && !game.isGhostEdible(g(gg)) && aNextNode[4] == aNode[g]){
						if(game.getGhostLairTime(g(gg))==0){
							minb[gg] = threshold[aNode[gg]][d(game.getGhostLastMoveMade(g(gg)))][junctionSet[bjunction]];
							minf[gg] = threshold[aNode[gg]][d(game.getGhostLastMoveMade(g(gg)))][junctionSet[fjunction]];
						}
						else {
							int ij = junctionSet[game.getGhostInitialNodeIndex()];
							minb[gg] = threshold[ij][5][junctionSet[bjunction]]+game.getGhostLairTime(g(gg));
							minf[gg] = threshold[ij][5][junctionSet[fjunction]]+game.getGhostLairTime(g(gg));
						}
					}
				}
				int p = game.getPowerPillIndex(junctionList[aNextNode[g]]);
				int dp = game.getPowerPillIndex(node[aNode[g]][getReverse(d(game.getGhostLastMoveMade(g(g))))]);
				int pdist=0;

				if(aNextNode[g] == aNode[4] && aNextNode[4] == aNode[g]){
					pdist = aDist[4]*2/3;
				}

				else if(aNextNode[4] == aNode[g] && ( p==-1 || (p!= -1 && !game.isPowerPillStillAvailable(p)) ||
						(p!= -1 && game.isPowerPillStillAvailable(p) && aDist[4] - aDist[g] + 1 <= EAT_DISTANCE))){
					pdist = (aDist[4]-EAT_DISTANCE)*2;
				}
				else if(aNextNode[4] == aNode[g] && (p!= -1 && game.isPowerPillStillAvailable(p) && aDist[4] - aDist[g] + 1 > EAT_DISTANCE)){
					pdist = 200;
				}
				else {
					int edist = aDist[g]*2;
					pdist = pth[aNextNode[4]][aNextNode[g]]+aDist[4];
					if(edist-pdist>0) pdist = (pdist - EAT_DISTANCE)*2;
					else pdist = pdist + (pdist-edist- EAT_DISTANCE)*2;

					if((pth[aNextNode[4]][aNode[g]]+aDist[4]-EAT_DISTANCE)*2 < pdist){
						pdist = (pth[aNextNode[4]][aNode[g]]+aDist[4]-EAT_DISTANCE)*2;
					}
				}

				if(pdist!=0){

					for(int gg=0;gg<4;gg++){
						for(int ggg=0;ggg<4;ggg++){
							if(!game.isGhostEdible(g(gg)) && !game.isGhostEdible(g(ggg)) && gg != ggg && aNextNode[g] == aNode[4] && minf[gg] - (2*pdist+pth[junctionSet[game.getPacmanCurrentNodeIndex()]][junctionSet[fjunction]]) <= EAT_DISTANCE &&
								(minb[ggg] - (pth[aNode[4]][aNode[g]]+pth[aNode[g]][junctionSet[bjunction]]) <= EAT_DISTANCE+1 || (dp!= -1 && game.isPowerPillStillAvailable(dp)))
									&& aNextNode[4] == aNode[g] && !game.isJunction(game.getGhostCurrentNodeIndex(g(g)))){
								gflg[g] = true;
								break;
							}
							else if(!game.isGhostEdible(g(gg)) && !game.isGhostEdible(g(ggg)) && gg != ggg && aNextNode[g] != aNode[4] && minb[gg] - (2*pdist+pth[junctionSet[game.getPacmanCurrentNodeIndex()]][junctionSet[bjunction]]) <= EAT_DISTANCE &&
								(minf[ggg] - (pth[aNode[4]][aNode[g]]+pth[aNode[g]][junctionSet[fjunction]]) <= EAT_DISTANCE+1 || (p!= -1 && game.isPowerPillStillAvailable(p)))
									&& aNextNode[4] == aNode[g] && !game.isJunction(game.getGhostCurrentNodeIndex(g(g)))){
								gflg[g] = true;
								break;
							}
						}
					}
				}
			}
		}

		for(int g=0;g<4;g++){
			if(game.getGhostEdibleTime(g(g))>0 && !gflg[g] && reachable[aNode[g]]>0){
				int pdist=0;
				int p = game.getPowerPillIndex(junctionList[aNextNode[g]]);

				if(aNextNode[g] == aNode[4] && aNextNode[4] == aNode[g]){
					pdist = aDist[4]*2/3;
				}

				else if(aNextNode[4] == aNode[g] && ( p==-1 || (p!= -1 && !game.isPowerPillStillAvailable(p)) ||
						(p!= -1 && game.isPowerPillStillAvailable(p) && aDist[4] - aDist[g] + 1 <= EAT_DISTANCE))){
					pdist = (aDist[4] - EAT_DISTANCE)*2;
				}
				else if(aNextNode[4] == aNode[g] && (p!= -1 && game.isPowerPillStillAvailable(p) && aDist[4] - aDist[g] + 1 > EAT_DISTANCE)){
					pdist = 200;
				}

				else {
					int fdist = pth[aNextNode[4]][aNextNode[g]]+aDist[4];
					//int gdist = aDist[g];
					int gdist = aDist[g]*2;
					//pdist = (fdist+gdist)*2/3;
					if(gdist - fdist > 0)pdist = fdist + (gdist - fdist)/3;
					else pdist = gdist + (fdist - gdist - EAT_DISTANCE)*2;

					if((pth[aNextNode[4]][aNode[g]]+aDist[4]-EAT_DISTANCE)*2 < pdist){
						pdist = (pth[aNextNode[4]][aNode[g]]+aDist[4]-EAT_DISTANCE)*2;
					}
				}

				if(pdist  /*- EAT_DISTANCE*/ <= game.getGhostEdibleTime(g(g))){
					gdl[g] = pdist;
				}
				else
					gdl[g] = 1000;
			}
			else gdl[g]=1000;
		}
		int temp;
		int[] sup = new int[4];
		for(int g=0;g<4;g++)sup[g] = g;
		for(int i=0;i<4;i++){
			for(int j=i+1;j<4;j++){
				if(gdl[i] > gdl[j]){
					temp =gdl[i];
					gdl[i] = gdl[j];
					gdl[j] = temp;

					temp = sup[i];
					sup[i] = sup[j];
					sup[j] = temp;
				}
			}
		}
		if(gdl[0]!=1000)return gdl[0];
		else return 1000;
	}


	private void thwalk(int curNode, int pacDir, int[] reachable, int distance, int[] ghostNode, int[] ghostNextNode, int[] ghostDir, int[] ghostDist, int[] edibleTime,
			int count, boolean[] wat, int cpn, int cpnn){
		int p = game.getPowerPillIndex(junctionList[curNode]);
		if(p==-1 || !game.isPowerPillStillAvailable(p))
		for(int d=0;d<4;d++){
			int nextNode = node[curNode][d];
			int p2dist;

			boolean watres = false;
			if(curNode == cpnn && nextNode == cpn){
				p2dist = curpDist + 2;
				watres = true;
			}
			else p2dist = distance+dist[curNode][d];
			if(nextNode!=-1 && (reachable[nextNode]==0 || reachable[nextNode] > p2dist)){
				boolean flg= false;
				boolean[] cwat = new boolean[4];
				for(int g=0;g<4;g++){
					if(!watres)cwat[g] = wat[g];

					int g2dist = threshold[ghostNextNode[g]][ghostDir[g]][nextNode]+ghostDist[g];
					if((curNode == ghostNode[g] || (nextNode == ghostNode[g] && dir[curNode][d] == ghostDir[g])) && (nextNode==ghostNextNode[g])){
						cwat[g] = true;
					}
					if((nextNode == ghostNode[g]) && (curNode == ghostNextNode[g]) && p2dist*2/3 - edibleTime[g]>=EAT_DISTANCE-2){
						flg = true;
						break;
					}
					else if((!cwat[g] || edibleTime[g]>0) && ((((g2dist +edibleTime[g]/2) - p2dist <= EAT_DISTANCE) && g2dist > p2dist/2)||
								((g2dist + p2dist)*2/3 >= edibleTime[g]) && g2dist <= p2dist/2)){
						flg = true;
						break;
					}
				}
				int ghostDd = 0;
				for(int g=0;g<4;g++){
					if(!((nextNode == ghostNode[g]) && (curNode == ghostNextNode[g])))ghostDd += ghostDist[g]+threshold[ghostNextNode[g]][ghostDir[g]][nextNode];
				}
				if(max_dist[hk] < ghostDd+6*distance){
					max_dist[hk] = ghostDd+6*distance;
				}
				if(!flg){
					if(reachable[nextNode]==0)countt++;

					reachable[nextNode] = p2dist;
					thwalk(nextNode, dir[curNode][d], reachable, p2dist, ghostNode, ghostNextNode, ghostDir, ghostDist, edibleTime, count+1, cwat, cpn, cpnn);
				}
			}
		}
	}


	private void thwalk_search(int curNode, int pacDir, int[] reachable, int distance, int[] ghostNode, int[] ghostNextNode, int[] ghostDir, int[] ghostDist, int[] edibleTime,
			int count, boolean[] wat, int cpn, int cpnn){
		int p = game.getPowerPillIndex(junctionList[curNode]);
		if(p==-1 || !game.isPowerPillStillAvailable(p))
		for(int d=0;d<4;d++){
			int nextNode = node[curNode][d];
			int p2dist;

			boolean watres = false;
			if(curNode == cpnn && nextNode == cpn){
				p2dist = curpDist + 2;
				watres = true;
			}
			else p2dist = distance+dist[curNode][d];
			if(nextNode!=-1 && (reachable[nextNode]==0 || reachable[nextNode] > p2dist)){
				boolean flg= false;
				boolean[] cwat = new boolean[4];
				for(int g=0;g<4;g++){
					if(!watres)cwat[g] = wat[g];

					int g2dist = threshold[ghostNextNode[g]][ghostDir[g]][nextNode]+ghostDist[g];
					if((curNode == ghostNode[g] || (nextNode == ghostNode[g] && dir[curNode][d] == ghostDir[g])) && (nextNode==ghostNextNode[g])){
						cwat[g] = true;
					}
					if((nextNode == ghostNode[g]) && (curNode == ghostNextNode[g]) && p2dist*2/3 - edibleTime[g]>=EAT_DISTANCE-2){
						flg = true;
						break;
					}
					else if((!cwat[g] || edibleTime[g]>0) && ((((g2dist +edibleTime[g]/2) - p2dist <= EAT_DISTANCE) && g2dist > p2dist/2)||
								((g2dist + p2dist)*2/3 >= edibleTime[g]) && g2dist <= p2dist/2)){
						flg = true;
						break;
					}
				}
				if(!flg){
					if(reachable[nextNode]==0)countt++;

					reachable[nextNode] = p2dist;
					thwalk_search(nextNode, dir[curNode][d], reachable, p2dist, ghostNode, ghostNextNode, ghostDir, ghostDist, edibleTime, count+1, cwat, cpn, cpnn);
				}
			}
		}
	}



	private void thwalk_getClose(int curNode, int pacDir, int[] reachable, int distance, int[] ghostNode, int[] ghostNextNode, int[] ghostDir, int[] ghostDist, int[] edibleTime,
			boolean[] wat){
		int p = game.getPowerPillIndex(junctionList[curNode]);
		if(p==-1 || !game.isPowerPillStillAvailable(p))
		for(int d=0;d<4;d++){
			int nextNode = node[curNode][d];
			int p2dist;

			p2dist = distance+dist[curNode][d];
			if(nextNode!=-1 && (reachable[nextNode]==0 || reachable[nextNode] > p2dist)){
				boolean flg= false;
				boolean[] cwat = new boolean[4];
				for(int g=0;g<4;g++){
					cwat[g] = wat[g];

					int g2dist = threshold[ghostNextNode[g]][ghostDir[g]][nextNode]+ghostDist[g];
					if((curNode == ghostNode[g] || (nextNode == ghostNode[g] && dir[curNode][d] == ghostDir[g])) && (nextNode==ghostNextNode[g])){
						cwat[g] = true;
					}
					if((nextNode == ghostNode[g]) && (curNode == ghostNextNode[g]) && p2dist*2/3 - edibleTime[g]>=EAT_DISTANCE-2){
						flg = true;
						break;
					}
					else if((!cwat[g] || edibleTime[g]>0) && ((((g2dist +edibleTime[g]/2) - p2dist <= EAT_DISTANCE) && g2dist > p2dist/2)||
								((g2dist + p2dist)*2/3 >= edibleTime[g]) && g2dist <= p2dist/2)){
						flg = true;
						break;
					}
				}
				if(!flg){

					reachable[nextNode] = p2dist;
					thwalk_getClose(nextNode, dir[curNode][d], reachable, p2dist, ghostNode, ghostNextNode, ghostDir, ghostDist, edibleTime, cwat);
				}
			}
		}
	}


	int[] pillDist;
	int revflg;


	boolean lpf;

	private boolean dfs(int[] aNode, int[] aNextNode, int[] aDir, int[] aDist, int[] rDist, int[] edibleTime,
			int pThreshold, int[] distList, int[] distType, int distance, int ngd, boolean rev, int cutoff){

		boolean fitch = false;


		if(distance-aDist[4] > cutoff){
			return true;
		}


		int[] cNode = new int[5];
		int[] cNextNode = new int[5];
		int[] cDir = new int[5];
		int[] cDist = new int[5];
		int[] cDistList = new int[5];
		int[] cDistType = new int[5];
		int[] crDist = new int[5];

		int[] copyDistList = new int[5];
		int[] copyDistType = new int[5];

		int[] cEdibleTime = new int[4];

		for(int i=0;i<5;i++){
			cNode[i] = aNode[i];
			cNextNode[i] = aNextNode[i];
			cDir[i] = aDir[i];
			cDistList[i] = distList[i];
			cDistType[i] = distType[i];
			cDist[i] = aDist[i];
			crDist[i] = rDist[i];
		}

		for(int i=1; i<5; i++){
			cDistList[i] -= cDistList[0];
			cDist[i] -= cDistList[0];
			cEdibleTime[i-1] = edibleTime[i-1] - cDistList[0];
			if(cEdibleTime[i-1]<0)cEdibleTime[i-1]=0;
		}
		cDist[0]-=cDistList[0];
		cDistList[0] = 0;


		if(pcount<=1 && lpf && game.getActivePowerPillsIndices().length==0 && rev){
			if(search_last_dist <= rDist[4] - cDist[4]){
			//	System.out.println("t");
				return true;
			}
			else {
				for(int g=0;g<4;g++){
					if(rDist[4] == rDist[g] && aNextNode[g] == aNode[4] && aNextNode[4] == aNode[g] && cEdibleTime[g]==0){
						if(rDist[4] - aDist[4] + aDist[4]/2 - EAT_DISTANCE >= search_last_dist){
							return true;
						}
					}
				}
			}
		}


		for(int g=0; g<4; g++){
			if(rDist[4] == rDist[g] && aNextNode[g] == aNode[4] && aNextNode[4] == aNode[g] && cEdibleTime[g]==0){
				if(rev)revflg = g;
				return false;
			}

			if(game.getCurrentLevel()%4!=2){
				if(aNextNode[4] == aNextNode[g] && cDist[4]+cDist[g]<=EAT_DISTANCE && cEdibleTime[g] == 0){
					return false;
				}
			}
			else {
				if(aNextNode[4] == aNextNode[g] && cDist[g]-cDist[4]<=EAT_DISTANCE && cEdibleTime[g] == 0){
					return false;
				}
			}
			/*
			if(aNextNode[4] == aNextNode[g] && edibleTime[g] == 0){
				int gdist = cDist[g];
				int pdist = cDist[4];
				if(aDir[4] != aDir[g]){
					int min = 1000;
					for(int gg=0;gg<4;gg++){
						if(g != gg){
							if(min > cDist[gg]+threshold[aNextNode[gg]][aDir[gg]][aNode[4]])
								min = cDist[gg]+threshold[aNextNode[gg]][aDir[gg]][aNode[4]];
						}
					}

					if(gdist - pdist <= EAT_DISTANCE && rDist[4] - cDist[4] + EAT_DISTANCE >= min){
						if(rev)revflg = true;
						return false;
					}
				}
				if(gdist+pdist <= EAT_DISTANCE){
					if(rev)revflg = true;
					return false;
				}
			}*/

		}



		//revflg = false;
		int min=1000;

		int[] mnd = new int[4];
		for(int g=0;g<4;g++){
			if(cNode[4] == cNode[g] && cNextNode[4] == cNextNode[g]){
				if(cDist[4] < cDist[g]){
					mnd[g] = cDist[g] - cDist[4];
				}
				else{
					mnd[g] = cDist[g]+threshold[cNextNode[g]][cDir[g]][cNode[4]]+rDist[4]-cDist[4];
				}
			}
			else {
				int nnd = cDist[g]+threshold[cNextNode[g]][cDir[g]][cNextNode[4]]+cDist[4];
				int nd = cDist[g]+threshold[cNextNode[g]][cDir[g]][cNode[4]]+rDist[4]-cDist[4];
				if(nnd < nd)mnd[g] = nnd;
				else mnd[g] = nd;
			}
		}

		for(int g=0;g<4;g++){
			if(min > mnd[g] && game.getGhostEdibleTime(g(g)) == 0 && game.getGhostLairTime(g(g))==0){
				min = mnd[g];
			}
		}
		if(min-6 > ngd && rev){
			return true;
		}


		if(distType[0] == 4){
			int ppo = game.getPowerPillIndex(junctionList[aNextNode[4]]);
			if(ppo!=-1 && game.isPowerPillStillAvailable(ppo) && distance >= POWER_PILL_LIMIT_DISTANCE){
				return true;
			}
			int[] safe_count = new int[4];
			for(int d=0;d<4;d++){

				countt=0;
				if(node[aNextNode[4]][d]!=-1){
					int pp = game.getPowerPillIndex(junctionList[node[aNextNode[4]][d]]);
					if(pp!=-1 && game.isPowerPillStillAvailable(pp) && distance < POWER_PILL_LIMIT_DISTANCE)continue;

					boolean gfl = false;
					for(int g=0;g<4;g++){
						if(junctionList[aNextNode[4]] == game.getGhostCurrentNodeIndex(g(g)) && !game.isJunction(game.getGhostCurrentNodeIndex(g(g))) && d==getReverse(aDir[4]))gfl = true;
					}
					if(junctionList[aNextNode[4]] == game.getPacmanCurrentNodeIndex() && !game.isJunction(game.getPacmanCurrentNodeIndex()) && d==getReverse(aDir[4]))continue;

					if(gfl)continue;

					int[] reachable = new int[junctionNum];

					int[] decr = new int[4];
					boolean[] srnk = new boolean[4];
					boolean flggg = true;
					for(int g=0;g<4;g++){
						if(node[aNextNode[4]][d]==aNode[g]){
							if((aNextNode[4] == aNextNode[g] ) && (dist[aNextNode[4]][d]*2/3 - cEdibleTime[g]>=EAT_DISTANCE-2))flggg = false;
						}
						if(cEdibleTime[g]==0 && node[aNextNode[4]][d] == aNextNode[g] && dir[aNextNode[4]][d] == aDir[g] && cDist[g] < dist[aNextNode[4]][d]){
							srnk[g] = true;
						}
					}

					boolean flg = true;
					for(int g=0;g<4;g++){
						if(cEdibleTime[g]>cDist[g])decr[g] = cDist[g]/2;
						else decr[g] = cEdibleTime[g]/2;
						int p2dist = dist[aNextNode[4]][d];
						int g2dist = threshold[aNextNode[g]][aDir[g]][node[aNextNode[4]][d]]+cDist[g]-decr[g];
						if(!srnk[g] &&  ( ( ((g2dist+cEdibleTime[g]/2) - p2dist <= EAT_DISTANCE && g2dist > p2dist/2) ||
										((g2dist + p2dist)*2/3 >= cEdibleTime[g]) && g2dist <= p2dist/2) ||
											(aNode[g] == node[aNextNode[4]][d] && aNextNode[g]==aNextNode[4])  ) ){
							flg = false;
							break;
						}
					}

					countt=1;
					if(flggg && flg){
						reachable[node[aNextNode[4]][d]] = dist[aNextNode[4]][d];
						int max = 0;
						for(int g=0;g<4;g++)cDist[g] -= decr[g];
						curpDist = 0;
						thcurpNode = aNextNode[4];
						thwalk_search(node[aNextNode[4]][d],dir[aNextNode[4]][d],reachable,dist[aNextNode[4]][d],aNode,aNextNode,aDir,cDist,cEdibleTime,0,srnk,aNextNode[4],node[aNextNode[4]][d]);
						for(int g=0;g<4;g++)cDist[g] += decr[g];

						for(int i=0;i<junctionNum;i++){
							if(max < reachable[i])max = reachable[i];
						}
						if(max + distance - cDist[4]>cutoff){
							return true;
						}
						/*
						if(reachable[aNextNode[4]]>0)ccRev[d] = true;
						else ccRev[d] = false;*/
					}
					safe_count[d] = countt;
					if(safe_count[d]<=0) safe_count[d] = 1;
				}
			}

			int[] sup = new int[4];
			for(int i=0;i<4;i++){
				sup[i] = i;
			}
			for(int d=0; d<4; d++){
				for(int e=d+1;e<4;e++){
					if(safe_count[d] < safe_count[e]){
						int temp = safe_count[d];
						safe_count[d] = safe_count[e];
						safe_count[e] = temp;

						temp = sup[d];
						sup[d] = sup[e];
						sup[e] = temp;
					}
				}
			}

			for(int d=0; d<4; d++){
				if(node[aNextNode[4]][sup[d]]!=-1){
					int pp = game.getPowerPillIndex(junctionList[node[aNextNode[4]][sup[d]]]);
					if(pp!=-1 && game.isPowerPillStillAvailable(pp) && distance < POWER_PILL_LIMIT_DISTANCE)continue;

					boolean gfl = false;
					for(int g=0;g<4;g++){
						if(junctionList[aNextNode[4]] == game.getGhostCurrentNodeIndex(g(g)) && !game.isJunction(game.getGhostCurrentNodeIndex(g(g))) && sup[d]==getReverse(aDir[4]))gfl = true;
					}
					if(junctionList[aNextNode[4]] == game.getPacmanCurrentNodeIndex() && !game.isJunction(game.getPacmanCurrentNodeIndex()) && sup[d]==getReverse(aDir[4]))continue;

					if(gfl)continue;
					if(safe_count[d]-6 > pThreshold || safe_count[d]-6 > cut){
						return true;
					}
					cNode[4] = aNextNode[4];
					cNextNode[4] = node[cNode[4]][sup[d]];
					cDir[4] = dir[cNode[4]][sup[d]];

					int nextDist = dist[cNode[4]][sup[d]];

					cDist[4] = nextDist;
					for(int i=0;i<5;i++){
						copyDistList[i] = cDistList[i];
						copyDistType[i] = cDistType[i];
					}


					for(int i=4; i>=0; i--){
						if(copyDistList[i] < nextDist){
							for(int j=0; j<i; j++){
								copyDistList[j] = copyDistList[j+1];
								copyDistType[j] = copyDistType[j+1];
							}
							copyDistList[i] = nextDist;
							copyDistType[i] = 4;
							break;
						}
					}
					crDist[4] = cDist[4];


					if(pill[cNode[4]][sup[d]] > 0 && pill[cNode[4]][sup[d]] + distance < pillDist[uk]){
						pillDist[uk] = pill[cNode[4]][sup[d]] + distance/* - cDist[4]*/;
					}

					boolean lflg = false;
					if(pcount==1){
						if(cNode[4]==last_node1 && sup[d] == last_dir1){
							lpf = true;
							lflg = true;
							search_last_dist = last_dist1;
						}
						else if(cNode[4]==last_node2 && sup[d] == last_dir2){
							lpf = true;
							lflg = true;
							search_last_dist = last_dist2;
						}
					}
					fitch = dfs(cNode, cNextNode, cDir, cDist, crDist, cEdibleTime, safe_count[d], copyDistList, copyDistType, distance+dist[cNode[4]][sup[d]], min, rev, cutoff);

					if(lflg)
						lpf = false;


					if(/*pflg && */!fitch && pillDist[uk] == pill[cNode[4]][sup[d]] + distance/* - cDist[4]*/){
						pillDist[uk] = pcutoff;
					}

					if(rev)revflg = -1;
				}
				endTime = java.lang.System.nanoTime();
				if(fitch || (endTime-startTime) / 1000000 >= timecutoff){
					return true;
				}
			}

			return false;
		}

		else {

			int gNum = distType[0];
			int safe_count[] = new int[4];
			/*
			int save_th[] = new int[4];

			for(int d=0; d<4; d++){
				if(node[aNextNode[gNum]][d]!=-1 && d!=getReverse(aDir[gNum])){

					save_th[d] = 10000;
					for(int i=0; i<junctionNum; i++){
						if(threshold[aNextNode[gNum]][d][i]-(pth[aNextNode[4]][i]+cDist[4])<=EAT_DISTANCE && save_th[d] > threshold[aNextNode[gNum]][d][i]){
							save_th[d] = threshold[aNextNode[gNum]][d][i];
						}
					}


				}
			}
			int[] sup = new int[4];
			for(int i=0;i<4;i++){
				sup[i] = i;
			}
			for(int d=0; d<4; d++){
				for(int e=d+1;e<4;e++){
					if(save_th[d] > save_th[e]){
						int temp = save_th[d];
						save_th[d] = save_th[e];
						save_th[e] = temp;

						temp = sup[d];
						sup[d] = sup[e];
						sup[e] = temp;
					}
				}
			}*/
			int[] minDist = new int[4];
			for(int d=0; d<4; d++){
				if(aNextNode[4] == aNextNode[gNum] && aNode[4] == node[aNextNode[gNum]][d]){
					minDist[d] = dist[aNextNode[gNum]][d];
				}
				else {
					if(node[aNextNode[gNum]][d]!=-1 && d!=getReverse(aDir[gNum])){
						minDist[d] = dist[aNextNode[gNum]][d]+threshold[node[aNextNode[gNum]][d]][dir[aNextNode[gNum]][d]][aNextNode[4]];
					}
				}
			}
			int[] sup = new int[4];
			for(int i=0;i<4;i++){
				sup[i] = i;
			}

			for(int d=0;d<4;d++){
				for(int e=d+1;e<4;e++){
					if(minDist[d] > minDist[e]){
						int temp = minDist[d];
						minDist[d] = minDist[e];
						minDist[e] = temp;

						temp = sup[d];
						sup[d] = sup[e];
						sup[e] = temp;
					}
				}
			}

			for(int d=0; d<4; d++){

				boolean flg = false;
				if(node[aNextNode[gNum]][sup[d]]!=-1 && sup[d]!=getReverse(aDir[gNum])){

					cNode[gNum] = aNextNode[gNum];
					cNextNode[gNum] = node[cNode[gNum]][sup[d]];
					cDir[gNum] = dir[cNode[gNum]][sup[d]];

					int nextDist = dist[cNode[gNum]][sup[d]];
					int ext = cEdibleTime[gNum]/2;
					if(ext>nextDist){
						ext = nextDist;
					}

					if(cEdibleTime[gNum]>0){
						nextDist += ext;
					}

					cDist[gNum] = nextDist;

					boolean flgg = true;
					int[] decr = new int[4];

					boolean[] srnk = new boolean[4];
					boolean flggg = true;
					for(int g=0;g<4;g++){
						if(cNextNode[4]==cNode[g]){
							if((cNode[4] == cNextNode[g]) && (cDist[4]*2/3 - cEdibleTime[g]>=EAT_DISTANCE-2))flggg = false;
						}

						if(cEdibleTime[g]==0 && cNextNode[4] == cNextNode[g] && cDir[4] == cDir[g] && cDist[g] < cDist[4]){
							srnk[g] = true;
						}
					}

					for(int g=0;g<4;g++){
						if(cEdibleTime[g]>cDist[g])decr[g] = cDist[g]/2;
						else decr[g] = cEdibleTime[g]/2;
						int p2dist = cDist[4];
						int g2dist = threshold[cNextNode[g]][cDir[g]][cNextNode[4]]+cDist[g]-decr[g];
						if(!srnk[g] && ( ((g2dist+cEdibleTime[g]/2) - p2dist <= EAT_DISTANCE && g2dist > p2dist/2) ||
									((g2dist + p2dist)*2/3 >= cEdibleTime[g] && g2dist <= p2dist/2)||
										(cNode[g] == cNextNode[4] && cNextNode[g]==cNode[4]))	){
							flgg = false;
							break;
						}
					}

					int[] reachable = new int[junctionNum];
				//	cRev[sup[d]] = nRev;
					countt=0;
					if(flggg && flgg){
						reachable[cNextNode[4]] = cDist[4];
						int max = 0;
						countt=1;
						for(int g=0;g<4;g++)cDist[g] -= decr[g];
						thcurpNode = cNode[4];
						curpDist = rDist[4] - cDist[4];
						thwalk_search(cNextNode[4],cDir[4],reachable,cDist[4],cNode,cNextNode,cDir,cDist,cEdibleTime,0,srnk,cNode[4],cNextNode[4]);
						for(int g=0;g<4;g++)cDist[g] += decr[g];

						for(int i=0;i<junctionNum;i++){
							if(max<reachable[i])max = reachable[i];
						}
						if(distance+max-cDist[4]>cutoff)continue;
						/*
						if(reachable[cNode[4]]>0)cRev[sup[d]] = true;
						else cRev[sup[d]] = false;
					*/
					}
					safe_count[d] = countt;
					if(safe_count[d]<=0) safe_count[d] = 1;

					if(safe_count[d]-8 > pThreshold || safe_count[d]-8 > cut)continue;



					for(int i=0;i<5;i++){
						copyDistList[i] = cDistList[i];
						copyDistType[i] = cDistType[i];
					}

					for(int i=4; i>=0; i--){
						if(copyDistList[i] < nextDist){
							for(int j=0; j<i; j++){
								copyDistList[j] = copyDistList[j+1];
								copyDistType[j] = copyDistType[j+1];
							}

							copyDistList[i] = nextDist;
							copyDistType[i] = gNum;
							break;
						}
					}
					flg = true;
					crDist[gNum] = cDist[gNum] - decr[gNum];
					//crDist[gNum] = cDist[gNum];
					fitch = dfs(cNode, cNextNode, cDir, cDist, crDist, cEdibleTime, pThreshold, copyDistList, copyDistType, distance, min, rev, cutoff);

					endTime = java.lang.System.nanoTime();
					if((endTime-startTime) / 1000000 >= timecutoff)return true;


					if(REVERSAL_SWITCH && revflg==gNum && cDist[4]>0 && !fitch && rev && game.getCurrentLevel()%4!=2){
				//	if(REVERSAL_SWITCH && revflg && cDist[4]>0 && !fitch && rev && distance == rDist[4]){
					//if(REVERSAL_SWITCH && cDist[4]>0 && !fitch && rev && distance == rDist[4]){
				//	if(REVERSAL_SWITCH && cDist[4]>0 && !fitch && rev){
					//if(REVERSAL_SWITCH && revflg && cDist[4]>0 && !fitch && rev && distance != rDist[4]){

					//if((safe_count[d]-4 > pThreshold && cDist[4]>0 && reachable[cNode[4]]>0 && !fitch && rev)){
					//		revflg = false;

						/*
						rev = false;
						int[] copyRevDist = new int[5];
						int[] copyNextRevNode = new int[5];
						int[] copyRevNode = new int[5];
						int[] copyDir = new int[5];
						for(int i=0;i<5;i++){
							copyRevDist[i] = cDist[i];
							copyNextRevNode[i] = cNextNode[i];
							copyRevNode[i] = cNode[i];
							copyDir[i] = cDir[i];
						}
						copyNextRevNode[4] = cNode[4];
						copyRevDist[4] = rDist[4] - cDist[4] + 2;
						copyRevNode[4] = cNextNode[4];
						copyDir[4] = dir[cNextNode[4]][d(getReverse(d(cDir[4])))];
						for(int i=0;i<5;i++)
							if(copyDistType[i]==4){
								copyDistList[i] = copyRevDist[4];
								break;
							}

						for(int i=0;i<5;i++){
							for(int j=i+1;j<5;j++){
								if(copyDistList[i]>copyDistList[j]){
									int temp = copyDistList[i];
									copyDistList[i] = copyDistList[j];
									copyDistList[j] = temp;

									temp = copyDistType[i];
									copyDistType[i] = copyDistType[j];
									copyDistType[j] = temp;
								}
							}
						}
						*/



						rev = false;
						int[] copyRevDist = new int[5];
						int[] copyNextRevNode = new int[5];
						int[] copyRevNode = new int[5];
						int[] copyDir = new int[5];
						for(int i=0;i<5;i++){
							copyRevDist[i] = cDist[i];
							copyNextRevNode[i] = cNextNode[i];
							copyRevNode[i] = cNode[i];
							copyDir[i] = cDir[i];
						}
						copyNextRevNode[4] = cNode[4];
						copyRevDist[4] = (rDist[4]-cDist[4])%2+2;
						//if(copyRevDist[4] == 0)copyRevDist[4] = 2;

						copyRevNode[4] = cNextNode[4];
						copyDir[4] = dir[cNextNode[4]][getReverse(cDir[4])];
						for(int i=0;i<5;i++)
							if(copyDistType[i]==4){
								copyDistList[i] = copyRevDist[4];
								break;
							}

						for(int i=0;i<5;i++){
							for(int j=i+1;j<5;j++){
								if(copyDistList[i]>copyDistList[j]){
									int temp = copyDistList[i];
									copyDistList[i] = copyDistList[j];
									copyDistList[j] = temp;

									temp = copyDistType[i];
									copyDistType[i] = copyDistType[j];
									copyDistType[j] = temp;
								}
							}
						}


						if(pillDist[uk] == pill[cNode[4]][getReverse(copyDir[4])] + distance - rDist[4]){
							//System.out.println("t");
							pillDist[uk] = (pcutoff-1)+distance-rDist[4];
							//pillDist[uk] = pcutoff;
						}
						flg = true;
						fitch = dfs(copyRevNode, copyNextRevNode, copyDir, copyRevDist, crDist, cEdibleTime, pThreshold, copyDistList, copyDistType, distance-rDist[4]+2/*+cDist[4]*/, min, rev, cutoff);

						endTime = java.lang.System.nanoTime();
						if((endTime-startTime) >= 35000000)return true;


					}

				}
				if(!fitch && flg){
					return false;
				}
			}
			return true;
		}
	}


	private void createThreshold(){
		for(int i=0;i<junctionNum;i++){
			if(game.getGhostInitialNodeIndex() == junctionList[i]){
				ghostRound(i,5,0,threshold[i][5]);
				threshold[i][5][i] = 0;
			}
			for(int d=0;d<4;d++){
				if(node[i][getReverse(d)]!=-1){
					ghostRound(i,getReverse(d),0,threshold[i][d]);
					threshold[i][d][i] = 0;
				}
			}
			pacRound(i,0,pth[i]);
			pth[i][i] = 0;
		}
	}


	private void pacRound(int currentNode, int distance, int[] pacScore) {
		if (distance < 350 && (pacScore[currentNode] == 0 || distance < pacScore[currentNode])) {
			pacScore[currentNode] = distance;
				for (int d=0; d<4; d++) {
					int next = node[currentNode][d];
					if (next != -1)
						pacRound(next, distance+dist[currentNode][d], pacScore);
				}
		}
	}


	private void ghostRound(int currentNode, int banned, int distance, int[] ghostScore) {
		if (distance < 350 && (ghostScore[currentNode] == 0 || distance < ghostScore[currentNode])) {
			ghostScore[currentNode] = distance;
			for (int d=0; d<4; d++) {
				int next = node[currentNode][d];
				if (next != -1 && d != banned)
					ghostRound(next, getReverse(dir[currentNode][d]), distance+dist[currentNode][d], ghostScore);
			}
		}
	}


	private void createNode(){
		int nextNode;
		for(int i=0; i<junctionList.length; i++){
			for(int d=0; d<4; d++){
				distc = 1;
				nextNode = game.getNeighbour(junctionList[i], d(d));

				if(nextNode != -1){

					setPill = 0;
					int p = game.getPillIndex(nextNode);
					if(p!=-1 && game.isPillStillAvailable(p)){
						setPill = 1;
					}

					node[i][d] = junctionSet[nJunction(nextNode,1,d)];
					dist[i][d] = distc;
					dir[i][d] = gdir;
					pill[i][d] = setPill;
				}
			}
		}
	}

	private int nJunction(int node, int dist, int dir) {
		while (!game.isJunction(node) && game.getPacmanCurrentNodeIndex()!=node) {
			int p = game.getPowerPillIndex(node);
			if(p !=-1 && game.isPowerPillStillAvailable(p)){
				gdir = dir;
				setPill = 0;
				return node;
			}
			for(int g=0;g<4;g++){
				if(game.getGhostCurrentNodeIndex(g(g))==node){
					gdir = dir;
					return node;
				}
				else if(game.getGhostLairTime(g(g))>0 && game.getGhostInitialNodeIndex() == node){
					gdir = dir;
					return node;
				}
			}
			int d = 0;
			while (game.getNeighbour(node, d(d)) == -1 || d == getReverse(dir))
				d++;
			dir = d;
			node = game.getNeighbour(node, d(d));
			dist++;
			distc++;

			int pi = game.getPillIndex(node);
			if(setPill==0 && pi!=-1 && game.isPillStillAvailable(pi)){
				setPill = dist;
			}
		}
		gdir = dir;
		return node;
	}

	private int nextJunction(int node, int dist, int dir) {
		while (!game.isJunction(node) && game.getPacmanCurrentNodeIndex()!=node) {
			int d = 0;
			while (game.getNeighbour(node, d(d)) == -1 || d == getReverse(dir))
				d++;
			dir = d;
			node = game.getNeighbour(node, d(d));
			distc++;

		}
		gdir = dir;
		return node;
	}


	private int npNextJunction(int node, int dist, int dir) {
		while (!game.isJunction(node) && game.getPacmanCurrentNodeIndex()!=node) {
			int d = 0;
			while (game.getNeighbour(node, d(d)) == -1 || d == getReverse(dir))
				d++;
			dir = d;
			node = game.getNeighbour(node, d(d));
			distc++;

			int pi = game.getPillIndex(node);
			if(setPill==0 && pi!=-1 && game.isPillStillAvailable(pi)){
				setPill = dist;
			}
		}
		gdir = dir;
		return node;
	}


	private int pNextJunction(int node, int dist, int dir) {
		while (!game.isJunction(node)) {
			int d = 0;
			while (game.getNeighbour(node, d(d)) == -1 || d == getReverse(dir))
				d++;
			dir = d;
			node = game.getNeighbour(node, d(d));
			distc++;
		}
		gdir = dir;
		return node;
	}


	//	int jndist;
		private void pillWalk(int curNode, int distance, int[] reachable){
			if (distance < ub && (visited[curNode] == 0 || distance < visited[curNode])) {
				visited[curNode] = distance;
				for(int d=0; d<4; d++){
					boolean flg = false;
					int nextNode = node[curNode][d];
					if(nextNode != -1){
						int[] gn = new int[4];
						for(int g=0;g<4;g++){
						//	if(nextNode == junctionSet[game.getGhostCurrentNodeIndex(g(g))])flg = true;
							gn[g] = threshold[junctionSet[game.getGhostCurrentNodeIndex(g(g))]][d(game.getGhostLastMoveMade(g(g)))][nextNode];
						//	if(game.getGhostLairTime(g(g))==0 && distance+dist[curNode][d]+EAT_DISTANCE >= gn[g])flg = true;
						}
						if(!flg){
							int pp = game.getPowerPillIndex(junctionList[nextNode]);
							if(!(pp!=-1 && game.isPowerPillStillAvailable(pp))){
								if(pill[curNode][d]>0){
									ub = distance+pill[curNode][d];
						//			if(reachable[nextNode]==0)ub += 20;
							//		jndist = dist[curNode][d];
								}
								else pillWalk(nextNode,distance+dist[curNode][d],reachable);
							}
						}
					}
				}
			}
		}


}