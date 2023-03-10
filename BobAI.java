public class BobAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {}

    
    public Position decideMove(GameState s) {
        Tuple bobOutcome = new Tuple(0, new Position(0, 0));
        bobOutcome = MaxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return bobOutcome.position();
    }

    public Tuple MaxValue(GameState s, int alpha, int beta) {
        Position move = new Position(0, 0);

        if (s.isFinished()) {
            int[] score = s.countTokens();
            int playerNumber = s.getPlayerInTurn();
            s.changePlayer();
            int opponentNumber = s.getPlayerInTurn();

            return new Tuple(score[playerNumber-1] - score[opponentNumber-1], new Position(0, 0));
        }

        int v = Integer.MIN_VALUE;
        for (Position p : s.legalMoves()) {
            Tuple minOutCome = new Tuple(0, new Position(0, 0));
            s.insertToken(p);

            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            //Consider the Gamestate call, since we don't know which colour our bot is playing
            minOutCome = MinValue(s2, alpha, beta);
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

    public Tuple MinValue(GameState s, int alpha, int beta) {
        Position move = new Position(0, 0);

        if (s.isFinished()) {
            int[] score = s.countTokens();
            int opponentNumber = s.getPlayerInTurn();
            s.changePlayer();
            int playerNumber = s.getPlayerInTurn();

            return new Tuple(score[playerNumber-1] - score[opponentNumber-1], new Position(0, 0));
        }

        int v = Integer.MAX_VALUE;
        for (Position p : s.legalMoves()) {
            Tuple maxOutCome = new Tuple(0, new Position(0, 0));
            
            s.insertToken(p);
            GameState s2 = new GameState(s.getBoard(), s.getPlayerInTurn());
            //Consider the Gamestate call, since we don't know which colour our bot is playing
            maxOutCome = MaxValue(s2, alpha, beta);
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
}