package watson;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	public static RobotController rc;
	static Direction allDirs[] = Direction.values(); //indexed 0,7
	static Random rand = new Random();
	static int directions[] = new int[]{0,1,-1, 2, -2};
	
	public static void run(RobotController rcin){
		
		rc = rcin; //setting global controller
		rand.setSeed(rc.getRobot().getID());
		
		while(true){
			
			try{
				if(rc.getType()== RobotType.HQ){
					runHeadquarters();
				}
				else if (rc.getType() == RobotType.SOLDIER){
					runSoldier();
				}
				rc.yield();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private static void runSoldier() throws GameActionException {
		int currentByteCode = Clock.getBytecodeNum();
		checkCombat();
		
		//Communication
		int editingChannel = Clock.getRoundNum()%2; //alternates broadcasting to check all robots have reported location
		int usingChannel = editingChannel + 1;		
		int runningTotal = rc.readBroadcast(editingChannel);
		rc.broadcast(editingChannel, runningTotal +1);
				
		MapLocation runningVectorTotal = intToLoc(rc.readBroadcast(editingChannel) + 2);
		rc.broadcast(editingChannel + 2, locToInt(mlAdd(runningVectorTotal, rc.getLocation())));
		MapLocation avgPos = mlDivide(intToLoc(rc.readBroadcast(usingChannel+2)),rc.readBroadcast(usingChannel));
		
		rc.setIndicatorString(0, "read pos of swarm: " + locToInt(avgPos));
			
		swarmMove(avgPos);
	}
	
	private static void swarmMove(MapLocation avgPos) throws GameActionException{
		Direction chosenDir = rc.getLocation().directionTo(avgPos);
		if (rc.isActive()){
			if(rand.nextDouble()<0.2){//go to swarm center
				for(int directionalOffset:directions){
					int forwardInt = chosenDir.ordinal();
					Direction trialDir = allDirs[(forwardInt+directionalOffset+8)%8];
					if(rc.canMove(trialDir)){
						rc.move(trialDir);
						break;
					}
				}
			}else{ //go somewhere randomly
				Direction d = allDirs[(int)(rand.nextDouble()*8)];
				if(rc.isActive()&&rc.canMove(d)){
					rc.move(d);
				}
			}
		}
	}
	private static int locToInt(MapLocation m){
		return m.x * 100 + m.y;
	}
	private static MapLocation intToLoc(int i){
		return new MapLocation(i/100, i%100);
	}
	private static MapLocation mlAdd ( MapLocation m1, MapLocation m2){
		return new MapLocation (m1.x+ m2.x, m1.y + m2.y);
	}
	private static MapLocation mlDivide(MapLocation bigM, int divisor){
		return new MapLocation (bigM.x/divisor , bigM.y/divisor);
	}

	private static void checkCombat() throws GameActionException {
		
		//Combat
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 1000, rc.getTeam().opponent());
		if(enemies.length>0){ //enemies are sensed
			Robot badGuy = enemies[0];
			RobotInfo badGuyInfo;
			
			badGuyInfo = rc.senseRobotInfo(badGuy);
			if(badGuyInfo.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared){
				if(rc.isActive()){
					rc.attackSquare(badGuyInfo.location);
				}
			}			
		}
		else{  //no enemies build a tower
			if(rand.nextDouble() <0.001  && rc.sensePastrLocations(rc.getTeam()).length < 5 ){
				//rc.senseCowsAtLocation(arg0);
				if(rc.isActive()){
						rc.construct(RobotType.PASTR);
				}
			}
		}
	}

	private static void runHeadquarters() throws GameActionException {
		Direction spawnDir = Direction.NORTH;				

		if (rc.isActive() && rc.canMove(spawnDir) && rc.senseRobotCount()< GameConstants.MAX_ROBOTS)
		rc.spawn(spawnDir);
		
		int editingChannel = Clock.getRoundNum()%2; 
		int usingChannel = editingChannel + 1;
		rc.broadcast(editingChannel, 0);
		rc.broadcast(editingChannel + 2, 0);
	}
}