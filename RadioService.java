package nl.omroepoostwold.carradio;

import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class RadioService extends Service {
 public static final String URL = "https://omroep-oostwold.nl:5995/stream?user=twinky";
 public static final String STATUS = "nl.omroepoostwold.carradio.STATUS";
 private ExoPlayer player;
 private final Handler handler = new Handler(Looper.getMainLooper());
 private boolean shouldPlay = false;
 private int retry = 0;
 private final Runnable reconnect = () -> { if (shouldPlay) prepareStream(); };

 private void report(String message) {
  Intent i = new Intent(STATUS);
  i.setPackage(getPackageName());
  i.putExtra("status", message);
  sendBroadcast(i);
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  Notification.Builder b = new Notification.Builder(this).setContentTitle("Omroep Oostwold")
      .setContentText(message).setSmallIcon(android.R.drawable.ic_media_play);
  if (Build.VERSION.SDK_INT >= 26) b.setChannelId("radio");
  nm.notify(1,b.build());
 }
 @Override public void onCreate() {
  super.onCreate();
  if (Build.VERSION.SDK_INT >= 26) {
   NotificationChannel c = new NotificationChannel("radio", "Radio", NotificationManager.IMPORTANCE_LOW);
   getSystemService(NotificationManager.class).createNotificationChannel(c);
  }
  Notification.Builder b=new Notification.Builder(this).setContentTitle("Omroep Oostwold")
     .setContentText("Verbinden...").setSmallIcon(android.R.drawable.ic_media_play);
  if(Build.VERSION.SDK_INT>=26)b.setChannelId("radio");
  startForeground(1,b.build());
 }
 @Override public int onStartCommand(Intent intent,int flags,int id) {
  String action=intent==null?"PLAY":intent.getStringExtra("action");
  if("STOP".equals(action)) {shouldPlay=false;stopSelf();return START_NOT_STICKY;}
  shouldPlay=true;
  if(player==null) prepareStream();
  return START_STICKY;
 }
 private boolean hasNetwork() {
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  NetworkInfo info=cm==null?null:cm.getActiveNetworkInfo();
  return info!=null && info.isConnected();
 }
 private void prepareStream() {
  handler.removeCallbacks(reconnect);
  if (!shouldPlay) return;
  if (!hasNetwork()) { report("Geen internetverbinding • opnieuw proberen"); retryLater();return; }
  releasePlayer();
  report("Verbinden met radio...");
  player=new ExoPlayer.Builder(this).build();
  player.addListener(new Player.Listener() {
   @Override public void onPlaybackStateChanged(int state) {
    if(state==Player.STATE_READY && player!=null && player.isPlaying()) {retry=0;report("LIVE • radio speelt");}
    else if(state==Player.STATE_BUFFERING) report("Radio bufferen...");
   }
   @Override public void onIsPlayingChanged(boolean isPlaying) {
    if(isPlaying) {retry=0;report("LIVE • radio speelt");}
   }
   @Override public void onPlayerError(PlaybackException error) {
    report("Afspeelfout: "+error.getErrorCodeName());
    releasePlayer();retryLater();
   }
  });
  player.setMediaItem(MediaItem.fromUri(URL));
  player.setPlayWhenReady(true);
  player.prepare();
 }
 private void retryLater() {
  if(!shouldPlay)return;
  retry=Math.min(retry+1,5);
  long delay=Math.min(30000, 2000L*(1<<Math.min(retry-1,4)));
  handler.removeCallbacks(reconnect);
  handler.postDelayed(reconnect,delay);
 }
 private void releasePlayer() {if(player!=null){ExoPlayer old=player;player=null;old.release();}}
 @Override public void onDestroy() {shouldPlay=false;handler.removeCallbacksAndMessages(null);releasePlayer();super.onDestroy();}
 @Override public IBinder onBind(Intent i) {return null;}
}
