package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.Owner;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.StringWrapper;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;

@Command(
    aliases = {"alias", "sa", "name", "rename", "setname", "seta", "nameplot"},
    desc = "Set the plot name"
)
@CommandPermissions("plots.alias")
public class AliasCommand {

    @CommandPermissions("plots.alias.set")
    public boolean set(PlotPlayer player, @Owner Plot plot, String alias) {
        if (alias.isEmpty()) {
            Captions.COMMAND_SYNTAX.send(player, "/plot alias <set> <value>");
            return false;
        }
        if (alias.length() >= 50) {
            MainUtil.sendMessage(player, Captions.ALIAS_TOO_LONG);
            return false;
        }
        if (alias.contains(" ")) {
            Captions.NOT_VALID_VALUE.send(player);
            return false;
        }
        if (MathMan.isInteger(alias)) {
            Captions.NOT_VALID_VALUE.send(player);
            return false;
        }
        for (Plot p : PlotSquared.get().getPlots(plot.getArea())) {
            if (p.getAlias().equalsIgnoreCase(alias)) {
                MainUtil.sendMessage(player, Captions.ALIAS_IS_TAKEN);
                return false;
            }
        }
        if (UUIDHandler.nameExists(new StringWrapper(alias)) || PlotSquared.get()
            .hasPlotArea(alias)) {
            MainUtil.sendMessage(player, Captions.ALIAS_IS_TAKEN);
            return false;
        }
        plot.setAlias(alias);
        MainUtil.sendMessage(player, Captions.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
        return true;
    }

    @Command(
        aliases = "remove",
        desc = "Remove plot name"
    )
    @CommandPermissions("plots.alias.remove")
    public void remove(PlotPlayer player, @Owner Plot plot) {
        plot.setAlias(null);
        MainUtil.sendMessage(player, Captions.ALIAS_REMOVED.s());
    }


}
