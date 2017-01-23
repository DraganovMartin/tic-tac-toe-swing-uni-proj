package org.dmdev.serverside;

import org.dmdev.serverside.BoardOwner;
import org.dmdev.serverside.PlayerHelper;

	public class Game {
		private PlayerHelper PHelper1 = null;
		private PlayerHelper PHelper2 = null;

		private PlayerHelper CurrentPlayer = null;
		private BoardOwner Board[]; // game board

		public static final int VALIDMOVE = 1;
		public static final int INVALIDMOVE = 2;
		public static final int NOTYOURTURN = 3;

		private int NoOfMove = 0;

		public Game() {

			Board = new BoardOwner[9];
			for (int i = 0; i < 9; i++)
				Board[i] = new BoardOwner();

		}

		public void AddPlayer(PlayerHelper NewPlayer) {
			if (PHelper1 == null)
				PHelper1 = NewPlayer;
			else
				PHelper2 = NewPlayer;
		}

		public void SetOpponent() {
			PHelper1.SetOpponent(PHelper2);
			PHelper2.SetOpponent(PHelper1);

		}

		public boolean isFull() {
			return ((PHelper1 != null) && (PHelper2 != null));
		}

		public PlayerHelper GetPlayer1() {
			return PHelper1;

		}

		public PlayerHelper GetPlayer2() {
			return PHelper2;
		}

		// both players own the game object, so we use this as the
		// monitor
		public synchronized int ValidateMove(PlayerHelper Player, int Location) {

			int ReturnValue = INVALIDMOVE;

			try {

				if (Player == CurrentPlayer) {

					// if nobody choose that location
					if (Board[Location].Player == null) {

						// assign board location and move order
						Board[Location].Player = Player;
						Board[Location].MoveOrder = NoOfMove++;

						// set new current player
						CurrentPlayer = Player.GetOpponent();

						// call current player to record opponent move first
						// (the one
						// we have just processed above) before he moves
						CurrentPlayer.RecordOpponentMove(Location);

						// notify the other waiting player he can move now
						ReturnValue = VALIDMOVE;

					} else
						ReturnValue = INVALIDMOVE;
				} else
					// not his turn
					ReturnValue = NOTYOURTURN;

			} catch (Exception e) {
				System.out.println("SYSTEM MSG: Error when validating move.");
			}

			return ReturnValue;

		}

		// validate name when user press submit
		public synchronized boolean ValidateName(PlayerHelper Player, String InputName) {

			if (Player.GetOpponent().GetName() == null || !Player.GetOpponent().GetName().equals(InputName)) {

				Player.SetName(InputName);
				Player.GetOpponent().RecordOpponentName(InputName);
				return true;
			} else
				return false;

		}

		// send a move back request to the opponent
		public synchronized void ValidateMoveBack(PlayerHelper Player) {
			Player.GetOpponent().RequestMoveBack();

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
				if (CurrentPlayer == ResponsePlayer.GetOpponent()) {

					// modify server board
					// remove O's last move
					OLastMove = GetPlayerLastMove(CurrentPlayer.GetOpponent());
					RemoveMove(OLastMove);

					// remove X's last move
					XLastMove = GetPlayerLastMove(CurrentPlayer);
					RemoveMove(XLastMove);

					// modify client's board
					// do this again in client's program
					// return the name of the requester
					CurrentPlayer.GetOpponent().MoveBack(ResponsePlayer.GetOpponent().GetName(), OLastMove);
					CurrentPlayer.GetOpponent().MoveBack(ResponsePlayer.GetOpponent().GetName(), XLastMove);

					CurrentPlayer.MoveBack(ResponsePlayer.GetOpponent().GetName(), OLastMove);
					CurrentPlayer.MoveBack(ResponsePlayer.GetOpponent().GetName(), XLastMove);

					// current player remains currentplayer

				} else
				// case 2: X move 1 step, O is making decision, X request to
				// move back
				// and O authorize it
				{

					// modify server board
					// remove X's last move
					XLastMove = GetPlayerLastMove(CurrentPlayer.GetOpponent());
					RemoveMove(XLastMove);

					// modify client's board
					// return the name of the requester
					CurrentPlayer.GetOpponent().MoveBack(ResponsePlayer.GetOpponent().GetName(), XLastMove);
					CurrentPlayer.MoveBack(ResponsePlayer.GetOpponent().GetName(), XLastMove);

					// currentplayer change back to X
					SuperSetCurrentPlayer(CurrentPlayer.GetOpponent());
				}

			} else {

				// -1 means request not authorize
				CurrentPlayer.MoveBack(ResponsePlayer.GetOpponent().GetName(), -1);
				CurrentPlayer.GetOpponent().MoveBack(ResponsePlayer.GetOpponent().GetName(), -1);
			}

		}

		// delete a move from the game board
		public void RemoveMove(int Location) {
			Board[Location].Player = null;
			Board[Location].MoveOrder = -1;

			NoOfMove--;
		}

		// get the last move of player from history
		public int GetPlayerLastMove(PlayerHelper Player) {
			int MaxMoveOrder = -1;
			int LastMove = -1;

			for (int i = 0; i < 9; i++) {
				if (Board[i].Player == Player) {

					if (Board[i].MoveOrder > MaxMoveOrder) {
						MaxMoveOrder = Board[i].MoveOrder;
						LastMove = i;
					}

				}
			}

			return LastMove;

		}

		public PlayerHelper GetCurrentPlayer() {
			return CurrentPlayer;
		}

		public synchronized void SetCurrentPlayer(PlayerHelper Player) {
			if (CurrentPlayer == null)
				CurrentPlayer = Player;
		}

		public void SuperSetCurrentPlayer(PlayerHelper Player) {
			// use in requesting move back 1 step

			CurrentPlayer = Player;
		}

		public boolean HaveWinner() {
			// check the owner of each box to see the winner

			return (((Board[0].Player != null) && (Board[0].Player == Board[1].Player)
					&& (Board[1].Player == Board[2].Player))
					|| ((Board[3].Player != null) && (Board[3].Player == Board[4].Player)
							&& (Board[4].Player == Board[5].Player))
					|| ((Board[6].Player != null) && (Board[6].Player == Board[7].Player)
							&& (Board[7].Player == Board[8].Player))
					|| ((Board[0].Player != null) && (Board[0].Player == Board[3].Player)
							&& (Board[3].Player == Board[6].Player))
					|| ((Board[1].Player != null) && (Board[1].Player == Board[4].Player)
							&& (Board[4].Player == Board[7].Player))
					|| ((Board[2].Player != null) && (Board[2].Player == Board[5].Player)
							&& (Board[5].Player == Board[8].Player))
					|| ((Board[0].Player != null) && (Board[0].Player == Board[4].Player)
							&& (Board[4].Player == Board[8].Player))
					|| ((Board[2].Player != null) && (Board[2].Player == Board[4].Player)
							&& (Board[4].Player == Board[6].Player)));
		}

		// check whether game is tie
		public boolean Tie() {
			boolean BoardIsFull = true;

			for (int i = 0; i < 9; i++) {
				// if one of the board is null, then not full -> not tie
				if (Board[i].Player == null)
					BoardIsFull = false;
			}

			// if board is full but still no winner, then tie
			if ((BoardIsFull) && (HaveWinner() == false))
				return true;
			else
				return false;

		}

	}