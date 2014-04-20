package aiBot;

import java.util.ArrayList;
import java.util.LinkedList;

import battlecode.common.*;

public class BotHQ {	
	//vPrivate variables
	private static RobotController rc;	
	private static int width, height;
	private static int dx, dy; //distance between HQs
	private static double[][] mooCows;
	private static MapLocation myHQ, enemyHQ, myPastr, mapCenter, rallyP; 
	private static LinkedList<MapLocation[]> pathQ;
	private static ArrayList<MapLocation> targetsPathed;
	private static int pastrBuild;
	private static TerrainTile[][] map;
	public static int lastSpawnRound = -10000;
	
	public static void init(RobotController rcin) throws GameActionException{
		rc      = rcin;
		width   = rc.getMapWidth();
		height  = rc.getMapHeight();
		mooCows = rc.senseCowGrowth();
		myHQ    = RobotPlayer.myHQ;
		enemyHQ = RobotPlayer.enemyHQ;
		dx      = Math.abs(enemyHQ.x - myHQ.x);
		dy      = Math.abs(enemyHQ.y - myHQ.y);		
		mapCenter = new MapLocation(width/2 , height / 2);
			
		pathQ  = new LinkedList<MapLocation[]>();
		targetsPathed = new ArrayList<MapLocation>();
		
		rc.broadcast(height*width-1, -9999); //tells robots on given channel we are still building the internal Network
		Pathing.initNetworkPathing(rc); //Builds internal map representation, uses VOIDs in an area as a heuristic

		rallyP = CalcPasture(); //set rally point at location with best cow growth near base
		//Broadcaster.broadcastLocation(rc, 10, rallyP); //broadcast rally location on channel 10
	}
	public static void run(RobotController rc) throws GameActionException{
		tryToSpawn();
		Adverserial.hueristic(rc);
	}
	public static void tryToSpawn() throws GameActionException
	{
		if (rc.isActive()) // spawning
		{
			if (rc.senseRobotCount() < GameConstants.MAX_ROBOTS)
			{
				for (Direction dir : RobotPlayer.customDirections)
				{
					if (rc.canMove(dir))
					{
						rc.spawn(dir);
						lastSpawnRound=Clock.getRoundNum();
						break;
					}
				}
			}
		}
	}
	private static void initMap()
	{
		map = new TerrainTile[width][height];
		String line;
		for (int j = 0; j < height; j++)
		{
			line = "";
			for (int i = 0; i< width; i++)
			{
				map[i][j] = rc.senseTerrainTile(new MapLocation(i, j));
				switch (map[i][j]){
				case NORMAL:  line = line + ".";  break;
				case ROAD:    line = line + "-";  break;
				case VOID:    line = line + "X";  break;
				case OFF_MAP: line = line + "X"; break;
				}
			}
			//System.out.println(line);
		}
		MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(enemyHQ, 25);
		for (MapLocation l : locs)
		{
			if (l.x >= 0 && l.x < width && l.y >= 0 && l.y < height && (l.distanceSquaredTo(enemyHQ) != 25 || l.directionTo(enemyHQ).isDiagonal()))
			{
				map[l.x][l.y] = TerrainTile.VOID;
			}
		}
	}
	/**************************************************
	 *  CalcPasture Location
	 * 
	 *  Uses double[][] mooCows to calc good location for pasture
	 *  mooCows gives cow density
	 *  
	 *  This function does account for obstacles in determining closest  to base
	 * @throws GameActionException 
	 * 
	 *************************************************/
	public static MapLocation CalcPasture() throws GameActionException{
		double milk, tempM;
		MapLocation maxMilk, tempLoc;
		
		tempLoc = null;
		maxMilk = new MapLocation(1000, 1000);
		milk = 25; //initialize tempMilk to 25 since we only care about squares with high milk density;
		
		for ( int i = 0; i < width  ; i++){
			for(int j = 0; j < height; j++){
				
				tempM = 0;
								
				if ( i > 2 && i < width - 2 && j > 2 && j < height - 2){
					//----------------------------------------------
					for (int k = i - 2; k <= i + 2; k++){
						for (int l = j - 2; l <= j + 2; l++ ){
							tempM = tempM + mooCows[k][l]; //calculates milk growth in a 2 radius of spot (i,j)						
							
							//expensive code so try to spawn while executing
							tryToSpawn();
							
						}
					}
					//------------------------------------------------
					//finish calculating tempM and compare
					if (tempM >= milk){
						milk     = tempM;
						tempLoc = new MapLocation(i,j);
						if (closerToBase(tempLoc, maxMilk)){								
							maxMilk = tempLoc;
						}
					}
				}
			}
		}
		System.out.println("Best Milk Location at X: " + maxMilk.x + " Y: " + maxMilk.y);
		
		return maxMilk;
	}
	private static boolean closerToBase(MapLocation tempLoc, MapLocation maxMilk) {
		if (myHQ.distanceSquaredTo(tempLoc) < myHQ.distanceSquaredTo(maxMilk))
			return true;
		else
			return false;
	}
}
