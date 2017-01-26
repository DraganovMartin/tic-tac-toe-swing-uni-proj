package org.dmdev.clientside;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dmdev.serverside.ServerToClientStatusCodes;

public class GUI extends JFrame {
	private static final long serialVersionUID = -6713206219193321399L;

	// Panels for every section
	private JPanel buttonPanel;
	private JPanel InputPanel;
	private JPanel histPanel;
	private JPanel otherFuncPanel;
	private JPanel statusPanel;
	private JPanel vsPanel;
	private JPanel rightPanel;
	private JPanel bottomPanel;

	// -------- LABELS ---------
	public JLabel lblStatus;
	private JLabel lblPlayerName;
	private JLabel lblYourSignMsg;
	private JLabel lblYourSign;
	private JLabel myName;
	private JLabel oppName;
	private JLabel myTurn;
	private JLabel yourTurn;

	// -------- BUTTONS ---------
	private JButton squares[];
	private JButton submit;
	private JButton btnMoveBack;
	private JButton btnStatistics;
	private JButton btnBegin, btnPrev, btnNext, btnEnd;

	private JTextField txtPlayerName;

	// -------- IMAGES ---------
	private ImageIcon myIcon;
	private ImageIcon oppIcon;
	private ImageIcon turnIcon;

	// -------- CONNECTION ---------
	private Socket socket = null;
	private PrintWriter toServer = null;
	private BufferedReader fromServer = null;
	// -------- NON-SWING HELPER VARIABLES ---------
	private boolean gameOver = false;
	private boolean gameStart = false;
	private boolean oppArrived = false;
	private boolean isSessionFull=false;
	private String sign;
	private String playerName = null;
	private String opponentName = null;
	private String history[] = new String[9];
	private int noOfMove = 0;
	private int viewPointer = -1;
	// -------- STATISTICS ---------
	private String gameResult = null;
	private String statistics[];

	// constructor
	public GUI(String TargetIp, String in_stat[], String Name) {

		super("TicTacToeClient");

		// store statistics
		statistics = new String[1000];
		statistics = in_stat;

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		// setting up the socket
		try {
			socket = new Socket(TargetIp, 1357);
			toServer = new PrintWriter(socket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("SYSTEM MSG: Unknown host:" + TargetIp);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("SYSTEM MSG: Server " + TargetIp + " has no response. Game stopped");
			System.exit(1);
		}

		// *************layout the interface**************
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		// create a button panel to hold 9 squares
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 3, 0, 0));

		squares = new JButton[9];

