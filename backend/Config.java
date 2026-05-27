package             backend;

// game configuration constants
public class Config {
    // how many chips each player starts with
    private static final int STARTING_STACK = 1000;
    
    // blind amounts at the start of each hand
    // small blind is posted by the player after the dealer 
    private static final int SMALL_BLIND = 5;
    
    // big blind is posted by the next player 
    private static final int BIG_BLIND = 10;


    static int getStartingStack(){
        return STARTING_STACK;
    }
    
    static int getSmallBlind(){
        return SMALL_BLIND;
    }
    
    static int getBigBlind(){
        return BIG_BLIND;
    }

}
