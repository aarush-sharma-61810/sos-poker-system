package backend;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== SIMPLE POKER CHIP TRACKER ===");

        //input player names & new game
        Game game = new Game();
        int playerCount = askInt("How many players? ", 2, 8);
        for (int i = 0; i < playerCount; i++) {
            String name = askLine("Player " + (i + 1) + " name: ");
            game.addPlayer(name);
        }

        // hands continue till only 1 player left w chips
        int handNum = 1;
        while (game.countPlayersWithChips() > 1) {
            System.out.println("\n--- HAND " + handNum + " ---");
            game.startHand();
            System.out.println("Dealer: " + game.getPlayers().get(game.getDealerIndex()).getName());

            // post blinds then start pre-flop betting
            game.postBlinds();
            runBettingRound(game, "Pre-flop");
            
            // flop: 3 community cards dealt, new betting round
            if (game.getActiveCount() > 1) {
                game.startNewBettingRound();
                runBettingRound(game, "Flop");
            }
            
            // turn: 4th community card, another betting round
            if (game.getActiveCount() > 1) {
                game.startNewBettingRound();
                runBettingRound(game, "Turn");
            }
            
            // river: 5th community card, final betting round
            if (game.getActiveCount() > 1) {
                game.startNewBettingRound();
                runBettingRound(game, "River");
            }

            // if only 1 player left, win; 
            if (game.getActiveCount() == 1) {
                Player winner = game.getActivePlayers().get(0);
                winner.setChips(winner.getChips() + game.getPot());
                System.out.println(winner.getName() + " wins $" + game.getPot() + " (all others folded)");
            } else {
                // winner is inputted by players
                pickWinner(game);
            }

            // show chip count and move
            printStacks(game);
            game.advanceDealer();
            handNum++;
        }

    
        System.out.println("=== GAME OVER ===");
        printStacks(game);
    }

    //run one betting round
    private static void runBettingRound(Game game, String roundName) {
        System.out.println("\n[" + roundName + "]");
        runBetting(game, game.firstToAct());
    }

    // read line
    private static String askLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim(); //removes gap if user does dumb spaces
    }

    // ask for a number between min and max if they raised wrong or when counting players; continues if they fail
    private static int askInt(String prompt, int min, int max) {
        while (true) {
            // parse input
            String line = askLine(prompt);
            try {
                int value = Integer.parseInt(line);
               
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Enter a whole number between " + min + " and " + max + ".");
        }
    }

    // run one betting round for hand
    private static void runBetting(Game game, int startIndex) {
        // mark who has already acted this round (folded or broke people skip)
        boolean[] acted = new boolean[game.getPlayers().size()];
        for (int i = 0; i < acted.length; i++) {
            Player p = game.getPlayers().get(i);
            acted[i] = p.isFolded() || p.getChips() <= 0;
        }

        int index = startIndex;
        if (index < 0) {
            return;
        }

        // keep going till e veryone does smth or gives up 
        while (true) {
            if (game.getActiveCount() <= 1) {
                return;
            }

            Player p = game.getPlayers().get(index);
            if (!p.isFolded() && p.getChips() > 0) { // if they're in and they're not broke
                // call. amouunt needed
                int needToCall = game.getCurrentBet() - p.getRoundBet();
                if (!acted[index] || needToCall > 0) { 
                    // sneed to respond to bet or if they havent acted yet
                    String prompt;
                    if (needToCall > 0) {
                        prompt = p.getName() + " - fold(f) call(c) raise(r)? ";
                    } else {
                        prompt = p.getName() + " - fold(f) check(k) raise(r)? ";
                    } // can only check if nothing to call

                    String action = askLine(prompt).toLowerCase();
                    //needs valid action 
                    while (!isValidAction(action, needToCall == 0)) {
                        System.out.println("Use f, c, k, or r.");
                        action = askLine(prompt).toLowerCase();
                    }

                    // folded and marks as acted 
                    if (action.equals("f")) {
                        p.setFolded(true);
                        System.out.println(p.getName() + " folds.");
                        acted[index] = true;
                    } else if (action.equals("c")) {
                        // call matches bet or checks if no bet
                        if (needToCall > 0) {
                            int amount = Math.min(needToCall, p.getChips()); //put in all for check if not enough or put in wwhat needed
                            p.setChips(p.getChips() - amount);
                            p.setRoundBet(p.getRoundBet() + amount);
                            game.addToPot(amount);
                            System.out.println(p.getName() + " calls $" + amount + ".");
                        } else {
                            System.out.println(p.getName() + " checks."); //if need to call 0 but press check 
                        }
                        acted[index] = true;
                    } else if (action.equals("k")) { //check
                        System.out.println(p.getName() + " checks.");
                        acted[index] = true;
                    } else if (action.equals("r")) {
                        // higher bet
                        int minRaise = Math.max(1, game.getCurrentBet() - p.getRoundBet() + 1);
                        int raise = askInt("Raise by $", minRaise, p.getChips()); //min amount needd to raise 
                        p.setChips(p.getChips() - raise);
                        p.setRoundBet(p.getRoundBet() + raise);
                        game.addToPot(raise);
                        if (p.getRoundBet() > game.getCurrentBet()) {
                            game.setCurrentBet(p.getRoundBet());
                        } //makes bet larger if needed.
                        System.out.println(p.getName() + " raises by $" + raise + ".");
                        // reset everyone else acted to false unless broke or folded
                        for (int j = 0; j < acted.length; j++) {
                            if (j != index) {
                                Player other = game.getPlayers().get(j);
                                acted[j] = other.isFolded() || other.getChips() <= 0;
                            }
                        }
                        acted[index] = true;
                    }
                }
            }

            // check if odne
            if (allPlayersDone(game, acted)) {
                return;
            }

            // move to next player thats active
            index = game.getNextActiveIndex(index);
            if (index < 0) {
                return;
            }
        }
    }

    // check if betting is finished
    private static boolean allPlayersDone(Game game, boolean[] acted) {
        for (int i = 0; i < acted.length; i++) {
            Player p = game.getPlayers().get(i);
            // skip folded players and broke
            if (p.isFolded() || p.getChips() <= 0) {
                continue;
            }
            //everyon needs to act
            if (!acted[i]) {
                return false;
            }
            //needs to match or exceed
            if (p.getRoundBet() < game.getCurrentBet()) {
                return false;
            }
        }
        return true;
    }

    // only allow f/c/k/r/ buttons
    private static boolean isValidAction(String action, boolean canCheck) {
    
        if (action.equals("f") || action.equals("c") || action.equals("r")) {
            return true;
        }
        // check only when no call to match
        return canCheck && action.equals("k");
    }

    // Players input winner
    private static void pickWinner(Game game) {
        //picks active players 
        ArrayList<Player> active = game.getActivePlayers();
        System.out.print("Still in: ");
        for (int i = 0; i < active.size(); i++) {
            System.out.print(active.get(i).getName());
            if (i < active.size() - 1) { //no trailing comma
                System.out.print(", ");
            }
        }
        System.out.println(".");

        // keep asking until winner pijed or split pot
        while (true) {
            String winnerName = askLine("Enter winner name or S to split: ");
            if (winnerName.toLowerCase().equals("s")) {
                // split pot
                int share = game.getPot() / active.size();
                
                for (int i = 0; i < active.size(); i++) {
                    Player p = active.get(i);
                    p.setChips(p.getChips() + share ); 
                }
                System.out.println("Pot split " + active.size() + " ways: each gets $" + share + ".");
                return; 
            }
            // find player w/ name and give them chips
            for (Player p : active) {
                if (p.getName().toLowerCase().equals(winnerName)) {
                    p.setChips(p.getChips() + game.getPot());
                    System.out.println(p.getName() + " wins $" + game.getPot() + ".");
                    return;
                }
            }
            System.out.println("Name not found. Try one of the active players."); //if invalid name
        }
    }

    // print after each hand
    private static void printStacks(Game game) {
        System.out.println("Current stacks:");
        for (Player p : game.getPlayers()) {
            System.out.println(p.getName() + ": $" + p.getChips());
        }
    }
}
