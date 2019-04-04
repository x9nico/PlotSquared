package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotLoc;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.command.parametric.*;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.UUID;

import static com.sk89q.worldedit.util.command.parametric.BindingHelper.validate;

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

    /**
     * Gets a type from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return the requested type
     * @throws ParameterException on error
     */
    @BindingMatch(type = Vector3.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1,
            provideModifiers = true)
    public Vector3 getVector3(ArgumentStack context, Annotation[] modifiers) throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusY, radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusY = radiusZ = Math.max(1, parseNumericInput(radii[0]));
                break;

            case 3:
                radiusX = Math.max(1, parseNumericInput(radii[0]));
                radiusY = Math.max(1, parseNumericInput(radii[1]));
                radiusZ = Math.max(1, parseNumericInput(radii[2]));
                break;

            default:
                throw new ParameterException("You must either specify 1 or 3 radius values.");
        }
        return Vector3.at(radiusX, radiusY, radiusZ);
    }


    /**
     * Gets a type from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return the requested type
     * @throws ParameterException on error
     */
    @BindingMatch(type = Vector2.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1,
            provideModifiers = true)
    public Vector2 getVector2(ArgumentStack context, Annotation[] modifiers) throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusZ = Math.max(1, parseNumericInput(radii[0]));
                break;

            case 2:
                radiusX = Math.max(1, parseNumericInput(radii[0]));
                radiusZ = Math.max(1, parseNumericInput(radii[1]));
                break;

            default:
                throw new ParameterException("You must either specify 1 or 2 radius values.");
        }
        return Vector2.at(radiusX, radiusZ);
    }    /**
     * Gets a type from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return the requested type
     * @throws ParameterException on error
     */
    @BindingMatch(type = BlockVector3.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1,
            provideModifiers = true)
    public BlockVector3 getBlockVector3(ArgumentStack context, Annotation[] modifiers) throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusY, radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusY = radiusZ = Math.max(1, parseNumericInput(radii[0]));
                break;

            case 3:
                radiusX = Math.max(1, parseNumericInput(radii[0]));
                radiusY = Math.max(1, parseNumericInput(radii[1]));
                radiusZ = Math.max(1, parseNumericInput(radii[2]));
                break;

            default:
                throw new ParameterException("You must either specify 1 or 3 radius values.");
        }
        return BlockVector3.at(radiusX, radiusY, radiusZ);
    }


    /**
     * Gets a type from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return the requested type
     * @throws ParameterException on error
     */
    @BindingMatch(type = BlockVector2.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1,
            provideModifiers = true)
    public BlockVector2 getBlockVector2(ArgumentStack context, Annotation[] modifiers) throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusZ = Math.max(1, parseNumericInput(radii[0]));
                break;

            case 2:
                radiusX = Math.max(1, parseNumericInput(radii[0]));
                radiusZ = Math.max(1, parseNumericInput(radii[1]));
                break;

            default:
                throw new ParameterException("You must either specify 1 or 2 radius values.");
        }
        return BlockVector2.at(radiusX, radiusZ);
    }

    @BindingMatch(type = PlotLoc.class,
            behavior = BindingBehavior.CONSUMES,
            consumedCount = 1,
            provideModifiers = true)
    public PlotLoc getPlotLoc(ArgumentStack context, Annotation[] modifiers) throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusZ = Math.max(1, parseNumericInput(radii[0]));
                break;

            case 2:
                radiusX = Math.max(1, parseNumericInput(radii[0]));
                radiusZ = Math.max(1, parseNumericInput(radii[1]));
                break;

            default:
                throw new ParameterException("You must either specify 1 or 2 radius values.");
        }
        return new PlotLoc((int) radiusX, (int) radiusZ);
    }

    public static @Nullable Double parseNumericInput(@Nullable String input) throws ParameterException {
        if (input == null) {
            return null;
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e1) {
            try {
                Expression expression = Expression.compile(input);
                return expression.evaluate();
            } catch (EvaluationException e) {
                throw new ParameterException(String.format(
                        "Expected '%s' to be a valid number (or a valid mathematical expression)", input));
            } catch (ExpressionException e) {
                throw new ParameterException(String.format(
                        "Expected '%s' to be a number or valid math expression (error: %s)", input, e.getMessage()));
            }
        }
    }

}
