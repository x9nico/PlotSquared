package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotLoc;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.command.parametric.ArgumentStack;
import com.sk89q.worldedit.util.command.parametric.BindingBehavior;
import com.sk89q.worldedit.util.command.parametric.BindingHelper;
import com.sk89q.worldedit.util.command.parametric.BindingMatch;
import com.sk89q.worldedit.util.command.parametric.ParameterException;
import com.sk89q.worldedit.world.World;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"}) @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class PlotSquaredBindings extends BindingHelper {

    /*

     PlotPlayer - provides
     @Consume PlotPlayer - consumes
     Plot - provides
     @Consume Plot - consumes
     PlotArea - provides
     @Consume PlotArea - consumes
     UUID - consumes
     BlockBucket - consumes
     World
     @Consume World
     Flag - consumes
     Vector3 - consumes
     BlockVector3 - consumes
     Vector2 - consumes
     BlockVector2 - consumes
     @Text String - consumes all


     */
    private final PlotSquared plotSquared;

    private static Double parseNumericInput(final @Nullable String input)
        throws ParameterException {
        if (input == null) {
            throw new ParameterException(Captions.NOT_VALID_NUMBER
                .f(String.format("[-%.0f,%.0f]", Double.MIN_VALUE, Double.MAX_VALUE)));
        }
        try {
            return Double.parseDouble(input);
        } catch (final NumberFormatException e1) {
            try {
                final Expression expression = Expression.compile(input);
                return expression.evaluate();
            } catch (final EvaluationException e) {
                throw new ParameterException(String.format(
                    "Expected '%s' to be a valid number (or a valid mathematical expression)",
                    input));
            } catch (final ExpressionException e) {
                throw new ParameterException(String
                    .format("Expected '%s' to be a number or valid math expression (error: %s)",
                        input, e.getMessage()));
            }
        }
    }

    @BindingMatch(type = String.class, behavior = BindingBehavior.CONSUMES, classifier = Choice.class, provideModifiers = true, consumedCount = 1)
    public String getChoice(final ArgumentStack context, final Annotation[] annotations)
        throws ParameterException {
        final String input = context.next();
        for (final Annotation annotation : annotations) {
            if (annotation instanceof Choice) {
                String[] choices = ((Choice) annotation).value();
                for (String choice : choices) {
                    if (input.equalsIgnoreCase(choice)) {
                        return choice;
                    }
                }
                throw new ParameterException(
                    Captions.SUBCOMMAND_SET_OPTIONS_HEADER.f(StringMan.join(choices, ",")));
            }
        }
        // Never happens
        throw new ParameterException("idk");
    }

    @BindingMatch(type = PlotPlayer.class, behavior = BindingBehavior.PROVIDES)
    public PlotPlayer getCurrentPlayer(final ArgumentStack context) {
        final Actor sender = context.getContext().getLocals().get(Actor.class);
        return PlotPlayer.wrap(sender.getName());
    }

    @BindingMatch(type = Location.class, behavior = BindingBehavior.PROVIDES)
    public Location getCurrentLocation(final ArgumentStack context) {
        final PlotPlayer plr = getCurrentPlayer(context);
        return plr.getLocation();
    }

    @BindingMatch(type = PlotPlayer.class, behavior = BindingBehavior.CONSUMES, classifier = Consume.class, consumedCount = 1)
    public PlotPlayer getPlayer(final ArgumentStack context) throws ParameterException {
        final String input = context.next();
        final PlotPlayer plr = PlotPlayer.wrap(input);
        if (plr == null) {
            throw new ParameterException(Captions.INVALID_PLAYER.f(input));
        }
        return plr;
    }

    @BindingMatch(type = Plot.class, classifier = Consume.class, behavior = BindingBehavior.PROVIDES, consumedCount = 1)
    public Plot getCurrentPlot(final ArgumentStack context) throws ParameterException {
        final Plot plot = getCurrentPlayer(context).getCurrentPlot();
        if (plot == null) {
            throw new ParameterException(Captions.NOT_IN_PLOT.s());
        }
        return plot;
    }

    @BindingMatch(type = Plot.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public Plot getPlot(final ArgumentStack context) throws ParameterException {
        final String input = context.next();
        if ("me".equals(input)) {
            return getCurrentPlot(context);
        }
        final PlotPlayer plr = getCurrentPlayer(context);
        final Plot plot = MainUtil.getPlotFromString(plr, input, true);
        if (plot == null) {
            throw new ParameterException(String.format("Invalid plot: %s", input));
        }
        return plot;
    }

    @BindingMatch(type = PlotArea.class, behavior = BindingBehavior.CONSUMES, classifier = Consume.class, consumedCount = 1)
    public PlotArea getCurrentPlotArea(final ArgumentStack context) throws ParameterException {
        final PlotArea area = getCurrentPlayer(context).getApplicablePlotArea();
        if (area == null) {
            throw new ParameterException(Captions.NOT_IN_PLOT_WORLD.s());
        }
        return area;
    }

    @BindingMatch(type = PlotArea.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public PlotArea getPlotArea(final ArgumentStack context) throws ParameterException {
        final PlotPlayer plr = getCurrentPlayer(context);
        final String input = context.next();
        if ("me".equals(input)) {
            return getCurrentPlotArea(context);
        }
        final PlotArea area = PlotSquared.get().getPlotAreaByString(input);
        if (area == null) {
            throw new ParameterException(Captions.NOT_VALID_PLOT_WORLD.f(input));
        }
        return area;
    }

    @BindingMatch(type = UUID.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public UUID getUUID(final ArgumentStack context) throws ParameterException {
        final PlotPlayer plr = getCurrentPlayer(context);
        final String input = context.next();
        try {
            return UUID.fromString(input);
        } catch (final IllegalArgumentException e) {
            final UUID uuid = UUIDHandler.getUUID(input, null);
            if (uuid == null) {
                throw new ParameterException(String.format("Illegal UUID: %s", e.getMessage()));
            }
            return uuid;
        }
    }

    @BindingMatch(type = BlockBucket.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public BlockBucket getBlockBucket(final ArgumentStack context) throws ParameterException {
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

    private World getWorld(final String worldName) throws ParameterException {
        final Platform platform =
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        final List<? extends World> worlds = platform.getWorlds();
        for (final World current : worlds) {
            if (current.getName().equalsIgnoreCase(worldName)) {
                return current;
            }
        }
        throw new ParameterException(Captions.NOT_VALID_WORLD.f(worldName));
    }

    @BindingMatch(type = World.class, behavior = BindingBehavior.PROVIDES, consumedCount = 1)
    public World getCurrentWorld(final ArgumentStack context) throws ParameterException {
        final Actor sender = context.getContext().getLocals().get(Actor.class);
        if (sender instanceof Player) {
            return ((Player) sender).getWorld();
        }
        throw new ParameterException(Captions.IS_CONSOLE.s());
    }

    @BindingMatch(type = World.class, classifier = Consume.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public World getWorld(final ArgumentStack context) throws ParameterException {
        final String input = context.next();
        return getWorld(input);
    }

    @BindingMatch(type = Flag.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public Flag getFlag(final ArgumentStack context) throws ParameterException {
        final String flagName = context.next();
        final Flag flag = Flags.getFlag(flagName);
        if (flag == null) {
            throw new ParameterException(Captions.INVALID_COMMAND_FLAG.f(flagName));
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
    @BindingMatch(type = Vector3.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = true)
    public Vector3 getVector3(final ArgumentStack context, final Annotation[] modifiers)
        throws ParameterException {
        final String radiusString = context.next();
        final String[] radii = radiusString.split(",");
        final double radiusX;
        final double radiusY;
        final double radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusY = radiusZ = parseNumericInput(radii[0]);
                break;
            case 3:
                radiusX = parseNumericInput(radii[0]);
                radiusY = parseNumericInput(radii[1]);
                radiusZ = parseNumericInput(radii[2]);
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
    @BindingMatch(type = Vector2.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = true)
    public Vector2 getVector2(final ArgumentStack context, final Annotation[] modifiers)
        throws ParameterException {
        final String radiusString = context.next();
        final String[] radii = radiusString.split(",");
        final double radiusX;
        final double radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusZ = parseNumericInput(radii[0]);
                break;
            case 2:
                radiusX = parseNumericInput(radii[0]);
                radiusZ = parseNumericInput(radii[1]);
                break;
            default:
                throw new ParameterException("You must either specify 1 or 2 radius values.");
        }
        return Vector2.at(radiusX, radiusZ);
    }

    /**
     * Gets a type from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return the requested type
     * @throws ParameterException on error
     */
    @BindingMatch(type = BlockVector3.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = true)
    public BlockVector3 getBlockVector3(final ArgumentStack context, final Annotation[] modifiers)
        throws ParameterException {
        final String radiusString = context.next();
        final String[] radii = radiusString.split(",");
        final double radiusX;
        final double radiusY;
        final double radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusY = radiusZ = parseNumericInput(radii[0]);
                break;
            case 3:
                radiusX = parseNumericInput(radii[0]);
                radiusY = parseNumericInput(radii[1]);
                radiusZ = parseNumericInput(radii[2]);
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
    @BindingMatch(type = BlockVector2.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = true)
    public BlockVector2 getBlockVector2(final ArgumentStack context, final Annotation[] modifiers)
        throws ParameterException {
        final String radiusString = context.next();
        final String[] radii = radiusString.split(",");
        final double radiusX;
        final double radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusZ = parseNumericInput(radii[0]);
                break;
            case 2:
                radiusX = parseNumericInput(radii[0]);
                radiusZ = parseNumericInput(radii[1]);
                break;
            default:
                throw new ParameterException("You must either specify 1 or 2 radius values.");
        }
        return BlockVector2.at(radiusX, radiusZ);
    }

    @BindingMatch(type = PlotLoc.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = true)
    public PlotLoc getPlotLoc(final ArgumentStack context, final Annotation[] modifiers)
        throws ParameterException {
        final String radiusString = context.next();
        final String[] radii = radiusString.split(",");
        final double radiusX;
        final double radiusZ;
        switch (radii.length) {
            case 1:
                radiusX = radiusZ = parseNumericInput(radii[0]);
                break;
            case 2:
                radiusX = parseNumericInput(radii[0]);
                radiusZ = parseNumericInput(radii[1]);
                break;
            default:
                throw new ParameterException("You must either specify 1 or 2 radius values.");
        }
        return new PlotLoc((int) radiusX, (int) radiusZ);
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
    @SuppressWarnings("WeakerAccess") public @interface Consume {
    }


    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
    public @interface Choice {
        String[] value();
    }

}
