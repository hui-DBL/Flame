package hello;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author hui.zhong
 * @date 2020-04-09
 */
public class FileTest {

	public static void main(String[] args) {
		try {
			FileInputStream fileInputStream = new FileInputStream("/Users/xmly/Downloads/result.csv");
			System.out.println(1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
