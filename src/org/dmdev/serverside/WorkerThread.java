package org.dmdev.serverside;

import org.dmdev.clientside.ClientToServerStatusCode;

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
	private BufferedReader fromClient = null;
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
			fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

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
		String command = null;
		String statusCode = null;
		String param = null;

		try {

			while (!gameIsOver) {

				command = fromClient.readLine();
				if (command != null) {
					statusCode = command.substring(0, 6);
					param = command.substring(6).trim();
				}

				if (statusCode.equals(ClientToServerStatusCode.SEND_MOVE)) {
					// The first player that moves will be set as the current player
					if (game.getCurrentPlayer() == null)
						game.setCurrentPlayer(this);

					// Validate the move when a client makes a move
					if (game.validateMove(this, Integer.parseInt(param)) == Game.VALID_MOVE) {
						toClient.println(ServerToClientStatusCodes.MOVE_ACCEPTED +" "+param);

						if (game.checkForWinner())
							toClient.println(ServerToClientStatusCodes.GAME_WON);
						else if (game.isGameTie())
							toClient.println(ServerToClientStatusCodes.GAME_TIE);

					} else if (game.validateMove(this, Integer.parseInt(param)) == Game.INVALID_MOVE)
						toClient.println(ServerToClientStatusCodes.MOVE_NOT_OKEY);
					else if (game.validateMove(this, Integer.parseInt(param)) == Game.NOT_YOUR_TURN)
						toClient.println(ServerToClientStatusCodes.NOT_YOUR_TURN);

				} else if (statusCode.equals(ServerToClientStatusCodes.CLIENT_NAME)) {

					if (game.validateName(this, param))
						toClient.println(ServerToClientStatusCodes.NAME_OKEY +" " + param);
					else
						toClient.println(ServerToClientStatusCodes.NAME_NOT_OKEY);

				}
				// Requests a move back
				else if (statusCode.equals(ClientToServerStatusCode.MOVE_BACK)) {
					game.validateOneMoveBack(this);
				}
				// Listen for response from move back request
				else if (statusCode.equals("REQRES")) {
					game.announceMoveBack(param, this);
				}
				// If a player disconnects a message will be printed
				else if (statusCode.equals(ClientToServerStatusCode.GAME_QUIT)) {
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
		toClient.println(ServerToClientStatusCodes.OPPONENT_CONNECTED);
	}

	public void recordOpponentMove(int location) {
		// Record opponent move
		toClient.println(ServerToClientStatusCodes.OPPONENT_MOVE +" "+ location);

		if (game.checkForWinner())
			toClient.println(ServerToClientStatusCodes.YOU_LOSE);
		else if (game.isGameTie())
			toClient.println(ServerToClientStatusCodes.GAME_TIE);
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
		toClient.println(ServerToClientStatusCodes.OPPONENT_NAME + " " + inputName);
	}

	/**
	 * Requests one move back from the other player
	 */
	public void requestMoveBack() {
		toClient.println(ServerToClientStatusCodes.OPPONENT_REQUEST_MOVE_BACK);
	}

	/**
	 * Checks the result from the move back request
	 * 
	 * @param moveBackRequestSender the sender of the request
	 * @param squareLocToRemove the location on the board to reset
	 */
	public void moveBack(String moveBackRequestSender, int loc) {
		if (loc == -1)
			// not authorize
			toClient.println(ServerToClientStatusCodes.REQUEST_RESULT_NOT_OK+" " + "0" + moveBackRequestSender);
		else
			toClient.println(ServerToClientStatusCodes.REQUEST_RESULT_OK+" " + loc + moveBackRequestSender);

	}

}