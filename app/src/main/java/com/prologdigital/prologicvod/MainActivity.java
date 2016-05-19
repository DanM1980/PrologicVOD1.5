package com.prologdigital.prologicvod;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.InputStream;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    UIAnimation uiAnimation = new UIAnimation();
    DataHandler dataHandler = new DataHandler();
    private static String urlJSON = "http://prologdigital.com/api/PrologApi/";
    private int[] coursesIds;
    private int currentCourseID;
    private String userEmail = "oded@prolog.co.il"; //default email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farme);

        loadSettings();

        //Loading JSON data in background
        Log.d("JSON", "Updating JSON...");
        new GetLessons().execute();

        //setting screen size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        uiAnimation.setLayerSize(displaymetrics, (LinearLayout)findViewById(R.id.mainLayout), (FrameLayout)findViewById(R.id.hover));

        //loading splash into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.hover);
        View splashView = LayoutInflater.from(this).inflate(R.layout.splash, null);
        inclusionViewGroup.addView(splashView);

        //splash timeout
        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uiAnimation.splashFade(findViewById(R.id.hover));
                            Log.d("SPLASH", "Splash hide");
                        }
                    });
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{

                }
            }
        };
        timerThread.start();
    }

    private void loadSettings(){
        //getting user gmail
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                userEmail = account.name;
            }
        }

        //loading courses id's from settings.xml
        Resources res = getResources();
        coursesIds = res.getIntArray(R.array.prologIDs);
        currentCourseID = coursesIds[0];

        //building JSON url
        urlJSON = urlJSON+currentCourseID+"?useremail="+userEmail;
    }

    private class GetLessons extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {}
        @Override
        protected Void doInBackground(Void... arg0) {
            WebRequest webreq = new WebRequest();
            dataHandler.jsonStr = webreq.getURL(urlJSON);
            dataHandler.parseJSON();
            dataHandler.parseCourse();
            /*
            for (int i=0; i<dataHandler.pdfUrls.size(); i++){
                webreq.savePDF(dataHandler.pdfUrls.get(i));
            }
            */
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            updateMainScreen();
        }

    }

    public void updateMainScreen(){
        String video_url = dataHandler.lessonsList.get(0).get(dataHandler.TAG_URL_CURRENT);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        video.setVideoURI(Uri.parse(video_url));
        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pauseVideo();
                return true;
            }
        });

        String thumbnailURL = dataHandler.lessonsList.get(0).get(dataHandler.TAG_VIMEO_PREVIEW_IMAGE);
        new DownloadImageTask((ImageView) findViewById(R.id.videoThumbnail)).execute(thumbnailURL);
    }

    public void showUpgrade(View view){
        //load upgrade.xml into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.hover);
        inclusionViewGroup.removeAllViews();
        View upgradeView = LayoutInflater.from(this).inflate(R.layout.upgrade, null);
        inclusionViewGroup.addView(upgradeView);


        //view animation
        uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), true);
        uiAnimation.moveLeft((LinearLayout) findViewById(R.id.mainLayout));
    }
    public void showPDF(View view){
        Toast.makeText(getApplicationContext(), ""+dataHandler.pdfUrls.get(0), Toast.LENGTH_SHORT).show();

        //load upgrade.xml into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.hover);
        inclusionViewGroup.removeAllViews();
        //View upgradeView = LayoutInflater.from(this).inflate(R.layout.upgrade, null);
        WebView pdfView=new WebView(this);
        pdfView.getSettings().setJavaScriptEnabled(true);
        pdfView.loadUrl(dataHandler.pdfUrls.get(0));
        LinearLayout.LayoutParams pdfViewParams = new LinearLayout.LayoutParams(500, 500);
        inclusionViewGroup.addView(pdfView, pdfViewParams);


        //view animation
        uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), true);
        uiAnimation.moveLeft((LinearLayout) findViewById(R.id.mainLayout));
    }
    public void showLesson(View view){
        //load lesson.xml into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.hover);
        inclusionViewGroup.removeAllViews();
        View upgradeView = LayoutInflater.from(this).inflate(R.layout.lesson, null);
        inclusionViewGroup.addView(upgradeView);

        //inflating lessons
        LinearLayout lessonsContainer = (LinearLayout)findViewById(R.id.lessonsContainer);
        Button lessonButtons[] = new Button[dataHandler.lessonsList.size()];
        for(int i=0; i<dataHandler.lessonsList.size(); i++){
            lessonButtons[i] = new Button(this);
            lessonButtons[i].setText(""+i);
            lessonsContainer.addView(lessonButtons[i]);
        }

        //view animation
        uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
        uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton)findViewById(R.id.lessonsButton));
    }
    public void showInfo(View view){
        //load course_info.xml into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.hover);
        inclusionViewGroup.removeAllViews();
        View upgradeView = LayoutInflater.from(this).inflate(R.layout.course_info, null);
        inclusionViewGroup.addView(upgradeView);

        //updating hover layout
        new DownloadImageTask((ImageView) findViewById(R.id.courseInfoImage)).execute(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_ICON));
        TextView courseInfoMakat = (TextView)findViewById(R.id.courseInfoMakat);
        courseInfoMakat.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_MAKAT));
        TextView courseInfoName = (TextView)findViewById(R.id.courseInfoName);
        courseInfoName.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_NAME));
        TextView courseInfoDetails = (TextView)findViewById(R.id.courseInfoDetails);
        courseInfoDetails.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_DESCRIPTION));

        //view animation
        uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
        uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton)findViewById(R.id.lessonsButton));
    }
    public void showCourses(View view){
        Toast.makeText(getApplicationContext(), "showCourses", Toast.LENGTH_SHORT).show();
    }

    public void playVideo(View view){
        ImageButton playButton = (ImageButton) findViewById(R.id.playVideoButton);
        playButton.setVisibility(View.GONE);
        ImageView videoThumbnail = (ImageView) findViewById(R.id.videoThumbnail);
        videoThumbnail.setVisibility(View.GONE);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        video.start();
    }
    public void pauseVideo(){
        VideoView video = (VideoView) findViewById(R.id.videoView);
        video.pause();
        ImageButton playButton = (ImageButton) findViewById(R.id.playVideoButton);
        playButton.setVisibility(View.VISIBLE);
        ImageView videoThumbnail = (ImageView) findViewById(R.id.videoThumbnail);
        videoThumbnail.setVisibility(View.VISIBLE);
    }

    public void getProduct(View view){
        Toast.makeText(getApplicationContext(), "getProduct", Toast.LENGTH_SHORT).show();
    }

    public String getUserEmail(){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getBaseContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return null;
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }}
