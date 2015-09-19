package com.vst.itv52.v1.util;


public class StringFilter {
	private static String [] filterStrings=new String[]{
		"720","HD","BD","DB","1080P","SD","DVD","[","]","hd","bd"
		,"db","1080p","sd","dvd","{","3D"
		,".rmvb",".RMVB",".rm",".avi",".mp4",
		".3gp",".mkv",".flv",".f4v",".rm"
	};
	
	public static String FilterString(String str){
		for (int i = 0; i < filterStrings.length; i++) {
			if(str.contains(filterStrings[i])){
				return str.substring(0, str.indexOf(filterStrings[i]));
			}
		}
		
		return str;
		
	}
}
