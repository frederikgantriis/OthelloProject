import java.util.Map;

public class BobAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {}

    
    public Position decideMove(GameState s) {
        Tuple bobOutcome = new Tuple(0, new Position(0, 0));
        bobOutcome = MaxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        return bobOutcome.position();
    }

    public Tuple MaxValue(GameState s, int alpha, int beta, int depth) {
        Position move = new Position(0, 0);

        if (isCutOff(s, depth) || s.isFinished()) {

            return new Tuple(evaluate(s), new Position(0, 0));
        }

        int v = Integer.MIN_VALUE;
        for (Position p : s.legalMoves()) {
            Tuple minOutCome = new Tuple(0, new Position(0, 0));
            

            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s2.insertToken(p);

            minOutCome = MinValue(s2, alpha, beta, depth + 1);
            if (minOutCome.value() >= v) {
                v = minOutCome.value();
                move = p;
                alpha = Math.max(alpha, v);
            }
            if (v >= beta) {
                return new Tuple(v, move);
            }

            move = p;
        }
        return new Tuple(v, move);
    }

    public Tuple MinValue(GameState s, int alpha, int beta, int depth) {
        Position move = new Position(0, 0);

        if (isCutOff(s, depth) || s.isFinished()) {

            return new Tuple(evaluate(s), new Position(0, 0));
        }

        int v = Integer.MAX_VALUE;
        for (Position p : s.legalMoves()) {
            Tuple maxOutCome = new Tuple(0, new Position(0, 0));
            
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s2.insertToken(p);

            maxOutCome = MaxValue(s2, alpha, beta, depth + 1);
            if (maxOutCome.value() <= v) {
                v = maxOutCome.value();
                move = p;
                beta = Math.min(beta, v);
            }
            if (v <= alpha) {
                return new Tuple(v, move);
            }
        }
        return new Tuple(v, move);
    }

    public Integer evaluate(GameState s) {
        int playerNumber = s.getPlayerInTurn();
        s.changePlayer();
        int opponentNumber = s.getPlayerInTurn();

        int[][] board = s.getBoard();

        int[][] pointboard = makeHeuristic(board);

        int playerScore = 0;
        int opponentScore = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == playerNumber) {
                    playerScore += pointboard[i][j];
                } else if (board[i][j] == opponentNumber) {
                    opponentScore += pointboard[i][j];
                }
            }
        }

        return playerScore - opponentScore;
    }

    public int[][] makeHeuristic(int[][] board) {
        int[][] pointboard = new int[board.length][board.length];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i == 0 && j == 0) {
                    pointboard[i][j] = 4000000;
                } else {
                    pointboard[i][j] = -400000;
                }
            }
        }

        return pointboard;
    }

    public boolean isCutOff(GameState s, int depth) {
        return depth >= 9;
    }
}