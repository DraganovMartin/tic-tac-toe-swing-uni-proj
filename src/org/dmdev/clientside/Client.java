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
import java.io.File;
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

public class Client extends JFrame {
	private static final long serialVersionUID = -6713206219193321399L;
	private JPanel buttonPanel;
	private JButton squares[];
	public JLabel lblStatus;

	private JPanel InputPanel;
	private JLabel lblPlayerName;
	private JTextField txtPlayerName;
	private JButton submit;
	private JLabel lblYourSignMsg;
	private JLabel lblYourSign;

	private JPanel HistPanel;
	private JButton BtnBegin, BtnPrev, BtnNext, BtnEnd;

	private JPanel OtherFuncPanel;
	private JButton BtnMoveBack;
	private JButton BtnStat;

	private JPanel StatusPanel;

	private JPanel VsPanel;
	private JLabel MyName;
	private JLabel OppName;
	private ImageIcon TurnIcon;
	private JLabel MyTurn;
	private JLabel YourTurn;

	private JPanel RightPanel;
	private JPanel BottomPanel;

	private Socket TTTSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private boolean GameOver = false;
	private boolean GameStart = false;
	private boolean OppArrived = false;
	private String Sign;
	private ImageIcon MyIcon;
	private ImageIcon OppIcon;
	private String PlayerName = null;
	private String OpponentName = null;

	private String History[] = new String[9];
	private int NoOfMove = 0;
	private int ViewPointer = -1;

	// statistics
	private String GameResult = null;
	private String Statistics[];

	// constructor
	public Client(String TargetIp, String in_stat[], String Name) {

		super("TicTacToeClient");

		// store statistics
		Statistics = new String[1000];
		Statistics = in_stat;

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		// setting up the socket
		try {
			TTTSocket = new Socket(TargetIp, 1357);
			out = new PrintWriter(TTTSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(TTTSocket.getInputStream()));
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
					// playerhelper
					if (GameStart && !GameOver) {
						out.println("MOVETO " + j);
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

				if (OppArrived) {
					if (txtPlayerName.getText().trim().length() > 0) {
						// send your name to the server to check
						out.println("MYNAME " + txtPlayerName.getText());
					} else
						ShowErrMessageDialog("Please enter your name before pressing Submit",
								"TicTacToe Error Message");

				} else
					ShowErrMessageDialog("Please wait until game start", "TicTacToe Error Message");

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

		RightPanel = new JPanel();
		RightPanel.add(InputPanel);

		// bottom of button panel
		BottomPanel = new JPanel();
		BottomPanel.setLayout(new BoxLayout(BottomPanel, BoxLayout.Y_AXIS));

		VsPanel = new JPanel();
		VsPanel.setLayout(new BoxLayout(VsPanel, BoxLayout.X_AXIS));

		MyName = new JLabel(" ");
		OppName = new JLabel(" ");
		MyTurn = new JLabel(" ");
		YourTurn = new JLabel(" ");
		TurnIcon = new ImageIcon("reddot1.gif");

		VsPanel.add(Box.createRigidArea(new Dimension(140, 0)));
		VsPanel.add(MyTurn);
		VsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		VsPanel.add(MyName);
		VsPanel.add(new JLabel("   VS   "));
		VsPanel.add(OppName);
		VsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		VsPanel.add(YourTurn);
		VsPanel.add(Box.createHorizontalGlue());

		HistPanel = new JPanel();
		HistPanel.setLayout(new BoxLayout(HistPanel, BoxLayout.X_AXIS));
		BtnBegin = new JButton("BEGIN");
		BtnPrev = new JButton("PREV");
		BtnNext = new JButton("NEXT");
		BtnEnd = new JButton("END");
		DirectionListener directionListener = new DirectionListener();
		BtnBegin.addActionListener(directionListener);
		BtnPrev.addActionListener(directionListener);
		BtnNext.addActionListener(directionListener);
		BtnEnd.addActionListener(directionListener);
		HistPanel.add(Box.createRigidArea(new Dimension(65, 0)));
		HistPanel.add(BtnBegin);
		HistPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		HistPanel.add(BtnPrev);
		HistPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		HistPanel.add(BtnNext);
		HistPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		HistPanel.add(BtnEnd);
		HistPanel.add(Box.createHorizontalGlue());

		OtherFuncPanel = new JPanel();
		OtherFuncPanel.setLayout(new BoxLayout(OtherFuncPanel, BoxLayout.X_AXIS));
		BtnMoveBack = new JButton("Take back move");
		BtnStat = new JButton("Statistics");
		BtnMoveBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// request if not blank
				if (IHaveMoved() && !GameOver) {
					if (ConfirmRequest() == true) {
						// request to take back 1 move
						out.println("MOVEBK");
						lblStatus.setText("Status: You move back request is sent, waiting for response...");
					} else
						lblStatus.setText("Status: Move back request is cancelled");

				} else {
					if (!GameOver)
						ShowErrMessageDialog("Status: Move back request invalid. You have not made you move yet",
								"TicTacToe Error Message");
				}

			}
		});

