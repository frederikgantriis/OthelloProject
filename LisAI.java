public class LisAI extends BaseAI {

    @Override
    public int heuristic(BetterGameState s, int player) {
        var opponent = player == 1 ? 2 : 1;
        var playerValue = cornerValue(s, player);
        var opponentValue = cornerValue(s, opponent);
        if ((playerValue + opponentValue != 0)) {
            return 100 * (playerValue - opponentValue) / (playerValue + opponentValue);
        }
        return 0;
    }

    public int cornerValue(BetterGameState s, int player) {
        var capturedCorners = 0;
        var board = s.getBoard();

        if (board[0][0] == player) capturedCorners++;
        if (board[0][board.length - 1] == player) capturedCorners++;
        if (board[board.length - 1][0] == player) capturedCorners++;
        if (board[board.length - 1][board.length - 1] == player) capturedCorners++;

        var potentialCorners = 0;
        for (var it = s.legalMoves(); it.hasNext(); ) {
            var move = it.next();
            if ((move.col == 0 || move.col == board.length - 1) && (move.row == 0 || move.row == board.length - 1)) {
                potentialCorners++;
            }
        }

        return capturedCorners + potentialCorners;
    }

    @Override
    public boolean isCutOff(int depth) {
        return depth >= 6;
    }
}
