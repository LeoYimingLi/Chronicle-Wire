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
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.IORuntimeException;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.core.util.StringUtils;
import net.openhft.chronicle.core.values.IntValue;
import net.openhft.chronicle.core.values.LongArrayValues;
import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.wire.util.BooleanConsumer;
import net.openhft.chronicle.wire.util.ByteConsumer;
import net.openhft.chronicle.wire.util.FloatConsumer;
import net.openhft.chronicle.wire.util.ShortConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.*;

/**
 * Created by peter.lawrey on 14/01/15.
 */
public interface ValueIn {

    /*
     * Text / Strings.
     */
    @NotNull
    WireIn bool(@NotNull BooleanConsumer flag);

    @NotNull
    WireIn text(@NotNull Consumer<String> s);

    @Nullable
    default String text() {
        StringBuilder sb = Wires.acquireStringBuilder();
        return textTo(sb) == null ? null : sb.toString();
    }

    @Nullable
    <ACS extends Appendable & CharSequence> ACS textTo(@NotNull ACS s);

    @NotNull
    WireIn int8(@NotNull ByteConsumer i);

    @NotNull
    WireIn bytes(@NotNull Bytes<?> toBytes);

    @NotNull
    WireIn bytes(@NotNull Consumer<WireIn> wireInConsumer);

    byte[] bytes();

    default BytesStore bytesStore() {
        return BytesStore.wrap(bytes());
    }

    @NotNull
    WireIn wireIn();

    /**
     * the length of the field as bytes including any encoding and header character
     */
    long readLength();

    @NotNull
    WireIn uint8(@NotNull ShortConsumer i);

    @NotNull
    WireIn int16(@NotNull ShortConsumer i);

    @NotNull
    WireIn uint16(@NotNull IntConsumer i);

    @NotNull
    WireIn int32(@NotNull IntConsumer i);

    @NotNull
    WireIn uint32(@NotNull LongConsumer i);

    @NotNull
    WireIn int64(@NotNull LongConsumer i);

    @NotNull
    WireIn float32(@NotNull FloatConsumer v);

    @NotNull
    WireIn float64(@NotNull DoubleConsumer v);

    @NotNull
    WireIn time(@NotNull Consumer<LocalTime> localTime);

    @NotNull
    WireIn zonedDateTime(@NotNull Consumer<ZonedDateTime> zonedDateTime);

    @NotNull
    WireIn date(@NotNull Consumer<LocalDate> localDate);

    boolean hasNext();

    boolean hasNextSequenceItem();

    @NotNull
    WireIn uuid(@NotNull Consumer<UUID> uuid);

    @NotNull
    WireIn int64array(@Nullable LongArrayValues values, @NotNull Consumer<LongArrayValues> setter);

    @NotNull
    WireIn int64(@Nullable LongValue value, @NotNull Consumer<LongValue> setter);

    @NotNull
    WireIn int32(@Nullable IntValue value, @NotNull Consumer<IntValue> setter);

    @NotNull
    WireIn sequence(@NotNull Consumer<ValueIn> reader);

    <T> T applyToMarshallable(Function<WireIn, T> marshallableReader);

    @Nullable
    default <T extends ReadMarshallable> T typedMarshallable() {
        try {
            StringBuilder sb = Wires.acquireStringBuilder();
            type(sb);
            if (StringUtils.isEqual(sb, "!null")) {
                text();
                return null;
            }

            if (StringUtils.isEqual(sb, "!binary")) {
                bytesStore();
                return null;
            }

            // its possible that the object that you are allocating may not have a
            // default constructor
            final Class clazz = ClassAliasPool.CLASS_ALIASES.forName(sb);

            if (!Marshallable.class.isAssignableFrom(clazz))
                throw new IllegalStateException("its not possible to Marshallable and object that" +
                        " is not of type Marshallable, type=" + sb);

            final ReadMarshallable m = OS.memory().allocateInstance((Class<ReadMarshallable>) clazz);

            marshallable(m);
            return (T) m;
        } catch (Exception e) {
            throw new IORuntimeException(e);
        }
    }

    @NotNull
    WireIn type(@NotNull StringBuilder s);

    @NotNull
    WireIn typeLiteralAsText(@NotNull Consumer<CharSequence> classNameConsumer);

    @NotNull
    default WireIn typeLiteral(@NotNull Function<CharSequence, Class> typeLookup, @NotNull Consumer<Class> classConsumer) {
        return typeLiteralAsText(sb -> classConsumer.accept(typeLookup.apply(sb)));
    }

    @NotNull
    default WireIn typeLiteral(@NotNull Consumer<Class> classConsumer) {
        return typeLiteral(ClassAliasPool.CLASS_ALIASES::forName, classConsumer);
    }

    @NotNull
    WireIn marshallable(@NotNull ReadMarshallable object);

    /**
     * reads the map from the wire
     */
    default void map(@NotNull Map<String, String> usingMap) {
        map(String.class, String.class, usingMap);
    }

    <K extends ReadMarshallable, V extends ReadMarshallable>
    void typedMap(@NotNull final Map<K, V> usingMap);

    /**
     * reads the map from the wire
     */
    @Nullable
    <K, V> Map<K, V> map(@NotNull Class<K> kClazz,
                         @NotNull Class<V> vClass,
                         @NotNull Map<K, V> usingMap);

    boolean bool();

    byte int8();

    short int16();

    int uint16();

    int int32();

    long int64();

    double float64();

    float float32();

    default Throwable throwable(boolean appendCurrentStack) {
        return Wires.throwable(this, appendCurrentStack);
    }

    @Nullable
    default <E> E object(@NotNull Class<E> clazz) {
        return object(null, clazz);
    }

    @Nullable
    <E> E object(@Nullable E using, @NotNull Class<E> clazz);

    Consumer<ValueIn> DISCARD = v -> {
    };

    default Class typeLiteral(){
        Class[] clazz = {null};
        typeLiteral(
        ClassAliasPool.CLASS_ALIASES::forName,c->clazz[0]=c);
        return clazz[0];
    }
}
