public class LisAI extends BaseAI {

    @Override
    public int heuristic(BetterGameState s, int player) {
        // Get corners.

        var opponent = player == 1 ? 2 : 1;
        var playerValue = cornerValue(s, player);
        var opponentValue = cornerValue(s, opponent);
        if ((playerValue + opponentValue != 0)) {
            return 100 * (playerValue - opponentValue) / (playerValue + opponentValue);
        }
        return 0;
    }

    public int cornerValue(BetterGameState s, int player) {
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
