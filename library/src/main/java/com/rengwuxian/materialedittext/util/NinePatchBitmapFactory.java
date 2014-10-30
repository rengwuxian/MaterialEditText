package com.rengwuxian.materialedittext.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * thanks to briangriffey https://gist.github.com/briangriffey/4391807
 */
public class NinePatchBitmapFactory {

	// The 9 patch segment is not a solid color.
	private static final int NO_COLOR = 0x00000001;

	public static ByteBuffer getByteBuffer(int left, int top, int right, int bottom) {
		//Docs check the NinePatchChunkFile
		ByteBuffer buffer = ByteBuffer.allocate(56).order(ByteOrder.nativeOrder());
		//was translated
		buffer.put((byte) 0x01);
		//divx size
		buffer.put((byte) 0x02);
		//divy size
		buffer.put((byte) 0x02);
		//color size
		buffer.put((byte) 0x02);

		//skip
		buffer.putInt(0);
		buffer.putInt(0);

		//padding
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putInt(0);

		//skip 4 bytes
		buffer.putInt(0);

		buffer.putInt(left);
		buffer.putInt(right);
		buffer.putInt(top);
		buffer.putInt(bottom);
		buffer.putInt(NO_COLOR);
		buffer.putInt(NO_COLOR);

		return buffer;
	}

}