package com.github.intellectualsites.plotsquared.plot.history;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import lombok.NonNull;

/**
 * A plot snapshot stored entirely in memory
 */
public class MemoryPlotSnapshot implements PlotSnapshot {

    private final Plot plot;
    private final PlotBlock[][] blocks;
    private final long timestamp;

    private final int minHeight;
    private final int maxHeight;
    private final int height;
    private final int width;

    public MemoryPlotSnapshot(@NonNull final Plot plot, @NonNull final long timestamp,
        @NonNull final BlockProvider provider) {
        this.plot = plot;
        this.timestamp = timestamp;

        final Location[] corners = MainUtil.getCorners(plot.getWorldName(), plot.getRegions());
        final Location bot = corners[0];
        final Location top = corners[1];

        this.width = top.getX() - bot.getX() + 1;
        this.minHeight = plot.getArea().MIN_BUILD_HEIGHT;
        this.maxHeight = plot.getArea().MAX_BUILD_HEIGHT;
        this.height = this.maxHeight - this.minHeight;

        this.blocks = new PlotBlock[this.height][this.width * this.width];

        // Initialize the entire array with air
        for (int y = 0; y < this.height; y++) {
            final PlotBlock[] level = this.blocks[y];
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    level[getIndex(x, z)] = provider.getBlock(x, y, z);
                }
            }
        }
    }

    private int getIndex(int x, int z) {
        return x + (z * width);
    }

    @Override public Plot getPlot() {
        return this.plot;
    }

    @Override public long getTimestamp() {
        return this.timestamp;
    }

    @Override public PlotBlock getBlock(int x, int y, int z) {
        final int index = getIndex(x, z);
        final PlotBlock block = this.blocks[y][index];
        if (block == null) {
            final PlotBlock plotBlock = this.plot.getBlock(x, y, z);
            this.blocks[y][index] = plotBlock;
            return plotBlock;
        } else {
            return block;
        }
    }

    @Override public LocalBlockQueue getQueue() {
        final LocalBlockQueue queue = this.plot.getArea().getQueue(false);
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    queue.setBlock(x, y, z, getBlock(x, y, z));
                }
            }
        }
        return queue;
    }
}
