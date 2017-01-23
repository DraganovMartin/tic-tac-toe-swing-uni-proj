package org.dmdev.serverside;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHelper extends Thread {
	private PlayerHelper Opponent;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Socket socket;

	private Game game;
	private char sign;
	private String MyName;
	private boolean GameOver = false;

	public PlayerHelper(Game gm, char tempsign, Socket clientSocket) throws Exception {

		game = gm;
		sign = tempsign;

		try {

			// client connects and we setup clientsocket to communicate
			// with client
			socket = clientSocket;
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// first response to client after first connect
			out.println("WELCOM");
			out.println("YRSIGN " + sign);
			out.flush();
		} catch (IOException e) {
			System.out.println("SYSTEM MSG: Unable to start player thread.");
		}

	}

	public PlayerHelper GetOpponent() {
		return Opponent;
	}

	public void SetOpponent(PlayerHelper NewPlayer) {
		Opponent = NewPlayer;
		out.println("OPPARR");

	}

	// thread started to listen to client's command
	public void run() {
		String Command = null; // command = optcode + param
		String OptCode = null;
		String Param = null;

		try {

			while (!GameOver) {

				Command = in.readLine();
				if (Command != null) {
					OptCode = Command.substring(0, 6);
					Param = Command.substring(6).trim();
				}

				if (OptCode.equals("MOVETO")) {

					// the player who move first will be set as
					// currentplayer
					if (game.GetCurrentPlayer() == null)
						game.SetCurrentPlayer(this);

					// client wants to move, so go to the game to
					// validate that move
					if (game.ValidateMove(this, Integer.parseInt(Param)) == game.VALIDMOVE) {
						out.println("MOVEOK " + Param);

						if (game.HaveWinner())
							out.println("YOUWIN");
						else if (game.Tie())
							out.println("GAMTIE");

					} else if (game.ValidateMove(this, Integer.parseInt(Param)) == game.INVALIDMOVE)
						out.println("MOVENK");
					else if (game.ValidateMove(this, Integer.parseInt(Param)) == game.NOTYOURTURN)
						out.println("NOTTRN");

				} else if (OptCode.equals("MYNAME")) {

					if (game.ValidateName(this, Param) == true)
						out.println("NAMEOK " + Param);
					else
						out.println("NAMENK");

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
			Opponent.RecordOpponentDead();

		} finally {

			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("SYSTEM MSG: Error when closing playerhelp socket.");
			}

		}

	}

	public void RecordOpponentDead() {
		out.println("OPDEAD");
	}

	public void RecordOpponentMove(int Loc) {
		// call this player to record opponent move
		out.println("OPPMOV " + Loc);

		if (game.HaveWinner())
			out.println("YOULSE");
		else if (game.Tie())
			out.println("GAMTIE");
	}

	public String GetName() {
		return MyName;
	}

	public void SetName(String InputName) {

		MyName = InputName;

	}

	public void RecordOpponentName(String InputName) {
		out.println("OPPNAM " + InputName);
	}

	// other side request to move back
	public void RequestMoveBack() {
		out.println("OPPREQ");
	}

	// announce result of moveback request
	// requester is the player who make the request to move back
	// location is the square that needs to be removed
	public void MoveBack(String Requester, int Location) {
		if (Location == -1)
			// not authorize
			out.println("REQRES NK " + "0" + Requester);
		else
			out.println("REQRES OK " + Location + Requester);

	}

}