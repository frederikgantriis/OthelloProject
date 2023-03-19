import java.util.Iterator;

public class LadAI extends BaseAI {
    @Override
    public int heuristic(BetterGameState s, int player) {
        // As few moves for the opponent as possible.

        s.changePlayer();
        var i = 0;
        for (var it = s.legalMoves(); it.hasNext(); ) {
            it.next();
            i++;
        }
        s.changePlayer();
        return -i;
    }
}
