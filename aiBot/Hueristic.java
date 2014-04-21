package aiBot;

import java.util.ArrayList;

import battlecode.common.*;

public class Hueristic {
	
	public MapLocation loc;//the proposed outermost location
	public Direction dir;//the direction to the outermost location
	public int dist;//the distance via the route implied
	
	public Hueristic(MapLocation toMapLoc,Direction fromDirection, int fromDistance){
		loc=toMapLoc;
		dir=fromDirection;
		dist=fromDistance;
	}
	
	//Adds Heuristic to Breadth First Cost
	public static void generateHueristics(MapLocation locus, int distToLocus,int incrementalDist,ArrayList<Hueristic> hList, Direction[] consideredDirs){
		for(Direction d:consideredDirs){
			Hueristic h;
			if(d.isDiagonal()){
				h = new Hueristic(locus.add(d),d,distToLocus+incrementalDist*14);
			}else{
				h = new Hueristic(locus.add(d),d,distToLocus+incrementalDist*10);
			}
			int val = AStar.getMapData(h.loc);
			if(val>0){//not off-map or entirely void-filled
				h.dist+=Math.pow((val-10000),2)*10;//TODO evaluate fudge factor of 10 for importance of void spaces
				hList.add(h);
			}
		}
	}
	
}