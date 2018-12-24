package com.github.intellectualsites.plotsquared.plot.history;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BlockEvent {

    private final PlotId plotId;
    private final PlotBlock block;
    private final Location location;
    private final EventType type;
    private final EventOwner owner;

    public enum EventType {
        PLACE, REMOVE
    }

    public enum EventOwnerType {
        PLAYER, COMMAND
    }

    @Getter
    @RequiredArgsConstructor
    public static class EventOwner {

        private final String specifier;
        private final EventOwnerType type;

    }

}
