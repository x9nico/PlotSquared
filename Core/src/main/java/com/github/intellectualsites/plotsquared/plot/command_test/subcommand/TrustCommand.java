package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.command_test.binding.Owner;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.UUIDSet;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class TrustCommand {

    @Command(aliases = {"trust", "t"}, usage = "<player>", desc = "Allow a/some user(s) to build"
        + " in a plot while you are offline")
    @CommandPermissions("plots.trust")
    public boolean trust(PlotPlayer player, @Owner Plot plot, UUIDSet uuids) {
        final Set<UUID> uuidSet = uuids.getView();
        final Iterator<UUID> iterator = uuidSet.iterator();
        int size = plot.getTrusted().size() + plot.getMembers().size();
        while (iterator.hasNext()) {
            final UUID uuid = iterator.next();
            if (uuid == DBFunc.EVERYONE && !(
                Permissions.hasPermission(player, Captions.PERMISSION_TRUST_EVERYONE) || Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST))) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_OWNER, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            if (plot.getTrusted().contains(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            size += plot.getMembers().contains(uuid) ? 0 : 1;
        }

        if (uuidSet.isEmpty()) {
            return false;
        }

        if (size > plot.getArea().MAX_PLOT_MEMBERS && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST)) {
            Captions.PLOT_MAX_MEMBERS.send(player);
            return false;
        }

        for (final UUID uuid : uuidSet) {
            if (uuid != DBFunc.EVERYONE) {
                if (!plot.removeMember(uuid)) {
                    if (plot.getDenied().contains(uuid)) {
                        plot.removeDenied(uuid);
                    }
                }
            }
            plot.addTrusted(uuid);
            EventUtil.manager.callTrusted(player, plot, uuid, true);
            MainUtil.sendMessage(player, Captions.TRUSTED_ADDED);
        }

        return true;
    }

}