		BtnStat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float NoOfTie = 0;
				float NoOfWin = 0;
				float NoOfLose = 0;
				float Total = 0;
				float TiePercent = 0;
				float WinPercent = 0;
				float LosPercent = 0;
				String Output = null;

				for (int i = 1; i < Statistics.length && Statistics[i] != null; i++) {
					if (Statistics[i].equals("WIN"))
						NoOfWin++;
					else if (Statistics[i].equals("LOS"))
						NoOfLose++;
					else if (Statistics[i].equals("TIE"))
						NoOfTie++;

				}

				Total = NoOfWin + NoOfLose + NoOfTie;

				if (Total > 0) {
					WinPercent = (NoOfWin / Total) * 100;
					LosPercent = (NoOfLose / Total) * 100;
					TiePercent = (NoOfTie / Total) * 100;
				}

				Output = "Game Statistics: \n" + "Total number of games played: " + (int) Total
						+ "\nNumber of games WIN: " + (int) NoOfWin + "\nNumber of games LOSE: " + (int) NoOfLose
						+ "\nNumber of games TIE: " + (int) NoOfTie + "\n" + "\n Average winning percentage is: "
						+ WinPercent + "%" + "\n Average losing percentage is: " + LosPercent + "%"
						+ "\n Average tieing percentage is: " + TiePercent + "%";

