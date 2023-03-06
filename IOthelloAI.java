/**
 * Interface containing the method that an AI needs to implement to play Othello.
 *  
 * @author Mai Ajspur
 * @version 9.2.2018
 */
public interface IOthelloAI {
	
	/**
	 * Calculates the move to make for the given game state.
	 * @param s The current state of the game in which it should be the AI's turn.
	 * @return the position where the AI wants to put its token. 
	 * Is only called when a move is possible, but feel free to return 
	 * e.g. (-1, -1) if no moves are possible.
	 */
	public Position decideMove(GameState s);
	
}
