package io;

import java.io.IOException;

/**
 * 基类-输入
 * 功能: 读取数据
 * * 1.从哪读数据?
 * * 2.读取到的数据放哪--放到内存中，由调用方处理
 * InputStream关心的是怎么读，而以上两个问题需要基于磁盘或者网络的工作机制来进一步讨论
 *
 * @author hui.zhong
 * @date 2020-04-08
 */
public abstract class InputStream {

	private static final int MAX_SKIP_BUFFER_SIZE = 2048;

	/**
	 * 抽象方法，自行实现
	 *
	 * @return 返回所读取数据的下一个字节，如果到末尾了，则返回-1。这个方法会阻塞直到有输入的数据、已经到流的末尾或者有异常
	 * @throws IOException 该异常由调用方处理
	 *                     1. 怎么判定为末尾? native方法
	 *                     2. 如何阻塞?
	 */
	public abstract int read() throws IOException;

	/**
	 * 从输入字节流读取数组长度的数据到byte数组中
	 *
	 * @param b
	 * @return 实际读取到的字节数
	 */
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * 从输入字节流读取指定长度的数据到byte数组中
	 *
	 * @param b   存储读取的字节
	 * @param off 从b数组的off下标处开始存
	 * @param len 指定所读取的数据的最大长度
	 * @return 实际读取到的字节数
	 */
	public int read(byte b[], int off, int len) throws IOException {
		// 校验读取的参数
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		// 读取第一个字节
		int c = read();
		// 是否到末端了
		if (c == -1) {
			return -1;
		}
		// 否则开始赋值
		b[off] = (byte) c;

		//前面已经读了一个字节了，所以从1开始
		int i = 1;
		for (; i < len; i++) {
			// 继续读下一个字节
			c = read();
			if (c == -1) {
				// 是否到末端了
				break;
			}
			// 否则继续赋值
			b[off + 1] = (byte) c;
		}
		// 此时的i应该是实际读取到数据的长度
		return i;
	}

	/**
	 * 跳过并丢弃输入流中的n个字节的数据
	 *
	 * @param n
	 * @return 实际实际跳过的长度
	 * @throws IOException
	 */
	public long skip(long n) throws IOException {
		// 校验参数
		if (n <= 0) {
			return 0;
		}
		// 限制可跳过的的长度
		int size = (int) Math.min(MAX_SKIP_BUFFER_SIZE, n);
		// 垃圾桶
		byte[] skipBuffer = new byte[size];
		// 未跳过的长度
		long remaining = n;
		// 读取的长度（辅助用）
		int nr;
		while (remaining > 0) {
			nr = read(skipBuffer, 0, (int) Math.min(size, remaining));
			// 没有读到
			if (nr < 0) {
				break;
			}
			remaining -= nr;
		}
		// 应该跳过的长度-未跳过的长度=实际跳过的长度
		return n - remaining;
	}

	/**
	 * 这个方法应该被子类覆盖
	 *
	 * @return 在不阻塞的情况下可以从此输入流读取（或跳过）的字节数的估计
	 * @throws IOException
	 */
	public int available() throws IOException {
		return 0;
	}

	/**
	 * 关闭此输入流并释放与该流关联的所有系统资源
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
	}

	/**
	 * 标记此输入流中的当前位置，随后对reset的调用可以将此流重新定位在最后标记的位置
	 *
	 * @param readlimit 在reset调用前读取了超过readlimit个字节的数据，则mark失效
	 */
	public synchronized void mark(int readlimit) {
	}

	/**
	 * 将此流重新定位到在此流上最后调用mark方法时的位置
	 *
	 * @throws IOException
	 */
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	/**
	 * 测试该输入流是否支持mark和reset方法
	 *
	 * @return
	 */
	public boolean markSupported() {
		return false;
	}
}
