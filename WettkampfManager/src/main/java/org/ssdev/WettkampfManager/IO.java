/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ssdev.WettkampfManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;

public class IO {
	private static int lastProgressLength = 0;
	
	public static void writeFile(String filename, String data, String charset, boolean fatal) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			fos.write(data.getBytes(charset));
			fos.close();
		} catch (FileNotFoundException e) {
			if (fatal) {
				/* Write the data to stderr, otherwise it'll be lost */
				System.err.println(data);
				throw new RuntimeException("Fatal error writing file " + filename, e);
			}
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		} catch (IOException e) {
			if (fatal) {
				/* Write the data to stderr, otherwise it'll be lost */
				System.err.println(data);
				throw new RuntimeException("Fatal error writing file " + filename, e);
			}
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static String readFile(String filename) {
		return new String(IO.readBinaryFile(filename));
	}
	
	public static void writeObject(String filename, Object o) {
		FileOutputStream fos;
		BufferedOutputStream bos;
		ObjectOutputStream out;
		
		
		try {
			fos = new FileOutputStream(filename);
			bos = new BufferedOutputStream(fos);
			out = new ObjectOutputStream(bos);
			out.writeObject(o);
			out.close();
		} catch (FileNotFoundException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static Object readObject(String filename) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream in = null;
		
		Object o = null;
		
		try {
			fis = new FileInputStream(filename);
			bis = new BufferedInputStream(fis);
			in = new ObjectInputStream(bis);
			o = in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		} catch (ClassNotFoundException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
		
		return o;
	}
	
	public static ArrayList<String> readFileByLine (String filename) {
		ArrayList<String> result = new ArrayList<String>();
		readFileByLine(filename, 0, result);
		return result;
	}
	
	public static <T extends Collection<String>> void readFileByLine(String filename, int offset, T result) {
		int currentLine = 1;
		
		FileInputStream fos;
		try {
			fos = new FileInputStream(filename);
		    DataInputStream in = new DataInputStream(fos);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    
		    String line;
		    while ((line = br.readLine()) != null)   {
		    	if (currentLine < offset) {
		    		currentLine++;
		    	} else {
		    		result.add(line);
		    	}
		    }
			
			br.close();
		} catch (FileNotFoundException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static void writeFileUTF8(String filename, String data) {
		writeFile(filename, data, "UTF-8", false);
	}
	
	public static void writeFileUTF8(String filename, String data, boolean fatal) {
		writeFile(filename, data, "UTF-8", fatal);
	}
	
	public static void deleteFile(String filename) {
		File f = new File(filename);
		f.delete();
	}
	
	public static void deleteDirectory(String directory) {
		File f = new File(directory);
		try {
			FileUtils.deleteDirectory(f);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static void progress(String prefix, String content) {
		char[] blank = new char[lastProgressLength];
		Arrays.fill(blank, ' ');
		System.err.print(prefix + String.valueOf(blank) + "\r" + prefix + content + "\r");
		lastProgressLength = content.length();
	}
	
	public static ArrayList<String> filesByExtension(String path, ArrayList<String> extList) {
		/* For backwards compatibility, implicitly assume recursive search */
		ArrayList<String> result = new ArrayList<String>();
		for (String ext: extList) {
			result.addAll(IO.filesByExtension(path, ext, true));
		}
		return result;
	}
	
	public static ArrayList<String> filesByExtension(String path, String ext) {
		/* For backwards compatibility, implicitly assume recursive search */
		return IO.filesByExtension(path, ext, true);
	}
	
	public static ArrayList<String> filesByExtension(String path, String ext, boolean recursive) {
		ArrayList<String> ret = new ArrayList<String>();
		IO.filesByExtension(path, ext, ret, recursive);
		return ret;
	}
	
	private static void filesByExtension(String path, String ext, ArrayList<String> list, boolean recursive) {
		if (path == null) {
			return;
		}
		
		File folder = new File(path);
		File[] listFiles = folder.listFiles(); 
		if (listFiles == null) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), new RuntimeException("Error reading path: " + path));
			return;
		}
		for (File file : listFiles) {
			/* Exists check can fail for dangling symlinks */
			if (file.getAbsoluteFile().exists() && file.getAbsoluteFile().canRead()) {
				if (file.isDirectory()) {
					if (recursive) {
						filesByExtension(file.getAbsolutePath(), ext, list, recursive);
					}
				} else {
					if (file.getAbsolutePath().endsWith("." + ext)) {
						list.add(file.getAbsolutePath());
					}
				}
			}
		}
	}
	
	public static String getFileExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1);
	}
	
	public static String getNewestFile(String path, String ext) {
		ArrayList<String> files = filesByExtension(path, ext);
		
		File newestFile = null;
		for (String filePath : files) {
			File file = new File(filePath);
			if (newestFile == null || file.lastModified() > newestFile.lastModified()) {
				newestFile = file;
			}
		}
		
		String newestFilePath = null;
		
		if (newestFile != null) {
			try {
				newestFilePath = newestFile.getCanonicalPath();
			} catch (IOException e) {
				RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
			}
		}
		
		return newestFilePath;
	}

	public static void progressFinish() {
		System.err.println("");
	}

	public static void ensureDirExists(String destDir) {
		File dir = new File(destDir);
		
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new RuntimeException("Destination " + destDir + " exists but is not a directory");
			}
		} else {
			synchronized (IO.class) {
				if (!dir.exists() && !dir.mkdirs()) {
					throw new RuntimeException("Error creating directory structure: " + destDir);
				}
			}
		}
	}

	public static void copyFileDirectory(String srcFile, String destDir) {		
		File dest = new File(destDir);
		File src = new File(srcFile);
		
		try {
			FileUtils.copyFileToDirectory(src, dest);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}	
	}
	
	public static void copyFile(String srcFile, String destFile) {		
		File dest = new File(destFile);
		File src = new File(srcFile);
		
		try {
			FileUtils.copyFile(src, dest);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}	
	}
	
	public static String toCanonicalPath(String srcFileOrDirectory) throws IOException {
		File src = new File(srcFileOrDirectory);
		return src.getCanonicalPath();
	}
	
	public static void closeStream(InputStream stream) {
		try {
			stream.close();
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static void closeStream(OutputStream stream) {
		try {
			stream.close();
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static void sleepSilent(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	public static Properties readPropertiesConfiguration(String configFile) {
		Properties properties = new Properties();
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(configFile));
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Invalid configuration file", e);
		}
		
		try {
			properties.load(stream);
		} catch (IOException e) {
			throw new ConfigurationException("Invalid configuration file", e);
		}
		
		closeStream(stream);
		return properties;
	}

	public static void printBugOutput(String msg, List<String> files) {
		System.err.println("*** BUG: " + msg);
		for (String errLine : files) {
			System.err.println("*** BUG: " + errLine);
		}
	}
	
	public static String toHex(List<Byte> bytes) {
		return toHex(ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()])));
	}
	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
	
	public static final int bytesToUnsignedShortBE(List<Byte> bytes) {
	    int i = 0;
	    i |= bytes.get(0) & 0xFF;
	    i <<= 8;
	    i |= bytes.get(1) & 0xFF;
	    return i;
	}
	
	public static final long bytesToUnsignedIntBE(List<Byte> bytes) {
	    long i = 0;
	    i |= bytes.get(0) & 0xFF;
	    i <<= 8;
	    
	    i |= bytes.get(1) & 0xFF;
	    i <<= 8;
	    
	    i |= bytes.get(2) & 0xFF;
	    i <<= 8;
	    
	    i |= bytes.get(3) & 0xFF;
	    return i;
	}
	
	public static final ArrayList<Byte> unsignedIntBEToBytes(long val) {
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		
		bytes.add(null);
		bytes.add(null);
		bytes.add(null);
		bytes.add(null);

		bytes.set(3, (byte)((byte) val & 0xFF));
		val >>= 8;
		bytes.set(2, (byte)((byte) val & 0xFF));
		val >>= 8;
		bytes.set(1, (byte)((byte) val & 0xFF));
		val >>= 8;
		bytes.set(0, (byte) val);
	    return bytes;
	}
	
	public static final int byteToUnsigned(Byte b) {
		return 0x000000FF & (int)b;
	}

	public static byte[] readBinaryFile(String filename) {
		FileInputStream fos;
		
		File file = new File(filename);
		byte[] data = new byte[(int)file.length()];
		
		try {
			fos = new FileInputStream(file);
			fos.read(data);
			fos.close();
		} catch (FileNotFoundException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
			return null;
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
			return null;
		}
		
		return data;
	}

	public static void writeBinaryFile(String filename, byte[] data) {
		FileOutputStream fos;
		
		File file = new File(filename);
		
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
	}

	public static List<String> getFilesByWildcard(String filePath, String filePattern) {
		ArrayList<String> fileList = new ArrayList<String>();
		File fileDir = new File(filePath);
		FileFilter fileFilter = new WildcardFileFilter(filePattern);
		File[] files = fileDir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
		   try {
			   fileList.add(files[i].getCanonicalPath());
		   } catch (IOException e) {
			   RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		   }
		}
		
		return fileList;
	}

	public static String getCanonicalPath(String filename) {
		File file = new File(filename);
		String canonicalName = null;
		try {
			canonicalName = file.getCanonicalPath();
		} catch (IOException e) {
			RecoverableExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
		}
		return canonicalName;
	}
}
