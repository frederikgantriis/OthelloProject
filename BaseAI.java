import java.util.Random;
import java.util.function.BiFunction;

public abstract class BaseAI implements IOthelloAI {
    record Tuple(Integer value, Position position) {
    }

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

    public int evaluate(BetterGameState s, MinMax minmax) {
        return heuristic(s, switch (minmax) {
            case Min -> s.getPlayerInTurn() == 1 ? 2 : 1;
            case Max -> s.getPlayerInTurn();
        });
    }

    public Random random = new Random();

    public abstract int heuristic(BetterGameState s, int player);

    public boolean isCutOff(int depth) {
        return depth >= 8;
    }

    public Position decideMove(GameState s) {
        var state = new BetterGameState(s);
        var move = Value(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, MinMax.Max);
        return move.position();
    }

    public Tuple Value(BetterGameState s, int alpha, int beta, int depth, MinMax minmax) {
        if (s.isFinished() || isCutOff(depth)) {
            return new Tuple(evaluate(s, minmax), null);
        }

        if (!s.legalMoves().hasNext()) {
            s.changePlayer();
            return new Tuple(Value(s, alpha, beta, depth + 1, minmax.next()).value(), null);
        }

        var v = minmax.extreme;
        var move = new Position(-1, -1);
        for (var it = s.legalMoves(); it.hasNext(); ) {
            var action = it.next();
            var sNew = new BetterGameState(s);
            sNew.insertToken(action);
            var result = Value(sNew, alpha, beta, depth + 1, minmax.next());

            if (minmax.cmp.apply(result.value(), v)
                    // Low chance that, if the value is the same, we choose this move instead. It makes it easier to
                    // test AIs against each other.
                    || depth == 0 && result.value() == v && random.nextDouble() < .1) {
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
}
