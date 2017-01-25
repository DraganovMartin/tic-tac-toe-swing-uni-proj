package org.dmdev.serverside;

public class WinnerChecker {
	private InputSquare[] board;
	
	public WinnerChecker(InputSquare[] board) {
		this.board = board;
	}
	
	public boolean checkRows(){
		for (int i = 0; i <3; i++) {
			if((board[i].player != null) && (board[i].player == board[i+1].player)
					&& (board[i+1].player.getSign() == board[1+2].player.getSign())){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkColumns(){
		for (int i = 0; i <3; i++) {
			if((board[i].player != null) && (board[i].player == board[i+3].player)
					&& (board[i+3].player == board[1+3+3].player)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkDiagonals(){
		for (int i = 0; i <2; i++) {
			if((board[i].player != null) && (board[i].player == board[4].player)
					&& (board[4].player == board[8-2*i].player)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkAll(){
		if(checkRows() || checkColumns() ||checkDiagonals()){
			return true;
		}
		
		return false;
	}
}
