import java.util.Random;

public class RandomAI implements IOthelloAI {
    Random random = new Random();

    /**
     * Returns a random legal move
     */
    public Position decideMove(GameState s) {
        var moves = s.legalMoves();
        return moves.get(random.nextInt(moves.size()));
    }
}
