package             backend;

// game configuration constants
public class Config {
    // how many chips each player starts with
    private static final int startingStack = 1000;
    
    // blind amounts at the start of each hand
    // small blind is posted by the player after the dealer 
    private static final int smallBlind = 5;
    
    // big blind is posted by the next player 
    private static final int bigBlind = 10;


    public static int getStartingStack(){
        return startingStack;
    }
    
    public static int getSmallBlind(){
        return smallBlind;
    }
    
    public static int getBigBlind(){
        return bigBlind;
    }

}
