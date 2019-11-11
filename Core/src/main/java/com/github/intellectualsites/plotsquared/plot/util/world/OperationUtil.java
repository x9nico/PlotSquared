package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OperationUtil {
    private static final boolean ASYNC;
    private static final ThreadLocal<EditSession> CACHE = new ThreadLocal<>();

    static {
        boolean hasFawe = true;
        try {
            Class.forName("com.boydti.fawe.Fawe");
        } catch (ClassNotFoundException ignore) {
            hasFawe = false;
        }
        ASYNC = hasFawe;
    }

    private class PlotOperation implements Closeable {
        public Actor actor;
        public LocalSession session;
        public EditSession editSession;
        public int depth;

        @Override public void close() throws IOException {
            if (session != null) {

            }
        }
    }

    public static void createOperation(PlotPlayer plotPlayer, Runnable task) {

    }

    public static EditSession withExtent(Consumer<EditSession> task) {
        return null;
    }

    private static ListenableFuture<Boolean> withEditSession(@Nullable PlotPlayer plotPlayer, @NotNull Consumer<EditSession> consumer) {
        return withEditSession(plotPlayer, consumer, null);
    }

    private static ListenableFuture<Boolean> withEditSession(@Nullable PlotPlayer plotPlayer, @NotNull Consumer<EditSession> consumer, @Nullable Consumer<Throwable> exceptionHandler) {
        EditSession es = CACHE.get();
        if (es != null) {
            consumer.accept(es);
        }
        if (ASYNC && PlotSquared.get().isMainThread(Thread.currentThread())) {
            ListeningExecutorService exec = WorldEdit.getInstance().getExecutorService();
            return exec.submit(
                () -> {
                    withEditSessionOnThread(plotPlayer, consumer, exceptionHandler);
                    return true;
                });
        } else {
            withEditSessionOnThread(plotPlayer, consumer, exceptionHandler);
        }
        return Futures.immediateFuture(true);
    }

    private static ListenableFuture<Boolean> withEditSession(Callable<Boolean> task) {
        if (ASYNC && PlotSquared.get().isMainThread(Thread.currentThread())) {
            ListeningExecutorService exec = WorldEdit.getInstance().getExecutorService();
            return exec.submit(task);
        } else {
            Boolean result;
            try {
                result = task.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    e.printStackTrace();
                }
                result = false;
            }
            return Futures.immediateFuture(result);
        }
    }

    private static void withEditSessionOnThread(PlotPlayer plotPlayer, Consumer<EditSession> consumer, Consumer<Throwable> exceptionHandler) {
        Actor actor = plotPlayer.toActor();
        World weWorld = getWorld(plotPlayer, actor);
        withEditSessionOnThread(weWorld, actor, consumer, exceptionHandler);
    }

    private static void withEditSessionOnThread(World weWorld, Actor actor, Consumer<EditSession> consumer, Consumer<Throwable> exceptionHandler) {
        LocalSession session = getSession(actor);
        try (EditSession es = createEditSession(weWorld, actor, session)) {
            try {
                CACHE.set(es);
                consumer.accept(es);
            } finally {
                CACHE.set(null);
                es.close();
                session.remember(es);
            }
        } catch (Throwable e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    private static World getWorld(String worldName) {
        Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        List<? extends World> worlds = platform.getWorlds();
        for (World current : worlds) {
            if (current.getName().equals(worldName)) {
                return current;
            }
        }
        return null;
    }

    private static World getWorld(PlotPlayer plotPlayer, Actor actor) {
        World weWorld;
        if (actor instanceof Player) {
            weWorld = ((Player) actor).getWorld();
        } else {
            @NotNull Location loc = plotPlayer.getLocation();
            String world = loc.getWorld();
            weWorld = getWorld(world);
        }
        return weWorld;
    }

    private static  EditSession createEditSession(PlotPlayer plotPlayer) {
        Actor actor = plotPlayer.toActor();
        World weWorld = getWorld(plotPlayer, actor);
        return createEditSession(weWorld, actor);
    }

    private static LocalSession getSession(Actor actor) {
        return WorldEdit.getInstance().getSessionManager().get(actor);
    }

    private static  EditSession createEditSession(World world, Actor actor) {
        return createEditSession(world, actor, getSession(actor));
    }

    private static  EditSession createEditSession(World world, Actor actor, LocalSession session) {
        EditSession editSession;
        Player player = actor.isPlayer() ? (Player) actor : null;
        editSession = WorldEdit.getInstance().getEditSessionFactory()
            .getEditSession(world, -1, null, player);

        editSession.setFastMode(!actor.isPlayer());
        editSession.setReorderMode(EditSession.ReorderMode.FAST);
        return editSession;
    }
}
