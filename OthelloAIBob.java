import java.util.*;
import java.util.function.BiFunction;

public class OthelloAIBob extends CornersMovesTokens {
}

class Corners extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        return Heuristic.corners(s, player);
    }
}

class Tokens extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        return Heuristic.tokens(s, player);
    }

    @Override
    public boolean isCutOff(int depth) {
        return depth >= 8;
    }
}

class Moves extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        return Heuristic.moves(s);
    }

    @Override
    public boolean isCutOff(int depth) {
        return depth >= 6;
    }
}

class CornersTokens extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        return 100 * Heuristic.corners(s, player) + Heuristic.tokens(s, player);
    }
}

class MovesTokens extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        return 100 * Heuristic.moves(s) + Heuristic.tokens(s, player);
    }

    @Override
    public boolean isCutOff(int depth) {
        return depth >= 6;
    }
}

class CornersMovesTokens extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        return 10000 * Heuristic.corners(s, player) + 100 * Heuristic.moves(s) + Heuristic.tokens(s, player);
    }

    @Override
    public boolean isCutOff(int depth) {
        return depth >= 6;
    }
}

class RandomAI implements IOthelloAI {
    Random random = new Random();

    public Position decideMove(GameState s) {
        var moves = s.legalMoves();
        return moves.get(random.nextInt(moves.size()));
    }
}

class Heuristic {
    public static int moves(BetterGameState s) {
        // Get as few moves for the opponent as possible

        s.changePlayer();

        var count = 0;
        for (var it = s.legalMoves(); it.hasNext(); ) {
            it.next();
            count++;
        }

        s.changePlayer();

        return -count;
    }

    public static int tokens(BetterGameState s, int player) {
        // Get as many tokens as possible.

        var opponent = player == 1 ? 2 : 1;
        var playerTokens = s.countTokens(player);
        var opponentTokens = s.countTokens(opponent);

        return (int)((playerTokens - opponentTokens) / (playerTokens + opponentTokens + .0) * 100);
    }

    public static int corners(BetterGameState s, int player) {
        // Get as many corners as possible.

        var opponent = player == 1 ? 2 : 1;
        var playerValue = cornerValue(s, player);
        var opponentValue = cornerValue(s, opponent);

        return playerValue + opponentValue != 0 ? 10 * (playerValue - opponentValue) / (playerValue + opponentValue) : 0;
    }

    static int cornerValue(BetterGameState s, int player) {
        var board = s.getBoard();

        var capturedCorners = 0;
        if (board[0][0] == player) capturedCorners++;
        if (board[0][board.length - 1] == player) capturedCorners++;
        if (board[board.length - 1][0] == player) capturedCorners++;
        if (board[board.length - 1][board.length - 1] == player) capturedCorners++;

        var potentialCorners = 0;
        if (s.isLegalMove(new Position(0, 0))) potentialCorners++;
        if (s.isLegalMove(new Position(0, board.length - 1))) potentialCorners++;
        if (s.isLegalMove(new Position(board.length - 1, 0))) potentialCorners++;
        if (s.isLegalMove(new Position(board.length - 1, board.length - 1))) potentialCorners++;

        return capturedCorners + potentialCorners;
    }
}

