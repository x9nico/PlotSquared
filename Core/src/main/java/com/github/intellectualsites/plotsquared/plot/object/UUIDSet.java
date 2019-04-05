package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"}) public final class UUIDSet
    implements Iterable<UUID> {

    private static final UUIDSet EMPTY_SET = new UUIDSet(Collections.emptySet());
    private static final String EVERYONE = "*";

    private final Set<UUID> internalSet;

    public UUIDSet(final Collection<UUID> collection) {
        this.internalSet = Collections.unmodifiableSet(new HashSet<>(collection));
    }

    public UUIDSet(final String string) {
        final String[] split = string.split("[,;]");
        if (split.length == 0) {
            this.internalSet = Collections.emptySet();
        } else {
            final Set<UUID> result = new HashSet<>();
            for (final String name : split) {
                if (name.isEmpty()) {
                    this.internalSet = Collections.emptySet();
                    return;
                }
                if (EVERYONE.equals(name)) {
                    result.add(DBFunc.EVERYONE);
                } else if (name.length() > 16) {
                    try {
                        result.add(UUID.fromString(name));
                    } catch (IllegalArgumentException ignored) {
                        this.internalSet = Collections.emptySet();
                        return;
                    }
                } else {
                    final UUID uuid = UUIDHandler.getUUID(name, null);
                    if (uuid == null) {
                        this.internalSet = Collections.emptySet();
                        return;
                    } else {
                        result.add(uuid);
                    }
                }
            }
            this.internalSet = Collections.unmodifiableSet(result);
        }
    }

    public static UUIDSet emptySet() {
        return EMPTY_SET;
    }

    public int size() {
        return this.internalSet.size();
    }

    public boolean containsEveryone() {
        return this.contains(DBFunc.EVERYONE);
    }

    public boolean contains(final UUID uuid) {
        return this.internalSet.contains(uuid);
    }

    public Set<UUID> getView() {
        // has to be modifiable
        return new HashSet<>(this.internalSet);
    }

    public boolean isEmpty() {
        return this.internalSet.isEmpty();
    }

    @NotNull @Override public Iterator<UUID> iterator() {
        // return a copy of the internal set
        return this.getView().iterator();
    }

}
