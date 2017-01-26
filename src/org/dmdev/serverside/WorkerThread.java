package org.dmdev.serverside;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WorkerThread extends Thread {
	private WorkerThread opponent;
	private PrintWriter toClient = null;
	private BufferedReader in = null;
	private Socket socket;

	private Game game;
	private char sign;
	private String myName;
	private boolean gameIsOver = false;

	public WorkerThread(Game game, char playerSign, Socket clientSocket) throws Exception {

		this.game = game;
		sign = playerSign;

		try {
			// Sets up the client-server socket communication
			socket = clientSocket;
			toClient = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// first response to client after first connect
			// When a client is connected a connect status is sent 
			toClient.println(ServerToClientStatusCodes.CLIENT_CONNECTED);
			toClient.println(ServerToClientStatusCodes.PLAYER_SIGN +' '+ sign);
		} catch (IOException e) {
			System.out.println("SYSTEM MSG: Unable to start player thread.");
		}

	}

	public WorkerThread getOpponent() {
		return opponent;
	}

	public void setOpponent(WorkerThread player) {
		opponent = player;
		toClient.println(ServerToClientStatusCodes.OPPONENT_CONNECTED);

	}

	/**
	 * Listens for client commands.
	 * 
	 * The command consists of a status code and a parameter
	 */
	public void run() {
		String commnad = null;
		String statusCode = null;
		String param = null;

		try {

			while (!gameIsOver) {

				commnad = in.readLine();
				if (commnad != null) {
					statusCode = commnad.substring(0, 6);
					param = commnad.substring(6).trim();
				}

				if (statusCode.equals("MOVETO")) {

					// the player who move first will be set as
					// currentplayer
					if (game.getCurrentPlayer() == null)
						game.setCurrentPlayer(this);

					// client wants to move, so go to the game to
					// validate that move
					if (game.validateMove(this, Integer.parseInt(param)) == Game.VALID_MOVE) {
						toClient.println("MOVEOK " + param);

						if (game.checkForWinner())
							toClient.println("YOUWIN");
						else if (game.isGameTie())
							toClient.println("GAMTIE");

					} else if (game.validateMove(this, Integer.parseInt(param)) == Game.INVALID_MOVE)
						toClient.println("MOVENK");
					else if (game.validateMove(this, Integer.parseInt(param)) == Game.NOT_YOUR_TURN)
						toClient.println("NOTTRN");

				} else if (statusCode.equals("MYNAME")) {

					if (game.ValidateName(this, param) == true)
						toClient.println("MYNAME " +" " + param);
					else
						toClient.println("NAMENK");

				}
				// request move back
				else if (statusCode.equals("MOVEBK")) {
					game.validateOneMoveBack(this);
				}
				// response to move back request
				else if (statusCode.equals("REQRES")) {
					game.AnnounceMoveBack(param, this);
				}
				// when playerhelper catch the exception that a player
				// has disconnected,
				// it will tell the opponent of this player that his
				// opponent has disconnected
				else if (statusCode.equals("GMQUIT")) {
					System.out.println("SYSTEM MSG: Player " + (myName == null ? "No Name" : myName)
							+ " has disconnected, game stopped.");
					gameIsOver = true;
				}

			}

		} catch (IOException e) {
			System.out.println(
					"SYSTEM MSG: Player " + (myName == null ? "No Name" : myName) + " has disconnected, game stopped.");
			opponent.sendOpponentDisconnectedStatus();

		} finally {

			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("SYSTEM MSG: Error when closing playerhelp socket.");
			}

		}

	}

	public void sendOpponentDisconnectedStatus() {
		toClient.println("OPDEAD");
	}

	public void recordOpponentMove(int location) {
		// call this player to record opponent move
		toClient.println("OPPMOV " + location);

		if (game.checkForWinner())
			toClient.println("YOULSE");
		else if (game.isGameTie())
			toClient.println("GAMTIE");
	}

	public String getPlayerName() {
		return myName;
	}
	
	public char getSign(){
		return sign;
	}
	

	public void setPlayerName(String inputName) {

		myName = inputName;

	}

	public void saveOpponentName(String inputName) {
		toClient.println("OPPNAM " + inputName);
	}

	// other side request to move back
	public void requestMoveBack() {
		toClient.println("OPPREQ");
	}

	// announce result of moveback request
	// requester is the player who make the request to move back
	// location is the square that needs to be removed
	public void MoveBack(String requester, int loc) {
		if (loc == -1)
			// not authorize
			toClient.println("REQRES NK " + "0" + requester);
		else
			toClient.println("REQRES OK " + loc + requester);

	}

}