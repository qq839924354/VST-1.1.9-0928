package com.vst.itv52.v1.srt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.util.Log;

/**
 * Android Zip压缩解压缩
 * 
 * @author Ren.xia
 * @version 1.0
 * @updated 26-七月-2010 13:04:27
 */
public class XZip {
	/**
	 * 取得压缩包中的 文件列表(文件夹,文件自选)
	 * 
	 * @param zipFileString
	 *            压缩包名字
	 * @param bContainFolder
	 *            是否包括 文件夹
	 * @param bContainFile
	 *            是否包括 文件
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<File> GetFileList(String zipFileString,
			boolean bContainFolder, boolean bContainFile) throws Exception {
		Log.v("XZip", "GetFileList(String)");
		ArrayList<File> fileList = new ArrayList<File>();
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(
				zipFileString));
		ZipEntry zipEntry;
		String szName = "";
		Log.d("info", "有多少个压缩文件=" + inZip.getNextEntry());
		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();
			Log.i("info", "字幕文件名=" + szName);
			// 如果是目录
			if (zipEntry.isDirectory()) {
				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				// 文件夹
				File folder = new File(szName);
				if (bContainFolder) {
					fileList.add(folder);
				}
				// 如果不是文件夹
			} else {
				// 目前只支持srt格式的字幕
				File file = new File(szName);
				// 如果需要获取文件
				if (bContainFile) {
					// 暂时只支持.srt格式的字幕 其他的也没有必要取出来，是别的文件直接返回null
					if (szName.endsWith(".srt") || szName.endsWith(".SRT")) {
						fileList.add(file);
					}
				}
			}
		}// end of while
		inZip.close();
		return fileList;
	}

	/**
	 * 返回压缩包中的文件InputStream
	 * 
	 * @param zipFileString
	 *            压缩文件包
	 * @param fileString
	 *            解压文件的名字
	 * @return InputStream
	 * @throws Exception
	 */
	public static InputStream UpZip(String zipFileString, String fileString)
			throws Exception {
		Log.v("XZip", "UpZip(String, String)");
		ZipFile zipFile = new ZipFile(zipFileString);
		ZipEntry zipEntry = zipFile.getEntry(fileString);
		return zipFile.getInputStream(zipEntry);
	}

