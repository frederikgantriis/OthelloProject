public class BobAI extends LisAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        // Get corners, then get tokens.

        var opponent = player == 1 ? 2 : 1;

        var board = s.getBoard();
        var size = board.length;
        if (board[0][0] == 0 || board[0][size - 1] == 0 || board[size - 1][0] == 0 || board[size - 1][size - 1] == 0) {
            return 100 * super.heuristic(s, player);
        } else {
            var tokens = s.countTokens();
            return (tokens[player - 1] - tokens[opponent - 1]) / ((tokens[player - 1] + tokens[opponent - 1]) * 100);
        }
    }
}
