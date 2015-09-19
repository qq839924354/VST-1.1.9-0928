package com.vst.itv52.v1.srt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.SearchSrtAdapter;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.custom.LoadingDialog;
import com.vst.itv52.v1.model.ShooterSRTBean;
import com.vst.itv52.v1.player.PlayerMenuContrl;
import com.vst.itv52.v1.player.VodPlayer;
import com.vst.itv52.v1.player.XLLXPlayer;
import com.vst.itv52.v1.util.HttpWorkTask;
import com.vst.itv52.v1.util.HttpWorkTask.ParseCallBack;
import com.vst.itv52.v1.util.HttpWorkTask.PostCallBack;

/**
 * 设置字幕的类
 * 
 * @author w
 * 
 */
public class SearchSrtPop extends PopupWindow {
	private int autoDismiss = 60000;// 60秒隐藏
	private Context context;
	private Handler handler;
	private Button searchSrtContent;
	private ListView searchSrtlistview;
	private String videoName;
	private ArrayList<ShooterSRTBean> shooterSRTBeans;
	private LoadingDialog progressDialog;
	private SearchSrtAdapter adapter;
	private ShooterSRTBean shooterSRTBean;

	private Runnable autoHide = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(getSrtContent);
			dismiss();
		}
	};

	public SearchSrtPop(Context context, ArrayList<ShooterSRTBean> shooterSRTBeans, Handler handler,String videoName) {
		super();
		this.context = context;
		this.handler = handler;
		this.videoName=videoName;
		this.shooterSRTBeans= shooterSRTBeans;
		initView();
		initData();
		initListener();
	}

	private void initData() {
		if (shooterSRTBeans!=null && shooterSRTBeans.size()>0) {
			adapter = new SearchSrtAdapter(context, shooterSRTBeans);
			Log.d("info", shooterSRTBeans.toString());
			searchSrtlistview.setAdapter(adapter);
			handler.removeCallbacks(autoHide);
			handler.postDelayed(autoHide, autoDismiss);// 下载完成之后一分钟隐藏
		}else{
			ItvToast toast = ItvToast.makeText(context,"暂无该视频的字幕", 3000);
			toast.show();
			handler.removeCallbacks(autoHide);
			handler.postDelayed(autoHide, 1000);// 没有字幕的话5秒之后就隐藏
		}
		
	}

	private void initView() {
		setBackgroundDrawable(context.getResources().getDrawable(
				android.R.color.transparent));
		setFocusable(true);
		setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = inflater.inflate(R.layout.search_srt_pop, null);
		searchSrtContent = (Button) root
				.findViewById(R.id.search_srt_search_content);
		searchSrtlistview = (ListView) root
				.findViewById(R.id.search_srt_search_liseview);
		this.progressDialog = new LoadingDialog(context);
		progressDialog.setLoadingMsg("加载中...");
		setContentView(root);
	}

	private String srtZipPath;//压缩包文件夹
	private String srtFile;//解压文件夹路径
	private File file;
	private void initListener() {

		searchSrtlistview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				progressDialog.show();
				try {
					// 单击某项Item 进入当前这条字幕的下载
					// 去射手网联网解析悬挂字幕
					shooterSRTBean = adapter.getItem(position);
					Log.i("info", "sid" + shooterSRTBean.getId());
					srtZipPath = null;
					// 获得当前sdcard状态
//					String sdcardState = Environment.getExternalStorageState();
					// sdcard存在
//					if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
//						// 获得sdcard的根目录
//						String rootPath = Environment.getExternalStorageDirectory()
//								.getPath();
//						// 设置下载路径
//						srtZipPath = rootPath + "/vst/videoSrt.rar";
//						srtFile=rootPath + "/vst/srtFile";
//					} else {
						// sdcard不存在 则存储在内存缓存下载目录中
					srtZipPath = context.getCacheDir().getPath() + "/vst/videoSrt.rar";
					srtFile=context.getCacheDir().getPath()+ "/vst/srtFile";
//					}
					Log.i("info", "path=" + srtZipPath);
					//Toast.makeText(context, "路径="+srtZipPath, 3000).show();
					 file = new File(srtZipPath);
					if(!file.getParentFile().exists()){
						file.getParentFile().mkdirs();
					}
					// 如果目标文件已经存在，则删除。产生覆盖旧文件的效果
					if (file.exists()) {
						file.delete();
					}
					//重新创建文件
					file.createNewFile();
					new HttpWorkTask<Boolean>(new ParseCallBack<Boolean>() {

						@Override
						public Boolean onParse() {

							try {
								return ShooterSRTGetter.downloadSRTZip(
										shooterSRTBean.getId(), srtZipPath);
							} catch (Exception e) {
								e.printStackTrace();
								return false;
							}
						}
					}, new PostCallBack<Boolean>() {

						@Override
						public void onPost(Boolean result) {
							progressDialog.dismiss();
							if (result) {
									try {
										handler.post(getSrtContent);
									} catch (Exception e) {
										ItvToast toast = ItvToast.makeText(context,
												"获取文件列表失败", 2000);
										toast.show();
										e.printStackTrace();
									}
							} else {
								ItvToast toast = ItvToast.makeText(context,
										"下载失败，请重新下载", 2000);
								toast.show();
							}
						
						}
					}).execute();
				} catch (Exception e) {
					ItvToast toast = ItvToast.makeText(context,
							"字幕列表加载失败，请重试！", 3000);
					toast.show();
					e.printStackTrace();
				}

				handler.removeCallbacks(autoHide);
				handler.postDelayed(autoHide, autoDismiss);
			}
		});
	}
	Runnable getSrtContent = new Runnable() {
		
		@Override
		public void run() {
			File DirFile=new File(srtFile);
			//删除文件夹起到覆盖的作业
			if(DirFile.isDirectory()){
				FileOperation.delFolder(srtFile);
			}
			try {
				//先解压文件
				DeCompressUtil.deCompress(srtZipPath, srtFile);
				//再从解压文件中读取到文件的据对路径
				ArrayList<String> files=FileOperation.readfile(srtFile);
				// 获得该压缩包中的文件列表
				Log.i("info", "files=" + files.size()+","+files.toString());
				//当前files不为null，且长度大于1,则当前popWindow消失，设置字幕显示
				if (files!=null && files.size()>0 && !files.isEmpty()) {
					//隐藏当前pop
					dismiss();
					//如果是直播
					if (context instanceof XLLXPlayer) {
						XLLXPlayer new_name = (XLLXPlayer) context;
						new_name.showSRTPop(files);
						new_name.setFiles(files);
						//如果是点播
						XLLXPlayer.LXORDOWN=2;
					} else if (context instanceof VodPlayer) {
						VodPlayer new_name = (VodPlayer) context;
						new_name.showSRTPop(files);
						new_name.setFiles(files);
					}
					PlayerMenuContrl.showSrtSet(true, "设置字幕(共"+files.size()+"条字幕)");
				}else{
					if (context instanceof XLLXPlayer) {
						XLLXPlayer.LXORDOWN=1;
					}
					PlayerMenuContrl.showSrtSet(false, "");
					ItvToast toast = ItvToast.makeText(context,
							"不支持该字幕格式", 3000);
					toast.show();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				ItvToast toast = ItvToast.makeText(context,
						"获取文件列表失败", 2000);
				toast.show();
				e.printStackTrace();
			}
		}
	};
	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		searchSrtContent.setText(videoName);// 默认设置该影片的名称
		super.showAtLocation(parent, gravity, x, y);
	}

}
