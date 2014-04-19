package aiBot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer
{
	public static final Direction[] customDirections = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_WEST };
	public static MapLocation mapCenter;
	public static int startTurn;
	public static Team myTeam;
	public static MapLocation enemyHQ;
	public static MapLocation myHQ;
	
	public static void run (RobotController rc){
		mapCenter = new MapLocation(rc.getMapWidth()/2 , rc.getMapHeight() / 2);
		startTurn = Clock.getRoundNum();
		myTeam    = rc.getTeam();
		enemyHQ   = rc.senseEnemyHQLocation();
		myHQ      = rc.senseHQLocation();
		try {
			switch (rc.getType()){ //initialize Robots
			
			case HQ:      
					BotHQ.init(rc);  break;
			case SOLDIER: BotSoldier.init(rc); break;
			//case PASTR:   BotPastr.init(rc);   break;
			default: break;
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
		
		while (true){ //run robots
			try{
				switch (rc.getType()){ //initialize Robots
				
				case HQ:      BotHQ.run(rc);      break;
				case SOLDIER: BotSoldier.run(rc); break;
				//case PASTR:   BotPastr.run(rc);   break;
				default: break;
				}
			} catch (GameActionException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
	}
	
}