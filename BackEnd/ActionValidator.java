package BackEnd;

public class ActionValidator {
    public static String validate(Game g, Player p, Action a) {
        if (g.phase == Game.Phase.WAITING || g.phase == Game.Phase.SHOWDOWN) return "Not in a betting round";
        if (g.currentTurn < 0 || g.currentTurn >= g.players.size()) return "No active turn";
        if (g.players.get(g.currentTurn) != p) return "Not your turn";
        if (p.folded || p.allIn) return "You cannot act";
        switch (a.type) {
            case FOLD: return null;
            case CHECK:
                if (p.currentBet < g.currentBet) return "Cannot check, must call or fold";
                return null;
            case CALL:
                if (g.currentBet == p.currentBet) return "Nothing to call";
                return null;
            case RAISE: {
                int min = g.currentBet + g.lastRaiseSize;
                int max = p.currentBet + p.chips;
                if (a.amount > max) return "Not enough chips";
                if (a.amount < min && a.amount != max) return "Raise too small";
                if (a.amount % Config.RAISE_INCREMENT != 0 && a.amount != max) return "Use $" + Config.RAISE_INCREMENT + " increments";
                if (a.amount <= g.currentBet) return "Must be greater than current bet";
                return null;
            }
            case ALL_IN:
                if (p.chips == 0) return "No chips";
                return null;
        }
        return "Unknown action";
    }
}
