package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.HashSet;
import java.util.Set;

public class ReloadCommandBlock implements Listener {

    private final Set<String> forbidden = new HashSet<>();

    {
        forbidden.add("reload");
        forbidden.add("reload confirm");
        forbidden.add("rl");
        forbidden.add("rl confirm");
        forbidden.add("bukkit:reload");
        forbidden.add("bukkit:reload confirm");
        forbidden.add("bukkit:rl");
        forbidden.add("bukkit:rl confirm");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        if (forbidden.contains(command.substring(1))) {
            event.setCancelled(true);
            PlotSquared.log(Captions.PREFIX + "&6The reload command has been disabled by PlotSquared to prevent any damage to your server.\nRead more about it here: https://matthewmiller.dev/blog/problem-with-reload/");
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase();
        if (forbidden.contains(command)) {
            event.setCancelled(true);
            PlotSquared.log(Captions.PREFIX + "&6The reload command has been disabled by PlotSquared to prevent any damage to your server.\nRead more about it here: https://matthewmiller.dev/blog/problem-with-reload/");
        }
    }
}
