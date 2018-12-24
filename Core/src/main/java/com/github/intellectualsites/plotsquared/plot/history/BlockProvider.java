package com.github.intellectualsites.plotsquared.plot.history;

import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;

@FunctionalInterface
public interface BlockProvider {

    /**
     * Get the block type at the specified location, as stored in this snapshot
     *
     * @param x X block location relative to lower corner
     * @param y Y block location relative to lower corner
     * @param z Z block location relative to lower corner
     * @return block type at the specified location
     */
    PlotBlock getBlock(int x, int y, int z);

}
