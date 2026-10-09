package com.example.thaisuboverlay;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.nl.translate.*;
import java.nio.ByteBuffer;

public class ScreenOcrService extends Service {
    private static final String CHANNEL="capture_status";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private MediaProjection projection;
    private android.hardware.display.VirtualDisplay display;
    private ImageReader reader;
    private boolean finished=false;
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Screen OCR",NotificationManager.IMPORTANCE_LOW));
        Notification notification=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("ThaiSubOverlay: OCR test").setContentText("กำลังทดสอบอ่านข้อความบนหน้าจอหนึ่งครั้ง").build();
        try { startForeground(13,notification,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION); }
        catch(Exception e){stopSelf();return START_NOT_STICKY;}
        if(intent==null){stopSelf();return START_NOT_STICKY;}
        int result=intent.getIntExtra("resultCode",Activity.RESULT_CANCELED);
        Intent token;
        if(Build.VERSION.SDK_INT>=33) token=intent.getParcelableExtra("projectionData",Intent.class);
        else token=intent.getParcelableExtra("projectionData");
        if(token==null){showResult("ไม่พบสิทธิ์จับภาพ");return START_NOT_STICKY;}
        try {
            MediaProjectionManager manager=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
            projection=manager.getMediaProjection(result,token);
            projection.registerCallback(new MediaProjection.Callback(){@Override public void onStop(){cleanup();}},handler);
            DisplayMetrics dm=getResources().getDisplayMetrics();
            int width=dm.widthPixels,height=dm.heightPixels;
            reader=ImageReader.newInstance(width,height,PixelFormat.RGBA_8888,2);
            display=projection.createVirtualDisplay("ThaiSubOCR",width,height,dm.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
            handler.postDelayed(this::readFrame,3000);
        }catch(Exception e){showResult("จับภาพไม่สำเร็จ: "+e.getClass().getSimpleName());}
        return START_NOT_STICKY;
    }
    private void readFrame(){
        if(finished||reader==null)return;
        Image image=null;
        try {
            image=reader.acquireLatestImage();
            if(image==null){handler.postDelayed(this::readFrame,250);return;}
            Image.Plane plane=image.getPlanes()[0];
            ByteBuffer buffer=plane.getBuffer();
            int pixelStride=plane.getPixelStride(),rowStride=plane.getRowStride();
            int w=image.getWidth(),h=image.getHeight();
            Bitmap padded=Bitmap.createBitmap(w+(rowStride-pixelStride*w)/pixelStride,h,Bitmap.Config.ARGB_8888);
            padded.copyPixelsFromBuffer(buffer);
            Bitmap bitmap=Bitmap.createBitmap(padded,0,0,w,h);
            padded.recycle();
            image.close();image=null;
            // Only OCR the bottom 45% of the screenshot to focus on subtitles.
            int top=(int)(h*0.55f);
            Bitmap crop=Bitmap.createBitmap(bitmap,0,top,w,h-top);
            bitmap.recycle();
            var recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(InputImage.fromBitmap(crop,0)).addOnSuccessListener(text->{
                String english=text.getText().trim();
                recognizer.close();crop.recycle();
                if(english.isEmpty()){showResult("OCR ไม่พบข้อความด้านล่างจอ (อาจถูก DRM ป้องกัน)");return;}
                Translator translator=Translation.getClient(new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.THAI).build());
                translator.downloadModelIfNeeded().addOnSuccessListener(v->translator.translate(english)
                        .addOnSuccessListener(thai->{translator.close();showResult("OCR: "+english+"\n\nไทย: "+thai);})
                        .addOnFailureListener(e->{translator.close();showResult("OCR อ่านได้: "+english+"\nแปลไม่สำเร็จ");}))
                        .addOnFailureListener(e->{translator.close();showResult("OCR อ่านได้: "+english+"\nโมเดลไม่พร้อม");});
            }).addOnFailureListener(e->{recognizer.close();crop.recycle();showResult("OCR ไม่สำเร็จ: "+e.getMessage());});
        }catch(Exception e){showResult("เกิดข้อผิดพลาด: "+e.getClass().getSimpleName());}
        finally{if(image!=null)image.close();}
    }
    private void showResult(String s){
        if(finished)return;
        finished=true;
        cleanup();
        try{Intent overlay=new Intent(this,OverlayService.class);overlay.putExtra("subtitle",s);startService(overlay);}catch(Exception ignored){}
        stopSelf();
    }
    private void cleanup(){
        if(display!=null){display.release();display=null;}
        if(reader!=null){reader.close();reader=null;}
        if(projection!=null){MediaProjection p=projection;projection=null;p.stop();}
    }
    @Override public void onDestroy(){cleanup();super.onDestroy();}
}
