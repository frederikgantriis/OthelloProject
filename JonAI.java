public class JonAI extends LisAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        // Prioritize corners and get as many tokens as possible in the process.

        var opponent = player == 1 ? 2 : 1;
        var cornerValues = 100 * super.heuristic(s, player);

        var tokens = s.countTokens();
        var tokenValues = (tokens[player - 1] - tokens[opponent - 1]) / ((tokens[player - 1] + tokens[opponent - 1]) * 100);

        return cornerValues + tokenValues;
    }
}
