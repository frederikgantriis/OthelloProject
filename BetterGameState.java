import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class to represent the state of a game of Othello.  The state is defined by a 2-dimensional
 * board and whose turn it is.
 *
 * @author Mai Ajspur & Jonas Jørgensen
 * @version 9.2.2023
 */
public class BetterGameState {
    private final int[][] board;        // Possible values: 0 (empty), 1 (black), 2 (white)
    private int currentPlayer;    // The player who is next to put a token on the board. Value is 1 or 2.
    private final int size;            // The number of columns = the number of rows on the board

    //************ Constructors ****************//

    /**
     * Initializes a square board with the number of columns and rows equal to the given size.
     * The two middle positions on the left-leaning diagonal contains tokens for black (player 1),
     * the two middle positions on the right-leaning diagonal contains token for white (player 2).
     *
     * @param size          Number of columns (and number of rows) in the board. Should be an even number
     *                      greater or equal to 4.
     * @param playerToStart The player who will go first. Should be 1 (black) or 2 (white).
     */
    public BetterGameState(int size, int playerToStart) {
        this.size = size;
        board = new int[size][size];
        currentPlayer = playerToStart;
        int half = size / 2 - 1;
        board[half][half] = 1;
        board[half + 1][half + 1] = 1;
        board[half][half + 1] = 2;
        board[half + 1][half] = 2;
    }

