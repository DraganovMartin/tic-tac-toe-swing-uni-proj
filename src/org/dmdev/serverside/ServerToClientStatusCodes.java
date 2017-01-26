/**
 * @authors Marto,Dimcho
 */

package org.dmdev.serverside;

public abstract class ServerToClientStatusCodes {
	public static final String CLIENT_CONNECTED = "connected";
	public static final String CLIENT_NAME = "MYNAME";
	public static final String OPPONENT_CONNECTED= "op_connected";
	public static final String PLAYER_SIGN = "signSet";
	public static final String OPPONENT_NAME = "OPPNAM";
	public static final String MOVE_ACCEPTED = "MOVEOK";
	public static final String GAME_WON = "YOUWIN";
	public static final String GAME_TIE = "GAMTIE";
	public static final String MOVE_NOT_OKEY = "MOVENK";
	public static final String NOT_YOUR_TURN = "NOTTRN";
	public static final String NAME_NOT_OKEY = "NAMENK";
	public static final String NAME_OKEY = "NAMEOK";
	public static final String OPPONENT_DISCONNECTED = "OPDEAD";
	public static final String OPPONENT_MOVE = "OPPMOV";
	public static final String YOU_LOSE = "YOULSE";
	public static final String OPPONENT_REQUEST_MOVE_BACK = "OPPREQ";
	public static final String REQUEST_RESULT_OK = "REQRES OK";
	public static final String REQUEST_RESULT_NOT_OK = "REQRES NK";


}