		// initialize 9 squares
		for (int i = 0; i < squares.length; i++) {
			final int j = i;

			squares[i] = new JButton("");

			squares[i].addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {

					// when user press a square, moveto will send to the
					// server(PlayerHelper)
					if (gameStart && !gameOver) {
						toServer.println("MOVETO " + j);
					}
				}
			});

			buttonPanel.add(squares[i]);
		}

		// create other components

		// right-handed side of button panel
		InputPanel = new JPanel();
		InputPanel.setLayout(new BoxLayout(InputPanel, BoxLayout.Y_AXIS));

		lblPlayerName = new JLabel("Player Name: ");
		txtPlayerName = new JTextField(10);
		if (Name != null)
			txtPlayerName.setText(Name);
		lblYourSignMsg = new JLabel("Your sign is:");
		lblYourSign = new JLabel(" ");
		lblYourSign.setBorder(BorderFactory.createEtchedBorder());

		submit = new JButton("Submit");
		submit.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {

				if (oppArrived) {
					if (!txtPlayerName.getText().isEmpty()) {
						// send your name to the server to check
						toServer.println("MYNAME " + txtPlayerName.getText());
					} else
						showErrMessageDialog("Please enter your name before pressing Submit",
								"TicTacToe Error Message");

				} else
					showErrMessageDialog("Please wait until game start", "TicTacToe Error Message");

			}
		});

		InputPanel.add(lblPlayerName);
		InputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		InputPanel.add(txtPlayerName);
		InputPanel.add(submit);
		InputPanel.add(Box.createRigidArea(new Dimension(0, 30)));
		InputPanel.add(lblYourSignMsg);
		InputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		InputPanel.add(lblYourSign);

		rightPanel = new JPanel();
		rightPanel.add(InputPanel);

		// bottom of button panel
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		vsPanel = new JPanel();
		vsPanel.setLayout(new BoxLayout(vsPanel, BoxLayout.X_AXIS));

		myName = new JLabel(" ");
		oppName = new JLabel(" ");
		myTurn = new JLabel(" ");
		yourTurn = new JLabel(" ");
		turnIcon = new ImageIcon("reddot1.gif");

		vsPanel.add(Box.createRigidArea(new Dimension(140, 0)));
		vsPanel.add(myTurn);
		vsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		vsPanel.add(myName);
		vsPanel.add(new JLabel("   VS   "));
		vsPanel.add(oppName);
		vsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		vsPanel.add(yourTurn);
		vsPanel.add(Box.createHorizontalGlue());

		histPanel = new JPanel();
		histPanel.setLayout(new BoxLayout(histPanel, BoxLayout.X_AXIS));
		btnBegin = new JButton("BEGIN");
		btnPrev = new JButton("PREV");
		btnNext = new JButton("NEXT");
		btnEnd = new JButton("END");
		DirectionListener directionListener = new DirectionListener();
		btnBegin.addActionListener(directionListener);
		btnPrev.addActionListener(directionListener);
		btnNext.addActionListener(directionListener);
		btnEnd.addActionListener(directionListener);
		histPanel.add(Box.createRigidArea(new Dimension(65, 0)));
		histPanel.add(btnBegin);
		histPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		histPanel.add(btnPrev);
		histPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		histPanel.add(btnNext);
		histPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		histPanel.add(btnEnd);
		histPanel.add(Box.createHorizontalGlue());

		otherFuncPanel = new JPanel();
		otherFuncPanel.setLayout(new BoxLayout(otherFuncPanel, BoxLayout.X_AXIS));
		btnMoveBack = new JButton("Take back move");
		btnStatistics = new JButton("statistics");
		btnMoveBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// request if not blank
				if (iHaveMoved() && !gameOver) {
					if (confirmRequest()) {
						// request to take back 1 move
						toServer.println("MOVEBK");
						lblStatus.setText("Status: You move back request is sent, waiting for response...");
					} else
						lblStatus.setText("Status: Move back request is cancelled");

				} else {
					if (!gameOver)
						showErrMessageDialog("Status: Move back request invalid. You have not made you move yet",
								"TicTacToe Error Message");
				}

			}
		});

		btnStatistics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float NoOfTie = 0;
				float NoOfWin = 0;
				float NoOfLose = 0;
				float Total = 0;
				float TiePercent = 0;
				float WinPercent = 0;
				float LosPercent = 0;
				String Output = null;

				for (int i = 1; i < statistics.length && statistics[i] != null; i++) {
					if (statistics[i].equals("WIN"))
						NoOfWin++;
					else if (statistics[i].equals("LOS"))
						NoOfLose++;
					else if (statistics[i].equals("TIE"))
						NoOfTie++;

				}

				Total = NoOfWin + NoOfLose + NoOfTie;

				if (Total > 0) {
					WinPercent = (NoOfWin / Total) * 100;
					LosPercent = (NoOfLose / Total) * 100;
					TiePercent = (NoOfTie / Total) * 100;
				}

				Output = "Game statistics: \n" + "Total number of games played: " + (int) Total
						+ "\nNumber of games WIN: " + (int) NoOfWin + "\nNumber of games LOSE: " + (int) NoOfLose
						+ "\nNumber of games TIE: " + (int) NoOfTie + "\n" + "\n Average winning percentage is: "
						+ WinPercent + "%" + "\n Average losing percentage is: " + LosPercent + "%"
						+ "\n Average tieing percentage is: " + TiePercent + "%";

				showStatDialog(Output);

			}
		});

		otherFuncPanel.add(Box.createRigidArea(new Dimension(100, 0)));
		otherFuncPanel.add(btnMoveBack);
		otherFuncPanel.add(btnStatistics);
		otherFuncPanel.add(Box.createHorizontalGlue());

		statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		lblStatus = new JLabel("Status:");
		statusPanel.add(lblStatus);
		statusPanel.add(Box.createHorizontalGlue());

		bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		bottomPanel.add(vsPanel);
		bottomPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		bottomPanel.add(histPanel);
		bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		bottomPanel.add(otherFuncPanel);
		bottomPanel.add(statusPanel);

		// add all components to the content pane
		c.add(buttonPanel);
		c.add(bottomPanel, BorderLayout.SOUTH);
		c.add(rightPanel, BorderLayout.EAST);

		setSize(550, 500); // original 300, 300
		setVisible(true);
	}

	// client listen to server (PlayerHelper) response
	public void Play() throws Exception {
		String serverResponse = null;
		String OptionCode = null;
		String parameter = null;

		try {

			while (!gameOver) {
				serverResponse = fromServer.readLine();
				if(-1 == serverResponse.indexOf(' ')){
					OptionCode = serverResponse;
				}else{
					OptionCode = serverResponse.substring(0,serverResponse.indexOf(' '));
					parameter = serverResponse.substring(serverResponse.indexOf(' ')).trim();
				}

				if (OptionCode.equals(ServerToClientStatusCodes.CLIENT_CONNECTED)) {
					lblStatus.setText("Status: You are online, please wait for your opponent...");

				}
				// opponent has arrived
				else if (OptionCode.equals(ServerToClientStatusCodes.OPPONENT_CONNECTED)) {
					lblStatus.setText("Status: Another player has connected, please submit your name to start the game");
					oppArrived = true;
				}
				// your sign
				else if (OptionCode.equals(ServerToClientStatusCodes.PLAYER_SIGN)) {

					sign = parameter;

					if (parameter.equals("x")) {
						myIcon = new ImageIcon("cross.gif");
						oppIcon = new ImageIcon("circle.gif");

					} else {
						myIcon = new ImageIcon("./circle.gif");
						oppIcon = new ImageIcon("./cross.gif");

					}

					lblYourSign.setIcon(myIcon);
					lblYourSign.repaint();

				} else if (OptionCode.equals("OPPNAM")) {
					opponentName = parameter;
					oppName.setText(parameter);
					lblStatus.setText("Status: Your opponent is " + parameter);

					if (playerName != null)
						gameStart = true;

				} else if (OptionCode.equals("NAMEOK")) {			// my name is not like the opponent's

					playerName = parameter;
					myName.setText(parameter);
					lblStatus.setText("Status: Welcome, " + parameter);

					if (opponentName != null)
						gameStart = true;

					submit.setEnabled(false);
					txtPlayerName.setEnabled(false);

				}
				// name not ok
				else if (OptionCode.equals("NAMENK")) {

					showErrMessageDialog("Status: You name is already chosen by the opponent, please choose another",
							"TicTacToe Error Message");

				} else if (OptionCode.equals("MOVEOK")) {

					lblStatus.setText("Status: Waiting for opponent to move..");
					myTurn.setIcon(null);
					myTurn.repaint();
					yourTurn.setIcon(turnIcon);
					yourTurn.repaint();

					// store my move
					history[noOfMove++] = "MY" + parameter;
					viewPointer = noOfMove - 1;

					cleanAndRepaint();

				}

				else if (OptionCode.equals("MOVENK")) {		// u can move again, choose another correct square

					lblStatus.setText("Status: This square is already occupied, please choose another");
				} else if (OptionCode.equals("OPPMOV")) {

					lblStatus.setText("Status: Opponent moved. Your turn..");
					myTurn.setIcon(turnIcon);
					myTurn.repaint();
					yourTurn.setIcon(null);
					yourTurn.repaint();

					// store opponent's move
					history[noOfMove++] = "OP" + parameter;
					viewPointer = noOfMove - 1;

					cleanAndRepaint();

				}
				// not your turn
				else if (OptionCode.equals("NOTTRN")) {
					lblStatus.setText("Status: Not your turn, waiting for opponent to move..");
				}
				// opponent request to move back
				else if (OptionCode.equals("OPPREQ")) {
					int response = JOptionPane.showConfirmDialog(this, "Opponent requests to move back 1 step",
							"Tic Tac Toe ", JOptionPane.YES_NO_OPTION);

					if (JOptionPane.YES_OPTION == response)
						toServer.println("REQRES YES");
					else
						toServer.println("REQRES NO");
				}
				// receive request response
				else if (OptionCode.equals("REQRES")) {
					int location;
					String request = null;

					location = Integer.parseInt(parameter.substring(3, 4));
					request = parameter.substring(4).trim();

					// request is ok
					if (parameter.substring(0, 2).equals("OK")) {

						// reset history
						deleteMoveFromHistory(location);

						// repaint all square
						// cos user may be reviewing history while he is making
						// request
						cleanAndRepaint();

						if (request.equals(playerName)) {
							lblStatus.setText("Status: You have moved back one step, it is your turn now...");

							myTurn.setIcon(turnIcon);
							myTurn.repaint();
							yourTurn.setIcon(null);
							yourTurn.repaint();
						} else {
							lblStatus.setText("Status: Move back request granted, waiting for opponent to move...");

							myTurn.setIcon(null);
							myTurn.repaint();
							yourTurn.setIcon(turnIcon);
							yourTurn.repaint();
						}

					} else {// request not ok

						if (request.equals(playerName))
							lblStatus.setText("Status: The other side has denied your move back request");
						else
							lblStatus.setText("Status: You have denied the move back request");

					}

				}

				else if (OptionCode.equals("OPDEAD")) {
					lblStatus.setText("Status: Opponent has disconnected. Game stopped.");
					showErrMessageDialog("Status: Opponent has disconnected. Game stopped.", "TicTacToe Error Message");
					gameOver = true;

				} else if (OptionCode.equals("YOUWIN")) {
					lblStatus.setText("Status: You Win!!!");
					gameResult = "WIN";

					myTurn.setIcon(null);
					myTurn.repaint();
					yourTurn.setIcon(null);
					yourTurn.repaint();

					gameOver = true;

				} else if (OptionCode.equals("YOULSE")) {
					lblStatus.setText("Status: You Lose..");
					gameResult = "LOS";

					myTurn.setIcon(null);
					myTurn.repaint();
					yourTurn.setIcon(null);
					yourTurn.repaint();

					gameOver = true;

				} else if (OptionCode.equals("GAMTIE")) {
					lblStatus.setText("Status: Game Tie.");
					gameResult = "TIE";

					myTurn.setIcon(null);
					myTurn.repaint();
					yourTurn.setIcon(null);
					yourTurn.repaint();

					gameOver = true;
				}
			}

			// call player thread to stop
			toServer.println("GMQUIT");

		} catch (IOException e) {
			System.err.println("SYSTEM MSG: Server down! Game stopped.");
			lblStatus.setText("Status: Server down! Game stopped.");
			showErrMessageDialog("Status: Server down! Game stopped.", "TicTacToe Error Message");

			gameOver = true;
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("SYSTEM MSG: Error when closing client socket.");
			}
		}

	}

	public String getPlayerName() {
		return playerName;
	}

	public void showStatDialog(String output) {
		JOptionPane.showMessageDialog(this, output, playerName + " " + "statistics", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showErrMessageDialog(String output, String title) {
		JOptionPane.showMessageDialog(this, output, title, JOptionPane.ERROR_MESSAGE);
	}

	public String getGameResult() {
		return gameResult;
	}

	public void cleanAndRepaint() {
		for (int i = 0; i < 9; i++) {
			squares[i].setIcon(null);
			squares[i].repaint();
		}

		repaintAllSquare();
	}

	public void repaintAllSquare() {

		for (int i = 0; i <= noOfMove - 1; i++) {
			if (history[i].substring(0, 2).equals("MY"))
				squares[Integer.parseInt(history[i].substring(2).trim())].setIcon(myIcon);
			else
				squares[Integer.parseInt(history[i].substring(2).trim())].setIcon(oppIcon);

			squares[Integer.parseInt(history[i].substring(2).trim())].repaint();
		}
	}

	public boolean playAgain() {

		String temp = null;

		if (lblStatus.getText().equals("Status: You Win!!!") || lblStatus.getText().equals("Status: You Lose..")
				|| lblStatus.getText().equals("Status: Game Tie."))
			temp = lblStatus.getText();
		else
			temp = "";

		int response = JOptionPane.showConfirmDialog(this, temp + " Do you want to play again?", "Tic Tac Toe",
				JOptionPane.YES_NO_OPTION);

		if (JOptionPane.YES_OPTION == response)
			return true;
		else {
			lblStatus.setText("Status: You have been disconnected from server");
			return false;
		}

	}

	public void deleteMoveFromHistory(int location) {

		int histLoc;

		for (int i = 0; i < noOfMove; i++) {

			histLoc = Integer.parseInt(history[i].substring(2).trim());

			// delete this move
			if (histLoc == location) {
				// if it is the first or last, just delete it, no need to
				// rearrange the value
				if ((i == 0) || (i == noOfMove - 1))
					history[i] = null;
				else {
					for (int j = i; j < noOfMove - 1; j++)
						history[j] = history[j + 1];
				}
			}
		}

		noOfMove--;
		viewPointer = noOfMove - 1;

	}

	public boolean iHaveMoved() {
		String histPlayer = null;
		boolean result = false;

		for (int i = 0; i < noOfMove; i++) {
			histPlayer = history[i].substring(0, 2).trim();

			if (histPlayer.equals("MY")) {
				result = true;
				break;

			}

		}

		return result;

	}

	public boolean confirmRequest() {

		int response = JOptionPane.showConfirmDialog(this, "Are you sure to request move back one step?",
				"Tic Tac Toe ", JOptionPane.YES_NO_OPTION);

		if (JOptionPane.YES_OPTION == response)
			return true;
		else
			return false;

	}

	// review history by clicking the begin, prev, next and end button
	private class DirectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// View pointer points to the square that the player is viewing
			if (e.getSource() == btnBegin) {
				viewPointer = -1;

				for (int i = 0; i < 9; i++) {
					squares[i].setIcon(null);
					squares[i].repaint();
				}

				lblStatus.setText("Status: First move fromServer history...");

			} else if (e.getSource() == btnPrev) {
				if (viewPointer >= 0) {

					int pointingSquare = Integer.parseInt(history[viewPointer].substring(2).trim());

					squares[pointingSquare].setIcon(null);
					squares[pointingSquare].repaint();

					viewPointer--;
				}

				lblStatus.setText("Status: Reviewing previous move...");

			}

			else if (e.getSource() == btnNext) {

				if (!(viewPointer == (noOfMove - 1))) {
					viewPointer++;

					int pointingSquare = Integer.parseInt(history[viewPointer].substring(2).trim());

					if (history[viewPointer].substring(0, 2).equals("MY"))
						squares[pointingSquare].setIcon(myIcon);
					else
						squares[pointingSquare].setIcon(oppIcon);

					squares[pointingSquare].repaint();

					lblStatus.setText("Status: Reviewing previous move...");

				} else
				// last move
				{

					lblStatus.setText("Status: Last move fromServer history");

				}

			} else if (e.getSource() == btnEnd) {

				viewPointer = noOfMove - 1;

				for (int i = 0; i <= noOfMove - 1; i++) {
					repaintAllSquare();
				}

				lblStatus.setText("Status: Last move fromServer history");

			}
		}
	}
}
