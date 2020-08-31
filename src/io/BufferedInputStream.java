package io;

import java.io.IOException;

/**
 * 功能：缓冲输入流并支持mark和reset方法的功能
 */
public class BufferedInputStream extends FilterInputStream {

	/**
	 * 8kb
	 */
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * buf存在多线程竞争的可能，所以需要用volatile修饰来保证可见性和顺序性，以及在使用的入口用synchronized修饰保证原子性(线程安全)
	 */
	private volatile byte[] buf;

	/**
	 * buffer的当前位置
	 */
	protected int pos;

	/**
	 * 有效长度
	 */
	protected int count;

	/**
	 * 最后一次调用mark()方法得到的值，标记值
	 */
	protected int markpos = -1;

	/**
	 * 标记值到当前位置的最大限制(决定是否需要扩容)
	 */
	protected int marklimit;

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
	public synchronized int read() throws IOException {
		// 缓存是否读完
		if (pos >= count) {
			// 填充缓存
			fill();
			// 填充之后还是没有数据，则判断为末尾
			if (pos >= count) {
				return -1;
			}
		}
		// 正常来说，直接返回缓存数据
		return getBufIfOpen()[pos++] & 0xff;
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		// 校验buffer是否正常
		getBufIfOpen();
		if (len == 0) {
			return 0;
		}
		// 判断是否有负数
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throw new IndexOutOfBoundsException();
		}


	}

	/**
	 * 需要对buf进行修改，该方法需要同步方法调用来保证线程安全
	 */
	private void fill() throws IOException {
		byte[] buffer = getBufIfOpen();
		if ((markpos < 0 && pos >= buffer.length) || (markpos < 0 && pos < buffer.length)) {
			// 没有标记值，清空buffer，重新填充，从头开始读(pos),与pos大小无关
			pos = 0;
		} else if (markpos >= 0 && pos >= buffer.length) {
			// 有标记值，但buffer没有足够空间，需要扩容，制定扩容策略
			if ((markpos > 0 && buffer.length >= marklimit) || (markpos > 0 && buffer.length < marklimit)) {
				// 标记之前的扔掉，再将标记值转移至起始位置
				int sz = pos - markpos;
				System.arraycopy(buffer, markpos, buffer, 0, sz);
				pos = sz;
				markpos = 0;
			} else if (markpos == 0 && buffer.length >= marklimit) {
				// 读取的内容超过了marklimit，则标记失效
				markpos = -1;
				pos = 0;
			} else if (markpos == 0 && buffer.length >= MAX_BUFFER_SIZE) {
				throw new OutOfMemoryError("Required array size too large");
			} else {
				// 读取的内容没有超过了marklimit，则扩容，扩容大小为初始容量2倍但必须小于MAX_BUFFER_SIZE
				// 调整扩容大小为marklimit
				int nsz = (pos <= MAX_BUFFER_SIZE - pos) ? pos * 2 : MAX_BUFFER_SIZE;
				if (nsz > marklimit) {
					nsz = marklimit;
				}
				byte nbuf[] = new byte[nsz];
				System.arraycopy(buffer, 0, nbuf, 0, pos);
				// todo cas更新
				// 这里可能会有并发问题
				buffer = nbuf;
			}
		} else if (markpos >= 0 && pos < buffer.length) {
			// 有标记，但buffer还有足够的空间，无需扩容
		}
		count = pos;
		// 重新填充
		int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
		if (n > 0) {
			count = n + pos;
		}
	}

	private InputStream getInIfOpen() throws IOException {
		InputStream input = in;
		if (input == null) {
			throw new IOException("Stream closed");
		}
		return input;
	}

	private byte[] getBufIfOpen() throws IOException {
		byte[] buffer = buf;
		if (buffer == null) {
			throw new IOException("Stream closed");
		}
		return buffer;
	}

	@Override
	public void mark(int readlimit) {
		super.mark(readlimit);
	}


}
