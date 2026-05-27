package backend;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== SIMPLE POKER CHIP TRACKER ===");

        // set up the game and get player names
        Game game = new Game();
        int playerCount = askInt("How many players? ", 2, 8);
        for (int i = 0; i < playerCount; i++) {
            String name = askLine("Player " + (i + 1) + " name: ");
            game.addPlayer(name);
        }

        // keep playing hands until only 1 player has chips left
        int handNum = 1;
        while (game.countPlayersWithChips() > 1) {
            System.out.println("\n--- HAND " + handNum + " ---");
            game.startHand();
            System.out.println("Dealer: " + game.getPlayers().get(game.getDealerIndex()).getName());
            
            // post the small and big blinds before betting starts
            // blinds are forced bets that rotate around the table
            game.postBlinds();
            
            // run 3 betting rounds: pre-flop, flop, river
            // after each round, reset the bet level and player round bets so players can check
            runBettingRound(game, "Pre-flop");
            if (game.getActiveCount() > 1) {
                game.startNewBettingRound();  // reset bet for new round
                runBettingRound(game, "Flop");
            }
            if (game.getActiveCount() > 1) {
                game.startNewBettingRound();  // reset bet for new round
                runBettingRound(game, "River");
            }

            // if only 1 player left, they win automatically
            if (game.getActiveCount() == 1) {
                Player winner = game.getActivePlayers().get(0);
                winner.setChips(winner.getChips() + game.getPot());
                System.out.println(winner.getName() + " wins $" + game.getPot() + " (all others folded)");
            } else {
                // if multiple players left, ask the table who won the hand
                pickWinner(game);
            }

            // show chip counts and move to next hand
            printStacks(game);
            game.advanceDealer();
            handNum++;
        }

        // game is done, show final results
        System.out.println("\n=== GAME OVER ===");
        printStacks(game);
    }

    // run one betting round with a round label (like "Pre-flop", "Flop", "River")
    private static void runBettingRound(Game game, String roundName) {
        System.out.println("\n[" + roundName + "]");
        runBetting(game, game.firstToAct());
    }

    // read a line from the console and trim whitespace
    private static String askLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ask for a number between min and max, with input validation
    private static int askInt(String prompt, int min, int max) {
        while (true) {
            // get input and try to parse it
            String line = askLine(prompt);
            try {
                int value = Integer.parseInt(line);
                // check if it's in the valid range
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // invalid input, just keep looping
            }
            System.out.println("Enter a whole number between " + min + " and " + max + ".");
        }
    }

    // run one betting round for the hand, simple chip tracking
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

        // go around the table until everyone has acted and matched the current bet
        while (true) {
            if (game.getActiveCount() <= 1) {
                return;
            }

            Player p = game.getPlayers().get(index);
            if (!p.isFolded() && p.getChips() > 0) {
                // figure out how much this player needs to add to match the current bet
                int needToCall = game.getCurrentBet() - p.getRoundBet();
                if (!acted[index] || needToCall > 0) {
                    // show different prompts depending on if there's a bet to call
                    String prompt;
                    if (needToCall > 0) {
                        prompt = p.getName() + " - fold(f) call(c) raise(r)? ";
                    } else {
                        prompt = p.getName() + " - fold(f) check(k) raise(r)? ";
                    }
                    String action = askLine(prompt).toLowerCase();
                    while (!isValidAction(action, needToCall == 0)) {
                        System.out.println("Use f, c, k, or r.");
                        action = askLine(prompt).toLowerCase();
                    }

                    // handle each action and update the game state
                    if (action.equals("f")) {
                        p.setFolded(true);
                        System.out.println(p.getName() + " folds.");
                        acted[index] = true;
                    } else if (action.equals("c")) {
                        // call means match the current bet, or check if there's no bet
                        if (needToCall > 0) {
                            int amount = Math.min(needToCall, p.getChips());
                            p.setChips(p.getChips() - amount);
                            p.setRoundBet(p.getRoundBet() + amount);
                            game.addToPot(amount);
                            System.out.println(p.getName() + " calls $" + amount + ".");
                        } else {
                            System.out.println(p.getName() + " checks.");
                        }
                        acted[index] = true;
                    } else if (action.equals("k")) {
                        System.out.println(p.getName() + " checks.");
                        acted[index] = true;
                    } else if (action.equals("r")) {
                        // raise means bet more than the current amount
                        int minRaise = Math.max(1, game.getCurrentBet() - p.getRoundBet() + 1);
                        int raise = askInt("Raise by $", minRaise, p.getChips());
                        p.setChips(p.getChips() - raise);
                        p.setRoundBet(p.getRoundBet() + raise);
                        game.addToPot(raise);
                        if (p.getRoundBet() > game.getCurrentBet()) {
                            game.setCurrentBet(p.getRoundBet());
                        }
                        System.out.println(p.getName() + " raises by $" + raise + ".");
                        // when someone raises, reset everyone else so they can respond
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

            // check if we're done with this round
            if (allPlayersDone(game, acted)) {
                return;
            }

            // move to the next player
            index = game.getNextActiveIndex(index);
            if (index < 0) {
                return;
            }
        }
    }

    // check if the betting round is finished for everyone
    private static boolean allPlayersDone(Game game, boolean[] acted) {
        // go through each player and check if they've finished betting
        for (int i = 0; i < acted.length; i++) {
            Player p = game.getPlayers().get(i);
            // skip folded players and those out of chips
            if (p.isFolded() || p.getChips() <= 0) {
                continue;
            }
            // if a player hasn't acted yet, round isn't done
            if (!acted[i]) {
                return false;
            }
            // if a player hasn't matched the current bet, round isn't done
            if (p.getRoundBet() < game.getCurrentBet()) {
                return false;
            }
        }
        return true;
    }

    // only allow the buttons we expect: fold/call/check/raise
    private static boolean isValidAction(String action, boolean canCheck) {
        // fold, call, and raise are always valid
        if (action.equals("f") || action.equals("c") || action.equals("r")) {
            return true;
        }
        // check is only valid when there's no bet to call
        return canCheck && action.equals("k");
    }

    // if the players still have cards on the table, the table decides who wins
    private static void pickWinner(Game game) {
        // get the list of players still in the hand
        ArrayList<Player> active = game.getActivePlayers();
        System.out.print("Still in: ");
        for (int i = 0; i < active.size(); i++) {
            System.out.print(active.get(i).getName());
            if (i < active.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println(".");

        // keep asking until someone picks a winner or splits the pot
        while (true) {
            String winnerName = askLine("Enter winner name or S to split: ");
            if (winnerName.equalsIgnoreCase("S")) {
                // split the pot evenly among the remaining players
                int share = game.getPot() / active.size();
                int extra = game.getPot() % active.size();
                for (int i = 0; i < active.size(); i++) {
                    Player p = active.get(i);
                    p.setChips(p.getChips() + share + (i == 0 ? extra : 0));
                }
                System.out.println("Pot split " + active.size() + " ways: each gets $" + share + ".");
                return;
            }
            // find the player with that name and give them the pot
            for (Player p : active) {
                if (p.getName().equalsIgnoreCase(winnerName)) {
                    p.setChips(p.getChips() + game.getPot());
                    System.out.println(p.getName() + " wins $" + game.getPot() + ".");
                    return;
                }
            }
            System.out.println("Name not found. Try one of the active players.");
        }
    }

    // print everyone's chips after each hand so we can track the game
    private static void printStacks(Game game) {
        System.out.println("Current stacks:");
        for (Player p : game.getPlayers()) {
            System.out.println(p.getName() + ": $" + p.getChips());
        }
    }
}
