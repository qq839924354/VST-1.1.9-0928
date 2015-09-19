package com.vst.itv52.v1.adapters;

import java.util.ArrayList;

import net.tsz.afinal.FinalBitmap;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.VideoInfo;

public class VideoInfoAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<VideoInfo> videos;
	private FinalBitmap fb;
	private boolean isSearch;

	public VideoInfoAdapter(Context context, ArrayList<VideoInfo> videos) {
		this(context, videos, false);
	}

	public VideoInfoAdapter(Context context, ArrayList<VideoInfo> videos,
			boolean isSearch) {
		this.context = context;
		setVideos(videos);
		this.isSearch = isSearch;
		fb = FinalBitmap.create(context, context.getCacheDir().toString());
		fb.configLoadingImage(R.drawable.default_film_img);
		fb.configLoadfailImage(R.drawable.default_film_img);
		fb.configTransitionDuration(500);
	}

	public void setVideos(ArrayList<VideoInfo> videos) {
		if (videos != null) {
			this.videos = videos;
		} else {
			this.videos = new ArrayList<VideoInfo>();
		}

	}

	@Override
	public int getCount() {
		return videos.size();
	}

	@Override
	public Object getItem(int position) {
		return videos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.type_details_item, null);
			holder.poster = (ImageView) convertView
					.findViewById(R.id.video_poster);
			holder.spuerHd = (ImageView) convertView
					.findViewById(R.id.video_superHD);
			holder.banben = (TextView) convertView
					.findViewById(R.id.video_banben);
			holder.videoName = (TextView) convertView
					.findViewById(R.id.video_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 取得单个item的数据
		VideoInfo info = videos.get(position);
		// 显示影视名称
		if (isSearch) {
			holder.videoName.setText(Html.fromHtml(info.title));
		} else {
			holder.videoName.setText(info.title);
		}
		if (info.qxd != null && info.qxd.contains("高清")) {
			holder.spuerHd.setImageResource(R.drawable.sd);
			holder.spuerHd.setVisibility(View.VISIBLE);
		} else if (info.qxd != null && info.qxd.contains("超清")) {
			holder.spuerHd.setImageResource(R.drawable.hd);
			holder.spuerHd.setVisibility(View.VISIBLE);
		} else {
			holder.spuerHd.setVisibility(View.GONE);
		}
		try {
			if (Float.valueOf(info.mark) > 0) {
				holder.banben.setText("评分：" + info.mark);
			} else {
				holder.banben.setText("暂无评分");
			}
		} catch (Exception e) {
			e.printStackTrace();
			holder.banben.setText("暂无评分");
		}
		fb.display(holder.poster, info.img);
		return convertView;
	}

	public void changData(ArrayList<VideoInfo> arrayList) {
		setVideos(arrayList);
		notifyDataSetChanged();
	}

	class ViewHolder {
		private ImageView poster;
		private ImageView spuerHd;
		private TextView banben;
		private TextView videoName;
	}

}
