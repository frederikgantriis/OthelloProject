public class BobAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {}

    private static Position standardPos = new Position(-1, -1);
    
    public Position decideMove(GameState s) {
        
        Tuple bobOutcome = new Tuple(0, standardPos);
        bobOutcome = MaxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);

        System.out.println("Bob's move: " + bobOutcome.position());
        System.out.println("Bob's score: " + bobOutcome.value());
        System.out.println("Player in turn: " + s.getPlayerInTurn());
        System.out.println("Is finished: " + s.isFinished());
        System.out.println("Legal moves: " + s.legalMoves().size());    
        System.out.println();

        return bobOutcome.position();
    }

    public Tuple MaxValue(GameState s, int alpha, int beta, int depth) {
        Position move = standardPos;

        if (isCutOff(s, depth) || s.legalMoves().size() == 0) return new Tuple(CornerHeuristic(s), standardPos);

        if (s.isFinished()) return new Tuple(whoWon(s), standardPos);

        int v = Integer.MIN_VALUE;

        
        for (Position p : s.legalMoves()) {
            Tuple minOutCome = new Tuple(0, standardPos);
            
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s2.insertToken(p);

            minOutCome = MinValue(s2, alpha, beta, depth + 1);

            if (minOutCome.value() >= v) {
                v = minOutCome.value();
                move = p;
                alpha = Math.max(alpha, v);
            }

            if (v >= beta) return new Tuple(v, move);

            move = p;
        }
        
        return new Tuple(v, move);
    }

    public Tuple MinValue(GameState s, int alpha, int beta, int depth) {
        Position move = standardPos;

        if (isCutOff(s, depth) || s.legalMoves().size() == 0) return new Tuple(CornerHeuristic(s), standardPos);

        if (s.isFinished()) return new Tuple(whoWon(s), standardPos);
        
        int v = Integer.MAX_VALUE;

        for (Position p : s.legalMoves()) {
            Tuple maxOutCome = new Tuple(0, standardPos);
            
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s2.insertToken(p);

            maxOutCome = MaxValue(s2, alpha, beta, depth + 1);
            if (maxOutCome.value() <= v) {
                v = maxOutCome.value();
                move = p;
                beta = Math.min(beta, v);
            }

            if (v <= alpha) return new Tuple(v, move);
        }

        return new Tuple(v, move);
    }

    public boolean isCutOff(GameState s, int depth) {
        return depth >= 5;
    }

    public int CornerHeuristic(GameState s) {
        var player = s.getPlayerInTurn();
        var oppent = player == 1 ? 2 : 1;
        var playerValue = cornerValue(s, player);
        var opponentValue = cornerValue(s, oppent);
        if ((playerValue + opponentValue != 0)) {
            return 100 * (playerValue - opponentValue)/(playerValue + opponentValue);
        }
        return 0;
    }

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
    public int whoWon(GameState s){
        var player = s.getPlayerInTurn();
        var playerValue = 0;
        var opponentValue = 0;
        var board = s.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if(board[i][j] == player) playerValue++;
                else opponentValue++;
            }
        }
        
        return playerValue > opponentValue ? 101 : -101;
    }
}