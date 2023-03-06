import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * GUI to show the Othello game and to listen for input for the user/human player. When it is the user's turn, 
 * (s)he clicks on the place where (s)he wants to place a token, and when it is the computer's turn, the 
 * player needs to click anywhere in the frame to make the computer take it's turn. The user is made aware
 * of any illegal moves, or when (s)he - or the computer - have to pass because no legal moves are possible.  
 * @author Mai Ajspur
 * @version 9.2.2018
 */
public class OthelloGUI extends JComponent implements MouseListener
{
    static final long 	serialVersionUID = 1234567890;
	static final int 	imgSize = 100;
	
	private GameState state;		// The state of the game
    private int size;				// Number of rows and columns on the board
    private boolean humanPlayer;	// Whether a human player is playing or not
    private IOthelloAI ai1;			// The AI for player 1 if there are no human player
    private IOthelloAI ai2;			// The AI for player 2 

    // Images for drawing the game board
    private Image 		part, blackPion, whitePion, background;
    private Image 		border_left, border_right, border_top, border_bottom;
    private Image 		corner_left_top, corner_left_bottom, corner_right_top, corner_right_bottom;
    private Image 		blackWon, whiteWon, tie;
	
    /**
     * Initializes game
     * @param player1 The AI for player 1 (black); this argument is ignored if there is indeed a human player.
     * @param player2 The AI for player 2 (white).
     * @param size The number of rows and columns of the game board. Should be an
     * even number greater or equal to 4.
     * @param humanPlayer true if there is a (one) human player
     */
    public OthelloGUI(IOthelloAI ai1, IOthelloAI ai2, int size, boolean humanPlayer) throws IOException{
   		part = ImageIO.read(new File("imgs/maze.png"));
    	blackPion = ImageIO.read(new File("imgs/blackPion.png"));
    	whitePion = ImageIO.read(new File("imgs/whitePion.png"));
    	background = ImageIO.read(new File("imgs/background.png"));
    	border_left = ImageIO.read(new File("imgs/border_left.png"));
    	border_right = ImageIO.read(new File("imgs/border_right.png"));
    	border_top = ImageIO.read(new File("imgs/border_top.png"));
    	border_bottom = ImageIO.read(new File("imgs/border_bottom.png"));
    	corner_left_top = ImageIO.read(new File("imgs/corner_left_top.png"));
    	corner_left_bottom = ImageIO.read(new File("imgs/corner_left_bottom.png"));
    	corner_right_top = ImageIO.read(new File("imgs/corner_right_top.png"));
    	corner_right_bottom = ImageIO.read(new File("imgs/corner_right_bottom.png"));
    	blackWon = ImageIO.read(new File("imgs/blackWon.png"));
    	whiteWon = ImageIO.read(new File("imgs/whiteWon.png"));
    	tie = ImageIO.read(new File("imgs/tie.png"));
		
    	this.size = size;
    	this.state = new GameState(size, 1); // Player 1 (human if there is any) goes first
    	this.humanPlayer = humanPlayer;
    	if ( !humanPlayer )
    		this.ai1 = ai1;
    	this.ai2=ai2;
    	this.addMouseListener(this);
    }

