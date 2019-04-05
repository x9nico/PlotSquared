package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class ChatCommand {

    @Command(aliases = "chat", desc = "Toggle plot chat on or off")
    @CommandPermissions("plots.chat")
    public boolean chat(PlotPlayer player) {
        if (MainCommand.getInstance().toggle.toggle(player, "chat")) {
            MainUtil.sendMessage(player, Captions.TOGGLE_DISABLED, "chat");
        } else {
            MainUtil.sendMessage(player, Captions.TOGGLE_ENABLED, "chat");
        }
        return true;
    }

}