	/**
	 * 解压一个压缩文档 到指定位置
	 * 
	 * @param zipFileString
	 *            压缩包的名字
	 * @param outPathString
	 *            指定的路径
	 * @throws Exception
	 */
	public static void UnZipFolder(String zipFileString, String outPathString)
			throws Exception {
		Log.v("XZip", "UnZipFolder(String, String)");
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(
				zipFileString));
		ZipEntry zipEntry;
		String szName = "";
		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();
			if (zipEntry.isDirectory()) {
				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				File folder = new File(outPathString + File.separator + szName);
				folder.mkdirs();
			} else {
				File file = new File(outPathString + File.separator + szName);
				file.createNewFile();
				// get the output stream of the file
				FileOutputStream out = new FileOutputStream(file);
				int len;
				byte[] buffer = new byte[1024];
				// read (len) bytes into buffer
				while ((len = inZip.read(buffer)) != -1) {
					// write (len) byte from buffer at the position 0
					out.write(buffer, 0, len);
					out.flush();
				}
				out.close();
			}
		}// end of while
		inZip.close();
	}// end of func

	/**
	 * 压缩文件,文件夹
	 * 
	 * @param srcFileString
	 *            要压缩的文件/文件夹名字
	 * @param zipFileString
	 *            指定压缩的目的和名字
	 * @throws Exception
	 */
	public static void ZipFolder(String srcFileString, String zipFileString)
			throws Exception {
		Log.v("XZip", "ZipFolder(String, String)");
		// 创建Zip包
		ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(
				zipFileString));
		// 打开要输出的文件
		File file = new File(srcFileString);
		// 压缩
		ZipFiles(file.getParent() + File.separator, file.getName(), outZip);
		// 完成,关闭
		outZip.finish();
		outZip.close();
	}// end of func

	/**
	 * 压缩文件
	 * 
	 * @param folderString
	 * @param fileString
	 * @param zipOutputSteam
	 * @throws Exception
	 */
	private static void ZipFiles(String folderString, String fileString,
			ZipOutputStream zipOutputSteam) throws Exception {
		Log.v("XZip", "ZipFiles(String, String, ZipOutputStream)");
		if (zipOutputSteam == null)
			return;
		File file = new File(folderString + fileString);
		// 判断是不是文件
		if (file.isFile()) {
			ZipEntry zipEntry = new ZipEntry(fileString);
			FileInputStream inputStream = new FileInputStream(file);
			zipOutputSteam.putNextEntry(zipEntry);
			int len;
			byte[] buffer = new byte[4096];
			while ((len = inputStream.read(buffer)) != -1) {
				zipOutputSteam.write(buffer, 0, len);
			}
			zipOutputSteam.closeEntry();
		} else {
			// 文件夹的方式,获取文件夹下的子文件
			String fileList[] = file.list();
			// 如果没有子文件, 则添加进去即可
			if (fileList.length <= 0) {
				ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
				zipOutputSteam.putNextEntry(zipEntry);
				zipOutputSteam.closeEntry();
			}
			// 如果有子文件, 遍历子文件
			for (int i = 0; i < fileList.length; i++) {
				ZipFiles(folderString, fileString + File.separator
						+ fileList[i], zipOutputSteam);
			}// end of for
		}// end of if
	}// end of func

	/**
	 * 解压缩zipFile
	 * 
	 * @param file
	 *            要解压的zip文件对象
	 * @param outputDir
	 *            要解压到某个指定的目录下
	 * @throws IOException
	 */
	public void unZip(File file, String outputDir) throws IOException {
		org.apache.tools.zip.ZipFile zipFile = null;
		try {
			zipFile = new org.apache.tools.zip.ZipFile(file);
			createDirectory(outputDir, null);// 创建输出目录
			Enumeration<?> enums = zipFile.getEntries();
			while (enums.hasMoreElements()) {
				org.apache.tools.zip.ZipEntry entry = (org.apache.tools.zip.ZipEntry) enums
						.nextElement();
				System.out.println("解压." + entry.getName());
				if (entry.isDirectory()) {// 是目录
					createDirectory(outputDir, entry.getName());// 创建空目录
				} else {// 是文件
					File tmpFile = new File(outputDir + "/" + entry.getName());
					createDirectory(tmpFile.getParent() + "/", null);// 创建输出目录
					InputStream in = null;
					OutputStream out = null;
					try {
						in = zipFile.getInputStream(entry);
						;
						out = new FileOutputStream(tmpFile);
						int length = 0;
						byte[] b = new byte[2048];
						while ((length = in.read(b)) != -1) {
							out.write(b, 0, length);
						}
					} catch (IOException ex) {
						throw ex;
					} finally {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					}
				}
			}
		} catch (IOException e) {
			throw new IOException("解压缩文件出现异常", e);
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (IOException ex) {
				throw new IOException("关闭zipFile出现异常", ex);
			}
		}
	}

	/**
	 * 构建目录
	 * 
	 * @param outputDir
	 * @param subDir
	 */
	public void createDirectory(String outputDir, String subDir) {
		File file = new File(outputDir);
		if (!(subDir == null || subDir.trim().equals(""))) {// 子目录不为空
			file = new File(outputDir + "/" + subDir);
		}
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 清理文件(目录或文件)
	 * 
	 * @param file
	 */
	public void deleteDirectory(File file) {
		if (file.isFile()) {
			file.delete();// 清理文件
		} else {
			File list[] = file.listFiles();
			if (list != null) {
				for (File f : list) {
					deleteDirectory(f);
				}
				file.delete();// 清理目录
			}
		}
	}

	/**
	 * 
	 * 取得压缩包中的 文件列表(文件夹,文件自选)
	 * 
	 * @param zipfile
	 *            压缩文件
	 * @param bContainFolder
	 *            是否包括 文件夹
	 * @param bContainFile
	 *            是否包括 文件
	 * @throws IOException
	 */
	public static ArrayList<File> getZipDirs(File zipfile,
			boolean bContainFolder, boolean bContainFile) throws IOException {
		ArrayList<File> fileList = new ArrayList<File>();
		org.apache.tools.zip.ZipFile zipFile = new org.apache.tools.zip.ZipFile(
				zipfile);
		Log.d("info", zipfile.toString());
		Enumeration<?> enums = zipFile.getEntries();
		String szName = "";
		while (enums.hasMoreElements()) {
			org.apache.tools.zip.ZipEntry entry = (org.apache.tools.zip.ZipEntry) enums
					.nextElement();
			System.out.println("解压." + entry.getName());
			szName = entry.getName();
			Log.i("info", "字幕文件名=" + szName);
			// 如果是目录
			if (entry.isDirectory()) {
				szName = szName.substring(0, szName.length() - 1);
				File folder = new File(szName);
				// 需要获取文件夹目录
				if (bContainFolder) {
					fileList.add(folder);
				}
				// 如果不是文件夹
			} else {
				// 目前只支持srt格式的字幕
				File file = new File(szName);
				// 如果需要获取文件
				if (bContainFile) {
					// 暂时只支持.srt格式的字幕 其他的也没有必要取出来，是别的文件直接过滤掉
					if (szName.endsWith(".srt") || szName.endsWith(".SRT")) {
						fileList.add(file);
					}
				}
			}
		}
		return fileList;
	}

	/**
	 * 
	 * @param zipFile
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 */
	public static ArrayList<String> getEntriesNames(ZipFile zipFile) throws ZipException,
			IOException {
		ArrayList<String> entryNames = new ArrayList<String>();
		//ZipFile zf = new ZipFile(zipFile);
		Enumeration<?> entries = zipFile.entries();
		Log.d("info", entries.toString());
		while (entries.hasMoreElements()) {
			ZipEntry entry = ((ZipEntry) entries.nextElement());
			Log.d("info", "文件名="+new String(getEntryName(entry).getBytes("GB2312"),
					"utf-8"));
			entryNames.add(new String(getEntryName(entry).getBytes("GB2312"),
					"utf-8"));
		}
		return entryNames;
	}

	/**
	 * 
	 * @param entry
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getEntryName(ZipEntry entry)
			throws UnsupportedEncodingException {
		return new String(entry.getName().getBytes("GB2312"), "utf-8");
	}
}