abstract class BaseAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {
    }

    enum MinMax {
        Min((a, b) -> a < b, Integer.MAX_VALUE, (alpha, v) -> alpha, Math::min),
        Max((a, b) -> a > b, Integer.MIN_VALUE, Math::max, (beta, v) -> beta);

        final BiFunction<Integer, Integer, Boolean> cmp;
        final int extreme;
        final BiFunction<Integer, Integer, Integer> alpha;
        final BiFunction<Integer, Integer, Integer> beta;

        MinMax(
                BiFunction<Integer, Integer, Boolean> cmp,
                int extreme,
                BiFunction<Integer, Integer, Integer> alpha,
                BiFunction<Integer, Integer, Integer> beta
        ) {
            this.cmp = cmp;
            this.extreme = extreme;
            this.alpha = alpha;
            this.beta = beta;
        }

        MinMax next() {
            return switch (this) {
                case Min -> Max;
                case Max -> Min;
            };
        }

        boolean prune(int alpha, int beta, int v) {
            return switch (this) {
                case Min -> v == alpha || this.cmp.apply(v, alpha);
                case Max -> v == beta || this.cmp.apply(v, beta);
            };
        }
    }

    public int evaluate(BetterGameState s, MinMax minmax) {
        return heuristic(s, switch (minmax) {
            case Min -> s.getPlayerInTurn() == 1 ? 2 : 1;
            case Max -> s.getPlayerInTurn();
        });
    }

    public Random random = new Random();

    public abstract int heuristic(BetterGameState s, int player);

    public boolean isCutOff(int depth) {
        return depth >= 7;
    }

    public Position decideMove(GameState s) {
        var state = new BetterGameState(s);
        var move = Value(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, MinMax.Max);
        return move.position();
    }

    public Tuple Value(BetterGameState s, int alpha, int beta, int depth, MinMax minmax) {
        if (s.isFinished() || isCutOff(depth)) {
            return new Tuple(evaluate(s, minmax), null);
        }

        var it = s.legalMoves();
        if (!it.hasNext()) {
            s.changePlayer();
            return new Tuple(Value(s, alpha, beta, depth + 1, minmax.next()).value(), null);
        }

        var v = minmax.extreme;
        var move = new Position(-1, -1);

        if (depth == 0) {
            var actions = new ArrayList<Position>();
            it.forEachRemaining(actions::add);
            Collections.shuffle(actions);
            it = actions.iterator();
        }

        while (it.hasNext()) {
            var action = it.next();
            var sNew = new BetterGameState(s);
            sNew.insertToken(action);
            var result = Value(sNew, alpha, beta, depth + 1, minmax.next());

            if (minmax.cmp.apply(result.value(), v)) {
                v = result.value();
                move = action;
                alpha = minmax.alpha.apply(alpha, v);
                beta = minmax.beta.apply(beta, v);
            }

            if (minmax.prune(alpha, beta, v)) {
                return new Tuple(v, move);
            }
        }

        return new Tuple(v, move);
    }
}

/**
 * Class to represent the state of a game of Othello.  The state is defined by a 2-dimensional
 * board and whose turn it is.
 *
 * @author Mai Ajspur & Jonas Jørgensen
 * @version 9.2.2023
 */
class BetterGameState {
    private final int[][] board;        // Possible values: 0 (empty), 1 (black), 2 (white)
    private int currentPlayer;    // The player who is next to put a token on the board. Value is 1 or 2.
    private final int size;            // The number of columns = the number of rows on the board
    private int blackTokens;
    private int whiteTokens;

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
    public BetterGameState(int[][] board, int playerToTakeTurn, int black, int white) {
        this.size = board.length;
        this.board = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, size);
        }
        this.currentPlayer = playerToTakeTurn;
    }

    public BetterGameState(GameState s) {
        this(s.getBoard(), s.getPlayerInTurn(), 0, 0);
        var tokens = countTokens(s.getBoard());
        blackTokens = tokens[0];
        whiteTokens = tokens[1];
    }

    public BetterGameState(BetterGameState s) {
        this(s.getBoard(), s.getPlayerInTurn(), s.countTokens(1), s.countTokens(2));
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
    public static int[] countTokens(int[][] board) {
        int tokens1 = 0;
        int tokens2 = 0;
        for (int[] ints : board) {
            for (int v : ints) {
                if (v == 1)
                    tokens1++;
                else if (v == 2)
                    tokens2++;
            }
        }
        return new int[]{tokens1, tokens2};
    }

    public int countTokens(int player) {
        if (player == 1)
            return blackTokens;
        else
            return whiteTokens;
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
        var captures = 1;
        // Capturing all possible opponents of the current player
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaY = -1; deltaY <= 1; deltaY++) {
                int captives = captureInDirection(place, deltaX, deltaY);
                captures += captives;
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
            if (currentPlayer == 1)
                blackTokens += captures;
            else
                whiteTokens += captures;
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
                    if (isLegalMove(p)) {
                        advance = false;
                        next = p;
                        return true;
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
     * Returns whether the position is a legal move for the current player.
     */
    public boolean isLegalMove(Position p) {
        if (board[p.col][p.row] != 0) {
            return false;
        }

        for (int deltax = -1; deltax <= 1; deltax++) {
            for (int deltay = -1; deltay <= 1; deltay++) {
                if (!(deltax == 0 && deltay == 0) && captureInDirection(p, deltax, deltay) > 0) {
                    return true;
                }
            }
        }

        return false;
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
