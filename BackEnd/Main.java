package BackEnd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        line();
        println("                P O K E R");
        println("        chips-only in-person tracker");
        line();

        Game game = new Game();
        setupPlayers(game);

        int handNum = 1;
        while (playersWithChips(game) >= 2) {
            println("");
            line();
            println("  HAND #" + handNum + "  -  Dealer: " + dealerName(game));
            line();
            game.startHand();
            announceBlinds(game);
            playHand(game);

            handNum++;
            if (playersWithChips(game) < 2) break;

            promptEnter("Press Enter for next hand...");
            DealerRotation.advance(game);
        }

        println("");
        line();
        println("  GAME OVER");
        line();
        for (Player p : game.players) {
            println(String.format("  %-16s $%d", p.name, p.chips));
        }
        println("");
    }

    private static void setupPlayers(Game game) {
        int n = readInt("Number of players (" + Config.MIN_PLAYERS + "-" + Config.MAX_PLAYERS + "): ",
                Config.MIN_PLAYERS, Config.MAX_PLAYERS);
        Set<String> taken = new HashSet<>();
        for (int i = 0; i < n; i++) {
            while (true) {
                String name = readName("Player " + (i + 1) + " name: ");
                if (taken.contains(name.toLowerCase())) {
                    println("  That name is taken.");
                    continue;
                }
                taken.add(name.toLowerCase());
                game.addPlayer(name);
                break;
            }
        }
        println("");
        println("  Starting stack: $" + Config.STARTING_STACK);
        println("  Blinds: $" + Config.SMALL_BLIND + " / $" + Config.BIG_BLIND);
        println("");
    }

    private static void announceBlinds(Game game) {
        Player sb = game.players.get(BlindManager.smallBlindIndex(game));
        Player bb = game.players.get(BlindManager.bigBlindIndex(game));
        println("  " + sb.name + " posts small blind $" + Config.SMALL_BLIND);
        println("  " + bb.name + " posts big blind $" + Config.BIG_BLIND);
    }

    private static void playHand(Game game) {
        while (true) {
            if (game.handOver()) {
                game.pots.rebuild(game.players);
                int total = game.pots.total();
                Player winner = game.lastStanding();
                if (winner != null) {
                    winner.chips += total;
                    println("");
                    println("  " + winner.name + " wins $" + total + " (everyone else folded).");
                }
                game.pots.pots.clear();
                game.phase = Game.Phase.SHOWDOWN;
                return;
            }

            if (BettingRound.isComplete(game)) {
                if (game.phase == Game.Phase.RIVER) {
                    game.advancePhase();
                    showdown(game);
                    return;
                }
                printTable(game);
                println("");
                promptEnter("Round complete. Press Enter to deal " + nextStreetName(game.phase) + "...");
                game.advancePhase();
                if (game.activeNotAllIn() <= 1) {
                    while (game.phase != Game.Phase.SHOWDOWN) {
                        announceStreet(game.phase);
                        game.advancePhase();
                    }
                    showdown(game);
                    return;
                }
                announceStreet(game.phase);
                continue;
            }

            Player p = game.players.get(game.currentTurn);
            if (p.folded || p.allIn) {
                game.currentTurn = BettingRound.nextTurn(game);
                continue;
            }

            printTable(game);
            promptEnter("Pass the device to " + p.name + ". Press Enter when ready...");
            takeAction(game, p);

            game.currentTurn = BettingRound.nextTurn(game);
        }
    }

    private static void takeAction(Game game, Player p) {
        while (true) {
            int toCall = Math.max(0, game.currentBet - p.currentBet);
            println("");
            println("  " + p.name + ", you have $" + p.chips + ".");
            if (toCall > 0) println("  To call: $" + toCall);
            else println("  No bet to call.");
            println("");
            StringBuilder opts = new StringBuilder("  ");
            opts.append("[F]old   ");
            if (toCall == 0) opts.append("[C]heck  ");
            else opts.append("[C]all $" + Math.min(toCall, p.chips) + "  ");
            if (p.chips > toCall) opts.append("[R]aise  ");
            opts.append("[A]ll-in");
            println(opts.toString());
            String choice = readLine("  > ").trim().toLowerCase();
            if (choice.isEmpty()) continue;
            char c = choice.charAt(0);

            Action a = null;
            if (c == 'f') a = new Action(Action.Type.FOLD, 0);
            else if (c == 'c') {
                if (toCall == 0) a = new Action(Action.Type.CHECK, 0);
                else a = new Action(Action.Type.CALL, 0);
            } else if (c == 'r') {
                int min = game.currentBet + game.lastRaiseSize;
                int max = p.currentBet + p.chips;
                if (min > max) min = max;
                int amount = readInt("  Raise to (min $" + min + ", max $" + max + ", $" + Config.RAISE_INCREMENT + " increments): ", min, max);
                a = new Action(Action.Type.RAISE, amount);
            } else if (c == 'a') {
                a = new Action(Action.Type.ALL_IN, 0);
            } else {
                println("  Unknown choice.");
                continue;
            }

            String err = ActionValidator.validate(game, p, a);
            if (err != null) {
                println("  " + err + ".");
                continue;
            }
            game.applyAction(p, a);
            println("  " + describe(p, a));
            return;
        }
    }

    private static String describe(Player p, Action a) {
        switch (a.type) {
            case FOLD: return p.name + " folds.";
            case CHECK: return p.name + " checks.";
            case CALL: return p.name + " calls" + (p.allIn ? " all-in" : "") + ".";
            case RAISE: return p.name + " raises to $" + a.amount + (p.allIn ? " (all-in)" : "") + ".";
            case ALL_IN: return p.name + " is all-in for $" + p.currentBet + ".";
        }
        return "";
    }

    private static void showdown(Game game) {
        game.pots.rebuild(game.players);
        if (game.pots.pots.isEmpty()) {
            game.phase = Game.Phase.SHOWDOWN;
            return;
        }
        println("");
        line();
        println("  SHOWDOWN");
        line();
        showPlayersBrief(game);

        for (int i = 0; !game.pots.pots.isEmpty(); ) {
            Pot pot = game.pots.pots.get(0);
            String label = (i == 0) ? "Main Pot" : "Side Pot " + i;
            println("");
            println("  " + label + ": $" + pot.amount);

            if (pot.eligible.size() == 1) {
                Player only = findById(game, pot.eligible.get(0));
                if (only != null) {
                    only.chips += pot.amount;
                    println("  " + only.name + " wins $" + pot.amount + " (uncontested).");
                }
                game.pots.pots.remove(0);
                i++;
                continue;
            }

            List<Player> eligible = new ArrayList<>();
            for (String pid : pot.eligible) {
                Player pl = findById(game, pid);
                if (pl != null) eligible.add(pl);
            }

            println("  Who won this pot?");
            for (int j = 0; j < eligible.size(); j++) {
                println("    " + (j + 1) + ") " + eligible.get(j).name);
            }
            println("    " + (eligible.size() + 1) + ") Split between all");

            int choice = readInt("  > ", 1, eligible.size() + 1);
            if (choice == eligible.size() + 1) {
                int share = pot.amount / eligible.size();
                int rem = pot.amount - share * eligible.size();
                for (Player pl : eligible) pl.chips += share;
                if (rem > 0) eligible.get(0).chips += rem;
                println("  Pot split: $" + share + " each" + (rem > 0 ? " (+$" + rem + " odd chip to " + eligible.get(0).name + ")" : "") + ".");
            } else {
                Player w = eligible.get(choice - 1);
                w.chips += pot.amount;
                println("  " + w.name + " wins $" + pot.amount + ".");
            }
            game.pots.pots.remove(0);
            i++;
        }
        game.phase = Game.Phase.SHOWDOWN;
    }

    private static Player findById(Game g, String identityName) {
        for (Player p : g.players) if (p.name.equals(identityName)) return p;
        return null;
    }

    private static void showPlayersBrief(Game game) {
        for (Player p : game.players) {
            if (p.folded) continue;
            println("    " + p.name + "  $" + p.chips + (p.allIn ? "  (was all-in)" : ""));
        }
    }

    private static void announceStreet(Game.Phase phase) {
        if (phase == Game.Phase.FLOP) println("  --- FLOP ---");
        else if (phase == Game.Phase.TURN) println("  --- TURN ---");
        else if (phase == Game.Phase.RIVER) println("  --- RIVER ---");
    }

    private static String nextStreetName(Game.Phase phase) {
        switch (phase) {
            case PREFLOP: return "the flop";
            case FLOP: return "the turn";
            case TURN: return "the river";
            default: return "next street";
        }
    }

    private static void printTable(Game game) {
        println("");
        println("  Phase: " + phaseLabel(game.phase) + "    Pot: $" + currentPotTotal(game));
        println("");
        for (int i = 0; i < game.players.size(); i++) {
            Player p = game.players.get(i);
            String mark = (i == game.currentTurn) ? ">" : " ";
            String dealer = (i == game.dealerIndex) ? "[D]" : "   ";
            String status;
            if (p.folded) status = " folded";
            else if (p.allIn) status = " ALL-IN";
            else if (p.currentBet > 0) status = " bet $" + p.currentBet;
            else status = "";
            println(String.format("  %s %s %-16s $%-6d%s", mark, dealer, p.name, p.chips, status));
        }
        println("");
    }

    private static String phaseLabel(Game.Phase p) {
        switch (p) {
            case WAITING: return "Waiting";
            case PREFLOP: return "Pre-Flop";
            case FLOP: return "Flop";
            case TURN: return "Turn";
            case RIVER: return "River";
            case SHOWDOWN: return "Showdown";
        }
        return p.name();
    }

    private static int currentPotTotal(Game game) {
        int s = 0;
        for (Player p : game.players) s += p.totalBet;
        return s;
    }

    private static int playersWithChips(Game game) {
        int c = 0;
        for (Player p : game.players) if (p.chips > 0) c++;
        return c;
    }

    private static String dealerName(Game game) {
        if (game.dealerIndex < 0 || game.dealerIndex >= game.players.size()) return "?";
        return game.players.get(game.dealerIndex).name;
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    println("  Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                println("  Not a number.");
            }
        }
    }

    private static String readName(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            if (s.isEmpty()) { println("  Name cannot be empty."); continue; }
            if (s.length() > 16) { println("  Name too long (max 16)."); continue; }
            return s;
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SC.hasNextLine()) {
            println("");
            System.exit(0);
        }
        return SC.nextLine();
    }

    private static void promptEnter(String msg) {
        System.out.print("  " + msg);
        if (SC.hasNextLine()) SC.nextLine();
    }

    private static void println(String s) {
        System.out.println(s);
    }

    private static void line() {
        println("  ------------------------------------------");
    }
}
