package aiBot;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class MapScan {
	public static int[][] coarseMap;
	public static int bigBoxSize;
	
	public static void assessMap(int bigBoxSizeIn,RobotController rc){
		bigBoxSize=bigBoxSizeIn;
		int coarseWidth = rc.getMapWidth()/bigBoxSize;
		int coarseHeight = rc.getMapHeight()/bigBoxSize;
		coarseMap = new int[coarseWidth][coarseHeight];
		for(int x=0;x<coarseWidth*bigBoxSize;x++){
			for(int y=0;y<coarseHeight*bigBoxSize;y++){
				coarseMap[x/bigBoxSize][y/bigBoxSize]+=countObstacles(x,y,rc);
			}
		}
	}

	public static int countObstacles(int x, int y,RobotController rc){//returns a 0 or a 1
		int terrainOrdinal = rc.senseTerrainTile(new MapLocation(x,y)).ordinal();//0 NORMAL, 1 ROAD, 2 VOID, 3 OFF_MAP
		return (terrainOrdinal<2?0:1);
	}
}