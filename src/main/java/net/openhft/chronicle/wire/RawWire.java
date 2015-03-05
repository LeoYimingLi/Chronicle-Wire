package net.openhft.chronicle.wire;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesUtil;
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.values.IntValue;
import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.util.BooleanConsumer;
import net.openhft.chronicle.util.ByteConsumer;
import net.openhft.chronicle.util.FloatConsumer;
import net.openhft.chronicle.util.ShortConsumer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.*;

/**
 * Created by peter.lawrey on 19/01/15.
 */
public class RawWire implements Wire {
    final Bytes bytes;

    final RawValueOut writeValue = new RawValueOut();
    final RawValueIn readValue = new RawValueIn();

    public RawWire(Bytes bytes) {
        this.bytes = bytes;
    }

    @Override
    public void copyTo(WireOut wire) {
        if (wire instanceof RawWire) {
            wire.bytes().write(bytes);
        } else {
            throw new UnsupportedOperationException("Can only copy Raw Wire format to the same format.");
        }
    }

    @Override
    public String toString() {
        return bytes.toString();
    }

    @Override
    public ValueOut write() {
        return writeValue;
    }

    @Override
    public ValueOut write(WireKey key) {
        return writeValue;
    }

    @Override
    public ValueOut writeValue() {
        return writeValue;
    }

    @Override
    public ValueIn read() {
        return readValue;
    }

    @Override
    public ValueIn read(WireKey key) {
        return readValue;
    }

    @Override
    public ValueIn read(StringBuilder name) {
        return readValue;
    }

    @Override
    public boolean hasNextSequenceItem() {
        return false;
    }

    @Override
    public Wire writeComment(CharSequence s) {
        return RawWire.this;
    }

    @Override
    public Wire readComment(StringBuilder sb) {
        return RawWire.this;
    }

    @Override
    public boolean hasMapping() {
        return false;
    }

