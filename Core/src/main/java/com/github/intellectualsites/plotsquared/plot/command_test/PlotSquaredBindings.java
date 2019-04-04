package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.commands.Argument;
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
import retrofit2.http.HEAD;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.UUID;

public class PlotSquaredBindings extends BindingHelper {

    /*

     PlotPlayer - provides
     @Consume PlotPlayer - consumes

     // Plot Annotations @Owned @Owner
     Plot - provides
     @Consume Plot - consumes

     PlotArea - provides
     @Consume PlotArea - consumes
     UUID - provides
     @Consume UUID - consumes
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


    PlotSquaredBindings(PlotSquared ps) {
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
    @SuppressWarnings("WeakerAccess") public @interface Consume {}

    @SuppressWarnings("WeakerAccess") public @interface Owned {}

    @SuppressWarnings("WeakerAccess") public @interface Owner {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
    @SuppressWarnings("WeakerAccess") public @interface Choice {
        String[] value();
    }

    @BindingMatch(
        type = String.class,
        behavior = BindingBehavior.CONSUMES,
        classifier = Choice.class,
        provideModifiers = true,
        consumedCount = 1)
    public String getChoice(ArgumentStack context, Annotation[] annotations)
        throws ParameterException {

        String input = context.next();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Choice) {
                String[] choices = ((Choice) annotation).value();
                for (String choice : choices) {
                    if (input.equalsIgnoreCase(choice)) {
                        return choice;
                    }
                }
                throw new ParameterException(Captions.SUBCOMMAND_SET_OPTIONS_HEADER.f(StringMan.join(choices, ",")));
            }
        }
        // Never happens
        throw new ParameterException("idk");
    }

    @BindingMatch(type = PlotPlayer.class, behavior = BindingBehavior.PROVIDES)
    public PlotPlayer getCurrentPlayer(ArgumentStack context) {
        Actor sender = context.getContext().getLocals().get(Actor.class);
        return PlotPlayer.wrap(sender.getName());
    }

    @BindingMatch(type = Location.class,
        behavior = BindingBehavior.PROVIDES)
    public Location getCurrentLocation(ArgumentStack context) {
        PlotPlayer plr = getCurrentPlayer(context);
        return plr.getLocation();
    }

    @BindingMatch(
            type = PlotPlayer.class,
            behavior = BindingBehavior.CONSUMES,
            classifier = Consume.class,
            consumedCount = 1)
    public PlotPlayer getPlayer(ArgumentStack context) throws ParameterException {
        String input = context.next();
        PlotPlayer plr = PlotPlayer.wrap(input);
        if (plr == null) {
            throw new ParameterException(Captions.INVALID_PLAYER.f(input));
        }
        return plr;
    }

    @BindingMatch(type = Plot.class, classifier = Consume.class, behavior = BindingBehavior.PROVIDES, consumedCount = 1, provideModifiers = true)
    public Plot getCurrentPlot(ArgumentStack context, Annotation[] annotations) throws ParameterException {
        Plot plot = getCurrentPlayer(context).getCurrentPlot();
        if (plot == null) throw new ParameterException(Captions.NOT_IN_PLOT.s());
        validate(plot, context, annotations);
        return plot;
    }

    @BindingMatch(type = Plot.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = true)
    public Plot getPlot(ArgumentStack context, Annotation[] annotations) throws ParameterException {
        PlotPlayer plr = getCurrentPlayer(context);
        String input = context.next();
        switch (input) {
            case "me":
                return getCurrentPlot(context, annotations);
            default:
                Plot plot = MainUtil.getPlotFromString(plr, input, true);
                if (plot == null) throw new ParameterException();
                validate(plot, context, annotations);
                return plot;
        }
    }

    private void validate(Plot plot, ArgumentStack context, Annotation[] annotations) throws ParameterException {
        if (getOf(annotations, Owned.class) != null && !plot.hasOwner())
            throw new ParameterException(Captions.PLOT_NOT_CLAIMED.s());
        if (getOf(annotations, Owner.class) != null && !plot.isOwner(getCurrentUUID(context)))
            throw new ParameterException(Captions.NO_PLOT_PERMS.s());
    }

    @BindingMatch(type = PlotArea.class, behavior = BindingBehavior.CONSUMES, classifier = Consume.class, consumedCount = 1)
    public PlotArea getCurrentPlotArea(ArgumentStack context) throws ParameterException {
        PlotArea area = getCurrentPlayer(context).getApplicablePlotArea();
        if (area == null) throw new ParameterException(Captions.NOT_IN_PLOT_WORLD.s());
        return area;
    }

    @BindingMatch(type = PlotArea.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public PlotArea getPlotArea(ArgumentStack context) throws ParameterException {
        PlotPlayer plr = getCurrentPlayer(context);
        String input = context.next();
        switch (input) {
            case "me":
                return getCurrentPlotArea(context);
            default:
                PlotArea area = PlotSquared.get().getPlotAreaByString(input);
                if (area == null) throw new ParameterException(Captions.NOT_VALID_PLOT_WORLD.f(input));
                return area;
        }
    }

    @BindingMatch(type = UUID.class, classifier = Consume.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public UUID getUUID(ArgumentStack context) throws ParameterException {
        PlotPlayer plr = getCurrentPlayer(context);
        String input = context.next();
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            UUID uuid = UUIDHandler.getUUID(input, null);
            if (uuid == null) {
                throw new ParameterException(String.format("Illegal UUID: %s", e.getMessage()));
            }
            return uuid;
        }
    }

    @BindingMatch(type = UUID.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public UUID getCurrentUUID(ArgumentStack context) throws ParameterException {
        return context.getContext().getLocals().get(Actor.class).getUniqueId();
    }

    @BindingMatch(type = BlockBucket.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
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

    private World getWorld(String worldName) throws ParameterException {
        Platform platform =
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        List<? extends World> worlds = platform.getWorlds();
        for (World current : worlds) {
            if (current.getName().equalsIgnoreCase(worldName)) {
                return current;
            }
        }
        throw new ParameterException(Captions.NOT_VALID_WORLD.f(worldName));
    }

    @BindingMatch(type = World.class, behavior = BindingBehavior.PROVIDES, consumedCount = 1)
    public World getCurrentWorld(ArgumentStack context) throws ParameterException {
        Actor sender = context.getContext().getLocals().get(Actor.class);
        if (sender instanceof Player)
            return ((Player) sender).getWorld();
        throw new ParameterException(Captions.IS_CONSOLE.s());
    }

    @BindingMatch(type = World.class, classifier = Consume.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public World getWorld(ArgumentStack context) throws ParameterException {
        String input = context.next();
        return getWorld(input);
    }

    @BindingMatch(type = Flag.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
    public Flag getFlag(ArgumentStack context) throws ParameterException {
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
    public Vector3 getVector3(ArgumentStack context, Annotation[] modifiers)
        throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusY, radiusZ;
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
    public Vector2 getVector2(ArgumentStack context, Annotation[] modifiers)
        throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
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
    public BlockVector3 getBlockVector3(ArgumentStack context, Annotation[] modifiers)
        throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusY, radiusZ;
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
    public BlockVector2 getBlockVector2(ArgumentStack context, Annotation[] modifiers)
        throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
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
    public PlotLoc getPlotLoc(ArgumentStack context, Annotation[] modifiers)
        throws ParameterException {
        String radiusString = context.next();
        String[] radii = radiusString.split(",");
        final double radiusX, radiusZ;
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

    public static @Nullable Double parseNumericInput(@Nonnull String input)
        throws ParameterException {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e1) {
            try {
                Expression expression = Expression.compile(input);
                return expression.evaluate();
            } catch (EvaluationException e) {
                throw new ParameterException(String.format(
                    "Expected '%s' to be a valid number (or a valid mathematical expression)",
                    input));
            } catch (ExpressionException e) {
                throw new ParameterException(String
                    .format("Expected '%s' to be a number or valid math expression (error: %s)",
                        input, e.getMessage()));
            }
        }
    }

    public static <T> T getOf(Object[] arr, Class<T> ofType) {
        for (Object a : arr) {
            if (a != null && a.getClass() == ofType) {
                return (T) a;
            }
        }
        return null;
    }

}
