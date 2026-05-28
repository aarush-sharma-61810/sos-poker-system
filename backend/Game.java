package backend;

import java.util.ArrayList;

public class Game {
    private ArrayList<Player> players = new ArrayList<>(); // players 
    private int pot = 0;  
    private int currentBet = 0;  
    private int dealerIndex = 0;  // current dealer 

    // add a player to the table
    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    // reset hand to clean
    public void startHand() {
        pot = 0;  
        currentBet = 0;  
        for (Player p : players) {
            p.resetHand();  // reset player info
        }
    }

    // count players withh chips 
    public int countPlayersWithChips() {
        int count = 0;
        for (Player p : players) {
            if (p.getChips() > 0) {
                count++;
            }
        }
        return count;
    }

    // count players in CURENThand
    public int getActiveCount() {
        int count = 0;
        for (Player p : players) {
            if (p.getChips() > 0 && !p.isFolded()) {
                count++;
            }
        }
        return count;
    }

    //returns PLAYERS in curent hand. above returns number              
    public ArrayList<Player> getActivePlayers() {
        ArrayList<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (p.getChips() > 0 && !p.isFolded()) {
                active.add(p);
            }
        }
        return active;
    }

    // finds next player wwho still has chips
    public int getNextSeatWithChips(int index) {
        int size = players.size();
        // starts at index player and goes through each one after until one of them has chips 
        for (int i = 1; i < size; i++) {
            int next = (index + i) % size; // 2%4 = 2, 3%4 = 3, 4%4 = 0, 5%4 = 1 can reset indices to not exceed and restart at seat number
            if (players.get(next).getChips() > 0) {
                return next;
            }
        }
        return -1;
    }

    // find netx player who hsa chips and is playing hand
    public int getNextActiveIndex(int index) {
        int size = players.size();
        for (int i = 1; i < size; i++) {
            int next = (index + i) % size;
            Player p = players.get(next);
            if (p.getChips() > 0 && !p.isFolded()) {
                return next;
            }
        }
        return -1;
    }

    // starts wuth player after dealer who is active
    public int firstToAct() {
        return getNextActiveIndex(dealerIndex);
    }
          
    // dealer is next active player w chips
    public void advanceDealer() {
        int next = getNextSeatWithChips(dealerIndex);
        if (next >= 0) {
            dealerIndex = next;
        }
    }

   public void postBlinds() { 
    // small blind is after dealeer unless broke 
    int smallBlindIndex = getNextSeatWithChips(dealerIndex); 
    if (smallBlindIndex >= 0) { 
        Player smallBlind = players.get(smallBlindIndex); 
        int sbAmount;
        // enough or broke 
        if (smallBlind.getChips() >= Config.getSmallBlind()) {
            sbAmount = Config.getSmallBlind();
        } else {
            sbAmount = smallBlind.getChips(); // Take eall
        }
        smallBlind.setChips(smallBlind.getChips() - sbAmount); 
        smallBlind.setRoundBet(smallBlind.getRoundBet() + sbAmount); 
        pot += sbAmount; 
        System.out.println(smallBlind.getName() + " posts small blind $" + sbAmount); 
    } 
    
    // big blind is posted by the next player after small blind 
    int bigBlindIndex = getNextSeatWithChips(smallBlindIndex); 
    if (bigBlindIndex >= 0) { 
        Player bigBlind = players.get(bigBlindIndex); 
        int bbAmount;
        //broke or not
        if (bigBlind.getChips() >= Config.getBigBlind()) {
            bbAmount = Config.getBigBlind();
        } else {
            bbAmount = bigBlind.getChips(); // Take all
        }
        bigBlind.setChips(bigBlind.getChips() - bbAmount); 
        bigBlind.setRoundBet(bigBlind.getRoundBet() + bbAmount); 
        pot += bbAmount; 
        currentBet = bbAmount; 
        System.out.println(bigBlind.getName() + " posts big blind $" + bbAmount); 
    } 
}

    // reset bets
    public void startNewBettingRound() {
        currentBet = 0;  
        for (Player p : players) {
            if (!p.isFolded()) {  
                p.setRoundBet(0);  
            }
        }
    }

    // add chips to pot
    public void addToPot(int amount) {
        pot += amount;
    }
    
    
    public ArrayList<Player> getPlayers() {
        return players;
    }
    
    public int getPot() {
        return pot;
    }
    
    public void setPot(int amount) {
        pot = amount;
    }
    
    public int getCurrentBet() {
        return currentBet;
    }
    
    public void setCurrentBet(int amount) {
        currentBet = amount;
    }
    
    public int getDealerIndex() {
        return dealerIndex;
    }
    
    public void setDealerIndex(int index) {
        dealerIndex = index;
    }
}
