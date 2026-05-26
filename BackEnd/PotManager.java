package BackEnd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PotManager {
    public List<Pot> pots = new ArrayList<>();

    public void rebuild(List<Player> allPlayers) {
        pots.clear();
        List<Player> contributors = new ArrayList<>();
        for (Player p : allPlayers) {
            if (p.totalBet > 0) contributors.add(p);
        }
        contributors.sort(Comparator.comparingInt(a -> a.totalBet));

        int prev = 0;
        while (!contributors.isEmpty()) {
            int level = contributors.get(0).totalBet;
            if (level > prev) {
                int slice = level - prev;
                Pot pot = new Pot();
                pot.amount = slice * contributors.size();
                for (Player p : contributors) {
                    if (!p.folded) pot.eligible.add(p.name);
                }
                if (pot.eligible.isEmpty() && !pots.isEmpty()) {
                    pots.get(pots.size() - 1).amount += pot.amount;
                } else if (!pot.eligible.isEmpty()) {
                    pots.add(pot);
                }
                prev = level;
            }
            contributors.remove(0);
        }
    }

    public int total() {
        int s = 0;
        for (Pot p : pots) s += p.amount;
        return s;
    }
}
