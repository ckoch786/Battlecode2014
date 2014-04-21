package aiBot;

//things to do:
//defend pastrs that are under attack, or at least consider defending them
//battlecry when charging into battle -> concerted effort
//something like the opposite of a battlecry, when you're sure you're outnumbered

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	public static RobotController rc;
	public static Direction allDirections[] = Direction.values();
	public static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};

	static int bigBoxSize = 5;
	static MapLocation enemyHQ;
	
	//HQ data:
	static MapLocation rallyPoint;
	static boolean die = false;
		
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		Broadcaster.rc = rcIn;
		enemyHQ = rc.senseEnemyHQLocation();
		
		if(rc.getType()==RobotType.HQ){
			BotHQ.init(rcIn);
		}else{
			AStar.rc=rcIn;//slimmed down init
		}
		//MapLocation goal = getRandomLocation();
		//path = BreadthFirst.pathTo(VectorFunctions.mldivide(rc.getLocation(),bigBoxSize), VectorFunctions.mldivide(goal,bigBoxSize), 100000);
		//VectorFunctions.printPath(path,bigBoxSize);
		
		while(true){
			try{
				if(rc.getType()==RobotType.HQ){
					BotHQ.runHQ(rc);
					if(die)
						break;
				}else if(rc.getType()==RobotType.SOLDIER){
					BotSoldier.runSoldier(rc);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			rc.yield();
		}
	}		
}