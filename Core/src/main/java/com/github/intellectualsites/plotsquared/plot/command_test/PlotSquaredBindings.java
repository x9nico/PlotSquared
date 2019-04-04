package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.parametric.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

public class PlotSquaredBindings extends BindingHelper {

    PlotSquaredBindings(PlotSquared ps) {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @SuppressWarnings("WeakerAccess") public @interface Consume {
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
        if (plr == null) {
            throw new ParameterException(String.format("Illegal player: %s", input));
        }
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
            throw new ParameterException(String.format("Illegal UUID: %s", e.getMessage()));
        }
    }

    @BindingMatch(
        type = BlockBucket.class,
        behavior = BindingBehavior.CONSUMES,
        consumedCount = 1)
    public BlockBucket getBlockBucket(ArgumentStack context) throws ParameterException {
        final BlockBucket bucket;
        try {
            bucket = Configuration.BLOCK_BUCKET.parseString(context.next());
        } catch (final Configuration.UnsafeBlockException e) {
            throw new ParameterException(String.format("Unsafe block: %s", e.getUnsafeBlock()));
        } catch (final Configuration.UnknownBlockException e) {
            throw new ParameterException(String.format("Unknown block: %s", e.getUnknownValue()));
        }
        return bucket;
    }

    @BindingMatch(
        type = Flag.class,
        behavior = BindingBehavior.CONSUMES,
        consumedCount = 1)
    public Flag getFlag(ArgumentStack context) throws ParameterException {
        final String flagName = context.next();
        final Flag flag = Flags.getFlag(flagName);
        if (flag == null) {
            throw new ParameterException(String.format("Unknown flag: %s", flagName));
        }
        return flag;
    }

}
