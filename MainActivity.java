package nl.omroepoostwold.carradio;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import android.widget.*;import org.json.*;import java.net.*;import java.io.*;
public class MainActivity extends Activity {
 private TextView song,button,status;private boolean playing=true;private final Handler handler=new Handler();
 private final android.content.BroadcastReceiver receiver=new android.content.BroadcastReceiver(){
  @Override public void onReceive(Context context,Intent intent){
   String message=intent.getStringExtra("status");if(message!=null)status.setText(message);
  }
 };
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER);root.setPadding(32,20,32,20);root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff081427,0xff172e49,0xff381d51}));setContentView(root);
 song=new TextView(this);song.setText("♫  Omroep Oostwold • Current song laden...");song.setTextColor(-1);song.setTextSize(21);song.setSingleLine(true);song.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);song.setSelected(true);song.setGravity(Gravity.CENTER);song.setPadding(18,15,18,15);song.setBackgroundColor(0x77334466);root.addView(song,new LinearLayout.LayoutParams(-1,-2));
 TextView art=new TextView(this);art.setText("♫  ♪  ♬");art.setTextSize(76);art.setTextColor(0xff69e5ff);art.setGravity(Gravity.CENTER);root.addView(art,new LinearLayout.LayoutParams(-1,0,1));
 status=new TextView(this);status.setText("OMROEP OOSTWOLD • LIVE RADIO");status.setTextColor(0xffe8edff);status.setTextSize(18);status.setGravity(Gravity.CENTER);root.addView(status);
 button=new TextView(this);button.setText("■  STOP RADIO");button.setTextSize(25);button.setTextColor(-1);button.setGravity(Gravity.CENTER);button.setPadding(48,20,48,20);GradientDrawable bg=new GradientDrawable();bg.setColor(0xff136f8e);bg.setCornerRadius(26);button.setBackground(bg);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-2,-2);bp.topMargin=18;root.addView(button,bp);button.setOnClickListener(v->{playing=!playing;if(playing){startRadio();button.setText("■  STOP RADIO");}else{stopService(new Intent(this,RadioService.class));button.setText("▶  START RADIO");}});if(android.os.Build.VERSION.SDK_INT>=33)registerReceiver(receiver,new IntentFilter(RadioService.STATUS),Context.RECEIVER_NOT_EXPORTED);
 else registerReceiver(receiver,new IntentFilter(RadioService.STATUS));
 startRadio();refreshSong();}
 private void startRadio(){Intent i=new Intent(this,RadioService.class);i.putExtra("action","PLAY");if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}
 private void refreshSong(){new Thread(()->{String title="";try{URL u=new URL("https://omroep-oostwold.nl:5995/status-json.xsl");HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);try(InputStream in=c.getInputStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);JSONObject json=new JSONObject(out.toString("UTF-8"));Object source=json.getJSONObject("icestats").opt("source");JSONObject s=source instanceof JSONArray?((JSONArray)source).getJSONObject(0):(JSONObject)source;title=s.optString("title","");}}catch(Exception ignored){}final String t=title;runOnUiThread(()->song.setText(t.isEmpty()?"♫  Omroep Oostwold • LIVE": "♫  "+t));}).start();handler.postDelayed(this::refreshSong,15000);}
 @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);unregisterReceiver(receiver);super.onDestroy();}
}