    @Override
    public void writeDocument(Runnable writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeMetaData(Runnable writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasDocument() {
        return false;
    }

    @Override
    public <T> T readDocument(Function<WireIn, T> reader, Consumer<WireIn> metaDataReader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flip() {
        bytes.flip();
    }

    @Override
    public void clear() {
        bytes.clear();
    }

    @Override
    public Bytes bytes() {
        return bytes;
    }

    @Override
    public WireOut addPadding(int paddingToAdd) {
        throw new UnsupportedOperationException();
    }

    class RawValueOut implements ValueOut {
        @Override
        public Wire bool(Boolean flag) {
            if (flag == null)
                bytes.writeUnsignedByte(WireType.NULL.code);
            else
                bytes.writeUnsignedByte(flag ? WireType.TRUE.code : 0);
            return RawWire.this;
        }

        @Override
        public Wire text(CharSequence s) {
            bytes.writeUTFΔ(s);
            return RawWire.this;
        }

        @Override
        public Wire int8(byte i8) {
            bytes.writeByte(i8);
            return RawWire.this;
        }

        @Override
        public WireOut bytes(Bytes fromBytes) {
            writeLength(fromBytes.remaining());
            bytes.write(fromBytes);
            return RawWire.this;
        }

        @Override
        public ValueOut writeLength(long length) {
            bytes.writeStopBit(length);
            return this;
        }

        @Override
        public WireOut bytes(byte[] fromBytes) {
            writeLength(fromBytes.length);
            bytes.write(fromBytes);
            return RawWire.this;
        }

        @Override
        public Wire uint8checked(int u8) {
            bytes.writeUnsignedByte(u8);
            return RawWire.this;
        }

        @Override
        public Wire int16(short i16) {
            bytes.writeShort(i16);
            return RawWire.this;
        }

        @Override
        public Wire uint16checked(int u16) {
            bytes.writeUnsignedShort(u16);
            return RawWire.this;
        }

        @Override
        public Wire utf8(int codepoint) {
            BytesUtil.appendUTF(bytes, codepoint);
            return RawWire.this;
        }

        @Override
        public Wire int32(int i32) {
            bytes.writeInt(i32);
            return RawWire.this;
        }

        @Override
        public Wire uint32checked(long u32) {
            bytes.writeUnsignedInt(u32);
            return RawWire.this;
        }

        @Override
        public Wire float32(float f) {
            bytes.writeFloat(f);
            return RawWire.this;
        }

        @Override
        public Wire float64(double d) {
            bytes.writeDouble(d);
            return RawWire.this;
        }

        @Override
        public Wire int64(long i64) {
            bytes.writeLong(i64);
            return RawWire.this;
        }

        @Override
        public Wire time(LocalTime localTime) {
            long t = localTime.toNanoOfDay();
            bytes.writeLong(t);
            return RawWire.this;
        }

        @Override
        public Wire zonedDateTime(ZonedDateTime zonedDateTime) {
            bytes.writeUTFΔ(zonedDateTime.toString());
            return RawWire.this;
        }

        @Override
        public Wire date(LocalDate localDate) {
            bytes.writeStopBit(localDate.toEpochDay());
            return RawWire.this;
        }

        @Override
        public Wire type(CharSequence typeName) {
            bytes.writeUTFΔ(typeName);
            return RawWire.this;
        }

        @Override
        public WireOut uuid(UUID uuid) {
            bytes.writeLong(uuid.getMostSignificantBits());
            bytes.writeLong(uuid.getLeastSignificantBits());
            return RawWire.this;
        }

        @Override
        public WireOut int64(LongValue readReady) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireOut int32(IntValue value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireOut sequence(Runnable writer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireOut marshallable(Marshallable object) {
            long position = bytes.position();
            bytes.writeInt(0);
            object.writeMarshallable(RawWire.this);
            bytes.writeOrderedInt(position, Maths.toInt32(bytes.position() - position - 4, "Document length %,d out of 32-bit int range."));
            return RawWire.this;
        }
    }

    class RawValueIn implements ValueIn {

        public WireIn bytes(Bytes toBytes) {
            wireIn().bytes().withLength(readLength(), toBytes::write);
            return wireIn();
        }

        public WireIn bytes(Consumer<byte[]> bytesConsumer) {
            long length = readLength();
            byte[] byteArray = new byte[Maths.toInt32(length)];
            bytes.read(byteArray);
            bytesConsumer.accept(byteArray);
            return wireIn();
        }

        @Override
        public byte[] bytes() {
            throw new UnsupportedOperationException("todo");
        }

        @Override
        public Wire bool(BooleanConsumer flag) {
            int b = bytes.readUnsignedByte();
            if (b == WireType.NULL.code)
                flag.accept(null);
            else if (b == 0 || b == WireType.FALSE.code)
                flag.accept(false);
            else
                flag.accept(true);
            return RawWire.this;
        }

        @Override
        public Wire text(StringBuilder s) {
            bytes.readUTFΔ(s);
            return RawWire.this;
        }

        @Override
        public WireIn text(Consumer<String> s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Wire type(StringBuilder s) {
            bytes.readUTFΔ(s);
            return RawWire.this;
        }

        @Override
        public Wire int8(ByteConsumer i) {
            i.accept(bytes.readByte());
            return RawWire.this;
        }

        @Override
        public WireIn wireIn() {
            return RawWire.this;
        }

        @Override
        public long readLength() {
            return bytes.readStopBit();
        }

        @Override
        public Wire uint8(ShortConsumer i) {
            i.accept((short) bytes.readUnsignedByte());
            return RawWire.this;
        }

        @Override
        public Wire int16(ShortConsumer i) {
            i.accept(bytes.readShort());
            return RawWire.this;
        }

        @Override
        public Wire uint16(IntConsumer i) {
            i.accept(bytes.readUnsignedShort());
            return RawWire.this;
        }

        @Override
        public Wire int32(IntConsumer i) {
            i.accept(bytes.readInt());
            return RawWire.this;
        }

        @Override
        public Wire uint32(LongConsumer i) {
            i.accept(bytes.readUnsignedInt());
            return RawWire.this;
        }

        @Override
        public Wire int64(LongConsumer i) {
            i.accept(bytes.readLong());
            return RawWire.this;
        }

        @Override
        public boolean bool() {
            return bytes.readBoolean();
        }

        @Override
        public byte int8() {
            return bytes.readByte();
        }
        @Override
        public short int16() {
            return bytes.readShort();
        }

        @Override
        public int int32() {
            return bytes.readInt();
        }

        @Override
        public long int64() {
            return bytes.readLong();
        }

        @Override
        public Wire float32(FloatConsumer v) {
            v.accept(bytes.readFloat());
            return RawWire.this;
        }

        @Override
        public Wire float64(DoubleConsumer v) {
            v.accept(bytes.readDouble());
            return RawWire.this;
        }

        @Override
        public Wire time(Consumer<LocalTime> localTime) {
            localTime.accept(LocalTime.ofNanoOfDay(bytes.readLong()));
            return RawWire.this;
        }

        @Override
        public Wire zonedDateTime(Consumer<ZonedDateTime> zonedDateTime) {
            zonedDateTime.accept(ZonedDateTime.parse(bytes.readUTFΔ()));
            return RawWire.this;
        }

        @Override
        public Wire date(Consumer<LocalDate> localDate) {
            localDate.accept(LocalDate.ofEpochDay(bytes.readStopBit()));
            return RawWire.this;
        }

        @Override
        public boolean hasNext() {
            return bytes.remaining() > 0;
        }

        @Override
        public WireIn expectText(CharSequence s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireIn uuid(Consumer<UUID> uuid) {
            uuid.accept(new UUID(bytes.readLong(), bytes.readLong()));
            return RawWire.this;
        }

        @Override
        public WireIn int64(LongValue value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireIn int32(IntValue value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireIn sequence(Consumer<ValueIn> reader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WireIn marshallable(Marshallable object) {
            long length = bytes.readUnsignedInt();
            if (length >= 0) {
                long limit = bytes.readLimit();
                long limit2 = bytes.position() + length;
                bytes.limit(limit2);
                try {
                    object.readMarshallable(RawWire.this);
                } finally {
                    bytes.limit(limit);
                    bytes.position(limit2);
                }
            } else {
                object.readMarshallable(RawWire.this);
            }
            return RawWire.this;
        }


    }
}
