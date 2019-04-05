package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.command_test.binding.NotDone;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.NotTimerBound;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.Owner;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class ClearCommand {

    @Command(aliases = {"clear", "reset"}, desc = "Clear the plot you stand on")
    @CommandPermissions("plots.clear")
    public boolean clear(PlotPlayer player, @Owner @NotTimerBound @NotDone Plot plot) throws
        CommandException {
        player.confirm(); // wait for confirmation
        final long start = System.currentTimeMillis();
        boolean result = plot.clear(true, false, () -> {
            plot.unlink();
            GlobalBlockQueue.IMP.addTask(() -> {
                plot.removeRunning();
                // If the state changes, then mark it as no longer done
                if (plot.getFlag(Flags.DONE).isPresent()) {
                    FlagManager.removePlotFlag(plot, Flags.DONE);
                }
                if (plot.getFlag(Flags.ANALYSIS).isPresent()) {
                    FlagManager.removePlotFlag(plot, Flags.ANALYSIS);
                }
                MainUtil.sendMessage(player, Captions.CLEARING_DONE,
                    "" + (System.currentTimeMillis() - start));
            });
        });
        if (!result) {
            MainUtil.sendMessage(player, Captions.CLEARING_FAILED);
        } else {
            plot.addRunning();
        }
        return true;
    }
}
