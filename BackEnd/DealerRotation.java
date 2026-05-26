package BackEnd;

public class DealerRotation {
    public static void advance(Game g) {
        int n = g.players.size();
        for (int i = 1; i <= n; i++) {
            int idx = (g.dealerIndex + i) % n;
            if (g.players.get(idx).chips > 0) {
                g.dealerIndex = idx;
                return;
            }
        }
    }
}
