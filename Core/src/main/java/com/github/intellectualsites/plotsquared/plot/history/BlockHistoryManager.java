package com.github.intellectualsites.plotsquared.plot.history;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import lombok.NonNull;

import java.util.Collection;
import java.util.function.Predicate;

public interface BlockHistoryManager {

    default PlotSnapshot getSnapshot(@NonNull final Plot plot, final long earliest) {
        return getSnapshot(plot, earliest, event -> true);
    }

    PlotSnapshot getSnapshot(@NonNull final Plot plot, final long earliest, @NonNull final Predicate<BlockEvent> filter);

    Collection<BlockEvent> getEvents(@NonNull final Plot plot, final Predicate<BlockEvent> filter);

}
