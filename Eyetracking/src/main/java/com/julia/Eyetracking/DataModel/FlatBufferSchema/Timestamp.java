// automatically generated by the FlatBuffers compiler, do not modify

package com.julia.Eyetracking.DataModel.FlatBufferSchema;

import java.nio.*;
import java.lang.*;

import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Timestamp extends Struct {
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public Timestamp __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long seconds() { return bb.getLong(bb_pos + 0); }
  public int nanoseconds() { return bb.getInt(bb_pos + 8); }

  public static int createTimestamp(FlatBufferBuilder builder, long seconds, int nanoseconds) {
    builder.prep(8, 16);
    builder.pad(4);
    builder.putInt(nanoseconds);
    builder.putLong(seconds);
    return builder.offset();
  }
}

