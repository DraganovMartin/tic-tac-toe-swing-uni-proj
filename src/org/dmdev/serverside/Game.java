package org.dmdev.serverside;

/**
 * Handles game related operations like adding players, validating moves, check
 * for winner
 * @author dimcho
 */
public class Game {

	private WorkerThread currentPlayer = null;
	private InputSquare board[];

	private WorkerThread firstWorker = null;
	private WorkerThread secondWorker = null;

	public static final int VALID_MOVE = 1;
	public static final int INVALID_MOVE = 2;
	public static final int NOT_YOUR_TURN = 3;

	private int moveCnt = 0;

	public Game() {
		// Crates new InputSquares to create the board
		board = new InputSquare[9];
		for (int i = 0; i < 9; i++)
			board[i] = new InputSquare();
	}

	/**
	 * Add the player with its worker thread
	 * 
	 * @param player
	 *            the player to add
	 */
	public void addPlayer(WorkerThread player) {
		if (firstWorker == null)
			firstWorker = player;
		else
			secondWorker = player;
	}

	public boolean hasEnoughPlayers() {
		return ((firstWorker != null) && (secondWorker != null));
	}

	/**
	 * Sets the opponent for both players
	 */
	public void setOpponent() {
		firstWorker.setOpponent(secondWorker);
		secondWorker.setOpponent(firstWorker);

	}

	public WorkerThread getFirstPlayer() {
		return firstWorker;

	}

	public WorkerThread getSecondPlayer() {
		return secondWorker;
	}

	public WorkerThread getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Sets the current player to move
	 * 
	 * @param player
	 *            the player which to set for current
	 */
	public synchronized void setCurrentPlayer(WorkerThread player) {
		if (currentPlayer == null)
			currentPlayer = player;
	}

	/**
	 * Sets the current player when a move back is made
	 * 
	 * @param player
	 */
	public void superSetCurrentPlayer(WorkerThread player) {
		currentPlayer = player;
	}

	// Since both the workers access the game object, this method is used as the
	// monitor
	/**
	 * Validates a players move and checks for probable winners
	 * 
	 * @param player
	 * @param location
	 * @return the status
	 */
	public synchronized int validateMove(WorkerThread player, int location) {

		int returnStaus = INVALID_MOVE;

		try {

			if (player == currentPlayer) {
				if (board[location].player == null) {

					// Assigns the board location the player who clicked it
					board[location].player = player;
					board[location].moveNumber = moveCnt++;

					// Sets the opponent as the current player
					currentPlayer = player.getOpponent();

					// Record the previous current player's move
					currentPlayer.recordOpponentMove(location);

					// Notify the other waiting player that it's his turn
					returnStaus = VALID_MOVE;

				} else
					returnStaus = INVALID_MOVE;
			} else
				// Return status not your turn
				returnStaus = NOT_YOUR_TURN;

		} catch (Exception e) {
			System.out.println("SYSTEM MSG: Error when validating move.");
			e.printStackTrace();
		}

		return returnStaus;

	}

	/**
	 * Validates the palyer's name. Checks the if the opponent's name is the
	 * same
	 * 
	 * @param player
	 * @param inputName
	 * @return
	 */
	public synchronized boolean validateName(WorkerThread player, String inputName) {

		if (player.getOpponent().getPlayerName() == null || !player.getOpponent().getPlayerName().equals(inputName)) {

			player.setPlayerName(inputName);
			player.getOpponent().saveOpponentName(inputName);

			return true;
		} else
			return false;

	}

	// Send a move back request to the opponent
	public synchronized void validateOneMoveBack(WorkerThread Player) {
		Player.getOpponent().requestMoveBack();

	}

	/**
	 * Announce the result of the move back request
	 * 
	 * @param result
	 *            the result of the request
	 * @param authorizedPlayer
	 *            the other player which is playing
	 */
	public synchronized void announceMoveBack(String result, WorkerThread authorizedPlayer) {
		int xLastMove, oLastMove;

		// If the result is YES move back
		if (result.equals("YES")) {
			if (currentPlayer == authorizedPlayer.getOpponent()) {

				// Remove the move from the board object and remove the last
				// move
				oLastMove = getPlayerLastMove(currentPlayer.getOpponent());
				removeMove(oLastMove);

				// Remove the move from the board object and remove the last
				// move
				xLastMove = getPlayerLastMove(currentPlayer);
				removeMove(xLastMove);

				// Update the client's GUI
				currentPlayer.getOpponent().moveBack(authorizedPlayer.getOpponent().getPlayerName(), oLastMove);
				currentPlayer.getOpponent().moveBack(authorizedPlayer.getOpponent().getPlayerName(), xLastMove);

				currentPlayer.moveBack(authorizedPlayer.getOpponent().getPlayerName(), oLastMove);
				currentPlayer.moveBack(authorizedPlayer.getOpponent().getPlayerName(), xLastMove);

			} else {

				xLastMove = getPlayerLastMove(currentPlayer.getOpponent());
				removeMove(xLastMove);

				currentPlayer.getOpponent().moveBack(authorizedPlayer.getOpponent().getPlayerName(), xLastMove);
				currentPlayer.moveBack(authorizedPlayer.getOpponent().getPlayerName(), xLastMove);

				superSetCurrentPlayer(currentPlayer.getOpponent());
			}

		} else {

			// If result is -1 the move back was not authorized
			currentPlayer.moveBack(authorizedPlayer.getOpponent().getPlayerName(), -1);
			currentPlayer.getOpponent().moveBack(authorizedPlayer.getOpponent().getPlayerName(), -1);
		}

	}

	public boolean checkForWinner() {
		return new WinnerChecker(board).checkAll();
	}

	public boolean isGameTie() {
		boolean boardIsFull = true;

		for (int i = 0; i < 9; i++) {
			// if one of the board is null, then not full -> not tie
			if (board[i].player == null)
				boardIsFull = false;
		}

		// if board is full but still no winner, then tie
		if ((boardIsFull) && (checkForWinner() == false))
			return true;
		else
			return false;

	}

	/**
	 * Removes a move from the board
	 * 
	 * @param location
	 *            the location of the square on the board
	 */
	public void removeMove(int location) {
		board[location].player = null;
		board[location].moveNumber = -1;

		moveCnt--;
	}

	/**
	 * Gets the players previous move from the Statistics.
	 * 
	 * @param player
	 *            the player for which to get the last move
	 * @return the player's last move
	 */
	public int getPlayerLastMove(WorkerThread player) {
		int MaxMoveOrder = -1;
		int lastMove = -1;

		for (int i = 0; i < 9; i++) {
			if (board[i].player == player) {

				if (board[i].moveNumber > MaxMoveOrder) {
					MaxMoveOrder = board[i].moveNumber;
					lastMove = i;
				}

			}
		}

		return lastMove;

	}

}
