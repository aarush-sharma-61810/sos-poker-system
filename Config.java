package             backend;

// game configuration constants
public class Config {
    // how many chips each player starts with
    public static final int STARTING_STACK = 1000;
    
    // blind amounts at the start of each hand
    // small blind is posted by the player after the dealer (usually half the big blind)
    public static final int SMALL_BLIND = 5;
    
    // big blind is posted by the next player (forces initial action)
    public static final int BIG_BLIND = 10;
}
