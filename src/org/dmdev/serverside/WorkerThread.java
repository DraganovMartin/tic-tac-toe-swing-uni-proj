package org.dmdev.serverside;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles client operations
 * @author dimcho
 *
 */
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

					// The first player that moves will be set as the current player
					if (game.getCurrentPlayer() == null)
						game.setCurrentPlayer(this);

					// Validate the move when a client makes a move
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

					if (game.validateName(this, param) == true)
						toClient.println("MYNAME " +" " + param);
					else
						toClient.println("NAMENK");

				}
				// Requests a move back
				else if (statusCode.equals("MOVEBK")) {
					game.validateOneMoveBack(this);
				}
				// Listen for response from move back request
				else if (statusCode.equals("REQRES")) {
					game.announceMoveBack(param, this);
				}
				// If a player disconnects a message will be printed
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
		// Record opponent move
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

	/**
	 * Requests one move back from the other player
	 */
	public void requestMoveBack() {
		toClient.println("OPPREQ");
	}

	/**
	 * Checks the result from the move back request
	 * 
	 * @param moveBackRequestSender the sender of the request
	 * @param squareLocToRemove the location on the board to reset
	 */
	public void moveBack(String moveBackRequestSender, int squareLocToRemove) {
		if (squareLocToRemove == -1)
			
			toClient.println("REQRES NK " + "0" + moveBackRequestSender);
		else
			toClient.println("REQRES OK " + squareLocToRemove + moveBackRequestSender);

	}

}