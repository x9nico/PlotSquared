package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.parametric.ArgumentStack;
import com.sk89q.worldedit.util.command.parametric.Binding;
import com.sk89q.worldedit.util.command.parametric.BindingBehavior;
import com.sk89q.worldedit.util.command.parametric.BindingHelper;
import com.sk89q.worldedit.util.command.parametric.BindingMatch;
import com.sk89q.worldedit.util.command.parametric.ParameterException;

import java.lang.annotation.Annotation;

public class PlotSquaredBindings extends BindingHelper {
    public PlotSquaredBindings(PlotSquared ps) {
    }

    @BindingMatch(type = PlotPlayer.class,
            behavior = BindingBehavior.PROVIDES)
    public PlotPlayer getPlayer(ArgumentStack context) {
        Actor sender = context.getContext().getLocals().get(Actor.class);
        return PlotPlayer.wrap(sender.getName());
    }

    @BindingMatch(
            type = Plot.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1)
    public Plot getCommandContext(ArgumentStack context) throws ParameterException {
        PlotPlayer plr = getPlayer(context);
        String input = context.next();
        switch (input) {
            case "me":
                return plr.getCurrentPlot();
            default:
                return MainUtil.getPlotFromString(plr, input, false);
        }
    }
}
