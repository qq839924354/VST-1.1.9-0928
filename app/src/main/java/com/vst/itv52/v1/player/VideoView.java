package com.vst.itv52.v1.player;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;

public class VideoView extends SurfaceView implements MediaPlayerControl {

	private static final String TAG = "VSTVideoView";
	// settable by the client
	private Uri mUri;
	private Map<String, String> mHeaders;
	private int mDuration;

	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private MediaController mMediaController;

	private MediaPlayer.OnInfoListener mOnInfoListener = null;
	private MediaPlayer.OnCompletionListener mOnCompletionListener = null;
	private MediaPlayer.OnPreparedListener mOnPreparedListener = null;
	private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = null;
	private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = null;
	private MediaPlayer.OnErrorListener mOnErrorListener = null;

	private int mCurrentBufferPercentage;

	private int mSeekWhenPrepared; // recording the seek position while
									// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private Context mContext;
	private WindowManager wm;
	private ProgressBar loading;

	public VideoView(Context context) {
		super(context);
		mContext = context;
		initVideoView();

	}

	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initVideoView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		System.out.println("onMeasure == " + width + ":" + height);
		setMeasuredDimension(width, height);
	}

	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = desiredSize;
			break;

		case MeasureSpec.AT_MOST:
			/*
			 * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
			result = Math.min(desiredSize, specSize);
			break;

		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
		
	
		loading = (ProgressBar) LayoutInflater.from(mContext).inflate(
				R.layout.player_buf_pro, null);
		wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		isList = false;
		openVideo();
		requestLayout();
		invalidate();
	}

	private Uri[] mUris; //
	private int[] mDurations;
	private boolean isList = false;
	private int index;

	/**
	 * 设置播放列表
	 * 
	 * @param uris
	 *            url列表
	 * @param headers
	 *            公用头
	 * @param durations
	 *            每一段时长
	 */
	public void setVideoURI(Uri[] uris, Map<String, String> headers,
			int[] durations) {

		if (uris == null || durations == null
				|| uris.length != durations.length) {
			throw new IllegalArgumentException(
					"uris must not null , durations must nuo null and uris.length must =durations.length");
		}
		isResultSeek = false;
		mUris = uris;
		index = 0;
		isList = true;
		mDurations = durations;
		mUri = uris[index];
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	Handler handler = new Handler(Looper.getMainLooper());

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			return;
		}
		Log.d(TAG, "current uri = " + mUri.toString());
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the
		// framework.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);

		/* 设置取内置EPG信息默认值 */
		MyApp.setLive_Cookie("-");
		MyApp.setLiveSeek("0");
		MyApp.setLiveEpg("-");
		MyApp.setLiveNextEpg("-");
		MyApp.setLiveNextUrl("http://v.youku.com/player/getM3U8/vid/142361684/type/hd2/video.m3u8");

		final String video_url = mUri.toString();
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String rurl = getLocalURL(video_url);
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							mUri = Uri.parse(rurl);
							mMediaPlayer = new MediaPlayer();
							mMediaPlayer
									.setOnPreparedListener(mPreparedListener);
							mMediaPlayer
									.setOnVideoSizeChangedListener(mSizeChangedListener);
							mMediaPlayer
									.setOnSeekCompleteListener(mSeekCompleteListener);
							mMediaPlayer.setOnInfoListener(mInfoListener);
							mDuration = -1;
							mMediaPlayer
									.setOnCompletionListener(mCompletionListener);
							mMediaPlayer.setOnErrorListener(mErrorListener);
							mMediaPlayer
									.setOnBufferingUpdateListener(mBufferingUpdateListener);
							mCurrentBufferPercentage = 0;
							Log.d(TAG, "当前视频加载地址  = " + mUri.toString());
							
							mMediaPlayer.setDataSource(mContext, mUri, getPlayer_headers(mUri.toString(), mHeaders));
							mMediaPlayer.setDisplay(mSurfaceHolder);
							mMediaPlayer
									.setAudioStreamType(AudioManager.STREAM_MUSIC);
							mMediaPlayer.setScreenOnWhilePlaying(true);
							mMediaPlayer.prepareAsync();
							handler.postDelayed(TimeOutError, TIMEOUTDEFAULT);
							mCurrentState = STATE_PREPARING;
							attachMediaController();
						} catch (IOException ex) {
							Log.w(TAG, "Unable to open content: " + mUri, ex);
							mCurrentState = STATE_ERROR;
							mTargetState = STATE_ERROR;
							mErrorListener.onError(mMediaPlayer,
									MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
							return;
						} catch (IllegalArgumentException ex) {
							Log.w(TAG, "Unable to open content: " + mUri, ex);
							mCurrentState = STATE_ERROR;
							mTargetState = STATE_ERROR;
							mErrorListener.onError(mMediaPlayer,
									MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
							return;
						}
					}
				});
			}
		}).start();
	}

	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if (mMediaPlayer != null && mMediaController != null) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ? (View) this
					.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			mCurrentState = STATE_PREPARED;

			// Get the capabilities of the player for this stream
			// Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
			// MediaPlayer.BYPASS_METADATA_FILTER);
			//
			// if (data != null) {
			// mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
			// || data.getBoolean(Metadata.PAUSE_AVAILABLE);
			// mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
			// || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
			// mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
			// || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
			// } else {
			mCanPause = mCanSeekBack = mCanSeekForward = true;
			// }

			handler.removeCallbacks(TimeOutError);

			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}

			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}

			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared
													// may be
													// changed after seekTo()
													// call
			if (seekToPosition != 0) {
				Log.d(TAG, "seekToPosition =" + seekToPosition);
				seekTo(seekToPosition);
			}

			if (mVideoWidth != 0 && mVideoHeight != 0) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth
						&& mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying()
							&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a
							// video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {

			if (isList && index < mUris.length - 1) { // 是列表 但不最后一段 没有这正结束
				index += 1;
				mUri = mUris[index];
				Log.d(TAG, "index = " + index + ",uri = " + mUri);
				openVideo();
				return;
			}
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mMediaController != null) {
				mMediaController.hide();
			}

			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}

			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err,
						impl_err)) {
					return true;
				}
			}

			/*
			 * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
			if (getWindowToken() != null) {
				Resources r = mContext.getResources();
				String messageId;

				if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
					messageId = "playback error";
				} else {
					messageId = "unknown error ";
				}

				new AlertDialog.Builder(mContext)
						.setTitle("Sorry")
						.setMessage(messageId)
						.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/*
										 * If we get here, there is no onError
										 * listener, so at least inform them
										 * that the video is over.
										 */
										if (mOnCompletionListener != null) {
											mOnCompletionListener
													.onCompletion(mMediaPlayer);
										}
									}
								}).setCancelable(false).show();
			}
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
		}
	};

	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {

			if (isList) {
				isResultSeek = false;
			}
			if (mOnSeekCompleteListener != null) {
				mOnSeekCompleteListener.onSeekComplete(mp);
			}
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
		mOnInfoListener = l;
	}

	public void setOnBufferingUpdateListener(
			MediaPlayer.OnBufferingUpdateListener l) {
		mOnBufferingUpdateListener = l;
	}

	public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener l) {
		mOnSeekCompleteListener = l;
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mMediaController != null)
				mMediaController.hide();
			release(true);
		}
	};

	/*
	 * release the media player in any state
	 */
	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}

		handler.removeCallbacks(TimeOutError);

		if (loading.getParent() != null)
			wm.removeView(loading);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported
				&& mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
				if (!mMediaPlayer.isPlaying()) {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				}
				return true;
			} else if (keyCode == 185) {
				defaultScale = (defaultScale + 1) % 3;
				selectScales(defaultScale);
				if (mOnChangScaleListener != null) {
					mOnChangScaleListener.changeScale(defaultScale);
				}
				return true;
			} else {
				toggleMediaControlsVisiblity();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private int defaultScale = 0;

	public void setDefaultScale(int defaultScale) {
		this.defaultScale = defaultScale;
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	public void start() {
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;

		if (MyApp.LiveSeek.length() > 0 && MyApp.LiveSeek != "0"
				&& Integer.parseInt(MyApp.LiveSeek) > 1) {
			seekTo(Integer.parseInt(MyApp.LiveSeek) * 1000);
		}
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		release(false);
	}

	public void resume() {
		openVideo();
	}

	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			if (isList) {
				for (int i = 0; i < mDurations.length; i++) {
					mDuration += mDurations[i];
				}
				return mDuration;
			} else {
				mDuration = mMediaPlayer.getDuration();
				return mDuration;
			}
		}
		mDuration = -1;
		return mDuration;

	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			if (isList) { // �б�
				int currentPosition = 0;
				for (int i = 0; i < index; i++) {
					currentPosition += mDurations[i];
				}
				return currentPosition += mMediaPlayer.getCurrentPosition();

			} else {
				return mMediaPlayer.getCurrentPosition();
			}
		}
		return 0;
	}

	boolean isResultSeek = true;

	public void seekTo(int msec) {
		if (msec <= 0) {
			return;
		}
		Log.d(TAG, "msec = " + msec);
		if (isResultSeek) {
			if (isInPlaybackState()) {
				mMediaPlayer.seekTo(msec);
				mSeekWhenPrepared = 0;
			} else {
				mSeekWhenPrepared = msec;
				Log.d(TAG, "mSeekWhenPrepared = " + mSeekWhenPrepared);
			}
		} else { // 不是最终的 则需要计算
			for (int i = 0; i < mDurations.length; i++) {
				msec -= mDurations[i];
				if (msec < 0) {
					msec += mDurations[i];
					isResultSeek = true;
					if (index == i) {
						mMediaPlayer.seekTo(msec);
						mSeekWhenPrepared = 0;
					} else {
						index = i;
						mUri = mUris[i];
						mSeekWhenPrepared = msec;
						Log.d(TAG, "mSeekWhenPrepared = " + mSeekWhenPrepared);
						handler.post(new Runnable() {
							@Override
							public void run() {
								openVideo();
							}
						});
					}
					break;
				}
			}
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	private static final long TIMEOUTDEFAULT = 30000;
	private static final int MEDIA_ERROR_TIMED_OUT = 0xffffff92;
	private Runnable TimeOutError = new Runnable() {

		@Override
		public void run() {
			Log.e(TAG, "open video time out : Uri = " + mUri);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, -100);
			release(false);
		}
	};

	OnInfoListener mInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			Log.i(TAG, "OnInfoListener-------->what:" + what + ",  extra :"
					+ extra);

			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mp, what, extra);
				return true;
			}
			/*
			 * MEDIA_INFO_VIDEO_TRACK_LAGGING MEDIA_INFO_BUFFERING_START
			 * MEDIA_INFO_BUFFERING_END MEDIA_INFO_NOT_SEEKABLE
			 * MEDIA_INFO_DOWNLOAD_RATE_CHANGED
			 */
			if (getWindowToken() != null) {

				LayoutParams lp = new LayoutParams();
				lp.format = PixelFormat.TRANSPARENT;
				lp.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
				lp.width = LayoutParams.WRAP_CONTENT;
				lp.height = LayoutParams.WRAP_CONTENT;
				// lp.token = getWindowToken();
				lp.gravity = Gravity.CENTER;

				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					if (loading.getParent() == null)
						wm.addView(loading, lp);
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					if (loading.getParent() != null)
						wm.removeView(loading);
					break;
				default:
					break;
				}
			}
			return true;
		}
	};

	public final static int A_4X3 = 1;
	public final static int A_16X9 = 2;
	public final static int A_RAW = 4; // 原始大小
	public final static int A_DEFALT = 0; // 原始比例

	/**
	 * 全屏状态 才可以使用 选择比例
	 * 
	 * @param flg
	 * 
	 */
	public void selectScales(int flg) {
		if (getWindowToken() != null) {
			Rect rect = new Rect();
			getWindowVisibleDisplayFrame(rect);
			Log.d(TAG, "Rect = " + rect.top + ":" + rect.bottom + ":"
					+ rect.left + ":" + rect.right);

			double height = rect.bottom - rect.top;
			double width = rect.right - rect.left;
			Log.d(TAG, "diplay = " + width + ":" + height);

			if (height <= 0.0 || width <= 0.0 || mVideoHeight <= 0.0
					|| mVideoWidth <= 0.0) {
				return;
			}
			ViewGroup.LayoutParams param = getLayoutParams();
			switch (flg) {
			case A_4X3:
				if (width / height >= 4.0 / 3.0) { // 屏幕 宽了 以屏幕高为基�?
					param.height = (int) height;
					param.width = (int) (4 * height / 3);
				} else { // 屏幕 高了 以宽为基�?
					param.width = (int) width;
					param.height = (int) (3 * width / 4);
				}
				System.out.println("A_4X3 === " + param.width + ":"
						+ param.height);
				setLayoutParams(param);
				break;
			case A_16X9:
				if (width / height >= 16.0 / 9.0) { // 屏幕 宽了 以屏幕高为基�?
					param.height = (int) height;
					param.width = (int) (16 * height / 9);
				} else { // 屏幕 高了 以宽为基�?
					param.width = (int) width;
					param.height = (int) (9 * width / 16);
				}
				System.out.println("A_16X9 === " + param.width + ":"
						+ param.height);
				setLayoutParams(param);
				break;
			case A_DEFALT: //
				if (width / height >= mVideoWidth / mVideoHeight) { // 屏幕 宽了
																	// 以屏幕高为基�?
					param.height = (int) height;
					param.width = (int) (mVideoWidth * height / mVideoHeight);
				} else { // 屏幕 高了 以宽为基�?
					param.width = (int) width;
					param.height = (int) (mVideoHeight * width / mVideoWidth);
				}
				System.out.println("A_DEFALT === " + param.width + ":"
						+ param.height);
				setLayoutParams(param);
				break;
			}
		}
	}

	private OnChangScaleListener mOnChangScaleListener;

	public void setOnChangScaleListener(OnChangScaleListener l) {
		mOnChangScaleListener = l;
	}

	public interface OnChangScaleListener {
		public void changeScale(int scalemod);
	}

	/*
	 * 去掉直播请求头
	 */
	private Map<String, String> getPlayer_headers(String PlayLiveUrl, Map<String, String> PHeaders) {
		if (PHeaders != null) return PHeaders;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("User-Mac", MyApp.User_Mac);
		headers.put("User-Key", MyApp.get_livekey());
		headers.put("User-Ver", "GGwlPlayer/QQ243944493 ("+Build.MODEL+")");
		/**
		 * 判断是否自动加域名来路
		 */
		if (is_str(MyApp.Live_Referer, "|")){
			String[] Array_Live_Referer = MyApp.Live_Referer.split("\\|");
			for(int i = 0;i < Array_Live_Referer.length;i++) {
				String a_Referer = Array_Live_Referer[i];
				if (is_str(a_Referer, "@")) {
					String[] b_Referer = a_Referer.split("@");
					if (is_str(PlayLiveUrl, b_Referer[0])) {
						headers.put("Referer", b_Referer[1]);
					}
				}
				else if (is_str(PlayLiveUrl, a_Referer)) {
					headers.put("Referer", PlayLiveUrl);
				}
			}
		}
		else if (is_str(MyApp.Live_Referer, "@")) {
			String[] b_Referer = MyApp.Live_Referer.split("@");
			if (is_str(PlayLiveUrl, b_Referer[0])) {
				headers.put("Referer", b_Referer[1]);
			}
		}
		else if (is_str(PlayLiveUrl, MyApp.Live_Referer)) {
			headers.put("Referer", PlayLiveUrl);
		}
		if (MyApp.LiveCookie != null && MyApp.LiveCookie != "-") {
			headers.put("Cookie", MyApp.LiveCookie);
		}
		if (MyApp.Live_Range != "-" && is_str(MyApp.Live_Range, "|")){
			String[] Array_Live_Range = MyApp.Live_Range.split("\\|");
			for(int i = 0;i < Array_Live_Range.length;i++) {
				String a_Range = Array_Live_Range[i];
				if (is_str(PlayLiveUrl, a_Range)) {
					headers.put("Range", "bytes=");
					break;
				}
			}
		}
		else if (is_str(PlayLiveUrl, MyApp.Live_Range)) {
			headers.put("Range", "bytes=");
		}
		return headers;
	}
	
	/**
	 * 判断字符串是否存在
	 * @param str
	 * @param txt
	 * @return
	 */
	private static boolean is_str(String strUrl, String txt) {
		if (strUrl.contains(txt)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 特殊地址特殊处理
	 * @param urlString
	 * @return
	 */
//    public String getLocalURL(String urlString){
//    	urlString = urlString.replace("%20", " ").replace("+", " ").replace("%3a", ":").replace("%3A", ":");
//		urlString = urlString.replace("%2f", "/").replace("%2F", "/").replace("%40", "@").replace("%26", "&");
//    	//----排除HTTP特殊----//
//		String targetUrl = urlString;
//		if (is_str(urlString, "91vst.com") || is_str(urlString, "myvst.net") || is_str(urlString, "52itv.cn") || is_str(urlString, "hdplay.cn") || is_str(urlString, "ku6.com/broadcast/sub")) {
//			targetUrl = getRedirectUrl(urlString);
//		}
//    	if (is_str(targetUrl, "totiptv.com/live/") && !is_str(targetUrl, ".m3u8?bitrate=800")) {
//    		targetUrl = targetUrl + "?bitrate=800";
//    	}
//    	else if (targetUrl.length() > 12 && is_str(targetUrl, "channel=pa://")){
//    		targetUrl = get_CNLiveURL(targetUrl);
//    		System.out.println("解析CNTVLiveurl: " + targetUrl);
//    	}
//    	else if (targetUrl.length() > 12 && is_str(targetUrl, ".m3u8") && is_str(targetUrl, "gdtv.cn")){
//    		targetUrl = get_PlayM3U8LiveURL(targetUrl);
//    		System.out.println("解析M3U8Liveurl: " + targetUrl);
//    	}
//    	else if (targetUrl.length() > 12 && is_str(targetUrl, "$$") && is_str(targetUrl, "synacast.com")){
//    		targetUrl = get_PPTVLiveURL(targetUrl);
//    		System.out.println("解析PPTVLiveurl: " + targetUrl);
//    	}
//    	if (!targetUrl.contains("http://")) {
//    		return targetUrl;
//    	}
//        return targetUrl;
//    }
//    
//    
    
    
    
	
	/**
	 * 特殊地址特殊处理
	 * @param urlString
	 * @return
	 */
    public String getLocalURL(String urlString){
//    	
//    	 if ((urlString.startsWith("http://url.52itv.cn/live/")) && (urlString.endsWith(".vst")))
//         {
//    		 return urlString +"?seek=true";
//    		
//    		 
//    		
//         }
    	
    	
    	
    	
    	
    	
    	
    	urlString = urlString.replace("%20", " ").replace("+", " ").replace("%3a", ":").replace("%3A", ":");
		urlString = urlString.replace("%2f", "/").replace("%2F", "/").replace("%40", "@").replace("%26", "&");
    	//----排除HTTP特殊----//
		String targetUrl = urlString;
		if (is_str(urlString, "91vst.com") || is_str(urlString, "myvst.net") || is_str(urlString, "52itv.cn") || is_str(urlString, "hdplay.cn") || is_str(urlString, "ku6.com/broadcast/sub")) {
			targetUrl = getRedirectUrl(urlString);
		}
    	if (is_str(targetUrl, "totiptv.com/live/") && !is_str(targetUrl, ".m3u8?bitrate=800")) {
    		targetUrl = targetUrl + "?bitrate=800";
    	}
    	else if (targetUrl.length() > 12 && is_str(targetUrl, "channel=pa://")){
    		targetUrl = get_CNLiveURL(targetUrl);
    		System.out.println("解析CNTVLiveurl: " + targetUrl);
    	}
    	else if (targetUrl.length() > 12 && is_str(targetUrl, ".m3u8") && is_str(targetUrl, "gdtv.cn")){
    		targetUrl = get_PlayM3U8LiveURL(targetUrl);
    		System.out.println("解析M3U8Liveurl: " + targetUrl);
    	}
    	else if (targetUrl.length() > 12 && is_str(targetUrl, "$$") && is_str(targetUrl, "synacast.com")){
    		targetUrl = get_PPTVLiveURL(targetUrl);
    		System.out.println("解析PPTVLiveurl: " + targetUrl);
    	}
    	if (!targetUrl.contains("http://")) {
    		return targetUrl;
    	}
        return targetUrl;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /* 正宗的抓取重定向URL */
	
	private static HttpURLConnection conn;
	
	/* 抓取直播重定向 */
	static String getRedirectUrl(String urlStr) {
		if (!urlStr.startsWith("http://")) {
			return urlStr;
		}
		String playUrl = urlStr;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("User-Agent", MyApp.User_Agent);
			conn.setRequestProperty("User-Mac", MyApp.User_Mac);
			conn.setRequestProperty("User-Key", MyApp.get_livekey());
			conn.setRequestProperty("User-Ver", MyApp.User_Ver);
			String loginKey = MyApp.getLoginKey();
			if (loginKey != null && loginKey != "-" && loginKey.length() > 60) {
				conn.setRequestProperty("Cookie", loginKey);
			}
			if (MyApp.Live_Referer != null && MyApp.Live_Referer != "-"
					&& is_str(MyApp.Live_Referer, "|")) {
				String[] Array_Live_Referer = MyApp.Live_Referer.split("\\|");
				for (int i = 0; i < Array_Live_Referer.length; i++) {
					String a_Referer = Array_Live_Referer[i];
					if (is_str(a_Referer, "@")) {
						String[] b_Referer = a_Referer.split("@");
						if (is_str(urlStr, b_Referer[0])) {
							conn.setRequestProperty("Referer", b_Referer[1]);
						}
					} else if (is_str(urlStr, a_Referer)) {
						conn.setRequestProperty("Referer", urlStr);
					}
				}
			} else if (MyApp.Live_Referer != null
					&& is_str(MyApp.Live_Referer, "@")) {
				String[] b_Referer = MyApp.Live_Referer.split("@");
				if (is_str(urlStr, b_Referer[0])) {
					conn.setRequestProperty("Referer", b_Referer[1]);
				}
			} else if (MyApp.Live_Referer != null
					&& is_str(urlStr, MyApp.Live_Referer)) {
				conn.setRequestProperty("Referer", urlStr);
			}
			System.out.println("返回状态: " + conn.getResponseCode());
			if (conn.getResponseCode() == 301 || conn.getResponseCode() == 302) {
				playUrl = conn.getHeaderField("Location");
				String Play_epg = conn.getHeaderField("Play_epg");
				String Next_epg = conn.getHeaderField("Next_epg");
				String Next_url = conn.getHeaderField("Next_url");
				String Play_Seek = conn.getHeaderField("Play_Seek");
				if (conn.getHeaderField("Set-Cookie") != null) {
					MyApp.setLive_Cookie(conn.getHeaderField("Set-Cookie"));
				}
				if (conn.getHeaderField("Cookie") != null) {
					MyApp.setLive_Cookie(MyApp.LiveCookie + ";" + conn.getHeaderField("Cookie"));
				}
				if (Play_Seek != null && Play_Seek.length() > 0
						&& Integer.parseInt(Play_Seek) > 0) {
					System.out.println("定位时间: " + Play_Seek + "秒");
					MyApp.setLiveSeek(Play_Seek);
				}
				if (Play_epg != null && Play_epg.length() > 3) {
					Play_epg = URLDecoder.decode(Play_epg);
					System.out.println("当前节目EPG: " + Play_epg);
					MyApp.setLiveEpg(Play_epg);
				}
				if (Next_epg != null && Next_epg.length() > 3) {
					Next_epg = URLDecoder.decode(Next_epg);
					System.out.println("下个节目EPG: " + Next_epg);
					MyApp.setLiveNextEpg(Next_epg);
				}
				if (Next_url != null && Next_url.length() > 12) {
					System.out.println("下个节目URL: " + Next_url);
					MyApp.setLiveNextUrl(urlStr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		if (playUrl == null) {
			playUrl = urlStr;
		}
		return playUrl;
	}

	/* PPTV地址抓取 */
	private String get_PPTVLiveURL(String Playurl) {
		String LiveUrl = "";
		try {
			String[] SpSrc = Playurl.split("\\$\\$");
			String xmlurl = MyApp.curl(SpSrc[0].trim() + "?type=m3u8.web.pad");
			if (xmlurl != null && is_str(xmlurl, "server_host")) {
				Document document = new SAXReader().read(new ByteArrayInputStream(xmlurl.getBytes("utf-8")));
				Element root = document.getRootElement();
				String server_key = root.element("key").getText().trim();
				String server_host = root.element("server_host").getText().trim();
				LiveUrl = "http://" + server_host + "/" + SpSrc[1].trim() + "?type=ppbox&k=" + server_key;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (LiveUrl.length() > 12) {
			return LiveUrl;
		}
		return Playurl;
	}
	
	/* CNTV地址抓取 */
	private String get_CNLiveURL(String Playurl) {
		String LiveUrl = "";
		try {
			LiveUrl = "http://" + vst_jq(MyApp.curl(Playurl), "://", "\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (LiveUrl.length() > 12) {
			return LiveUrl;
		}
		return Playurl;
	}
	
	
	/* M3U8地址抓取 */
	private String get_PlayM3U8LiveURL(String Playurl) {
		String LiveUrl = "";
		try {
			LiveUrl = "http://" + vst_jq(MyApp.curl(Playurl), "://", "\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (LiveUrl.length() > 12) {
			return LiveUrl;
		}
		return Playurl;
	}
	
	
	/* 截取字符串 */
	private String vst_jq(String str, String start, String end) {
		String retstr = "";
		if (is_str(str, start)) {
			String string = str.split(start)[1].trim();
			if (end != null && is_str(string, end)) {
				retstr = string.split(end)[0].trim();
			}
			else return string;
		}
		return retstr;
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
