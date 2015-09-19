package com.vst.itv52.v1.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 盒子硬件信息获取Linux指令运行工具
 * 
 * @author mygica-hsj
 * 
 */

public class CMDExecute {
	public synchronized String run(String[] cmd, String workdirectory)
			throws IOException {
		String result = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			InputStream in = null;
			// ����һ��·��
			if (workdirectory != null) {
				builder.directory(new File(workdirectory));
				builder.redirectErrorStream(true);
				Process process = builder.start();
				in = process.getInputStream();
				byte[] re = new byte[1024];

				while (in.read(re) != -1)
					result = result + new String(re);
			}
			if (in != null) {
				in.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}