package org.dmdev.serverside;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private Game game = new Game();
	private ServerSocket serverSocket;
	private static final int LISTEN_PORT = 1357;
	private final char[] signs = { 'x', 'o' };

	public Server() throws Exception {

		// Sets up the server socket
		try {
			serverSocket = new ServerSocket(LISTEN_PORT);

		} catch (IOException e) {
			System.err.println("Error: failed to listen on port " + LISTEN_PORT + ".");
			System.exit(1);
		}

		try {
			int signNum = 0;
			Socket player = null;
			System.out.println("Listening on port " + LISTEN_PORT + "...");
			while (!game.hasEnoughPlayers()) {
				player = waitForPlayers();
				// Every client's game is handled by the PlayerHelper thread
				// The first player gets the x
				WorkerThread pHelper = new WorkerThread(game, signs[signNum++], player);
				game.AddPlayer(pHelper);

				pHelper.start();
			}
			
			game.setOpponent();

			// // Every client's game is handled by the PlayerHelper thread
			// // The first player gets the x
			// game.AddPlayer(new PlayerHelper(game, 'x', waitForPlayers()));
			// game.AddPlayer(new PlayerHelper(game, 'o', waitForPlayers()));
			//
			// if (game.hasEnoughPlayers()) {
			//
			// game.setOpponent();
			// game.getFirstPlayer().start();
			// game.getSecondPlayer().start();
			//
			// // Start a new game if there is no more room
			// game = new Game();
			// }
		} finally {
			try {
				serverSocket.close();
			} catch (IOException err) {
				System.out.println("Error: can not close connection to server");

			}
		}
	}

	/**
	 * Accepts client connections
	 * @return the client socket
	 */
	public Socket waitForPlayers() {
		Socket clientSocket = null;

		// Listen for incoming connections
		try {

			clientSocket = serverSocket.accept();
		} catch (IOException err) {
			System.err.println("Error: Failed to connect with client!");
			System.exit(1);
		}

		return clientSocket;

	}
}