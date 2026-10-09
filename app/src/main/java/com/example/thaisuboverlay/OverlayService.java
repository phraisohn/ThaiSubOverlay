package com.example.thaisuboverlay;
import android.app.*;import android.content.*;import android.graphics.Color;import android.graphics.PixelFormat;import android.os.*;import android.view.*;import android.widget.*;
public class OverlayService extends Service {
 WindowManager wm;TextView label;WindowManager.LayoutParams params;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int startId){String s=i==null?"":i.getStringExtra("subtitle");if(s==null)s="";if(label==null){wm=(WindowManager)getSystemService(WINDOW_SERVICE);label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(22);label.setGravity(Gravity.CENTER);label.setBackgroundColor(0xCC111111);label.setPadding(20,12,20,12);
 params=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);params.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;params.y=110;
 label.setOnTouchListener(new View.OnTouchListener(){float downY;int originalY;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==0){downY=e.getRawY();originalY=params.y;return true;}if(e.getAction()==1||e.getAction()==2){params.y=Math.max(0,originalY-(int)(e.getRawY()-downY));wm.updateViewLayout(label,params);return true;}return false;}});
 wm.addView(label,params);}label.setText(s);return START_NOT_STICKY;}
 @Override public void onDestroy(){if(label!=null){wm.removeView(label);label=null;}super.onDestroy();}
}
