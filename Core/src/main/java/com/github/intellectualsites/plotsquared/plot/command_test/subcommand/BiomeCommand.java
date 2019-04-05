package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.command_test.binding.NotTimerBound;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.Owner;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class BiomeCommand {

    @Command(aliases = {"setbiome", "biome", "sb", "setb", "b"}, desc = "Set the plot biome",
        usage = "<biome>")
    @CommandPermissions("plots.set.biome")
    public boolean biome(PlotPlayer player, @Owner @NotTimerBound Plot plot, String biomeType) {
        int biome = WorldUtil.IMP.getBiomeFromString(biomeType);
        if (biome == -1) {
            String biomes =
                StringMan.join(WorldUtil.IMP.getBiomeList(), Captions.BLOCK_LIST_SEPARATER.s());
            Captions.NEED_BIOME.send(player);
            MainUtil.sendMessage(player, Captions.SUBCOMMAND_SET_OPTIONS_HEADER.s() + biomes);
            return false;
        }

        plot.addRunning();
        plot.setBiome(biomeType.toUpperCase(), () -> {
            plot.removeRunning();
            MainUtil.sendMessage(player, Captions.BIOME_SET_TO.s() + biomeType.toLowerCase());
        });
        return true;
    }

}
