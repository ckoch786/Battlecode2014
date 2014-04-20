package aiBot;

import battlecode.common.*;

public class Adverserial {
	
	private static RobotController rc;
	private static int myRobots;
	private static MapLocation[] myPastrs, enemyPastrs;
	private static Team goodGuys, badGuys;
	
	public static int hueristic(RobotController rcin){
		rc = rcin;
		myRobots =  rc.senseRobotCount();
		
		
		return 0;
	}
}
