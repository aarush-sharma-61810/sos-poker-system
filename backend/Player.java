package backend;

// player object with chips and current round values
public class Player {
    private String name;  // player's name
    private int chips;  // how many chips they have left
    private int roundBet;  // how much they bet in this round so far
    private boolean folded;  // did they quit this hand?

    // make a player with the starting stack
    public Player(String name) {
        this.name = name;
        this.chips = Config.getStartingStack();
        this.roundBet = 0;  // no bets yet
        this.folded = false;  // they're in the hand
    }

    // reset round info before each new hand
    public void resetHand() {
        this.roundBet = 0;  // clear their round bet
        this.folded = false;  // they're back in the game
    }
    
    // getter and setter methods
    public String getName() {
        return name;
    }
    
    public int getChips() {
        return chips;
    }
    
    public void setChips(int amount) {
        chips = amount;
    }
    
    public int getRoundBet() {
        return roundBet;
    }
    
    public void setRoundBet(int amount) {
        roundBet = amount;
    }
    
    public boolean isFolded() {
        return folded;
    }
    
    public void setFolded(boolean value) {
        folded = value;
    }
}
