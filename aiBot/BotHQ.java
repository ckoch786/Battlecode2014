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
		rallyP    = mapCenter;
		
		pathQ  = new LinkedList<MapLocation[]>();
		targetsPathed = new ArrayList<MapLocation>();
		
		initMap();
		Broadcaster.broadcastLocation(rc, 10, rallyP); //broadcast rally location on channel 10
		//pastrBuild = pRound();
	}
	public static void run(RobotController rc) throws GameActionException{
		tryToSpawn();
	}
	private static void tryToSpawn() throws GameActionException
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
						Clock.getRoundNum();
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
		for (int j = 0; j <= height; j++)
		{
			line = "";
			for (int i = 0; i<= width; i++)
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
}
