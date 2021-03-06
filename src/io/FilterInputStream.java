package io;

import java.io.IOException;

/**
 * 基类-装饰器
 * 功能：对jdk提供的基本输入流提供转换或其他功能
 * *真正读数据的还是jdk定义的这些类，只不过包了一层
 *
 * @author hui.zhong
 * @date 2020-04-09
 */
public class FilterInputStream extends InputStream {

	/**
	 * in存在多线程竞争的可能，所以需要用volatile修饰来保证可见性和顺序性，以及在使用的入口用synchronized修饰保证原子性(线程安全)
	 */
	protected volatile InputStream in;

	/**
	 * 包和子类可见
	 *
	 * @param in
	 */
	protected FilterInputStream(InputStream in) {
		this.in = in;
	}

	/**
	 * 基类：直接调用in的read方法
	 * 额外的功能由子类提供
	 *
	 * @return
	 * @throws IOException
	 */
	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}
}
