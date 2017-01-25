package org.dmdev.serverside;

import org.dmdev.serverside.InputSquare;
import org.dmdev.serverside.PlayerHelper;

	public class Game {
		private PlayerHelper PHelper1 = null;
		private PlayerHelper PHelper2 = null;

		private PlayerHelper currentPlayer = null;
		private InputSquare board[]; // game board

		public static final int VALID_MOVE = 1;
		public static final int INVALID_MOVE = 2;
		public static final int NOT_YOUR_TURN = 3;

		private int NoOfMove = 0;

		public Game() {

			board = new InputSquare[9];
			for (int i = 0; i < 9; i++)
				board[i] = new InputSquare();

		}

		public void AddPlayer(PlayerHelper player) {
			if (PHelper1 == null)
				PHelper1 = player;
			else
				PHelper2 = player;
		}

		public void setOpponent() {
			PHelper1.SetOpponent(PHelper2);
			PHelper2.SetOpponent(PHelper1);

		}

		public boolean hasEnoughPlayers() {
			return ((PHelper1 != null) && (PHelper2 != null));
		}

		public PlayerHelper getFirstPlayer() {
			return PHelper1;

		}

		public PlayerHelper getSecondPlayer() {
			return PHelper2;
		}

		// both players own the game object, so we use this as the
		// monitor
		public synchronized int ValidateMove(PlayerHelper player, int location) {

			int ReturnValue = INVALID_MOVE;

			try {

				if (player == currentPlayer) {

					// if nobody choose that location
					if (board[location].player == null) {

						// assign board location and move order
						board[location].player = player;
						board[location].MoveOrder = NoOfMove++;

						// set new current player
						currentPlayer = player.getOpponent();

						// call current player to record opponent move first
						// (the one
						// we have just processed above) before he moves
						currentPlayer.RecordOpponentMove(location);

						// notify the other waiting player he can move now
						ReturnValue = VALID_MOVE;

					} else
						ReturnValue = INVALID_MOVE;
				} else
					// not his turn
					ReturnValue = NOT_YOUR_TURN;

			} catch (Exception e) {
				System.out.println("SYSTEM MSG: Error when validating move.");
				System.err.println(e.getMessage());
			}

			return ReturnValue;

		}

		// validate name when user press submit
		public synchronized boolean ValidateName(PlayerHelper Player, String InputName) {

			if (Player.getOpponent().GetName() == null || !Player.getOpponent().GetName().equals(InputName)) {

				Player.setPlayerName(InputName);
				Player.getOpponent().RecordOpponentName(InputName);
				return true;
			} else
				return false;

		}

		// send a move back request to the opponent
		public synchronized void ValidateMoveBack(PlayerHelper Player) {
			Player.getOpponent().RequestMoveBack();

		}

		// once the opponent response to the move back request(either deny
		// or confirm)
		// game will announce the move back decision
		// ResponsePlayer is the player who authorize the other to move back
		public synchronized void AnnounceMoveBack(String Result, PlayerHelper ResponsePlayer) {
			int XLastMove, OLastMove;

			if (Result.equals("YES")) {
				// ok to move back

				// case 1: X move 1 step, O move 1 step,
				// X make the request in his turn and O authorize it
				if (currentPlayer == ResponsePlayer.getOpponent()) {

					// modify server board
					// remove O's last move
					OLastMove = GetPlayerLastMove(currentPlayer.getOpponent());
					RemoveMove(OLastMove);

					// remove X's last move
					XLastMove = GetPlayerLastMove(currentPlayer);
					RemoveMove(XLastMove);

					// modify client's board
					// do this again in client's program
					// return the name of the requester
					currentPlayer.getOpponent().MoveBack(ResponsePlayer.getOpponent().GetName(), OLastMove);
					currentPlayer.getOpponent().MoveBack(ResponsePlayer.getOpponent().GetName(), XLastMove);

					currentPlayer.MoveBack(ResponsePlayer.getOpponent().GetName(), OLastMove);
					currentPlayer.MoveBack(ResponsePlayer.getOpponent().GetName(), XLastMove);

					// current player remains currentplayer

				} else
				// case 2: X move 1 step, O is making decision, X request to
				// move back
				// and O authorize it
				{

					// modify server board
					// remove X's last move
					XLastMove = GetPlayerLastMove(currentPlayer.getOpponent());
					RemoveMove(XLastMove);

					// modify client's board
					// return the name of the requester
					currentPlayer.getOpponent().MoveBack(ResponsePlayer.getOpponent().GetName(), XLastMove);
					currentPlayer.MoveBack(ResponsePlayer.getOpponent().GetName(), XLastMove);

					// currentplayer change back to X
					SuperSetCurrentPlayer(currentPlayer.getOpponent());
				}

			} else {

				// -1 means request not authorize
				currentPlayer.MoveBack(ResponsePlayer.getOpponent().GetName(), -1);
				currentPlayer.getOpponent().MoveBack(ResponsePlayer.getOpponent().GetName(), -1);
			}

		}

		// delete a move from the game board
		public void RemoveMove(int Location) {
			board[Location].player = null;
			board[Location].MoveOrder = -1;

			NoOfMove--;
		}

		// get the last move of player from history
		public int GetPlayerLastMove(PlayerHelper Player) {
			int MaxMoveOrder = -1;
			int LastMove = -1;

			for (int i = 0; i < 9; i++) {
				if (board[i].player == Player) {

					if (board[i].MoveOrder > MaxMoveOrder) {
						MaxMoveOrder = board[i].MoveOrder;
						LastMove = i;
					}

				}
			}

			return LastMove;

		}

		public PlayerHelper GetCurrentPlayer() {
			return currentPlayer;
		}

		public synchronized void SetCurrentPlayer(PlayerHelper Player) {
			if (currentPlayer == null)
				currentPlayer = Player;
		}

		public void SuperSetCurrentPlayer(PlayerHelper Player) {
			// use in requesting move back 1 step

			currentPlayer = Player;
		}

		public boolean checkForWinner() {
			// check the owner of each box to see the winner

			return new WinnerChecker(board).checkAll();
			
//			return ((board[0].player != null) && (board[0].player) == board[1].player)
//					&& (board[1].player == board[2].player) 
//					|| ((board[3].player != null) && (board[3].player == board[4].player)
//							&& (board[4].player == board[5].player))
//					|| ((board[6].player != null) && (board[6].player == board[7].player)
//							&& (board[7].player == board[8].player))
//					|| ((board[0].player != null) && (board[0].player == board[3].player)
//							&& (board[3].player == board[6].player))
//					|| ((board[1].player != null) && (board[1].player == board[4].player)
//							&& (board[4].player == board[7].player))
//					|| ((board[2].player != null) && (board[2].player == board[5].player)
//							&& (board[5].player == board[8].player))
//					|| ((board[0].player != null) && (board[0].player == board[4].player)
//							&& (board[4].player == board[8].player))
//					|| ((board[2].player != null) && (board[2].player == board[4].player)
//							&& (board[4].player == board[6].player));
		}

		// check whether game is tie
		public boolean Tie() {
			boolean BoardIsFull = true;

			for (int i = 0; i < 9; i++) {
				// if one of the board is null, then not full -> not tie
				if (board[i].player == null)
					BoardIsFull = false;
			}

			// if board is full but still no winner, then tie
			if ((BoardIsFull) && (checkForWinner() == false))
				return true;
			else
				return false;

		}

	}