    /**
     * Constructs a new game state that equals the one represented by the supplied board and player.
     *
     * @param board            The 2 dimensions of the array should have equal length, and possible values should be
     *                         0 (empty), 1 (black) or 2 (white).
     * @param playerToTakeTurn The player who will be the first to take a turn. Should be 1 (black)
     *                         or 2 (white)
     */
    public BetterGameState(int[][] board, int playerToTakeTurn) {
        this.size = board.length;
        this.board = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, size);
        }
        this.currentPlayer = playerToTakeTurn;
    }

    public BetterGameState(GameState s) {
        this(s.getBoard(), s.getPlayerInTurn());
    }

    public BetterGameState(BetterGameState s) {
        this(s.getBoard(), s.getPlayerInTurn());
    }

    //************ Getter methods *******************//

    /**
     * Returns the array representing the board of this game state
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Returns the player whose turn it is, i.e. 1 (black) or 2 (white).
     */
    public int getPlayerInTurn() {
        return currentPlayer;
    }

    //************* Methods ****************//

    /**
     * Skips the turn of the current player (without) changing the board.
     */
    public void changePlayer() {
        currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    /**
     * Returns true if the game is finished (i.e. none of the players can make any legal moves)
     * and false otherwise.
     */
    public boolean isFinished() {
        if (legalMoves().hasNext())
            return false;
        else { //current player has no legal moves
            changePlayer();
            if (!legalMoves().hasNext()) //next player also has no legal moves
                return true;
            else {
                changePlayer();
                return false;
            }
        }
    }

    /**
     * Counts tokens of the player 1 (black) and player 2 (white), respectively, and returns an array
     * with the numbers in that order.
     */
    public int[] countTokens() {
        int tokens1 = 0;
        int tokens2 = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 1)
                    tokens1++;
                else if (board[i][j] == 2)
                    tokens2++;
            }
        }
        return new int[]{tokens1, tokens2};
    }

    /**
     * If it is legal for the current player to put a token at the given place, then the token is inserted, the required
     * tokens from the opponent is turned, and true is returned. If the move is not legal, false is returned.
     * False is also returned if the given place does not represent a place on the board.
     */
    public boolean insertToken(Position place) {
        if (place.col < 0 || place.row < 0 || place.col >= size || place.row >= size) //not a position on the board
            return false;
        if (board[place.col][place.row] != 0) // The position is not empty
            return false;

        boolean capturesFound = false;
        // Capturing all possible opponents of the current player
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaY = -1; deltaY <= 1; deltaY++) {
                int captives = captureInDirection(place, deltaX, deltaY);
                if (captives > 0) {
                    capturesFound = true;
                    for (int i = 1; i <= captives; i++)
                        board[place.col + deltaX * i][place.row + deltaY * i] = currentPlayer;
                }
            }
        }

        if (capturesFound) {
            // Place the token at the given place
            board[place.col][place.row] = currentPlayer;
            this.changePlayer();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether the position is occupied by the opponent.
     */
    public boolean isOpponent(int x, int y) {
        return !(x < 0 || x >= size || y < 0 || y >= size) && board[x][y] == (getPlayerInTurn() & 1) + 1;
    }

    /**
     * Returns whether the position is likely to constitute a legal move for the current player. This never returns
     * false for any legal move, but might return true for an illegal move.
     */
    public boolean likelyLegal(int x, int y) {
        if (board[x][y] != 0) {
            return false;
        }

        return isOpponent(x - 1, y - 1) ||
                isOpponent(x - 1, y) ||
                isOpponent(x - 1, y + 1) ||
                isOpponent(x, y - 1) ||
                isOpponent(x, y + 1) ||
                isOpponent(x + 1, y - 1) ||
                isOpponent(x + 1, y) ||
                isOpponent(x + 1, y + 1);
    }

    /**
     * Returns an iterator of all the positions on the board that are likely to constitutes a legal move for the current
     * player. This is a superset of legalMoves().
     */
    public Iterator<Position> likelyPositions() {
        return new Iterator<>() {
            int i, j = -1;
            boolean empty, advance = true;

            @Override
            public boolean hasNext() {
                if (!advance) {
                    return !empty;
                }

                do {
                    if (j < size - 1) {
                        j++;
                    } else {
                        j = 0;
                        if (i < size - 1) {
                            i++;
                        } else {
                            empty = true;
                            break;
                        }
                    }
                } while (!likelyLegal(i, j));

                advance = false;
                return !empty;
            }

            @Override
            public Position next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("empty");
                } else {
                    advance = true;
                    return new Position(i, j);
                }
            }
        };
    }

    /**
     * Returns an iterator of all the positions on the board that constitutes a legal move for the current player.
     */
    public Iterator<Position> legalMoves() {
        return new Iterator<>() {
            final Iterator<Position> positions = likelyPositions();
            Position next;
            boolean advance = true;

            @Override
            public boolean hasNext() {
                if (!advance) {
                    return next != null;
                }

                while (positions.hasNext()) {
                    var p = positions.next();
                    for (int deltax = -1; deltax <= 1; deltax++) {
                        for (int deltay = -1; deltay <= 1; deltay++) {
                            if (captureInDirection(p, deltax, deltay) > 0) {
                                advance = false;
                                next = p;
                                return true;
                            }
                        }
                    }
                }

                advance = false;
                next = null;
                return false;
            }

            @Override
            public Position next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("empty");
                } else {
                    advance = true;
                    return next;
                }
            }
        };
    }

    /**
     * Checks how many tokens of the opponent the player can capture in the direction given by deltaX and deltaY
     * if the player puts a token at the given position.
     *
     * @param p      A position on the board
     * @param deltaX The step to be taken in the x-direction. Should be -1 (left), 0 (none), or 1 (right).
     * @param deltaY The step to be taken in the delta direction. Should be -1 (up), 0 (none), or 1 (down).
     */
    public int captureInDirection(Position p, int deltaX, int deltaY) {
        int opponent = (currentPlayer == 1 ? 2 : 1);

        int captured = 0;
        int cc = p.col;
        int rr = p.row;
        while (0 <= cc + deltaX && cc + deltaX < size && 0 <= rr + deltaY && rr + deltaY < size
                && board[cc + deltaX][rr + deltaY] == opponent) {
            cc = cc + deltaX;
            rr = rr + deltaY;
            captured++;
        }
        if (0 <= cc + deltaX && cc + deltaX < size && 0 <= rr + deltaY && rr + deltaY < size
                && board[cc + deltaX][rr + deltaY] == currentPlayer && captured > 0) {
            return captured;
        } else
            return 0;
    }

}
