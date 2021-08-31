package com.yocn.libnative;

import java.nio.ByteBuffer;

public class ReadByteBuffer {
    public native long init(String path);

    public native void read(ByteBuffer byteBuffer, long address);

    public native void flush(long address);
}
