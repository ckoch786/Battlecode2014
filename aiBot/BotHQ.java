package aiBot;

import battlecode.common.*;

public class BotHQ {	
	
	public static RobotController rc;
	static MapLocation targetedPastr;
	
	public static void init(RobotController rcin) throws GameActionException{
		rc = rcin;
		rc.broadcast(101,VectorFunctions.locToInt(VectorFunctions.mldivide(rc.senseHQLocation(),RobotPlayer.bigBoxSize)));//this tells soldiers to stay near HQ to start
		rc.broadcast(102,-1);//and to remain in squad 1
		tryToSpawn();
		AStar.init(rc, RobotPlayer.bigBoxSize);
		RobotPlayer.rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
	}
	
	public static void runHQ(RobotController rcin) throws GameActionException {
		//TODO consider updating the rally point to an allied pastr 
		rc = rcin;
		
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000000,rc.getTeam());
		
		//if my team is defeated, regroup at main base:
		if(Clock.getRoundNum()>400&&alliedRobots.length<5){//call a retreat
			MapLocation startPoint = findAverageAllyLocation(alliedRobots);
			Broadcaster.findPathAndBroadcast(2,startPoint,rc.senseHQLocation(),RobotPlayer.bigBoxSize,2);
			RobotPlayer.rallyPoint = rc.senseHQLocation();
		}else{//not retreating
			//tell them to go to the rally point
			Broadcaster.findPathAndBroadcast(1,rc.getLocation(),RobotPlayer.rallyPoint,RobotPlayer.bigBoxSize,2);

			//if the enemy builds a pastr, tell sqaud 2 to go there.
			MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastrs.length>0){
				MapLocation startPoint = findAverageAllyLocation(alliedRobots);
				targetedPastr = getNextTargetPastr(enemyPastrs,startPoint);
				//broadcast it
				Broadcaster.findPathAndBroadcast(2,startPoint,targetedPastr,RobotPlayer.bigBoxSize,2);
			}
		}
		
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		}
		
		//after telling them where to go, consider spawning
		tryToSpawn();
	}
	private static MapLocation findAverageAllyLocation(Robot[] alliedRobots) throws GameActionException {
		//find average soldier location
		MapLocation[] alliedRobotLocations = VectorFunctions.robotsToLocations(alliedRobots, rc, true);
		MapLocation startPoint;
		if(alliedRobotLocations.length>0){
			startPoint = VectorFunctions.meanLocation(alliedRobotLocations);
			if(Clock.getRoundNum()%100==0)//update rally point from time to time
				RobotPlayer.rallyPoint=startPoint;
		}else{
			startPoint = rc.senseHQLocation();
		}
		return startPoint;
	}

	private static MapLocation getNextTargetPastr(MapLocation[] enemyPastrs,MapLocation startPoint) {
		if(enemyPastrs.length==0)
			return null;
		if(targetedPastr!=null){//a targeted pastr already exists
			for(MapLocation m:enemyPastrs){//look for it among the sensed pastrs
				if(m.equals(targetedPastr)){
					return targetedPastr;
				}
			}
		}//if the targeted pastr has been destroyed, then get a new one
		return VectorFunctions.findClosest(enemyPastrs, startPoint);
	}
	public static void tryToSpawn() throws GameActionException {
		if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			for(int i=0;i<8;i++){
				Direction trialDir = RobotPlayer.allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}
}
