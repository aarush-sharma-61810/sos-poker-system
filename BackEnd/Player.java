package BackEnd;

public class Player {
    public String name;
    public int chips;
    public int currentBet;
    public int totalBet;
    public boolean folded;
    public boolean allIn;
    public boolean acted;

    public Player(String name) {
        this.name = name;
        this.chips = Config.STARTING_STACK;
    }

    public void resetForHand() {
        currentBet = 0;
        totalBet = 0;
        folded = false;
        allIn = (chips == 0);
        acted = false;
    }

    public void resetForRound() {
        currentBet = 0;
        acted = false;
    }
}
