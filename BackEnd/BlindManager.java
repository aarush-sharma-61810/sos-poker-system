package BackEnd;

public class BlindManager {
    public static void postBlinds(Game g) {
        int n = g.players.size();
        int sbIdx, bbIdx;
        if (n == 2) {
            sbIdx = g.dealerIndex;
            bbIdx = (g.dealerIndex + 1) % n;
        } else {
            sbIdx = nextWithChips(g, g.dealerIndex);
            bbIdx = nextWithChips(g, sbIdx);
        }
        Player sb = g.players.get(sbIdx);
        Player bb = g.players.get(bbIdx);
        post(sb, Config.SMALL_BLIND);
        post(bb, Config.BIG_BLIND);
        g.currentBet = Config.BIG_BLIND;
        g.lastRaiseSize = Config.BIG_BLIND;
    }

    private static int nextWithChips(Game g, int from) {
        int n = g.players.size();
        for (int i = 1; i <= n; i++) {
            int idx = (from + i) % n;
            if (g.players.get(idx).chips > 0) return idx;
        }
        return from;
    }

    private static void post(Player p, int amount) {
        int put = Math.min(amount, p.chips);
        p.chips -= put;
        p.currentBet = put;
        p.totalBet = put;
        if (p.chips == 0) p.allIn = true;
    }

    public static int smallBlindIndex(Game g) {
        int n = g.players.size();
        if (n == 2) return g.dealerIndex;
        return nextWithChips(g, g.dealerIndex);
    }

    public static int bigBlindIndex(Game g) {
        int n = g.players.size();
        if (n == 2) return (g.dealerIndex + 1) % n;
        int sb = nextWithChips(g, g.dealerIndex);
        return nextWithChips(g, sb);
    }
}
