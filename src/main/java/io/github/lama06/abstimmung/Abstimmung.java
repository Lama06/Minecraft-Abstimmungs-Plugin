package io.github.lama06.abstimmung;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

enum PlayerVote {
    YES,
    NO
}

enum VoteResult {
    YES,
    NO,
    DRAW
}

public final class Abstimmung extends JavaPlugin implements Listener {

    // Prefix für alle Chat Ausgaben
    private final String PREFIX = ChatColor.BLUE + "[Abstimmung] ";

    // Gibt an, ob aktuell eine Abstimmung läuft
    private boolean running = false;

    // Enthält alle Spieler und für was sie abgestimmt haben
    private final Map<String, PlayerVote> votes = new HashMap<>();








    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Dieser Command kann nicht in der Konsole ausgeführt werden");
            return true;
        } else if(cmd.getName().equals("abstimmung")) {
            if(running) {
                sender.sendMessage(PREFIX + " Es läuft bereits eine Abstimmung");
                return true;
            }

            StringBuilder question = new StringBuilder();

            for(String arg : args) {
                question.append(arg);
                question.append(" ");
            }

            startVote((Player) sender, question.toString());
        } else if(cmd.getName().equals("ja")) {
            addVoteYes((Player) sender);
        } else if(cmd.getName().equals("nein")) {
            addVoteNo((Player) sender);
        }

        return true;
    }

    @EventHandler
    public void onPlayerVotesInChat(AsyncPlayerChatEvent e) {
        if(!running) return;

        String msg = e.getMessage();
        Player player = e.getPlayer();

        if(msg.equalsIgnoreCase("Ja")) {
            addVoteYes(player);
        } else {
            addVoteNo(player);
        }

        // Verhindert, dass der Spieler zusätzlich in den Chat schreibt
        e.setCancelled(true);
    }








    // Startet die Abstimmung
    private void startVote(Player author, String question) {
        running = true;

        Bukkit.broadcastMessage(PREFIX + question + " (von " + author.getName() + ")");
        Bukkit.broadcastMessage(PREFIX + "Schreibe Ja oder Nein in den Chat um abzustimmen. Die Abstimmung endet in 30 Sekunden");

        getServer().getScheduler().scheduleSyncDelayedTask(this, this::endVote, 600L);
    }

    // Beendet die Abstimmung
    private void endVote() {
        Bukkit.broadcastMessage(PREFIX + "Die Abstimmung ist vorbei");

        switch(getVoteResult()) {
            case YES:
                Bukkit.broadcastMessage(PREFIX + "Das Ergebnis ist JA mit " + getVotesYes() + " zu " + getVotesNo() + " Stimmen");
                break;
            case NO:
                Bukkit.broadcastMessage(PREFIX + "Das Ergebnis ist NEIN mit " + getVotesNo() + " zu " + getVotesYes() + " Stimmen");
                break;
            case DRAW:
                Bukkit.broadcastMessage(PREFIX + "Das Ergebnis ist UNENTSCHIEDEN mit " + getVotesYes() + " zu " + getVotesNo() + " Stimmen");
        }

        // Abstimmung Zurücksetzten
        running = false;
        votes.clear();
    }

    // Fügt eine Stimme für JA hinzu
    private void addVoteYes(Player player) {
        if(!running) {
            player.sendMessage(PREFIX + "Es läuft aktuell keine Abstimmung");
            return;
        }

        if(votes.containsKey(player.getName())) {
            if(votes.get(player.getName()) == PlayerVote.YES) {
                player.sendMessage(PREFIX + "Du hast schon für JA abgestimmt");
            } else {
                Bukkit.broadcastMessage(PREFIX + player.getName() + " hat seine Meinung zu JA geändert");
            }
        } else {
            Bukkit.broadcastMessage(PREFIX + player.getName() + " hat für JA abgestimmt");
        }

        votes.put(player.getName(), PlayerVote.YES);
    }

    // Fügt eine Stimme für NEIN hinzu
    private void addVoteNo(Player player) {
        if(!running) {
            player.sendMessage(PREFIX + "Es läuft aktuell keine Abstimmung");
            return;
        }

        if(votes.containsKey(player.getName())) {
            if(votes.get(player.getName()) == PlayerVote.NO) {
                player.sendMessage(PREFIX + "Du hast schon für NEIN abgestimmt");
            } else {
                Bukkit.broadcastMessage(PREFIX + player.getName() + " hat seine Meinung zu NEIN geändert");
            }
        } else {
            Bukkit.broadcastMessage(PREFIX + player.getName() + " hat für NEIN abgestimmt");
        }

        votes.put(player.getName(), PlayerVote.NO);
    }

    // Gibt die Anzahl der Stimmen für JA zurück
    private int getVotesYes() {
        int votesYes = 0;

        for (Map.Entry<String, PlayerVote> vote : votes.entrySet()) {
            if(vote.getValue() == PlayerVote.YES) votesYes += 1;
        }

        return votesYes;
    }

    // Gibt die Anzahl der Stimmen für NEIN zurück
    private int getVotesNo() {
        int votesNo = 0;

        for (Map.Entry<String, PlayerVote> vote : votes.entrySet()) {
            if(vote.getValue() == PlayerVote.NO) votesNo += 1;
        }

        return votesNo;
    }

    // Gibt das Ergebnis der Abstimmung zurück
    private VoteResult getVoteResult() {
       int votesYes = getVotesYes();
       int votesNo = getVotesNo();

       if(votesYes > votesNo) {
           return VoteResult.YES;
       } else if(votesNo > votesYes) {
           return VoteResult.NO;
       } else {
           return VoteResult.DRAW;
       }
    }
}
