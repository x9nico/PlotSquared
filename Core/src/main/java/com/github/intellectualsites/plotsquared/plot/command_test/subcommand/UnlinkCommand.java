package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.command_test.binding.Merged;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.Owner;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.util.command.parametric.Optional;

public class UnlinkCommand {

    @Command(aliases = {"unlink", "u", "unmerge"}, desc = "Unlink a mega-plot", usage = "[createroads]")
    @CommandPermissions("plots.unlink")
    public boolean unlink(PlotPlayer player, @Owner @Merged Plot plot, @Optional("true") boolean createRoad) throws
        CommandException {
        player.confirm();
        if (!plot.unlinkPlot(createRoad, createRoad)) {
            MainUtil.sendMessage(player, "&cUnlink has been cancelled");
            return false;
        }
        Captions.UNLINK_SUCCESS.send(player);
        return true;
    }

}
