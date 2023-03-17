import java.util.function.BiFunction;

public class BobAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {}
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
    
    public Position decideMove(GameState s) {
        var move = Value(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, MinMax.Max);
        return move.position();
    }

    public Tuple Value(GameState s, int alpha, int beta, int depth, MinMax minmax) {
        if (s.isFinished() || isCutOff(depth)) {
            return new Tuple(evaluate(s), null);
        }

        var v = minmax.extreme;
        var move = new Position(-1, -1);
        for (var action : s.legalMoves()) {
            var sNew = new GameState(s.getBoard(), s.getPlayerInTurn());
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

    public boolean isCutOff(int depth) {
        return depth >= 8;
    }
}
