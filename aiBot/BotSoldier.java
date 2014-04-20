package aiBot;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class BotSoldier {
	
	public static Random randall = new Random();
	public static int directionalLooks[] = new int[]{0,1,-1,2,-2};
	public static RobotController rc;
	public static int w, h;
	public static MapLocation myLoc;
	public static int myID;
	public static MapLocation enemy;
	public static Robot[] enemyRobots;
	public static MapLocation closestEnemy;
	public static MapLocation enemyCenter;
	public static double runAwayHitpoints = 50;
	public static boolean foundPastrSite = false;
	
	/***************************************
	 *  Variables for Pathing data
	 **************************************/
	public static ArrayList<Direction> path = new ArrayList<Direction>();
	public static Direction[] dirs = {Direction.NORTH,Direction.NORTH_EAST,Direction.EAST,Direction.SOUTH_EAST,Direction.SOUTH,Direction.SOUTH_WEST,Direction.WEST,Direction.NORTH_WEST};
	public static Direction[] orthoDirs = {Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
	public static int[] dirSearch = new int[]{0,1,-1,2,-2,-3,3,4};
	public static int[][] minionData;
	public static boolean minionDataDownloaded=false;
	public static boolean positiveFollow = true;
	public static int lastNode = -1;
	public static int currentNode = -1;
	public static Direction persistentRandomDirection;
	public static boolean fleeing = false;
	
	public static void init(RobotController rcin){
		rc = rcin;
		w   = rc.getMapWidth();
		h  = rc.getMapHeight();
		enemy = rc.senseEnemyHQLocation();
		minionData = new int[w][h];
		persistentRandomDirection = dirs[(int)(8*random())];
	}
	public static void run(RobotController rcin) throws GameActionException{
		rc = rcin;
		myLoc = rc.getLocation();
		myID = rc.getRobot().getID();
		enemyCenter = findEnemyCenter();
		Robot[] alliedRobots;
		if(enemyRobots.length>0){//count the allies that are in range of the closest enemy
			alliedRobots = rc.senseNearbyGameObjects(Robot.class,closestEnemy,rc.getType().attackRadiusMaxSquared,rc.getTeam());
		}else{
			alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().attackRadiusMaxSquared,rc.getTeam());
		}
		if(!minionDataDownloaded){
			//check the network to see if minion data is available for download
			if(rc.readBroadcast(h*w-1)>-9999){//minion map is ready when its last value has been written
				downloadMinionMap();
			}
			randomPathing();
		}else{//data has been downloaded, so use it
			//detect all cows
			MapLocation[] es = rc.sensePastrLocations(rc.getTeam());
			MapLocation pastrSite = null;
			if(es.length<5){
				pastrSite = findPastrSite(foundPastrSite?1:4);
				foundPastrSite=(pastrSite!=null);
			}
			
			if(pastrSite!=null){//then go to pastr site and build there
				rc.setIndicatorString(1, "trying to build pastr");
				goBuildPastr(pastrSite);
			}else{
				rc.setIndicatorString(1, "patrolling");
				//TODO maybe there should be some followers of a pather.
				if(((rc.getHealth()>runAwayHitpoints//if healthy, don't flee
						||(alliedRobots.length+1)>enemyRobots.length)//can be unhealthy if you outnumber enemy
						||enemyRobots.length==0)//if no enemies around, don't flee
						&&myLoc.distanceSquaredTo(enemy)>RobotType.HQ.attackRadiusMaxSquared){//if near enemy HQ, flee
					fleeing=false;
					networkWalk();
				}else{
					fleeing=true;
					networkFlee();
				}
			}
		}

	}
	private static void goBuildPastr(MapLocation pastrSite) throws GameActionException{
		int myLocCode = minionData[myLoc.x][myLoc.y];
		if(myLocCode>1000||myLocCode<0||myLoc.distanceSquaredTo(pastrSite)>=GameConstants.PASTR_RANGE){
			tryToGo(rc.getLocation().directionTo(pastrSite));
			myLoc = rc.getLocation();
		}else{
			if(rc.isActive()){
				rc.construct(RobotType.PASTR);
			}
		}
	}
	
	private static MapLocation findPastrSite(int checkFraction) throws GameActionException{
		//int checkFraction = 4;//don't check all the sites every round. 
		double cowThreshold = 20.0/(1.0-GameConstants.NEUTRALS_TURN_DECAY);
		double mostCows = cowThreshold;
		double challengerCows = 0;
		MapLocation mostCowLoc = null;
		
		if(Clock.getBytecodeNum()<2000){
			MapLocation[] checkLocs = MapLocation.getAllMapLocationsWithinRadiusSq(myLoc, rc.getType().sensorRadiusSquared);
			double phase =(double) (Clock.getRoundNum()%checkFraction)/(double) checkFraction;
			int start = (int)(phase*checkLocs.length);
			int end = (int)((phase+1.0/checkFraction)*checkLocs.length);
			for(int i=start;i<end;i++){
				MapLocation m = checkLocs[i];
				challengerCows=rc.senseCowsAtLocation(m);
				if(challengerCows>mostCows){
					mostCowLoc = m;
					mostCows=challengerCows;
				}
			}
		}

		if(mostCowLoc!=null){
			//check that there is no allied robot about to take that tile
			if(tileOccupied(mostCowLoc))
				return null;
			
			//check for existing pastrs that cover the area in question
			int outsidePastrRange = GameConstants.PASTR_RANGE+1;
			for(MapLocation m:rc.sensePastrLocations(rc.getTeam())){
				if(mostCowLoc.distanceSquaredTo(m)<outsidePastrRange){
					return null;
				}
			}
			
			//a cow location is only valid if it is accessible by a straight line.
			MapLocation currentLoc = mostCowLoc;
			while(!rc.senseTerrainTile(currentLoc).equals(TerrainTile.VOID)){//testing for open path toward the mostCowLoc
				Direction d = currentLoc.directionTo(myLoc);
				currentLoc = currentLoc.add(d);
				if(currentLoc.equals(myLoc)){
					rc.setIndicatorString(2, "viable location found at "+mostCowLoc.x+","+mostCowLoc.y);
					return mostCowLoc;
				}
			}
		}
		
		return null;
	}	
	private static boolean tileOccupied(MapLocation t){
		return (rc.senseNearbyGameObjects(Robot.class,t,1,rc.getTeam()).length!=0);
	}
	private static MapLocation findEnemyCenter() throws GameActionException{
		//finds center of enemies and also closest enemy
		enemyRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().attackRadiusMaxSquared,rc.getTeam().opponent());
		if(enemyRobots.length>0){
			int closestDist = 100000;
			int challengerDist = 100000;
			int xtot=0;
			int ytot=0;
			int tot=0;
			for(Robot r:enemyRobots){
				MapLocation rl = rc.senseRobotInfo(r).location;
				xtot+=rl.x;
				ytot+=rl.y;
				tot++;
				challengerDist = myLoc.distanceSquaredTo(rl);
				if(challengerDist<closestDist){
					closestDist=challengerDist;
					closestEnemy = rl;
				}
			}
			return new MapLocation(xtot/tot,ytot/tot);
		}else{
			return null;
		}
	}
	private static double random(){
		double d = (Math.random()*myID*Clock.getRoundNum());
		return d-(int)d;
	}
	private static void randomPathing() throws GameActionException{
		if(random()>0.9){
			if(random()>0.5){
				persistentRandomDirection = persistentRandomDirection.rotateLeft();
			}else{
				persistentRandomDirection = persistentRandomDirection.rotateRight();
			}
		}
		if(!rc.canMove(persistentRandomDirection)){
			TerrainTile ahead = rc.senseTerrainTile(myLoc.add(persistentRandomDirection));
			if(ahead.equals(TerrainTile.OFF_MAP)||ahead.equals(TerrainTile.VOID)){
				if(random()>0.5){
					persistentRandomDirection = persistentRandomDirection.rotateLeft();
				}else{
					persistentRandomDirection = persistentRandomDirection.rotateRight();
				}
			}
		}
		rc.setIndicatorString(0, ""+persistentRandomDirection);
		tryToGo(persistentRandomDirection);//run around at random
	}
	private static void tryToGo(int dirInt) throws GameActionException{
		tryToGo(dirs[dirInt],false);
	}
	
	private static void tryToGo(Direction d) throws GameActionException{
		tryToGo(d,false);
	}
	
	private static void tryToGo(Direction d,boolean sneak) throws GameActionException{
		if(rc.isActive()&&!fleeing){
			//see if there's something to shoot
			if(enemyRobots.length>0){
				rc.attackSquare(closestEnemy);//rc.senseRobotInfo(enemyRobots[0]).location
			}
		}
		if(rc.isActive()){
			//otherwise, try to move in the given direction
			for(int dirInt:dirSearch){
				Direction trialDir = dirs[(d.ordinal()+8+dirInt)%8];
				if(rc.canMove(trialDir)){
					if(sneak){
						rc.sneak(trialDir);
					}else{
						rc.move(trialDir);
					}
					break;
				}
			}
		}
	}

	private static void downloadMinionMap() throws GameActionException {
		for(int x=0;x<w;x++){
			for(int y=0;y<h;y++){
				int index = y*+x;
				minionData[x][y] = rc.readBroadcast(index);
			}
		}
		minionDataDownloaded=true;
	}
	private static void networkFlee() throws GameActionException{
		int currentTile = minionData[myLoc.x][myLoc.y];
		rc.setIndicatorString(0, "network flee, "+currentTile);
		
		if(currentTile<0){//on a node: take exit that runs away
			//chooseFarthestExit(enemyCenter);
		}else if(currentTile<8){//out in the open: follow arrows to the closest edge
			tryToGo(currentTile);
		}else{//on an edge: take the direction that runs away
			int twodir = currentTile%1000;
			int dir1 = twodir%10;
			int dir2 = twodir/10;
			int dist1 = myLoc.add(dirs[dir1]).distanceSquaredTo(enemyCenter);
			int dist2 = myLoc.add(dirs[dir2]).distanceSquaredTo(enemyCenter);
			if(dist1>dist2){
				positiveFollow=false;
				tryToGo(dir1);
			}else{
				positiveFollow=true;
				tryToGo(dir2);
			}
			tryToGo(positiveFollow?dir2:dir1);
		}
	}
	private static void networkWalk() throws GameActionException{
		int currentTile = minionData[myLoc.x][myLoc.y];
		rc.setIndicatorString(0, "network walk,  "+currentTile);
		if(currentTile<0){//on a node: choose a random direction to move and choose a positive or negative direction
			int nodeID = currentTile+1000;
			if(currentNode!=nodeID){
				lastNode = currentNode;
				currentNode = nodeID;
			}
			chooseRandomExit();
		}else if(currentTile<8){//out in the open: follow arrows to the closest edge
			tryToGo(currentTile);
		}else{//on an edge: follow arrows in positive or negative direction
			int dirs = currentTile%1000;
			int dir1 = dirs%10;
			int dir2 = dirs/10;
			tryToGo(positiveFollow?dir2:dir1);
		}
	}
	private static void chooseRandomExit() throws GameActionException{//TODO this method will fail at map edge
		//locate valid directions
		ArrayList<Direction> validNeighbors = new ArrayList<Direction>();
		ArrayList<Boolean> followSign = new ArrayList<Boolean>();
		for(Direction d:dirs){
			MapLocation checkLoc = myLoc.add(d);
			int locationCode = minionData[checkLoc.x][checkLoc.y];
			if(locationCode>1000){
				int node1 = locationCode/1000000;
				int node2 = (locationCode%1000000)/1000;
				if((node1==lastNode&&node2==currentNode)||(node2==lastNode&&node1==currentNode)){
					//do not go back along old path
				}else if(currentNode==node2){
					validNeighbors.add(d);
					followSign.add(currentNode>node1);
				}else{// assuming (currentNode==node1)
					validNeighbors.add(d);
					followSign.add(currentNode>node2);
				}
			}else if(locationCode<0){//adjacent node is a valid destination
				validNeighbors.add(d);
				followSign.add(positiveFollow);//follow sign doesn't matter.
			}
		}
	}
}
