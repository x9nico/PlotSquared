package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

public class PlotSquaredBindings extends BindingHelper {
    public PlotSquaredBindings(PlotSquared ps) {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Consume {

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

    @BindingMatch(
            type = PlotPlayer.class,
            behavior = BindingBehavior.CONSUMES,
            classifier = Consume.class,
            consumedCount = 1)
    public PlotPlayer getOtherPlayer(ArgumentStack context) throws ParameterException {
        String input = context.next();
        PlotPlayer plr = PlotPlayer.wrap(input);
        if (plr == null) throw new ParameterException("Invalid player " + input);
        return plr;
    }

    @BindingMatch(
            type = UUID.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1)
    public UUID getUUID(ArgumentStack context) throws ParameterException {
        PlotPlayer plr = getPlayer(context);
        String input = context.next();
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            throw new ParameterException("Invalid uuid " + e.getMessage());
        }
    }
}
