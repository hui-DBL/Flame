package io;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 文件输入流
 * 功能：从文件系统以字节流的方式读取文件
 * *1.从文件中读取数据
 *
 * @author hui.zhong
 * @date 2020-04-09
 */
public class FileInputStream extends InputStream {

//	/**
//	 * 引入文件(这种方式引入文件的话，每次用文件都需要执行一遍打开的代码，就很烦，不如构造的时候直接打开)
//	 */
//	private File file;

	private final FileDescriptor fd;

	private final String path;

	public FileInputStream(File file) throws FileNotFoundException {
//		this.file = file;
		String name = (file != null ? file.getPath() : null);
		if (name == null) {
			throw new NullPointerException();
		}
		// 包访问权限
//		if (file.isInvalid()) {
//			throw new FileNotFoundException("Invalid file path");
//		}
		fd = new FileDescriptor();
		// 激活Closeable
//		fd.attach(this);
		path = name;
		// 打开文件并放到fd内
		open(name);
	}

	private void open(String name) throws FileNotFoundException {
		open0(name);
	}

	private native void open0(String name) throws FileNotFoundException;

	@Override
	public int read() throws IOException {
		// 读取文件
		return read0();
	}

	private native int read0() throws IOException;

	@Override
	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		return readBytes(b, off, len);
	}

	/**
	 * 字节的方式读取文件的数据
	 *
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	private native int readBytes(byte b[], int off, int len) throws IOException;
}
