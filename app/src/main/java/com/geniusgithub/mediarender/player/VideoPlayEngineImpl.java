package com.geniusgithub.mediarender.player;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.view.SurfaceHolder;

import com.gxh.video_cache.CacheListener;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CommonUtil;
import com.geniusgithub.mediarender.util.LogFactory;

import java.io.File;


public class VideoPlayEngineImpl extends AbstractMediaPlayEngine implements CacheListener {

  private final CommonLog log = LogFactory.createLog();
  private SurfaceHolder mHolder = null;
  private OnBufferingUpdateListener mBufferingUpdateListener;
  private OnSeekCompleteListener mSeekCompleteListener;
  private OnErrorListener mOnErrorListener;

  public VideoPlayEngineImpl(Context context, SurfaceHolder holder) {
    super(context);
    setHolder(holder);
  }

  @Override
  public void exit() {
    log.i("exit");
    super.exit();
    if(mMediaPlayer != null){
      mMediaPlayer.release();
    }
//    if (mMediaInfo != null) {
//      RenderApplication.getProxy(mContext).unregisterCacheListener(this, mMediaInfo.getUrl());
//    }
  }

  public void setHolder(SurfaceHolder holder) {
    mHolder = holder;
  }

  public void setOnBuffUpdateListener(OnBufferingUpdateListener listener) {
    mBufferingUpdateListener = listener;
  }

  public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
    mSeekCompleteListener = listener;
  }

  public void setOnErrorListener(OnErrorListener listener) {
    mOnErrorListener = listener;
  }

  @Override
  protected boolean prepareSelf() {
    log.i("prepareSelf");
    try {
//      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
      defaultParam();
//      HttpProxyCacheServer proxy = RenderApplication.getProxy(mContext);
//      proxy.registerCacheListener(this, mMediaInfo.getUrl());
//      String proxyUrl = proxy.getProxyUrl(mMediaInfo.getUrl());
      mMediaPlayer.setDataSource(mMediaInfo.getUrl());
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      if (mHolder != null) {
        mMediaPlayer.setDisplay(mHolder);
      }
      if (mBufferingUpdateListener != null) {
        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      }
      if (mSeekCompleteListener != null) {
        mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
      }
      if (mOnErrorListener != null) {
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
      }
      mMediaPlayer.prepareAsync();
      log.i("mMediaPlayer.prepareAsync path = " + mMediaInfo.getUrl());
      mPlayState = PlayState.MPS_PARESYNC;
      performPlayListener(mPlayState);
    } catch (Exception e) {
//      mMediaPlayer.release();
//      mMediaPlayer = null;
//      defaultParam();
      e.printStackTrace();
      mPlayState = PlayState.MPS_INVALID;
      performPlayListener(mPlayState);
      return false;
    }

    return true;
  }

  @Override
  protected boolean prepareComplete(MediaPlayer mp) {
    log.i("prepareComplete");
    mPlayState = PlayState.MPS_PARECOMPLETE;
    if (mPlayerEngineListener != null) {
      mPlayerEngineListener.onTrackPrepareComplete(mMediaInfo);
    }

    if (mHolder != null) {
      CommonUtil.ViewSize viewSize = CommonUtil.getFitSize(mContext, mp);
      mHolder.setFixedSize(viewSize.width, viewSize.height);
    }


    mMediaPlayer.start();

    mPlayState = PlayState.MPS_PLAYING;
    performPlayListener(mPlayState);

    return true;
  }

  @Override
  public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
    log.d("onCacheAvailable = " + percentsAvailable);
  }
}
