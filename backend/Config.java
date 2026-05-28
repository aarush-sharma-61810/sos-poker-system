package backend;
public class Config {
    // starting chips final
    private static final int startingStack = 1000;
    
    
    // small blind is one after dealer 
    private static final int smallBlind = 5;
    
    // big blind is 2 after dealer
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
