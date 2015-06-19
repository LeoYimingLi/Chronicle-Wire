/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.openhft.chronicle.wire;

import net.openhft.chronicle.bytes.Bytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The defines the stand interface for writing and reading sequentially to/from a Bytes stream. <p> Created by peter.lawrey on
 * 12/01/15.
 */
public interface WireIn {
    boolean isReady();
    
    void copyTo(@NotNull WireOut wire);

    /**
     * Read the field if present, or empty string if not present.
     */
    @NotNull
    ValueIn read();

    /**
     * Read the field if present which must match the WireKey.
     */
    @NotNull
    ValueIn read(@NotNull WireKey key);

    /**
     * Read a field, or string which is always written, even for formats which might drop the field such as RAW.
     */
    @NotNull
    default ValueIn readEventName(@NotNull StringBuilder name) {
        return read(name);
    }

    /**
     * Read the field if present, or empty string if not present.
     */
    @NotNull
    ValueIn read(@NotNull StringBuilder name);

    /**
     * Obtain the value in (for internal use)
     */
    @NotNull
    ValueIn getValueIn();

    /*
     * read and write comments.
     */
    @NotNull
    Wire readComment(@NotNull StringBuilder sb);

    void flip();

    void clear();

    Bytes<?> bytes();

    /**
     * @return if there is more data to be read in this document.
     */
    boolean hasMore();

    default boolean readDocument(@Nullable Consumer<WireIn> metaDataConsumer,
                                 @Nullable Consumer<WireIn> dataConsumer) {
        return Wires.readData(this, metaDataConsumer, dataConsumer);
    }

    default boolean readDocument(long position,
                                 @Nullable Consumer<WireIn> metaDataConsumer,
                                 @Nullable Consumer<WireIn> dataConsumer) {
        return Wires.readData(position, this, metaDataConsumer, dataConsumer);
    }
}
