package com.vst.itv52.v1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SharpnessEnum {
	SD("流畅", 0), BIAO("标清", 1), HD("高清", 2), SUPER("超清", 3), BLUE("蓝光", 4), _3D(
			"3D", 5);

	private String name;
	private int index;

	// 构造方法
	private SharpnessEnum(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (SharpnessEnum c : SharpnessEnum.values()) {
			if (c.getIndex() == index) {
				return c.name;
			}
		}
		return null;
	}

	public static SharpnessEnum getSuitSharp(SharpnessEnum target,
			List<SharpnessEnum> sharpList) {
		if (sharpList.contains(target)) {
			return target;
		} else {
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < sharpList.size(); i++) {
				int x = Math.abs(sharpList.get(i).index - target.index);
				System.out.println(x);
				list.add(x);
			}
			ArrayList<Integer> cpList = (ArrayList) list.clone();
			Collections.sort(cpList);
			int m = cpList.get(0);
			int index = list.indexOf(m);
			return sharpList.get(index);
		}
	}

	public static SharpnessEnum getSharp(int index) {
		switch (index) {
		case 0:
			return SD;
		case 1:
			return BIAO;
		case 2:
			return HD;
		case 3:
			return SUPER;
		case 4:
			return BLUE;
		case 5:
			return _3D;
		default:
			return null;
		}
	}

	// get set 方法
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
