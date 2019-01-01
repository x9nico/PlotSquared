package com.github.intellectualsites.plotsquared.plot.object;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode(of = {"plotPlayer", "location"})
@RequiredArgsConstructor
public final class PlayerBuildPermissionResult {

    @NonNull private final PlotPlayer plotPlayer;
    @NonNull private final Location location;
    @NonNull private final PlotBlock block;
    private final Plot plot;
    private final String errorMessage;

    public boolean isAllowed() {
        return this.errorMessage == null;
    }

}
