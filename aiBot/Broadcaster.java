package aiBot;

import battlecode.common.*;

public class Broadcaster {

	public static int locToInt(MapLocation loc)
	{
		return loc.x * 100 + loc.y;
	}

	public static MapLocation intToLoc(int x)
	{
		return new MapLocation(x / 100, x % 100);
	}
	public static void broadcastLocation(RobotController rc, int channel, MapLocation loc) throws GameActionException
	{
		rc.broadcast(channel, Broadcaster.locToInt(loc));
	}

	public static MapLocation readLocation(RobotController rc, int channel) throws GameActionException
	{
		return intToLoc(rc.readBroadcast(channel));
	}
}
