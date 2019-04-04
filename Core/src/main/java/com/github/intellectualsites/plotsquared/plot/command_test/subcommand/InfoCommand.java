package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.command_test.binding.Clazz;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.Consume;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.Owned;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.PlotSquaredBindings;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.parametric.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class InfoCommand {

    @Command(aliases = {"info", "i"}, desc = "Display plot info", flags = "f",
        usage = "[<id>] [<component>]", help = "-f forces the info to display even if the info is hidden")
    @CommandPermissions("plots.info")
    public boolean info(PlotPlayer player, @Consume @Owned
        @Optional("me") Plot plot, @Clazz(InfoComponent.class) @Optional("none") InfoComponent component,
        @Switch('f') boolean force) {

        // hide-info flag
        if (plot.getFlag(Flags.HIDE_INFO).orElse(false)) {
            if (force) {
                if (!player.hasPermission(Captions.PERMISSION_AREA_INFO_FORCE.s())) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_INFO_FORCE);
                    return false;
                }
            } else {
                Captions.PLOT_INFO_HIDDEN.send(player);
                return false;
            }
        }

        final String info;
        final boolean full;
        if (component != null && component != InfoComponent.NONE) {
            full = false;
            info = component.getCaption().s();
        } else {
            full = true;
            info = Captions.PLOT_INFO.s();
        }

        MainUtil.format(info, plot, player, full, new RunnableVal<String>() {
            @Override public void run(String value) {
                MainUtil.sendMessage(player,
                    Captions.PLOT_INFO_HEADER.s() + '\n' + value + '\n' + Captions.PLOT_INFO_FOOTER
                        .s(), false);
            }
        });
        return true;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE) @Getter public enum InfoComponent {
        TRUSTED(Captions.PLOT_INFO_TRUSTED),
        ALIAS(Captions.PLOT_INFO_ALIAS),
        BIOME(Captions.PLOT_INFO_BIOME),
        DENIED(Captions.PLOT_INFO_DENIED),
        FLAGS(Captions.PLOT_INFO_FLAGS),
        ID(Captions.PLOT_INFO_ID),
        SIZE(Captions.PLOT_INFO_SIZE),
        MEMBERS(Captions.PLOT_INFO_MEMBERS),
        OWNER(Captions.PLOT_INFO_OWNER),
        RATING(Captions.PLOT_INFO_RATING),
        LIKES(Captions.PLOT_INFO_LIKES),
        SEEN(Captions.PLOT_INFO_SEEN),
        NONE(null);

        private final Captions caption;

        @Override public String toString() {
            return this.name().toLowerCase();
        }
    }

}
