package BackEnd;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public enum Phase { WAITING, PREFLOP, FLOP, TURN, RIVER, SHOWDOWN }

    public List<Player> players = new ArrayList<>();
    public Phase phase = Phase.WAITING;
    public int dealerIndex = 0;
    public int currentBet = 0;
    public int lastRaiseSize = Config.BIG_BLIND;
    public int currentTurn = -1;
    public PotManager pots = new PotManager();

    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    public void startHand() {
        for (Player p : players) p.resetForHand();
        currentBet = 0;
        lastRaiseSize = Config.BIG_BLIND;
        pots = new PotManager();
        BlindManager.postBlinds(this);
        phase = Phase.PREFLOP;
        for (Player p : players) p.acted = false;
        currentTurn = BettingRound.firstToAct(this, true);
    }

    public void applyAction(Player p, Action a) {
        switch (a.type) {
            case FOLD:
                p.folded = true;
                break;
            case CHECK:
                break;
            case CALL: {
                int need = currentBet - p.currentBet;
                int put = Math.min(need, p.chips);
                p.chips -= put;
                p.currentBet += put;
                p.totalBet += put;
                if (p.chips == 0) p.allIn = true;
                break;
            }
            case RAISE: {
                int target = a.amount;
                int need = target - p.currentBet;
                if (need >= p.chips) {
                    int put = p.chips;
                    p.chips = 0;
                    p.currentBet += put;
                    p.totalBet += put;
                    p.allIn = true;
                    if (p.currentBet > currentBet) {
                        lastRaiseSize = Math.max(lastRaiseSize, p.currentBet - currentBet);
                        currentBet = p.currentBet;
                        resetActedOthers(p);
                    }
                } else {
                    p.chips -= need;
                    p.currentBet = target;
                    p.totalBet += need;
                    lastRaiseSize = target - currentBet;
                    currentBet = target;
                    resetActedOthers(p);
                }
                break;
            }
            case ALL_IN: {
                int put = p.chips;
                p.chips = 0;
                p.currentBet += put;
                p.totalBet += put;
                p.allIn = true;
                if (p.currentBet > currentBet) {
                    int raise = p.currentBet - currentBet;
                    lastRaiseSize = Math.max(lastRaiseSize, raise);
                    currentBet = p.currentBet;
                    resetActedOthers(p);
                }
                break;
            }
        }
        p.acted = true;
    }

    private void resetActedOthers(Player p) {
        for (Player o : players) {
            if (o != p && !o.folded && !o.allIn) o.acted = false;
        }
    }

    public boolean handOver() {
        int alive = 0;
        for (Player p : players) if (!p.folded) alive++;
        return alive <= 1;
    }

    public Player lastStanding() {
        for (Player p : players) if (!p.folded) return p;
        return null;
    }

    public int activeNotAllIn() {
        int c = 0;
        for (Player p : players) if (!p.folded && !p.allIn) c++;
        return c;
    }

    public void advancePhase() {
        for (Player p : players) p.resetForRound();
        currentBet = 0;
        lastRaiseSize = Config.BIG_BLIND;
        switch (phase) {
            case PREFLOP: phase = Phase.FLOP; break;
            case FLOP: phase = Phase.TURN; break;
            case TURN: phase = Phase.RIVER; break;
            case RIVER: phase = Phase.SHOWDOWN; break;
            default: break;
        }
        if (phase != Phase.SHOWDOWN) {
            currentTurn = BettingRound.firstToAct(this, false);
        } else {
            currentTurn = -1;
            pots.rebuild(players);
        }
    }
}
