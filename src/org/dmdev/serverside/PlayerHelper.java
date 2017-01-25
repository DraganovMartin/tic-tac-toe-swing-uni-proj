package org.dmdev.serverside;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHelper extends Thread {
	private PlayerHelper opponent;
	private PrintWriter toClient = null;
	private BufferedReader in = null;
	private Socket socket;

	private Game game;
	private char sign;
	private String MyName;
	private boolean GameOver = false;

	public PlayerHelper(Game game, char playerSign, Socket clientSocket) throws Exception {

		this.game = game;
		sign = playerSign;

		try {

			// Sets up client socket communication
			socket = clientSocket;
			toClient = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// first response to client after first connect
			toClient.println(ClientStatus.CLIENT_CONNECTED);
			toClient.println(ClientStatus.PLAYER_SIGN +' '+ sign);
		} catch (IOException e) {
			System.out.println("SYSTEM MSG: Unable to start player thread.");
		}

	}

	public PlayerHelper getOpponent() {
		return opponent;
	}

	public void SetOpponent(PlayerHelper NewPlayer) {
		opponent = NewPlayer;
		toClient.println(ClientStatus.OPPONENT_CONNECTED);

	}

	// thread started to listen to client's command
	public void run() {
		String commnad = null; // command = optcode + param
		String OptCode = null;
		String Param = null;

		try {

			while (!GameOver) {

				commnad = in.readLine();
				if (commnad != null) {
					OptCode = commnad.substring(0, 6);
					Param = commnad.substring(6).trim();
				}

				if (OptCode.equals("MOVETO")) {

					// the player who move first will be set as
					// currentplayer
					if (game.GetCurrentPlayer() == null)
						game.SetCurrentPlayer(this);

					// client wants to move, so go to the game to
					// validate that move
					if (game.ValidateMove(this, Integer.parseInt(Param)) == Game.VALID_MOVE) {
						toClient.println("MOVEOK " + Param);

						if (game.checkForWinner())
							toClient.println("YOUWIN");
						else if (game.Tie())
							toClient.println("GAMTIE");

					} else if (game.ValidateMove(this, Integer.parseInt(Param)) == Game.INVALID_MOVE)
						toClient.println("MOVENK");
					else if (game.ValidateMove(this, Integer.parseInt(Param)) == Game.NOT_YOUR_TURN)
						toClient.println("NOTTRN");

				} else if (OptCode.equals("MYNAME")) {

					if (game.ValidateName(this, Param) == true)
						toClient.println(ClientStatus.CLIENT_NAME +" " + Param);
					else
						toClient.println("NAMENK");

				}
				// request move back
				else if (OptCode.equals("MOVEBK")) {
					game.ValidateMoveBack(this);
				}
				// response to move back request
				else if (OptCode.equals("REQRES")) {
					game.AnnounceMoveBack(Param, this);
				}
				// when playerhelper catch the exception that a player
				// has disconnected,
				// it will tell the opponent of this player that his
				// opponent has disconnected
				else if (OptCode.equals("GMQUIT")) {
					System.out.println("SYSTEM MSG: Player " + (MyName == null ? "No Name" : MyName)
							+ " has disconnected, game stopped.");
					GameOver = true;
				}

			}

		} catch (IOException e) {
			System.out.println(
					"SYSTEM MSG: Player " + (MyName == null ? "No Name" : MyName) + " has disconnected, game stopped.");
			opponent.RecordOpponentDead();

		} finally {

			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("SYSTEM MSG: Error when closing playerhelp socket.");
			}

		}

	}

	public void RecordOpponentDead() {
		toClient.println("OPDEAD");
	}

	public void RecordOpponentMove(int location) {
		// call this player to record opponent move
		toClient.println("OPPMOV " + location);

		if (game.checkForWinner())
			toClient.println("YOULSE");
		else if (game.Tie())
			toClient.println("GAMTIE");
	}

	public String GetName() {
		return MyName;
	}
	
	public char getSign(){
		return sign;
	}
	

	public void setPlayerName(String InputName) {

		MyName = InputName;

	}

	public void RecordOpponentName(String InputName) {
		toClient.println(ClientStatus.OPPONENT_NAME + " " + InputName);
	}

	// other side request to move back
	public void RequestMoveBack() {
		toClient.println("OPPREQ");
	}

	// announce result of moveback request
	// requester is the player who make the request to move back
	// location is the square that needs to be removed
	public void MoveBack(String Requester, int Location) {
		if (Location == -1)
			// not authorize
			toClient.println("REQRES NK " + "0" + Requester);
		else
			toClient.println("REQRES OK " + Location + Requester);

	}

}