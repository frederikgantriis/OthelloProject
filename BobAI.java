public class BobAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {}

    private static Position standardPos = new Position(-1, -1);
    
    /**
     * Returns the best move for the player in turn
     * @param GameState s
     * @return Position
     */
    public Position decideMove(GameState s) {
        if (s.legalMoves().size() == 0) return standardPos;

        Tuple bobOutcome = MaxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);

        printAnalysis(bobOutcome, s);

        return bobOutcome.position();
    }

    /**
     * Prints the analysis of the best move
     * @param s
     * @param alpha
     * @param beta
     * @param depth
     * @return Tuple(value, move)
     */
    public Tuple MaxValue(GameState s, int alpha, int beta, int depth) {
        Position move = standardPos;
        
        if (s.isFinished()) return new Tuple(whoWon(s), standardPos);

        if (isCutOff(depth)) return new Tuple(CornerHeuristic(s, s.getPlayerInTurn()), standardPos);

        if (s.legalMoves().size() == 0) {
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn() == 1 ? 2 : 1);
            return MinValue(s2, alpha, beta, depth + 1);
        }

        int v = Integer.MIN_VALUE;

        for (Position p : s.legalMoves()) {            
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s2.insertToken(p);

            Tuple minOutCome = MinValue(s2, alpha, beta, depth + 1);

            if (minOutCome.value() >= v) {
                v = minOutCome.value();
                move = p;
                alpha = Math.max(alpha, v);
            }

            if (v >= beta) return new Tuple(v, move);
        }
        
        return new Tuple(v, move);
    }

    /**
     * Returns the minimum value of the next move
     * @param s
     * @param alpha
     * @param beta
     * @param depth
     * @return Tuple(value, move)
     */
    public Tuple MinValue(GameState s, int alpha, int beta, int depth) {
        Position move = standardPos;

        if (s.isFinished()) return new Tuple(-whoWon(s), standardPos);

        if (isCutOff(depth)) return new Tuple(CornerHeuristic(s, s.getPlayerInTurn() == 1 ? 2 : 1), standardPos);

        if (s.legalMoves().size() == 0) {
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn() == 1 ? 2 : 1);
            return MaxValue(s2, alpha, beta, depth + 1);
        }
        
        int v = Integer.MAX_VALUE;

        for (Position p : s.legalMoves()) {            
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s2.insertToken(p);

            Tuple maxOutCome = MaxValue(s2, alpha, beta, depth + 1);
            if (maxOutCome.value() <= v) {
                v = maxOutCome.value();
                move = p;
                beta = Math.min(beta, v);
            }

            if (v <= alpha) return new Tuple(v, move);
        }

        return new Tuple(v, move);
    }

    /**
     * Returns true if the depth is greater than or equal to 7
     * @param depth
     * @return boolean
     */
    public boolean isCutOff(int depth) {
        return depth >= 7;
    }

    /**
     * Returns the heuristic value of the player in turn
     * @param GameState s
     * @param int player
     * @return int
     */
    public int CornerHeuristic(GameState s, int player) {
        var oppent = player == 1 ? 2 : 1;
        var playerValue = cornerValue(s, player);
        var opponentValue = cornerValue(s, oppent);
        if ((playerValue + opponentValue != 0)) {
            return 100 * (playerValue - opponentValue)/(playerValue + opponentValue);
        }
        return 0;
    }

    /**
     * Returns the number of corners captured by the player and the number of potential corners
     * @param GameState s
     * @param int player
     * @return
     */
    public int cornerValue(GameState s, int player) {
        var capturedCorners = 0;
        var board = s.getBoard();

        if (board[0][0] == player) capturedCorners++;
        if (board[0][board.length-1] == player) capturedCorners++;
        if (board[board.length-1][0] == player) capturedCorners++;
        if (board[board.length-1][board.length-1] == player) capturedCorners++;

        var potentialCorners = 0;
        for (Position move : s.legalMoves()) {
            if ((move.col == 0 || move.col == board.length-1) && (move.row == 0 || move.row == board.length-1)) {
                potentialCorners++;
            }
        }

        return capturedCorners + potentialCorners;
    }

    /**
     * Returns the score of the player in turn
     * @param GameState s
     * @return int
     */
    public int whoWon(GameState s){
        var player = s.getPlayerInTurn();

        var score = s.countTokens();

        var playerValue = score[player-1];
        var opponentValue = score[player == 1 ? 1 : 0];
        
        if (playerValue == opponentValue) return 0;

        return playerValue > opponentValue ? 101 : -101;
    }

    /**
     * Prints the analysis of the best move
     * @param Tuple bobOutcome
     * @param GameState s
     */
    public void printAnalysis(Tuple bobOutcome, GameState s) {
        System.out.println("Bob's move: " + bobOutcome.position());
        System.out.println("Bob's score: " + bobOutcome.value());
        System.out.println("Player in turn: " + s.getPlayerInTurn());
        System.out.println("Is finished: " + s.isFinished());
        System.out.println("Legal moves: " + s.legalMoves().size());    
        System.out.println();
    }
}