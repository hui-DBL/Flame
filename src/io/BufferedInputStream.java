package io;

import java.io.IOException;

/**
 * 功能：缓冲输入流并支持mark和reset方法的功能
 */
public class BufferedInputStream extends FilterInputStream {

	/**
	 * 8kb
	 */
	public static final int DEFAULT_BUFFER_SIZE = 8192;

	private byte[] buf;

	/**
	 * 包和子类可见
	 *
	 * @param in
	 */
	protected BufferedInputStream(InputStream in) {
		// init
		this(in, DEFAULT_BUFFER_SIZE);
	}

	public BufferedInputStream(InputStream in, int size) {
		super(in);
		if (size <= 0) {
			throw new IllegalArgumentException("Buffer size<= 0");
		}
		buf = new byte[size];
	}

	/**
	 * & 0xff 保持补码的一致性
	 *
	 * @return
	 * @throws IOException
	 */
	@Override
	public int read() throws IOException {

	}


}
