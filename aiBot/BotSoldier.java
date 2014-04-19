package aiBot;

import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class BotSoldier {
	
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2};
	public static RobotController rc;
	static Direction allDirections[] = Direction.values();
	
	public static void init(RobotController rcin){
		rc = rcin;
	}
	public static void run(RobotController rc) throws GameActionException{
		tryToShoot();

		//movement
		Direction chosenDirection = allDirections[(int)(randall.nextDouble()*8)];
		if(rc.isActive()&&rc.canMove(chosenDirection)){
		rc.move(Direction.NORTH);
		}

	}

	private static void tryToShoot() throws GameActionException {
		//shooting
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//if there are enemies
			Robot anEnemy = enemyRobots[0];
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				if(rc.isActive()){
					rc.attackSquare(anEnemyInfo.location);
				}
			}
		}else{//there are no enemies, so build a tower
			if(randall.nextDouble()<0.001&& rc.sensePastrLocations(rc.getTeam()).length<5){
				//rc.senseCowsAtLocation(arg0);
				if(rc.isActive()){
					rc.construct(RobotType.PASTR);
				}
			}
		}
	}

}
