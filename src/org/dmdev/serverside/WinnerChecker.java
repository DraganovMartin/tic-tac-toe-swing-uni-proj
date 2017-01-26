package org.dmdev.serverside;

/**
 * Checks game for winners
 * @author dimcho
 *
 */
public class WinnerChecker {
	private InputSquare[] board;
	
	public WinnerChecker(InputSquare[] board) {
		this.board = board;
	}
	
	/**
	 * Checks all the rows for a win
	 * @return true if a winner was found false otherwise
	 */
	public boolean checkRows(){
		for (int i = 0; i <3; i++) {
			if((board[i].player != null) && (board[i].player == board[i+1].player)
					&& (board[i+1].player == board[i+2].player)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks all the columns for a win
	 * @return true if a winner was found false otherwise
	 */
	public boolean checkColumns(){
		for (int i = 0; i <3; i++) {
			if((board[i].player != null) && (board[i].player == board[i+3].player)
					&& (board[i+3].player == board[i+3+3].player)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the diagonals for a win
	 * @return true if a winner was found false otherwise
	 */
	public boolean checkDiagonals(){
		for (int i = 0; i <2; i++) {
			if((board[i].player != null) && (board[i].player == board[4].player)
					&& (board[4].player == board[8-2*i].player)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the whole board for a winner
	 * @return true if a winner was found false otherwise
	 */
	public boolean checkAll(){
		if(checkRows() || checkColumns() ||checkDiagonals()){
			return true;
		}
		
		return false;
	}
}
