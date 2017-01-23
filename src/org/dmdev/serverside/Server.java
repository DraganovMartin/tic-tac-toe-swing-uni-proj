package org.dmdev.serverside;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private Game game = new Game();
	private ServerSocket serverSocket = null;
//	private Socket clientSocket = null;

	public Server() throws Exception {

			// Setup socket
			try {
				serverSocket = new ServerSocket(1357);

			} catch (IOException e) {
				System.err.println("Error: can not listen on port 1357.");
				System.exit(1);
			}

			try {
				while (true) {
					// Every client's game is handled by the PlayerHelper thread
					// The first player gets the x 
					game.AddPlayer(new PlayerHelper(game, 'x', listen()));
					game.AddPlayer(new PlayerHelper(game, 'o', listen()));

					if (game.isFull()) {

						game.SetOpponent();
						game.GetPlayer1().start();
						game.GetPlayer2().start();

						// Start a new game if there is no more room
						game = new Game();
					}
				}
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("Error: can not close connection to server");

				}
			}
		}

	public Socket listen() {
		Socket TempSocket = null;

		// Listen for incoming connections
		try {
			System.out.println("Listening on port 1357...");

			TempSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Error: Failed to connect with client!");
			System.exit(1);
		}

		return TempSocket;

	}
}