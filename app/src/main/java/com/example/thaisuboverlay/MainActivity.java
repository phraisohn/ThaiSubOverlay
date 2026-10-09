package com.example.thaisuboverlay;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.Settings;import android.view.*;import android.widget.*;
import com.google.mlkit.nl.translate.*;
public class MainActivity extends Activity {
 EditText input; TextView status; Translator translator;
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(28,32,28,20);
 TextView h=new TextView(this);h.setText("ThaiSub Overlay • Prototype\nแปลข้อความอังกฤษเป็นไทยและแสดงซับลอย");h.setTextSize(21);l.addView(h);
 input=new EditText(this);input.setHint("Paste English subtitle here");input.setMinLines(2);l.addView(input);
 status=new TextView(this);status.setText("เริ่มต้น: ดาวน์โหลดโมเดลภาษาเมื่อเชื่อมต่ออินเทอร์เน็ต");l.addView(status);
 Button permission=new Button(this);permission.setText("1. อนุญาตแสดงทับแอปอื่น");l.addView(permission);permission.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()));startActivity(i);}});
 Button translate=new Button(this);translate.setText("2. แปลและแสดงซับลอย");l.addView(translate);translate.setOnClickListener(v->runTranslation());
 Button stop=new Button(this);stop.setText("หยุดแสดงซับ");l.addView(stop);stop.setOnClickListener(v->stopService(new Intent(this,OverlayService.class)));
 TextView note=new TextView(this);note.setText("เวอร์ชันนี้รับข้อความที่วางด้วยตนเอง ยังไม่อ่านซับหรือเสียงจาก Netflix อัตโนมัติ");l.addView(note);setContentView(l);
 TranslatorOptions options=new TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.THAI).build();translator=Translation.getClient(options);
 translator.downloadModelIfNeeded().addOnSuccessListener(x->status.setText("โมเดลพร้อมแปล (offline)")).addOnFailureListener(e->status.setText("ดาวน์โหลดโมเดลไม่สำเร็จ: "+e.getMessage())); }
 void runTranslation(){String s=input.getText().toString().trim();if(s.isEmpty()){status.setText("กรอกข้อความก่อน");return;}if(!Settings.canDrawOverlays(this)){status.setText("กรุณาอนุญาต overlay ก่อน");return;}status.setText("กำลังแปล...");translator.translate(s).addOnSuccessListener(t->{status.setText("แปลสำเร็จ");Intent i=new Intent(this,OverlayService.class);i.putExtra("subtitle",t);startService(i);}).addOnFailureListener(e->status.setText("แปลไม่สำเร็จ: "+e.getMessage())); }
 @Override public void onDestroy(){if(translator!=null)translator.close();super.onDestroy();}
}
