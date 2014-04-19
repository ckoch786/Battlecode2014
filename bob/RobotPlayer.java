package bob;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class RobotPlayer{
	public static void run(RobotController rc){
		while(true){
			if(rc.getType()== RobotType.HQ){
				Direction spawnDir = Direction.NORTH;				
				try {
					if (rc.isActive() && rc.canMove(spawnDir) && rc.senseRobotCount()< GameConstants.MAX_ROBOTS)
					rc.spawn(spawnDir);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (rc.getType() == RobotType.SOLDIER){
				
				//Movement
				Direction allDirs[] = Direction.values(); //indexed 0,7
				Direction chosenDir = allDirs[(int) (Math.random() * 8)];
				if (rc.isActive() && rc.canMove(chosenDir)){
					try {
						rc.move(chosenDir);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//Combat
				Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 1000, rc.getTeam().opponent());
				if(enemies.length>0){ //enemies are sensed
					Robot badGuy = enemies[0];
					RobotInfo badGuyInfo;
					try {
						badGuyInfo = rc.senseRobotInfo(badGuy);
						if(badGuyInfo.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared){
							if(rc.isActive()){
								rc.attackSquare(badGuyInfo.location);
							}
						}
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{  //no enemies build a tower
					if(Math.random()<0.01){
						if(rc.isActive()){
							try {
								rc.construct(RobotType.PASTR);
							} catch (GameActionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			rc.yield();
		}
	}
}