    /**
     * Draws the current game board and shows if someone won.
     */
    public void paint(Graphics g){
		int[][] gameBoard = state.getBoard();
    	this.setDoubleBuffered(true);
    	Insets in = getInsets();               
    	g.translate(in.left, in.top);            

    	for (int c = 0; c < size; c++){
    		for (int r = 0; r < size; r++){
    			int player = gameBoard[c][r];
    			if (player == 0) // background
    				g.drawImage(background, imgSize+imgSize*c, imgSize+imgSize*r, this);
    			else if (player == 2) // white = player2
    				g.drawImage(whitePion, imgSize+imgSize*c, imgSize+imgSize*r, this);
    			else // black = player1
    				g.drawImage(blackPion, imgSize+imgSize*c, imgSize+imgSize*r, this);
    			g.drawImage(part, imgSize+imgSize*c, imgSize+imgSize*r, this);
    			if (c == 0){
    				g.drawImage(border_left, 0, imgSize+imgSize*r, this); 
    				g.drawImage(border_right, size*imgSize+imgSize, imgSize+imgSize*r, this); 
    			}
    		}
    		g.drawImage(border_top, imgSize+imgSize*c, 0, this);
    		g.drawImage(border_bottom, imgSize+imgSize*c, size*imgSize+imgSize, this);
    	}
    	g.drawImage(corner_left_top, 0, 0, this);
    	g.drawImage(corner_left_bottom, 0, size*imgSize+imgSize, this);
    	g.drawImage(corner_right_top, imgSize+imgSize*size, 0, this);
    	g.drawImage(corner_right_bottom, imgSize+imgSize*size, size*imgSize+imgSize, this);
		
    	if ( state.isFinished() ){
    		int[] tokens = state.countTokens();
    		if ( tokens[0] > tokens[1] )
    			g.drawImage(blackWon, size*imgSize/2-(imgSize/2), size*imgSize/2+(imgSize/4), this);
    		else if ( tokens[0] < tokens[1] )
    			g.drawImage(whiteWon, size*imgSize/2-(imgSize/2), size*imgSize/2+(imgSize/4), this);
    		else
    			g.drawImage(tie, size*imgSize/2-(imgSize/2), size*imgSize/2+(imgSize/4), this);
    	}		
    }

    public void mouseClicked(MouseEvent e){
    	int currentPlayer = state.getPlayerInTurn();
    	if ( !state.isFinished() ){
    		Position place = getPlaceForNextToken(e);
    		if ( state.insertToken(place) ){ // Chosen move is legal
				boolean nextPlayerCannotMove = state.legalMoves().isEmpty();
   				if ( nextPlayerCannotMove ){ // The next player cannot move
					repaint();
   					state.changePlayer();
   					if ( humanPlayer ){ // If there is a human involved, (s)he needs to know this
   	  					boolean canMoveAfterwards = !state.legalMoves().isEmpty();
   	   					if ( canMoveAfterwards ){
   	   						String message = currentPlayer == 1 ? "Your opponent has no legal moves. It is your turn again." 
   	   													 	    : "You have no legal moves. Your opponent will make another move (click again).";
   	   						JOptionPane.showMessageDialog(this, message);
   	   					}  						
   					}
   				}
 			}
   			else 
   				illegalMoveAttempted(place); 		
    		repaint();
    	}
    }
    
    /**
     * Get a position to place the next token (i.e. read mouse click if human
     * player is in turn, otherwise ask corresponding AI)
     */
    private Position getPlaceForNextToken(MouseEvent e){
    	if ( state.getPlayerInTurn() == 2 ) 
			return ai2.decideMove(state);
		else {
			if ( humanPlayer )
				return humanSelectedPlace(e);
			else
				return ai1.decideMove(state);
		}
    }

    /**
     * Display message for when an illegal move has been attempted 
     */
    private void illegalMoveAttempted(Position place){
    	int currentPlayer = state.getPlayerInTurn();
    	if ( humanPlayer && currentPlayer == 1 )
    		JOptionPane.showMessageDialog(this, "That is not a legal move (position " + place +"). Try again.");
		else {
			JOptionPane.showMessageDialog(this, "The AI for player "+ currentPlayer + 
				(currentPlayer == 1 ? " (black)" : " (white)") + " chose an invalid "
   				+ "move (position " + place +"). Please debug!", "Invalid Move", JOptionPane.ERROR_MESSAGE); 		
		}
    }
    
    /**
     * Translate the given clicks on the screen to a position on the game board
     */
    private Position humanSelectedPlace(MouseEvent e){
    	int x = e.getX();
    	int y = e.getY();
    	if ( imgSize <= x && x <= imgSize*(size+1) && imgSize <= y && y <= imgSize*(size+1) ){ 
    		return new Position((x-imgSize)/imgSize, (y-imgSize)/imgSize);
    	}
    	return new Position(-1,-1);
    }

    // Not used methods from the interface of MouseListener 
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
}

