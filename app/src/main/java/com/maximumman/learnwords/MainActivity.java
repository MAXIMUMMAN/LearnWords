package com.maximumman.learnwords;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    ArrayList<Word> wordsData;
    TextView word,translate,timer;
    ImageView audio,settings,check;
    ProgressBar progressBar;
    SeekBar pitch,speechRate;
    CardView cardDialog;
    ArrayList<Integer> tempOrder;
    Random random;
    Word currentWord;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //needs to add in Thread
        wordsData = getWordsFromAssetFile(this);

        tempOrder = new ArrayList<>();
        for(int i = 0; i<wordsData.size(); i++) tempOrder.add(i);

        findViews();

        random = new Random();
        currentWord = getNewWord();

        word.setText(currentWord.getWord());
        translate.setText(currentWord.getTranslation());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                String[] time = timer.getText().toString().split(":");
                                int sec = Integer.parseInt(time[1]);
                                int min = Integer.parseInt(time[0]);
                                if (++sec > 59) {
                                    sec = 0;
                                    min++;
                                }


                               if(sec%20 == 0) {
                                   currentWord = getNewWord();
                                   word.setText(currentWord.getWord());
                                   translate.setText(currentWord.getTranslation());
                               }
                               if(sec<10)timer.setText(min + ":0" + sec); else timer.setText(min + ":" + sec);
                               progressBar.setProgress(sec%20);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                    tts.setPitch(0.6f);
                    tts.setSpeechRate(0.9f);
                }
            }
        });

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak(currentWord.getWord(),TextToSpeech.QUEUE_FLUSH, null);
            }
        });

       settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cardDialog.getAlpha() == 0)  cardDialog.animate().alpha(1).setDuration(200); else cardDialog.animate().alpha(0).setDuration(200);
            }
        });

       check.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               tts.setLanguage(Locale.UK);
               tts.setPitch((float) (pitch.getProgress()+1)/10);
               tts.setSpeechRate((float) (speechRate.getProgress()+1)/10);
               Toast.makeText(MainActivity.this, "Ok : pitch - " + (pitch.getProgress()+1)/10f + "\n speech rate - "+ (speechRate.getProgress()+1)/10f, Toast.LENGTH_SHORT).show();
           }
       });
    }

    @Override
    protected void onPause() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    Word getNewWord(){
        int key = random.nextInt(tempOrder.size());
        Word newWord = wordsData.get(tempOrder.get(key));
        tempOrder.remove(key);
        return newWord;
    }

    private void findViews(){
        word = findViewById(R.id.word);
        translate = findViewById(R.id.translate);
        timer = findViewById(R.id.timer);
        audio = findViewById(R.id.audio);
        settings = findViewById(R.id.settings);
        progressBar = findViewById(R.id.progressBar);
        check = findViewById(R.id.check);
        pitch = findViewById(R.id.pitch);
        speechRate = findViewById(R.id.speechRate);
        cardDialog = findViewById(R.id.cardDialog);
    }

    private ArrayList<Word> getWordsFromAssetFile(Activity activity)
    {
        ArrayList<Word> temp = new ArrayList<>();
        AssetManager am = activity.getAssets();

        try {
            Scanner in = new Scanner(am.open("words.eng"));
            String[] buf;
            while(in.hasNext()){
                buf = in.nextLine().split(" - ");
                temp.add(new Word(buf[0],buf[1]));
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }
}

class Word {
    private String word;
    private String translation;

    public Word(String word, String translation) {
        this.word = word;
        this.translation = translation;
    }

    public String getWord() {
        return word;
    }

    public String getTranslation() {
        return translation;
    }
}