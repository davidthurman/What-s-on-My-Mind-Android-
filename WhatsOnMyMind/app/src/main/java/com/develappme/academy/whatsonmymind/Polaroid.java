package com.develappme.academy.whatsonmymind;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class Polaroid extends AppCompatActivity implements SensorEventListener{

    private ImageView imageView;
    private String selectedImagePath;
    private TextView textView;
    private static final int SELECT_PICTURE = 1;
    final ArrayList<String> quotes = new ArrayList<>();
    //Vibrate variables
    Sensor accelerometer;
    SensorManager sm;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    int test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polaroid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);

        imageView = (ImageView) findViewById(R.id.currentImage);
        textView = (TextView) findViewById(R.id.quoteTxt);

        quotes.add("People who think they know everything are a great annoyance to those of us who do.");
        quotes.add("A woman's mind is cleaner than a man's: She changes it more often.");
        quotes.add("Do not take life too seriously. You will never get out of it alive.");
        quotes.add("Always remember that you are absolutely unique. Just like everyone else.");
        quotes.add("I believe that if life gives you lemons, you should make lemonade... And try to find somebody whose life has given them vodka, and have a party.");
        quotes.add("One advantage of talking to yourself is that you know at least somebody's listening.");
        quotes.add("Go to Heaven for the climate, Hell for the company.");
        quotes.add("I can resist everything except temptation.");
        quotes.add("I looked up my family tree and found out I was the sap.");
        quotes.add("Friendship is like peeing on yourself: everyone can see it, but only you get the warm feeling that it brings.");
        //10
        quotes.add("My therapist told me the way to achieve true inner peace is to finish what I start. So far I’ve finished two bags of M&Ms and a chocolate cake. I feel better already.");
        quotes.add("God gave us our relatives; thank God we can choose our friends.");
        quotes.add("People say that money is not the key to happiness, but I always figured if you have enough money, you can have a key made.");
        quotes.add("May your coffee be strong and your Monday be short.");
        quotes.add("I would challenge you to a battle of wits, but I see you are unarmed.");
        quotes.add("I put the pro in procrastinate.");
        quotes.add("We live in the era of smart phones and stupid people.");
        quotes.add("It’s all fun and games, until someone calls the cops. Then it’s a new game; hide and seek.");
        quotes.add("I’m gonna go take a hot shower. It’s like a normal shower, but with me in it.");
        quotes.add("My life is all math. I am trying to add to my income, subtract from my weight, divide my time, and avoid multiplying.");
        //20
        quotes.add("Patience is something you admire in the driver behind you, but not in the one ahead.");
        quotes.add("I’m so broke, I can’t even pay attention.");
        quotes.add("A good friend will help you move, a best friend will help you move a dead body.");
        quotes.add("Tomorrow is often the busiest day of the week.");
        quotes.add("Anything that is unrelated to elephants is irrelephant.");
        quotes.add("You know, somebody actually complimented me on my driving today. They left a little note on the windscreen, it said ‘Parking Fine.");
        quotes.add("I am sorry for those that disagree with me because I know that they are wrong.");
        quotes.add("I stay up late every night and realize it’s a bad idea every morning.");
        quotes.add("Wouldn’t it be nice if the world was flat? That way we could just push off the people we don’t like.");
        quotes.add("A best friend is like a four leaf clover, hard to find, lucky to have. - http://coolfunnyquotes.com ");
        //30
        quotes.add("God please give me patience, if you give me strength I will just punch them in the face.");
        quotes.add("All my life I thought air was free, until I bought a bag of chips.");
        quotes.add("In the morning you beg to sleep more, in the afternoon you are dying to sleep, and at night you refuse to sleep.");
        quotes.add("I consider myself a crayon, I might not be your favorite color but one day you'll need me to complete your picture.");
        quotes.add("Life isn't measured by the number of breaths you take, but by the number of moments that take your breath away.");
        quotes.add("When I said that I cleaned my room, I just meant I made a path from the doorway to my bed.");
        quotes.add("I love everyone! I love to be around some people, I love to stay away from others, and some I'd just love to punch right in the face!");
        quotes.add("The great pleasure in life is doing what people say you cannot do.");
        quotes.add("I'm old enough to know better, but young enough to do it anyway.");
        quotes.add("I love being married. It's so great to find that one special person you want to annoy for the rest of your life.");
        //40
        quotes.add("If we were on a sinking ship, and there was only one life vest... I would miss you so much.");
        quotes.add("Stop worrying about the world ending today. It's already tomorrow in Australia.");
        quotes.add("Long time ago I used to have a life, until someone told me to create a Facebook account.");
        quotes.add("I don't need a psychiatrist to prod into my personal life and make me tell them all my secrets, I have my friends for that.");
        quotes.add("The most important thing in life is not knowing everything, it's having the phone number of somebody who does!");
        quotes.add("You come into the world with nothing, and the purpose of your life is to make something out of nothing.");
        quotes.add("There's life without Facebook and Internet? Really? Send me the link.");
        quotes.add("I look to the future because that's where I'm going to spend the rest of my life.");
        quotes.add("The best things in life are free. The rest are too expensive.");
        quotes.add("I never made a mistake in my life. I thought I did once, but I was wrong.");
        //50

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sm.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        System.out.print("NUMBER: 1");
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        System.out.println("NUMBER: 2");
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                Random rand = new Random();
                final int randomNumber = rand.nextInt(49); // 0-9.
                System.out.println("NUMBER: 3");

                handleShakeEvent(count, randomNumber);
            }
        });


        final Button saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtn.setVisibility(View.GONE);
                System.out.println("Saving");
                View content = findViewById(R.id.rlid);
                content.setDrawingCacheEnabled(true);
                Bitmap bitmap = content.getDrawingCache();
                File file = new File("/sdcard/Pictures/" + "Polaroid" + ".png");
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
                    ostream.close();
                    content.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    content.setDrawingCacheEnabled(false);
                }

            }
        });







    }

    private void handleShakeEvent(int count, int randomNumber) {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
        System.out.println("NUMBER: 4");
        test = animate(textView, test);
        System.out.println("Success");
        textView.setText("\"" +quotes.get(randomNumber) + "\"");

        return;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                //System.out.println("Image Path : " + selectedImagePath);
                imageView.setImageURI(selectedImageUri);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public int animate(TextView x, int y){

        System.out.println("Animating!");


        if (y == 1) {
            x.animate().rotation(360).setDuration(300);

            y = 2;

            System.out.println("Testing 1");

        }
        else{
            x.animate().rotation(-360).setDuration(300);

            y = 1;
            System.out.println("Testing 2");
        }
        return y;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}
