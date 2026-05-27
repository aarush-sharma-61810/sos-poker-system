package backend;

import java.util.ArrayList;

public class Game {
    private ArrayList<Player> players = new ArrayList<>(); // all players at the table
    private int pot = 0;  // total chips in the middle
    private int currentBet = 0;  // the bet everyone needs to match
    private int dealerIndex = 0;  // which player is dealing this hand

    // add a player to the table
    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    // reset the hand state so the next hand starts clean
    public void startHand() {
        pot = 0;  // clear the pot
        currentBet = 0;  // no bet yet
        for (Player p : players) {
            p.resetHand();  // reset each player's round info
        }
    }

    // count how many players still have chips left
    public int countPlayersWithChips() {
        int count = 0;
        for (Player p : players) {
            if (p.getChips() > 0) {
                count++;
            }
        }
        return count;
    }

    // count the players who are still in this hand (didn't fold)
    public int getActiveCount() {
        int count = 0;
        for (Player p : players) {
            if (p.getChips() > 0 && !p.isFolded()) {
                count++;
            }
        }
        return count;
    }

    // get a list of players who can still win this hand
    public ArrayList<Player> getActivePlayers() {
        ArrayList<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (p.getChips() > 0 && !p.isFolded()) {
                active.add(p);
            }
        }
        return active;
    }

    // find the next seat with chips, even if they folded last hand
    public int getNextSeatWithChips(int index) {
        int size = players.size();
        // loop through players circularly until we find one with chips
        for (int i = 1; i < size; i++) {
            int next = (index + i) % size;
            if (players.get(next).getChips() > 0) {
                return next;
            }
        }
        return -1;
    }

    // find the next player who is still active in this hand
    public int getNextActiveIndex(int index) {
        int size = players.size();
        // loop through players circularly until we find one still in the hand
        for (int i = 1; i < size; i++) {
            int next = (index + i) % size;
            Player p = players.get(next);
            if (p.getChips() > 0 && !p.isFolded()) {
                return next;
            }
        }
        return -1;
    }

    // figure out who acts first after the dealer
    public int firstToAct() {
        return getNextActiveIndex(dealerIndex);
    }

    // move the dealer button to the next player with chips
    public void advanceDealer() {
        int next = getNextSeatWithChips(dealerIndex);
        if (next >= 0) {
            dealerIndex = next;
        }
    }

    // post the blinds at the start of a hand (small blind and big blind)
    public void postBlinds() {
        // small blind is posted by the player after the dealer
        int smallBlindIndex = getNextSeatWithChips(dealerIndex);
        if (smallBlindIndex >= 0) {
            Player smallBlind = players.get(smallBlindIndex);
            // take chips from the player but don't go negative if they don't have enough
            int sbAmount = Math.min(Config.getSmallBlind(), smallBlind.getChips());
            smallBlind.setChips(smallBlind.getChips() - sbAmount);
            smallBlind.setRoundBet(smallBlind.getRoundBet() + sbAmount);  // track their bet for this round
            pot += sbAmount;  // add to the pot
            System.out.println(smallBlind.getName() + " posts small blind $" + sbAmount);
        }
        
        // big blind is posted by the next player after small blind
        int bigBlindIndex = getNextSeatWithChips(smallBlindIndex);
        if (bigBlindIndex >= 0) {
            Player bigBlind = players.get(bigBlindIndex);
            // take chips from the player but don't go negative if they don't have enough
            int bbAmount = Math.min(Config.getBigBlind(), bigBlind.getChips());
            bigBlind.setChips(bigBlind.getChips() - bbAmount);
            bigBlind.setRoundBet(bigBlind.getRoundBet() + bbAmount);  // track their bet for this round
            pot += bbAmount;  // add to the pot
            currentBet = bbAmount;  // big blind sets the minimum bet level that other players need to match
            System.out.println(bigBlind.getName() + " posts big blind $" + bbAmount);
        }
    }

    // reset bets for a new betting round (players keep their fold status and chips)
    public void startNewBettingRound() {
        currentBet = 0;  // fresh bet amount for this round
        for (Player p : players) {
            if (!p.isFolded()) {  // only reset active players
                p.setRoundBet(0);  // they start fresh in this round
            }
        }
    }

    // add chips to the pot when someone bets or calls
    public void addToPot(int amount) {
        pot += amount;
    }
    
    // getter methods for private variables
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
