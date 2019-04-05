package com.github.intellectualsites.plotsquared.plot.command_test.subcommand;

import com.github.intellectualsites.plotsquared.plot.command_test.binding.Consume;
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

public class AddCommand {

    @Command(aliases = "add", desc = "Allow a/some user(s) to build in the plot while you are online",
        usage = "<players>")
    @CommandPermissions("plots.add")
    public boolean add(PlotPlayer player, @Owner Plot plot, @Consume UUIDSet uuids) {
        int size = plot.getTrusted().size() + uuids.size();

        final Set<UUID> uuidSet = uuids.getView();
        final Iterator<UUID> iterator = uuidSet.iterator();
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
            if (plot.getMembers().contains(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            size += plot.getTrusted().contains(uuid) ? 0 : 1;
        }

        if (uuids.isEmpty()) {
            return false; // No need to throw an exception here, the messages above should
                          // cover that imo
        }

        if (size > plot.getArea().MAX_PLOT_MEMBERS && !Permissions.hasPermission(player,
            Captions.PERMISSION_ADMIN_COMMAND_TRUST)) {
            Captions.PLOT_MAX_MEMBERS.send(player);
            return false;
        }

        for (final UUID uuid : uuidSet) {
            if (uuid != DBFunc.EVERYONE) {
                if (!plot.removeTrusted(uuid) && plot.getDenied().contains(uuid)) {
                    plot.removeDenied(uuid);
                }
                plot.addMember(uuid);
                EventUtil.manager.callMember(player, plot, uuid, true);
                MainUtil.sendMessage(player, Captions.MEMBER_ADDED);
            }
        }

        return true;
    }

}
