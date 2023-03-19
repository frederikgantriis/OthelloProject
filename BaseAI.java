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

    public int evaluate(GameState s, MinMax minmax) {
        return heuristic(s, switch (minmax) {
            case Min -> s.getPlayerInTurn() == 1 ? 2 : 1;
            case Max -> s.getPlayerInTurn();
        });
    }

    public abstract int heuristic(GameState s, int player);

    public abstract boolean isCutOff(int depth);

    public Position decideMove(GameState s) {
        var move = Value(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, MinMax.Max);
        return move.position();
    }

    public Tuple Value(GameState s, int alpha, int beta, int depth, MinMax minmax) {
        if (s.isFinished() || isCutOff(depth) || s.legalMoves().size() == 0) {
            return new Tuple(evaluate(s, minmax), null);
        }

        var v = minmax.extreme;
        var move = new Position(-1, -1);
        for (var action : s.legalMoves()) {
            var sNew = new GameState(s.getBoard(), s.getPlayerInTurn());
            sNew.insertToken(action);
            var result = Value(sNew, alpha, beta, depth + 1, minmax.next());

            if (minmax.cmp.apply(result.value(), v)) {
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
