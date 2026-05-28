package backend;


public class Player {
    private String name;  
    private int chips;  // chips left
    private int roundBet;  // the player's total bet
    private boolean folded;  // quit

    // make a player with the starting stack
    public Player(String name) {
        this.name = name;
        chips = Config.getStartingStack();
        roundBet = 0;  //no bet at start
        folded = false;  
    }

    // reset round info 
    public void resetHand() {
        roundBet = 0;  
        folded = false;  
    }
    
    
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
