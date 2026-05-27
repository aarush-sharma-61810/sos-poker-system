package backend;

// player object with chips and current round values
public class Player {
    public String name;  // player's name
    public int chips;  // how many chips they have left
    public int roundBet;  // how much they bet in this round so far
    public boolean folded;  // did they quit this hand?

    // make a player with the starting stack
    public Player(String name) {
        this.name = name;
        this.chips = Config.STARTING_STACK;  // everyone starts with the same amount
        this.roundBet = 0;  // no bets yet
        this.folded = false;  // they're in the hand
    }

    // reset round info before each new hand
    public void resetHand() {
        this.roundBet = 0;  // clear their round bet
        this.folded = false;  // they're back in the game
    }
}
