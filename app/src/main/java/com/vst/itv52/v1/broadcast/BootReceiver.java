package com.vst.itv52.v1.broadcast;

import com.vst.itv52.v1.activity.SettingPlay;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.player.LivePlayer;
import com.vst.itv52.v1.util.ConstantUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			if (MyApp.getAutoLive() == SettingPlay.AUTOLIVE_BOXBOOT) {
				LiveChannelInfo lastChannel = LiveDataHelper.getInstance(
						context).getChannelByVid(MyApp.getLastChannel());
				if (lastChannel != null) {
					Intent intentb = new Intent(context, LivePlayer.class);
					intentb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intentb.putExtra(ConstantUtil.LIVE_VID_EXTRA,
							lastChannel.vid);
					intentb.putExtra(ConstantUtil.LIVE_TID_EXTRA,
							lastChannel.tid[0]);
					context.startActivity(intentb);
				}
			}
		}
	}
}
