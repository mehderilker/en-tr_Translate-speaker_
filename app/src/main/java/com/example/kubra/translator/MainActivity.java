package com.example.kubra.translator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    TextToSpeech tts;
    private static final int RC_CHECKTTSDATA=100;
    ImageView bayrak1_iv,bayrak2_iv,ok_iv,ara_kelime_hop_iv,sonuc_hop_iv;
    EditText arananKelime_et;
    TextView sonuc_tv;
    String dilCifti="tr-en";
    Button ceviriButton;
    String arananKelime;
    String yandexKey="trnsl.1.1.20170826T124332Z.c7f36074597a666f.f831f314a08423422cd841afca69af8e4a869564";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, RC_CHECKTTSDATA);

        arananKelime_et= (EditText) findViewById(R.id.editTextArananKelime);
        sonuc_tv= (TextView) findViewById(R.id.textViewSonuc);
        ara_kelime_hop_iv= (ImageView) findViewById(R.id.imageViewKelimeDinle);
        sonuc_hop_iv= (ImageView) findViewById(R.id.imageViewSonucDinle);

        bayrak1_iv= (ImageView) findViewById(R.id.imageViewBayrak1);
        bayrak1_iv.setTag(R.drawable.turkish);
        bayrak2_iv= (ImageView) findViewById(R.id.imageViewBayrak2);
        bayrak2_iv.setTag(R.drawable.english);



        ok_iv= (ImageView) findViewById(R.id.imageViewOk);
        ok_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         dilCifti=yerDegistir(bayrak1_iv,bayrak2_iv);}
        });

    ceviriButton= (Button) findViewById(R.id.buttonCevir);
        ceviriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arananKelime=arananKelime_et.getText().toString();
                String query = null;
                try {
                    query = URLEncoder.encode(arananKelime, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String urlString="https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + yandexKey
                        + "&text=" + query + "&lang=" + dilCifti;
                new TranslatorBackgroundTask().execute(urlString);
            }
        });
        ara_kelime_hop_iv.setOnClickListener(new View.OnClickListener() {
          @Override
           public void onClick(View v) {
             setLanguage("aranan",dilCifti);
              speak(tts,arananKelime);
             }
         });
        sonuc_hop_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage("sonuç",dilCifti);
                speak(tts,sonuc_tv.getText().toString());
            }
        });


    }


    public String yerDegistir(ImageView bayrak1,ImageView bayrak2){
    String dilCifti = null;
    int bayrak1Tag=(Integer)bayrak1.getTag();
    int bayrak2Tag=(Integer)bayrak2.getTag();

    bayrak1.setImageResource(bayrak2Tag);
    bayrak1.setTag(bayrak2Tag);

    bayrak2.setImageResource(bayrak1Tag);
    bayrak2.setTag(bayrak1Tag);

    if((Integer)bayrak1.getTag()==R.drawable.turkish && (Integer)bayrak2.getTag()==R.drawable.english ) {
        dilCifti="tr-en";
    }
    else if((Integer)bayrak1.getTag()==R.drawable.english && (Integer)bayrak2.getTag()==R.drawable.turkish )
        {
        dilCifti="en-tr";

    }
        return dilCifti;
}


    class TranslatorBackgroundTask extends AsyncTask<String,Void,String> {
    @Override
protected String doInBackground(String... params) {
        String urlString=params[0];
        StringBuilder jsonString=new StringBuilder();
        try {
            URL yandexUrl=new URL(urlString);
            HttpURLConnection httpURLConnection= (HttpURLConnection) yandexUrl.openConnection();
            InputStream inputStream=httpURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                jsonString.append(line);
            }
            inputStream.close();
            bufferedReader.close();
            httpURLConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString.toString();
}
    @Override
    protected void onPostExecute(String json) {
        JsonObject jsonObject=new JsonParser().parse(json).getAsJsonObject();
        String sonuc=jsonObject.get("text").getAsString();
        sonuc_tv.setText(sonuc);
        Log.i("sonuc",sonuc);
    }



    }

    @Override
    public void onInit(int status) {
        if(status== TextToSpeech.SUCCESS){
            if(dilCifti.equals("en-tr")){
                tts.setLanguage(Locale.ENGLISH);}
            else {tts.setLanguage(new Locale("tr"));}
        }

        else{
            Toast.makeText(this, "Tts tanımlaması başarısız", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_CHECKTTSDATA){
            if(resultCode==TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                tts=new TextToSpeech(this,this);
            }
            else {
                Intent installIntent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public void speak(TextToSpeech tts,String text) {
        if(tts.isSpeaking()){
            tts.stop();
        }
        else{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                if(text!=""){
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null,null);
                }
                else {
                    Toast.makeText(this, "Kelime giriniz", Toast.LENGTH_SHORT).show();
                }

            }
            else{
                if(text!=""){
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null);}
                else{
                    Toast.makeText(this, "Kelime giriniz", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
    public void setLanguage(String flag,String dilCifti){
        if(flag.equals("aranan")){
            if(dilCifti.equals("en-tr")){
                tts.setLanguage(Locale.ENGLISH);
            }
            else  if(dilCifti.equals("tr-en")){
                tts.setLanguage(new Locale("tr"));
            }
        }

        else if(flag.equals("sonuç")){
            if(dilCifti.equals("en-tr")){
                tts.setLanguage(new Locale("tr"));
            }
            else if(dilCifti.equals("tr-en")){
                tts.setLanguage(Locale.ENGLISH);
            }
        }
    }

}