				ShowStatDialog(Output);

			}
		});

		OtherFuncPanel.add(Box.createRigidArea(new Dimension(100, 0)));
		OtherFuncPanel.add(BtnMoveBack);
		OtherFuncPanel.add(BtnStat);
		OtherFuncPanel.add(Box.createHorizontalGlue());

		StatusPanel = new JPanel();
		StatusPanel.setLayout(new BoxLayout(StatusPanel, BoxLayout.X_AXIS));
		lblStatus = new JLabel("Status:");
		StatusPanel.add(lblStatus);
		StatusPanel.add(Box.createHorizontalGlue());

		BottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		BottomPanel.add(VsPanel);
		BottomPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		BottomPanel.add(HistPanel);
		BottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		BottomPanel.add(OtherFuncPanel);
		BottomPanel.add(StatusPanel);

		// add all components to the content pane
		c.add(buttonPanel);
		c.add(BottomPanel, BorderLayout.SOUTH);
		c.add(RightPanel, BorderLayout.EAST);

		setSize(550, 500); // original 300, 300
		setVisible(true);
	}

	// client listen to playhelper's response
	public void Play() throws Exception {
		String ServerResponse = null;
		String OptCode = null;
		String Param = null;

		try {

			while (!GameOver) {
				ServerResponse = in.readLine();
				OptCode = ServerResponse.substring(0, 6);
				Param = ServerResponse.substring(6).trim();

				if (OptCode.equals("WELCOM")) {
					lblStatus.setText("Status: CONNECTED, waiting for your opponent...");

				}
				// opponent has arrived
				else if (OptCode.equals("OPPARR")) {
					lblStatus
							.setText("Status: Another player has connected, please submit your name to start the game");
					OppArrived = true;
				}
				// your sign
				else if (OptCode.equals("YRSIGN")) {

					Sign = Param;

					if (Param.equals("x")) {
						MyIcon = new ImageIcon("cross.gif");
						OppIcon = new ImageIcon("circle.gif");

					} else {
						MyIcon = new ImageIcon("./circle.gif");
						OppIcon = new ImageIcon("./cross.gif");

					}

					lblYourSign.setIcon(MyIcon);
					lblYourSign.repaint();

				} else if (OptCode.equals("OPPNAM")) {
					OpponentName = Param;
					OppName.setText(Param);
					lblStatus.setText("Status: Your opponent is " + Param);

					if (PlayerName != null)
						GameStart = true;

				} else if (OptCode.equals("NAMEOK")) {
					// my name is ok

					PlayerName = Param;
					MyName.setText(Param);
					lblStatus.setText("Status: Welcome, " + Param);

					if (OpponentName != null)
						GameStart = true;

					submit.setEnabled(false);
					txtPlayerName.setEnabled(false);

				}
				// name not ok
				else if (OptCode.equals("NAMENK")) {
					ShowErrMessageDialog("Status: You name is already chosen by the opponent, please choose another",
							"TicTacToe Error Message");

				} else if (OptCode.equals("MOVEOK")) {

					lblStatus.setText("Status: Waiting for opponent to move..");
					MyTurn.setIcon(null);
					MyTurn.repaint();
					YourTurn.setIcon(TurnIcon);
					YourTurn.repaint();

					// store my move
					History[NoOfMove++] = "MY" + Param;
					ViewPointer = NoOfMove - 1;

					CleanAndRepaint();

				}

				else if (OptCode.equals("MOVENK")) {
					// u can move again, choose another correct square
					lblStatus.setText("Status: This square is already occupied, please choose another");
				} else if (OptCode.equals("OPPMOV")) {

					lblStatus.setText("Status: Opponent moved. Your turn..");
					MyTurn.setIcon(TurnIcon);
					MyTurn.repaint();
					YourTurn.setIcon(null);
					YourTurn.repaint();

					// store opponent's move
					History[NoOfMove++] = "OP" + Param;
					ViewPointer = NoOfMove - 1;

					CleanAndRepaint();

				}
				// not your turn
				else if (OptCode.equals("NOTTRN")) {
					lblStatus.setText("Status: Not your turn, waiting for opponent to move..");
				}
				// opponent request to move back
				else if (OptCode.equals("OPPREQ")) {
					int response = JOptionPane.showConfirmDialog(this, "Opponent requests to move back 1 step",
							"Tic Tac Toe ", JOptionPane.YES_NO_OPTION);

					if (JOptionPane.YES_OPTION == response)
						out.println("REQRES YES");
					else
						out.println("REQRES NO");
				}
				// receive request response
				else if (OptCode.equals("REQRES")) {
					int Location;
					String Requester = null;

					Location = Integer.parseInt(Param.substring(3, 4));
					Requester = Param.substring(4).trim();

					// request is ok
					if (Param.substring(0, 2).equals("OK")) {

						// reset history
						DeleteMoveFromHistory(Location);

						// repaint all square
						// cos user may be reviewing history while he is making
						// request
						CleanAndRepaint();

						if (Requester.equals(PlayerName)) {
							lblStatus.setText("Status: You have moved back one step, it is your turn now...");

							MyTurn.setIcon(TurnIcon);
							MyTurn.repaint();
							YourTurn.setIcon(null);
							YourTurn.repaint();
						} else {
							lblStatus.setText("Status: Move back request granted, waiting for opponent to move...");

							MyTurn.setIcon(null);
							MyTurn.repaint();
							YourTurn.setIcon(TurnIcon);
							YourTurn.repaint();
						}

					} else {// request not ok

						if (Requester.equals(PlayerName))
							lblStatus.setText("Status: The other side has denied your move back request");
						else
							lblStatus.setText("Status: You have denied the move back request");

					}

				}

				else if (OptCode.equals("OPDEAD")) {
					lblStatus.setText("Status: Opponent has disconnected. Game stopped.");
					ShowErrMessageDialog("Status: Opponent has disconnected. Game stopped.", "TicTacToe Error Message");
					GameOver = true;

				} else if (OptCode.equals("YOUWIN")) {
					lblStatus.setText("Status: You Win!!!");
					GameResult = "WIN";

					MyTurn.setIcon(null);
					MyTurn.repaint();
					YourTurn.setIcon(null);
					YourTurn.repaint();

					GameOver = true;

				} else if (OptCode.equals("YOULSE")) {
					lblStatus.setText("Status: You Lose..");
					GameResult = "LOS";

					MyTurn.setIcon(null);
					MyTurn.repaint();
					YourTurn.setIcon(null);
					YourTurn.repaint();

					GameOver = true;

				} else if (OptCode.equals("GAMTIE")) {
					lblStatus.setText("Status: Game Tie.");
					GameResult = "TIE";

					MyTurn.setIcon(null);
					MyTurn.repaint();
					YourTurn.setIcon(null);
					YourTurn.repaint();

					GameOver = true;
				}
			}

			// call player thread to stop
			out.println("GMQUIT");

		} catch (IOException e) {
			System.err.println("SYSTEM MSG: Server down! Game stopped.");
			lblStatus.setText("Status: Server down! Game stopped.");
			ShowErrMessageDialog("Status: Server down! Game stopped.", "TicTacToe Error Message");

			GameOver = true;
		} finally {
			try {
				TTTSocket.close();
			} catch (IOException e) {
				System.out.println("SYSTEM MSG: Error when closing client socket.");
			}
		}

	}

	public String GetName() {
		return PlayerName;
	}

	public void ShowStatDialog(String output) {
		JOptionPane.showMessageDialog(this, output, PlayerName + "Statistics", JOptionPane.INFORMATION_MESSAGE);
	}

	public void ShowErrMessageDialog(String output, String title) {
		JOptionPane.showMessageDialog(this, output, title, JOptionPane.ERROR_MESSAGE);
	}

	public String GetGameResult() {
		return GameResult;
	}

	public void CleanAndRepaint() {
		for (int i = 0; i < 9; i++) {
			squares[i].setIcon(null);
			squares[i].repaint();
		}

		for (int i = 0; i <= NoOfMove - 1; i++) {
			if (History[i].substring(0, 2).equals("MY"))
				squares[Integer.parseInt(History[i].substring(2).trim())].setIcon(MyIcon);
			else
				squares[Integer.parseInt(History[i].substring(2).trim())].setIcon(OppIcon);

			squares[Integer.parseInt(History[i].substring(2).trim())].repaint();
		}
	}

	public void RepaintAllSquare() {

		for (int i = 0; i <= NoOfMove - 1; i++) {
			if (History[i].substring(0, 2).equals("MY"))
				squares[Integer.parseInt(History[i].substring(2).trim())].setIcon(MyIcon);
			else
				squares[Integer.parseInt(History[i].substring(2).trim())].setIcon(OppIcon);

			squares[Integer.parseInt(History[i].substring(2).trim())].repaint();
		}
	}

	public boolean PlayAgain() {

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

	public void DeleteMoveFromHistory(int Location) {

		int HistLoc;

		for (int i = 0; i < NoOfMove; i++) {

			HistLoc = Integer.parseInt(History[i].substring(2).trim());

			// delete this move
			if (HistLoc == Location) {
				// if it is the first or last node, just delete it, no need to
				// rearrange the value
				if ((i == 0) || (i == NoOfMove - 1))
					History[i] = null;
				else {
					for (int j = i; j < NoOfMove - 1; j++)
						History[j] = History[j + 1];
				}
			}
		}

		NoOfMove--;
		ViewPointer = NoOfMove - 1;

	}

	public boolean IHaveMoved() {
		String HistPlayer = null;
		boolean Result = false;

		for (int i = 0; i < NoOfMove; i++) {
			HistPlayer = History[i].substring(0, 2).trim();

			if (HistPlayer.equals("MY")) {
				Result = true;
				break;

			}

		}

		return Result;

	}

	public boolean ConfirmRequest() {

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
			if (e.getSource() == BtnBegin) {
				ViewPointer = -1;

				for (int i = 0; i < 9; i++) {
					squares[i].setIcon(null);
					squares[i].repaint();
				}

				lblStatus.setText("Status: First move in history...");

			} else if (e.getSource() == BtnPrev) {
				if (ViewPointer >= 0) {

					int PointingSquare = Integer.parseInt(History[ViewPointer].substring(2).trim());

					squares[PointingSquare].setIcon(null);
					squares[PointingSquare].repaint();

					ViewPointer--;
				}

				lblStatus.setText("Status: Reviewing previous move...");

			}

			else if (e.getSource() == BtnNext) {

				if (!(ViewPointer == (NoOfMove - 1))) {
					ViewPointer++;

					int PointingSquare = Integer.parseInt(History[ViewPointer].substring(2).trim());

					if (History[ViewPointer].substring(0, 2).equals("MY"))
						squares[PointingSquare].setIcon(MyIcon);
					else
						squares[PointingSquare].setIcon(OppIcon);

					squares[PointingSquare].repaint();

					lblStatus.setText("Status: Reviewing previous move...");

				} else
				// last move
				{

					lblStatus.setText("Status: Last move in history");

				}

			} else if (e.getSource() == BtnEnd) {

				ViewPointer = NoOfMove - 1;

				for (int i = 0; i <= NoOfMove - 1; i++) {
					if (History[i].substring(0, 2).equals("MY"))
						squares[Integer.parseInt(History[i].substring(2).trim())].setIcon(MyIcon);
					else
						squares[Integer.parseInt(History[i].substring(2).trim())].setIcon(OppIcon);

					squares[Integer.parseInt(History[i].substring(2).trim())].repaint();
				}

				lblStatus.setText("Status: Last move in history");

			}
		}
	}
}
