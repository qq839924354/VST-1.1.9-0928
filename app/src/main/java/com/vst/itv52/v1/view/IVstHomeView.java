package com.vst.itv52.v1.view;


/**
 * 页面视图实例接口
 * 
 * @author Administrator
 * 
 */
public interface IVstHomeView {

	public void initView();// 初始化视图
	public void updateData();// 更新可变的页面数据 这个里面放置一些 需要重新设置页面的方法 例如 重绘倒影 重新加载播放记录等
	public void destroy();
	public void initListener() ;
}
