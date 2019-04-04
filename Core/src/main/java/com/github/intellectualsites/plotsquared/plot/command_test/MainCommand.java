package com.github.intellectualsites.plotsquared.plot.command_test;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.command_test.binding.PlotSquaredBindings;
import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.BiomeCommands;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.internal.command.ActorAuthorizer;
import com.sk89q.worldedit.internal.command.UserCommandCompleter;
import com.sk89q.worldedit.internal.command.WorldEditBinding;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.fluent.CommandGraph;
import com.sk89q.worldedit.util.command.parametric.ParametricBuilder;
import com.sk89q.worldedit.util.eventbus.Subscribe;

public class MainCommand {
    private static MainCommand instance;

    private final WorldEdit we;
    private final PlotSquared ps;

    private final ParametricBuilder builder;
    private final Dispatcher dispatcher;

    public MainCommand(PlotSquared ps, WorldEdit worldEdit, PlatformManager platformManager) {
        this.we = worldEdit;
        this.ps = ps;

        worldEdit.getEventBus().register(this);

        builder = new ParametricBuilder();
        builder.setAuthorizer(new ActorAuthorizer());
        builder.setDefaultCompleter(new UserCommandCompleter(platformManager));
        builder.addBinding(new WorldEditBinding(worldEdit));
        builder.addBinding(new PlotSquaredBindings());

        dispatcher = new CommandGraph()
                .builder(builder)
                .commands().

                        registerMethods(new BiomeCommands(worldEdit))

                .graph()
                .getDispatcher();
    }

    public void register() {
        Platform platform = we.getPlatformManager().queryCapability(Capability.USER_COMMANDS);
        platform.registerCommands(dispatcher);
    }

    public static MainCommand getInstance() {
        if (instance == null) {
            WorldEdit we = WorldEdit.getInstance();
            instance = new MainCommand(PlotSquared.get(), we, we.getPlatformManager());
            instance.register();
        }
        return instance;
    }

    public String[] commandDetection(String[] split) {
        return split;
    }

    @Subscribe
    public void handleCommand(CommandEvent event) {
        Actor actor = event.getActor();

        CommandLocals locals = new CommandLocals();
        locals.put(Actor.class, actor);
        locals.put("arguments", event.getArguments());

        String[] split = commandDetection(event.getArguments().split(" "));

        // No command found!
        if (!dispatcher.contains(split[0])) {
            return;
        }


        try {
            // This is a bit of a hack, since the call method can only throw CommandExceptions
            // everything needs to be wrapped at least once. Which means to handle all WorldEdit
            // exceptions without writing a hook into every dispatcher, we need to unwrap these
            // exceptions and rethrow their converted form, if their is one.
            try {
                Object result = dispatcher.call(Joiner.on(" ").join(split), locals, new String[0]);
                System.out.println("Result " + result);
            } catch (CommandException e) {
                actor.print(e.getMessage());
            } catch (Throwable t) {
                // TODO p2 error handling
            }
        } finally {
            // cleanup
            event.setCancelled(true);
        }
    }

    @Subscribe
    public void handleCommandSuggestion(CommandSuggestionEvent event) {
        try {
            CommandLocals locals = new CommandLocals();
            locals.put(Actor.class, event.getActor());
            locals.put("arguments", event.getArguments());
            event.setSuggestions(dispatcher.getSuggestions(event.getArguments(), locals));
        } catch (CommandException e) {
            event.getActor().printError(e.getMessage());
        }
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}
