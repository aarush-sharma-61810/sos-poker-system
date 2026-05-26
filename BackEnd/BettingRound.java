package BackEnd;

public class BettingRound {
    public static boolean isComplete(Game g) {
        for (Player p : g.players) {
            if (p.folded || p.allIn) continue;
            if (!p.acted) return false;
            if (p.currentBet != g.currentBet) return false;
        }
        return true;
    }

    public static int nextTurn(Game g) {
        int n = g.players.size();
        for (int i = 1; i <= n; i++) {
            int idx = (g.currentTurn + i) % n;
            Player p = g.players.get(idx);
            if (!p.folded && !p.allIn) return idx;
        }
        return -1;
    }

    public static int firstToAct(Game g, boolean preflop) {
        int n = g.players.size();
        int start;
        if (preflop) {
            start = (n == 2) ? g.dealerIndex : (g.dealerIndex + 3) % n;
        } else {
            start = (g.dealerIndex + 1) % n;
        }
        for (int i = 0; i < n; i++) {
            int idx = (start + i) % n;
            Player p = g.players.get(idx);
            if (!p.folded && !p.allIn) return idx;
        }
        return -1;
    }
}
