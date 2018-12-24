package com.github.intellectualsites.plotsquared.plot.history;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;

/**
 * A plot snapshot is the entire state of a plot at a given
 * point in time
 */
public interface PlotSnapshot extends BlockProvider {

    /**
     * Get the plot represented by this snapshot
     *
     * @return plot represented by the snapshot
     */
    Plot getPlot();

    /**
     * Get the time that this snapshot represents
     *
     * @return time in milliseconds
     */
    long getTimestamp();

    /**
     * Get the block type at the specified location, as stored in this snapshot
     *
     * @param x X block location relative to lower corner
     * @param y Y block location relative to lower corner
     * @param z Z block location relative to lower corner
     * @return block type at the specified location
     */
    PlotBlock getBlock(final int x, final int y, final int z);

    /**
     * Get a local block queue with the entire snapshot loaded into it
     * Use {@link LocalBlockQueue#enqueue()} to paste the snapshot into the plot
     *
     * @return queue with blocks loaded
     */
    LocalBlockQueue getQueue();

}
