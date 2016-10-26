package com.prologdigital.prologvod;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    UIAnimation uiAnimation = new UIAnimation();
    DataHandler dataHandler = new DataHandler();

    private static String urlBaseCoursesJSON = "http://prologdigital.com/api/PrologApi/DigitalProductMainDetails/";
    private static String urlCoursesJSON = "http://prologdigital.com/api/PrologApi/DigitalProductMainDetails/";
    private boolean fullAppPurched = false;
    private int[] coursesIds;
    private int currentCourseID;
    private int selectedCourseId;
    private int currentPdfNumber = 0;

    private static String urlBaseJSON = "http://prologdigital.com/api/PrologApi/";
    private static String urlJSON = "http://prologdigital.com/api/PrologApi/";
    private int currentLesson = 0;
    private String userEmail = "oded@prolog.co.il"; //default email

    IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            Log.d("inapp billing", "inapp billing");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farme);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!isNetworkAvailable()) {
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
            errorDialog.setTitle("Error");
            errorDialog.setMessage(getString(R.string.noInternet));
            errorDialog.setPositiveButton(getString(R.string.noInternetOK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            errorDialog.show();
        } else {
            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            bindService(serviceIntent, mServiceConn, BIND_AUTO_CREATE);

            loadSettings();

            //Loading JSON data in background
            new GetLessons().execute();

            //setting screen size
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            uiAnimation.setLayerSize(displaymetrics, (LinearLayout) findViewById(R.id.mainLayout), true);
            //uiAnimation.setLayerSize(displaymetrics, (LinearLayout)findViewById(R.id.pdfLayout), true);
            uiAnimation.setLayerSize(displaymetrics, (LinearLayout) findViewById(R.id.videoLayout), false);

            //loading splash into hover layout
            ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.hover);
            View splashView = LayoutInflater.from(this).inflate(R.layout.splash, null);
            inclusionViewGroup.addView(splashView);

            //splash timeout
            Thread timerThread = new Thread() {
                public void run() {
                    try {
                        sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uiAnimation.splashFade(findViewById(R.id.hover));
                                Log.d("SPLASH", "Splash hide");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {

                    }
                }
            };
            timerThread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        WebView videoWebView = (WebView) findViewById(R.id.videoWebView);
        videoWebView.loadUrl("about:blank");
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        if (!resetMainScreen())
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

    public boolean resetMainScreen() {
        boolean response = false;
        if (uiAnimation.animXdirection < 0) {
            response = true;
            //getting hover frame width
            View hoverView = findViewById(R.id.hover);
            hoverView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int marginWidth = hoverView.getMeasuredWidth();

            //view animation
            ImageButton upgradeButton;
            if (fullAppPurched) {
                upgradeButton = (ImageButton) findViewById(R.id.purchedIcon);
            } else {
                upgradeButton = (ImageButton) findViewById(R.id.UpgradeButton);
            }
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), true);
            uiAnimation.moveLeft((LinearLayout) findViewById(R.id.mainLayout), upgradeButton, (ImageButton) findViewById(R.id.UpgradeCloseButton), marginWidth);
        }

        if (uiAnimation.animYdirection < 0) {
            response = true;
            //getting hover frame height
            View hoverView = findViewById(R.id.hover);
            hoverView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int marginHeight = hoverView.getMeasuredHeight();

            //view animation
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
            uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton) findViewById(R.id.showLessonsButton), marginHeight);

            ((ImageButton) findViewById(R.id.UpgradeButton)).setEnabled((uiAnimation.animYdirection < 0));
            ((ImageButton) findViewById(R.id.pdfButton)).setEnabled((uiAnimation.animYdirection < 0));
        }

        if (!uiAnimation.videoPositionCenter) {
            response = true;
            uiAnimation.moveVideo((LinearLayout) findViewById(R.id.videoLayout), (LinearLayout) findViewById(R.id.lessonTextContainerLayout), false);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!uiAnimation.videoPositionCenter) {
                    uiAnimation.moveVideo((LinearLayout) findViewById(R.id.videoLayout), (LinearLayout) findViewById(R.id.lessonTextContainerLayout), false);
                }
            }
        }, 1600);

        if (uiAnimation.videoFullscreen) {
            response = true;
            videoFullscreen(new View(this));
        }

        if (uiAnimation.pdfViewerOpen) {
            response = true;
            if (uiAnimation.pdfViewerFullscreen) fullscreenPDF(new View(this));
            else showPDF(new View(this));
        }

        LinearLayout additionalCourses = (LinearLayout) findViewById(R.id.additionalCourses);
        if (additionalCourses != null) {
            response = true;
            hideCourses(new View(this));
        }

        return response;
    }

    private void loadSettings() {
        userEmail = (getUserEmail() != null) ? getUserEmail() : userEmail;

        //loading courses id's from settings.xml
        Resources res = getResources();
        coursesIds = res.getIntArray(R.array.prologIDs);
        int defaultCourseID = res.getInteger(R.integer.defaultCourseId);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        currentCourseID = sharedPreferences.getInt("currentCourseID", defaultCourseID);
        fullAppPurched = sharedPreferences.getBoolean("fullApp", false);

        //building JSON url
        urlJSON = urlBaseJSON + currentCourseID + "?useremail=" + userEmail;

        //building Courses JSON url
        urlCoursesJSON = urlBaseCoursesJSON;
        for (int i = 0; i < coursesIds.length; i++) {
            urlCoursesJSON += coursesIds[i];
            if ((i + 1) < coursesIds.length) urlCoursesJSON += ",";
        }

        //changing screen direction
        if (res.getBoolean(R.bool.RTL)) {
            LinearLayout lessonTextLayout = (LinearLayout) findViewById(R.id.lessonTextLayout);
            lessonTextLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    private class GetLessons extends AsyncTask<Void, Void, Void> {
        WebView videoView = (WebView) findViewById(R.id.videoWebView);

        @Override
        protected void onPreExecute() {
            videoView.setVisibility(View.GONE);
            disableEnableControls(false, (ViewGroup) findViewById(R.id.mainLayout));
            uiAnimation.fadeLoading((LinearLayout) findViewById(R.id.loadingLayout), true);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            WebRequest webreq = new WebRequest();
            dataHandler.jsonStr = webreq.getURL(urlJSON);
            dataHandler.jsonCoursesStr = webreq.getURL(urlCoursesJSON);

            dataHandler.jsonCoursesStr = dataHandler.jsonCoursesStr.replace(":{", ": [{");
            dataHandler.jsonCoursesStr = dataHandler.jsonCoursesStr.replace("},", "}],");
            dataHandler.jsonCoursesStr = dataHandler.jsonCoursesStr.replace("}}", "}]}");

            dataHandler.parseJSON();
            dataHandler.parseCourse();
            dataHandler.parseCourses(coursesIds);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            videoView.setVisibility(View.VISIBLE);
            updateMainScreen();
            uiAnimation.fadeLoading((LinearLayout) findViewById(R.id.loadingLayout), false);
            disableEnableControls(true, (ViewGroup) findViewById(R.id.mainLayout));
        }

    }

    public void updateMainScreen() {
        Resources res = getResources();
        if (res.getBoolean(R.bool.fullApp)) fullAppPurched = true;
        else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            fullAppPurched = sharedPreferences.getBoolean("fullapp_" + currentCourseID, false);
        }

        updateVideo();
        TextView lessonName = (TextView) findViewById(R.id.lessonName);
        lessonName.setText(dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_NAME));
        TextView lessonDescription = (TextView) findViewById(R.id.lessonDescription);
        lessonDescription.setText(dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_DESCRIPTION));

        ImageButton UpgradeButton = (ImageButton) findViewById(R.id.UpgradeButton);
        ImageButton purchedIcon = (ImageButton) findViewById(R.id.purchedIcon);
        Log.d("fullAppPurched", "Checking Upgrade");
        if (fullAppPurched) {
            Log.d("fullAppPurched", "Hiding Upgrade");
            UpgradeButton.setVisibility(View.GONE);
            purchedIcon.setVisibility(View.VISIBLE);
        } else {
            Log.d("fullAppPurched", "Showing Upgrade");
            UpgradeButton.setVisibility(View.VISIBLE);
            purchedIcon.setVisibility(View.GONE);
        }
    }

    public void updateVideo() {
        ImageView videoThumbnail = (ImageView) findViewById(R.id.videoThumbnail);
        new DownloadImageTask((ImageView) findViewById(R.id.videoThumbnail)).execute(dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_VIMEO_PREVIEW_IMAGE));
        videoThumbnail.setVisibility(View.VISIBLE);

        if (dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_IS_FREE).equals("true") || fullAppPurched) {
            //enable "on air" animation
            final ImageView onAir = (ImageView) findViewById(R.id.videoOnAir);
            onAir.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse);
            onAir.startAnimation(animation);

            //hiding basket button
            FrameLayout buyFromThumbFrame = (FrameLayout) findViewById(R.id.buyFromThumbFrame);
            buyFromThumbFrame.setVisibility(View.GONE);

            //showing fullscreen button
            FrameLayout videoFullscreen = (FrameLayout) findViewById(R.id.videoFullscreenFrame);
            videoFullscreen.setVisibility(View.VISIBLE);

            //setting video height by width ratio
            FrameLayout videoFrame = (FrameLayout) findViewById(R.id.videoFrameHeight);
            ViewGroup.LayoutParams layout = videoFrame.getLayoutParams();
            layout.height = (int) Math.round(videoFrame.getWidth() / 1.333333333333333);
            videoFrame.setLayoutParams(layout);

            //updating webview settings
            WebView videoView = (WebView) findViewById(R.id.videoWebView);
            String newUA = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; Android SDK built for x86 Build/MASTER; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/44.0.2403.119 Safari/537.36";
            videoView.getSettings().setUserAgentString(newUA);
            videoView.getSettings().setJavaScriptEnabled(true);
            videoView.getSettings().setAllowFileAccess(true);
            videoView.getSettings().setAppCacheEnabled(true);
            videoView.getSettings().setDomStorageEnabled(true);
            videoView.getSettings().setPluginState(WebSettings.PluginState.OFF);
            videoView.getSettings().setAllowFileAccess(true);
            videoView.setWebViewClient(new WebViewClient());

            //building HTML
            String videoURL = dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_URL_CURRENT);
            if (!videoURL.contains("/video/")) {
                videoURL = videoURL.replace("https://player.vimeo.com/", "https://player.vimeo.com/video/");
            }
            Log.d("VideoURL", videoURL);
            String iframe = "<html>\n" +
                    "\t<head></head>\n" +
                    "\t<body style=\"margin:0; width:100%; height: 100%;\" onresize=\"resizeIframe()\">\n" +
                    "\t\t<iframe src=\"" + videoURL + "\" width=\"640px\" height=\"480px\" frameborder=\"0\" id=\"videoFrame\" onLoad=\"resizeIframe()\"></iframe>\n" +
                    "\t</body>\n" +
                    "</html>\n" +
                    "<script type=\"text/javascript\">\n" +
                    "function resizeIframe(e){\n" +
                    "\tdocument.getElementById('videoFrame').style.height=window.outerHeight+\"px\";\n" +
                    "\tdocument.getElementById('videoFrame').style.width=window.outerWidth+\"px\";\n" +
                    "}\n" +
                    "</script>\n";

            //finish loading listner
            videoView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    ImageView videoThumbnail = (ImageView) findViewById(R.id.videoThumbnail);
                    videoThumbnail.setVisibility(View.GONE);
                    onAir.clearAnimation();
                    onAir.setVisibility(View.GONE);
                }
            });
            videoView.loadData(iframe, "text/html; charset=UTF-8", null);
            //videoView.loadUrl(dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_URL_CURRENT));
        } else {
            //disabling video
            WebView videoView = (WebView) findViewById(R.id.videoWebView);
            videoView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    ImageView videoThumbnail = (ImageView) findViewById(R.id.videoThumbnail);
                    videoThumbnail.setVisibility(View.VISIBLE);
                }
            });
            videoView.loadUrl("about:blank");
            //videoView.setVisibility(View.GONE);

            //showing basket button
            FrameLayout buyFromThumbFrame = (FrameLayout) findViewById(R.id.buyFromThumbFrame);
            buyFromThumbFrame.setVisibility(View.VISIBLE);

            //hiding fullscreen button
            FrameLayout videoFullscreen = (FrameLayout) findViewById(R.id.videoFullscreenFrame);
            videoFullscreen.setVisibility(View.GONE);
        }
    }

    public void showUpgrade(View view) {
        View upgradeView;
        resetMainScreen();
        ImageButton pdfButton = (ImageButton) findViewById(R.id.pdfButton);

        if (uiAnimation.animXdirection > 0) {
            //load upgrade.xml into hover layout
            ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.hover);
            inclusionViewGroup.removeAllViews();
            upgradeView = LayoutInflater.from(this).inflate(R.layout.upgrade, null);
            inclusionViewGroup.addView(upgradeView);

            //updating upgrade layout
            new DownloadImageTask((ImageView) findViewById(R.id.upgradeCourseImage)).execute(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_ICON));
            TextView upgradeCoursePrice = (TextView) findViewById(R.id.upgradeCoursePrice);
            String coursePrice = "";
            if (mService != null) {
                Log.d("inapp billing", "mService not null");
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add("fullapp_" + currentCourseID);
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                try {
                    Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
                    int response = skuDetails.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                        for (String thisResponse : responseList) {
                            JSONObject object = new JSONObject(thisResponse);
                            String sku = object.getString("productId");
                            String price = object.getString("price");
                            if (sku.equals("fullapp_" + currentCourseID)) coursePrice = price;
                        }
                    }
                } catch (RemoteException e) {

                } catch (JSONException e) {

                }
            } else Log.d("inapp billing", "mService IS null");
            upgradeCoursePrice.setText(getString(R.string.wouldLikeUpgrade) + " " + coursePrice + "?");
            TextView upgradeCourseName = (TextView) findViewById(R.id.upgradeCourseName);
            upgradeCourseName.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_NAME));
            TextView upgradeCourseText = (TextView) findViewById(R.id.upgradeCourseText);
            upgradeCourseText.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_PURCHASE_TEXT));
            UIAnimation.fadeButton(pdfButton, true);
        } else {
            upgradeView = (View) findViewById(R.id.hover);
            UIAnimation.fadeButton(pdfButton, false);
        }

        //getting hover frame width
        upgradeView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int marginWidth = upgradeView.getMeasuredWidth();

        //view animation
        ImageButton upgradeButton;
        if (fullAppPurched) {
            upgradeButton = (ImageButton) findViewById(R.id.purchedIcon);
        } else {
            upgradeButton = (ImageButton) findViewById(R.id.UpgradeButton);
        }
        uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), true);
        uiAnimation.moveLeft((LinearLayout) findViewById(R.id.mainLayout), upgradeButton, (ImageButton) findViewById(R.id.UpgradeCloseButton), marginWidth);
    }

    public void showPDF(View view) {
        LinearLayout pdfLayout = (LinearLayout) findViewById(R.id.pdfLayout);
        ImageButton upgradeButton = (ImageButton) findViewById(R.id.UpgradeButton);
        ImageButton purchedIcon = (ImageButton) findViewById(R.id.purchedIcon);
        ImageButton showLessonsButton = (ImageButton) findViewById(R.id.showLessonsButton);
        ImageButton hideLessonsButton = (ImageButton) findViewById(R.id.hideLessonsButton);
        ImageButton upgradeCloseButton = (ImageButton) findViewById(R.id.UpgradeCloseButton);
        ImageButton pdfNextButton = (ImageButton) findViewById(R.id.pdfNext);
        ImageButton infoButton = (ImageButton) findViewById(R.id.infoButton);
        ImageButton appsButton = (ImageButton) findViewById(R.id.appsButton);

        if (uiAnimation.pdfViewerOpen) {
            Log.d("pdf", "CLOSE");
            //showing right menu
            findViewById(R.id.rightMenu).setVisibility(View.VISIBLE);

            //changing menu buttons
            UIAnimation.fadeButton(showLessonsButton, false);
            UIAnimation.fadeButton(hideLessonsButton, false);
            if (fullAppPurched) UIAnimation.fadeButton(purchedIcon, false);
            else UIAnimation.fadeButton(upgradeButton, false);
            UIAnimation.fadeButton(pdfNextButton, true);
            UIAnimation.fadeButton(infoButton, false);
            UIAnimation.fadeButton(appsButton, false);

            //view animation
            uiAnimation.scrollPDF(pdfLayout, "close");
            uiAnimation.pdfViewerOpen = false;
        } else {
            Log.d("pdf", "OPEN");

            resetMainScreen();

            //show loading over
            final LinearLayout loadingPDFLayout = (LinearLayout) findViewById(R.id.loadingPDFLayout);
            loadingPDFLayout.setVisibility(View.VISIBLE);

            //view animation
            uiAnimation.scrollPDF(pdfLayout, "open");
            uiAnimation.pdfViewerOpen = true;

            //changing menu buttons
            if (fullAppPurched) UIAnimation.fadeButton(purchedIcon, true);
            else UIAnimation.fadeButton(upgradeButton, true);
            UIAnimation.fadeButton(showLessonsButton, true);
            UIAnimation.fadeButton(hideLessonsButton, true);
            UIAnimation.fadeButton(pdfNextButton, false);
            UIAnimation.fadeButton(infoButton, true);
            UIAnimation.fadeButton(appsButton, true);
            upgradeCloseButton.setVisibility(View.GONE);

            //loading PDF into webview
            WebView pdfViewr = (WebView) findViewById(R.id.webViewPDF);
            pdfViewr.getSettings().setJavaScriptEnabled(true);
            pdfViewr.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            pdfViewr.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    loadingPDFLayout.setVisibility(View.INVISIBLE);
                }
            });
            pdfViewr.loadUrl("https://docs.google.com/viewer?url=" + dataHandler.pdfUrls.get(currentPdfNumber));
            pdfLayout.setVisibility(View.VISIBLE);
        }
    }

    public void fullscreenPDF(View view) {
        ImageButton fullscreenPDF = (ImageButton) findViewById(R.id.fullscreenPDF);
        if (uiAnimation.pdfViewerFullscreen) {
            disableEnableControls(true, (ViewGroup) findViewById(R.id.mainLayout));
            uiAnimation.scrollPDF((LinearLayout) findViewById(R.id.pdfLayout), "middle");
            uiAnimation.pdfViewerFullscreen = false;
            fullscreenPDF.setImageResource(R.drawable.fullscreen_icon);

        } else {
            disableEnableControls(false, (ViewGroup) findViewById(R.id.mainLayout));
            uiAnimation.scrollPDF((LinearLayout) findViewById(R.id.pdfLayout), "fullscreen");
            uiAnimation.pdfViewerFullscreen = true;
            fullscreenPDF.setImageResource(R.drawable.fullscreen_exit_icon);
        }
    }

    public void sharePDF(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, dataHandler.pdfUrls.get(currentPdfNumber));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this site!");
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void pdfNext(View view) {
        if (dataHandler.pdfUrls.size() > (currentPdfNumber + 1)) currentPdfNumber++;
        else currentPdfNumber = 0;

        //show loading over
        final LinearLayout loadingPDFLayout = (LinearLayout) findViewById(R.id.loadingPDFLayout);
        loadingPDFLayout.setVisibility(View.VISIBLE);

        //loading PDF into webview
        WebView pdfViewr = (WebView) findViewById(R.id.webViewPDF);
        pdfViewr.getSettings().setJavaScriptEnabled(true);
        pdfViewr.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        pdfViewr.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                loadingPDFLayout.setVisibility(View.INVISIBLE);
            }
        });
        pdfViewr.loadUrl("https://docs.google.com/viewer?url=" + dataHandler.pdfUrls.get(currentPdfNumber));
    }

    static class lessonButton {
        LinearLayout lessonButtonLayout;
        TextView lessonName;
        FrameLayout presentFrame;
    }

    private ArrayList<lessonButton> lessonsButtons = new ArrayList<>(dataHandler.lessonsList.size());

    public void showLesson(View view) {
        //showing hiding menu buttons
        if (fullAppPurched)
            UIAnimation.fadeButton((ImageButton) findViewById(R.id.purchedIcon), uiAnimation.animYdirection > 0);
        else
            UIAnimation.fadeButton((ImageButton) findViewById(R.id.UpgradeButton), uiAnimation.animYdirection > 0);
        UIAnimation.fadeButton((ImageButton) findViewById(R.id.pdfButton), uiAnimation.animYdirection > 0);
        UIAnimation.fadeButton((ImageButton) findViewById(R.id.infoButton), uiAnimation.animYdirection > 0);
        UIAnimation.fadeButton((ImageButton) findViewById(R.id.appsButton), uiAnimation.animYdirection > 0);

        //hiding PDF
        if (uiAnimation.pdfViewerOpen) showPDF(view);

        if (uiAnimation.animXdirection > 0) {
            if (uiAnimation.animYdirection > 0) updateLessonLayout();

            //getting hover frame height
            View lessonsView = (View) findViewById(R.id.hover);
            lessonsView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int marginHeight = lessonsView.getMeasuredHeight();

            //view animation
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
            uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton) findViewById(R.id.showLessonsButton), marginHeight);
        }
    }

    public void updateLessonLayout() {
        //load lesson.xml into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.hover);
        inclusionViewGroup.removeAllViews();
        View lessonsView = LayoutInflater.from(this).inflate(R.layout.lesson, null);
        inclusionViewGroup.addView(lessonsView);

        TextView selectItemText = (TextView) findViewById(R.id.selectItemText);
        TextView itemNotAviableText = (TextView) findViewById(R.id.itemNotAviableText);
        if (dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_IS_FREE).equals("false") && !fullAppPurched) {
            selectItemText.setVisibility(View.INVISIBLE);
            itemNotAviableText.setVisibility(View.VISIBLE);
        } else {
            selectItemText.setVisibility(View.VISIBLE);
            itemNotAviableText.setVisibility(View.INVISIBLE);
        }

        //inflating lessons
        int lessonsLines = (int) Math.ceil(dataHandler.lessonsList.size() / 5 + 1);
        LinearLayout lessonsContainer = (LinearLayout) findViewById(R.id.lessonsContainer);
        LinearLayout lessonsLine[] = new LinearLayout[lessonsLines];
        for (int i = 0; i < lessonsLines; i++) {
            lessonsLine[i] = new LinearLayout(this);
            lessonsLine[i].setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            lessonsLine[i].setLayoutParams(params);
            lessonsContainer.addView(lessonsLine[i]);
        }

        lessonsButtons = new ArrayList<lessonButton>(dataHandler.lessonsList.size());
        int buttonWidth = (Math.round(uiAnimation.screenWidth / 6));
        int buttonHeight = (Math.round(buttonWidth / 3) * 2);
        int buttonMargin = 1;//(Math.round(buttonWidth/8));

        for (int i = 0; i < dataHandler.lessonsList.size(); i++) {
            int line = (int) Math.floor(i / 5);
            View lessonButtonView = LayoutInflater.from(this).inflate(R.layout.lesson_button, null);
            LinearLayout.LayoutParams lessonButtonParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
            lessonButtonParams.setMargins(buttonMargin, buttonMargin, buttonMargin, buttonMargin);
            lessonButtonView.setOnClickListener(updateLesson(lessonButtonView, i));
            lessonsLine[line].addView(lessonButtonView, lessonButtonParams);

            lessonButton lessonButton = new lessonButton();
            lessonButton.lessonButtonLayout = (LinearLayout) lessonButtonView.findViewById(R.id.lessonButtonLayout);
            lessonButton.lessonName = (TextView) lessonButtonView.findViewById(R.id.lessonName);
            lessonButton.presentFrame = (FrameLayout) lessonButtonView.findViewById(R.id.presentFrame);

            if (currentLesson == i)
                lessonButton.lessonButtonLayout.setBackgroundColor(Color.parseColor("#C0C0C0"));
            lessonButton.lessonName.setText(dataHandler.lessonsList.get(i).get(dataHandler.TAG_NAME));
            if (dataHandler.lessonsList.get(i).get(dataHandler.TAG_IS_FREE).equals("false") || fullAppPurched)
                lessonButton.presentFrame.setBackgroundResource(0);

            lessonsButtons.add(lessonButton);
        }

        //changing height to max height
        ScrollView lessonButtonsScrollView = (ScrollView) findViewById(R.id.lessonButtonsScrollView);
        lessonButtonsScrollView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int currentHeight = lessonButtonsScrollView.getMeasuredHeight();
        int maxHeight = Math.round(uiAnimation.screenHeight / 3) - 100;
        if (currentHeight > maxHeight) lessonButtonsScrollView.getLayoutParams().height = maxHeight;
    }

    public void showInfo(View view) {
        View infoView;
        if (uiAnimation.pdfViewerOpen) showPDF(view);
        //showing hiding menu buttons
        if (fullAppPurched)
            UIAnimation.fadeButton((ImageButton) findViewById(R.id.purchedIcon), uiAnimation.animYdirection > 0);
        else
            UIAnimation.fadeButton((ImageButton) findViewById(R.id.UpgradeButton), uiAnimation.animYdirection > 0);
        UIAnimation.fadeButton((ImageButton) findViewById(R.id.pdfButton), uiAnimation.animYdirection > 0);
        UIAnimation.fadeButton((ImageButton) findViewById(R.id.infoButton), uiAnimation.animYdirection > 0);
        UIAnimation.fadeButton((ImageButton) findViewById(R.id.appsButton), uiAnimation.animYdirection > 0);

        if (uiAnimation.animYdirection > 0) {
            //load course_info.xml into hover layout
            ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.hover);
            //inclusionViewGroup.setVisibility(View.VISIBLE);
            inclusionViewGroup.removeAllViews();
            infoView = LayoutInflater.from(this).inflate(R.layout.course_info, null);
            inclusionViewGroup.addView(infoView);

            //updating hover layout
            new DownloadImageTask((ImageView) findViewById(R.id.courseInfoImage)).execute(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_ICON));
            TextView courseInfoMakat = (TextView) findViewById(R.id.courseInfoMakat);
            courseInfoMakat.setText("#" + dataHandler.courseInfo.get(dataHandler.TAG_COURSE_MAKAT));
            TextView courseInfoName = (TextView) findViewById(R.id.courseInfoName);
            courseInfoName.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_NAME));
            TextView courseInfoDetails = (TextView) findViewById(R.id.courseInfoDetails);
            courseInfoDetails.setText(dataHandler.courseInfo.get(dataHandler.TAG_COURSE_DESCRIPTION));
        } else infoView = (View) findViewById(R.id.hover);

        //getting hover frame width
        infoView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int marginHeight = infoView.getMeasuredHeight();

        //view animation
        uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
        uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton) findViewById(R.id.showLessonsButton), marginHeight);

        ((ImageButton) findViewById(R.id.UpgradeButton)).setEnabled((uiAnimation.animYdirection < 0));
        ((ImageButton) findViewById(R.id.pdfButton)).setEnabled((uiAnimation.animYdirection < 0));
    }

    static class courseButton {
        ImageView courseRadio;
        ImageView purchesedIcon;
        ImageView courseIcon;
        TextView courseName;
    }

    public void showCourses(View view) {
        disableEnableControls(uiAnimation.animYdirection < 0, (ViewGroup) findViewById(R.id.mainLayout));
        if (uiAnimation.animYdirection < 0) {
            //getting hover frame width
            View hoverView = (View) findViewById(R.id.hover);
            hoverView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int marginHeight = hoverView.getMeasuredHeight();

            //view animation
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
            uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton) findViewById(R.id.showLessonsButton), marginHeight);
        } else {
            updateVideo();
            if (uiAnimation.pdfViewerOpen) showPDF(view);
            selectedCourseId = currentCourseID;
            updateCoursesLayout();
            //view animation
            uiAnimation.scrollDown((FrameLayout) findViewById(R.id.hover));
        }
    }

    private ArrayList<courseButton> courseButtons = new ArrayList<>(dataHandler.coursesList.size());

    public void updateCoursesLayout() {
        //load courses.xml into hover layout
        ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.hover);
        inclusionViewGroup.removeAllViews();
        View coursesView = LayoutInflater.from(this).inflate(R.layout.courses, null);
        inclusionViewGroup.addView(coursesView);

        //updating hover layout
        courseButtons = new ArrayList<>(dataHandler.coursesList.size());
        LinearLayout coursesList = (LinearLayout) findViewById(R.id.coursesList);
        for (int i = 0; i < dataHandler.coursesList.size(); i++) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean appPurched = sharedPreferences.getBoolean("fullapp_" + Integer.parseInt(dataHandler.coursesList.get(i).get(dataHandler.TAG_COURSES_ID)), false);

            View courseButtonView = LayoutInflater.from(this).inflate(R.layout.course_button, null);
            courseButtonView.setOnClickListener(selectCourse(courseButtonView, i, Integer.parseInt(dataHandler.coursesList.get(i).get(dataHandler.TAG_COURSES_ID))));
            coursesList.addView(courseButtonView);
            courseButton courseButton = new courseButton();

            courseButton.courseRadio = (ImageView) courseButtonView.findViewById(R.id.courseRadio);
            courseButton.purchesedIcon = (ImageView) courseButtonView.findViewById(R.id.purchesedIcon);
            courseButton.courseIcon = (ImageView) courseButtonView.findViewById(R.id.courseIcon);
            courseButton.courseName = (TextView) courseButtonView.findViewById(R.id.courseName);

            if (!appPurched) courseButton.purchesedIcon.setVisibility(View.GONE);

            if (dataHandler.coursesList.get(i).get(dataHandler.TAG_COURSES_ID).equals("" + selectedCourseId)) {
                courseButton.courseRadio.setBackgroundResource(R.drawable.radio_on);
                courseButton.courseName.setText(dataHandler.coursesList.get(i).get(dataHandler.TAG_COURSES_NAME));
            } else courseButton.courseName.setText("");
            String thumbnailURL = dataHandler.coursesList.get(i).get(dataHandler.TAG_COURSES_ICON);
            new DownloadImageTask((ImageView) courseButton.courseIcon.findViewById(R.id.courseIcon)).execute(thumbnailURL);


            courseButtons.add(courseButton);
        }
    }

    public void hideCourses(View view) {
        disableEnableControls(true, (ViewGroup) findViewById(R.id.mainLayout));

        uiAnimation.scrollUp((FrameLayout) findViewById(R.id.hover));
    }

    public void videoFullscreen(View view) {
        //ViewGroup hoverViewGroup = (ViewGroup) findViewById(R.id.hover);
        //hoverViewGroup.removeAllViews();

        //exiting fullscreen mode
        if (uiAnimation.videoFullscreen) {
            disableEnableControls(true, (ViewGroup) findViewById(R.id.mainLayout));

            View videoView = (View) findViewById(R.id.videoWebView);
            ((ViewGroup) videoView.getParent()).removeAllViews();
            ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.videoWebViewLayout);
            inclusionViewGroup.addView(videoView);
            View fullscreenButton = LayoutInflater.from(this).inflate(R.layout.fullscreen_button, null);
            inclusionViewGroup.addView(fullscreenButton);
            ((ImageButton) fullscreenButton.findViewById(R.id.videoFullscreen)).setImageResource(R.drawable.fullscreen_icon);
            FrameLayout buyFromThumbFrame = (FrameLayout) findViewById(R.id.buyFromThumbFrame);
            buyFromThumbFrame.bringToFront();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
            uiAnimation.videoFullscreen = false;
            //entering fullscreen mode
        } else {
            if (uiAnimation.animYdirection < 0) resetMainScreen();
            disableEnableControls(false, (ViewGroup) findViewById(R.id.mainLayout));

            View videoView = (View) findViewById(R.id.videoWebView);
            ((ViewGroup) videoView.getParent()).removeView(videoView);
            ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.hover);
            inclusionViewGroup.addView(videoView);
            View fullscreenButton = LayoutInflater.from(this).inflate(R.layout.fullscreen_button, null);
            inclusionViewGroup.addView(fullscreenButton);
            ImageButton fullscreenImageButton = (ImageButton) fullscreenButton.findViewById(R.id.videoFullscreen);
            fullscreenImageButton.setImageResource(R.drawable.fullscreen_exit_icon);

            //view animation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), true);
            uiAnimation.videoFullscreen = true;
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                WebView videoWebView = (WebView) findViewById(R.id.videoWebView);
                videoWebView.loadUrl("javascript:resizeIframe(document.getElementById('videoFrame'));");
            }
        }, 500);
    }

    public void getProduct(View view) {
        currentCourseID = selectedCourseId;
        currentLesson = 0;
        currentPdfNumber = 0;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentCourseID", selectedCourseId);
        editor.commit();

        fullAppPurched = sharedPreferences.getBoolean("fullApp", false);

        disableEnableControls(true, (ViewGroup) findViewById(R.id.mainLayout));
        uiAnimation.scrollUp((FrameLayout) findViewById(R.id.hover));
        loadSettings();
        new GetLessons().execute();
        updateMainScreen();
    }

    View.OnClickListener updateLesson(final View button, final int lessonID) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                if (currentLesson == lessonID) {
                    if (fullAppPurched)
                        UIAnimation.fadeButton((ImageButton) findViewById(R.id.purchedIcon), false);
                    else
                        UIAnimation.fadeButton((ImageButton) findViewById(R.id.UpgradeButton), false);
                    UIAnimation.fadeButton((ImageButton) findViewById(R.id.pdfButton), false);
                    UIAnimation.fadeButton((ImageButton) findViewById(R.id.infoButton), false);
                    UIAnimation.fadeButton((ImageButton) findViewById(R.id.appsButton), false);
                    resetMainScreen();
                } else {
                    currentLesson = lessonID;
                    updateMainScreen();
                    uiAnimation.moveVideo((LinearLayout) findViewById(R.id.videoLayout), (LinearLayout) findViewById(R.id.lessonTextContainerLayout), true);
                    for (int i = 0; i < dataHandler.lessonsList.size(); i++) {
                        lessonButton lessonButton = lessonsButtons.get(i);
                        lessonButton.lessonButtonLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.lesson_buttons));
                    }
                    lessonButton selectedButton = lessonsButtons.get(currentLesson);
                    selectedButton.lessonButtonLayout.setBackgroundColor(Color.parseColor("#C0C0C0"));

                    TextView selectItemText = (TextView) findViewById(R.id.selectItemText);
                    TextView itemNotAviableText = (TextView) findViewById(R.id.itemNotAviableText);
                    if (dataHandler.lessonsList.get(currentLesson).get(dataHandler.TAG_IS_FREE).equals("false") && !fullAppPurched) {
                        selectItemText.setVisibility(View.INVISIBLE);
                        itemNotAviableText.setVisibility(View.VISIBLE);
                    } else {
                        selectItemText.setVisibility(View.VISIBLE);
                        itemNotAviableText.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
    }

    View.OnClickListener selectCourse(final View button, final int courseNumber, final int courseId) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                selectedCourseId = courseId;
                for (int i = 0; i < courseButtons.size(); i++) {
                    courseButton courseButton = courseButtons.get(i);
                    courseButton.courseRadio.setBackgroundResource(R.drawable.radio_off);
                    courseButton.courseName.setText("");
                }
                courseButton selectedCourse = courseButtons.get(courseNumber);
                selectedCourse.courseRadio.setBackgroundResource(R.drawable.radio_on);
                selectedCourse.courseName.setText(dataHandler.coursesList.get(courseNumber).get(dataHandler.TAG_COURSES_NAME));
            }
        };
    }

    public void buyProduct(View view) {
        if (uiAnimation.animYdirection < 0) {
            //getting hover frame width
            View lessonsView = (View) findViewById(R.id.lessonLayer);
            lessonsView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int marginHeight = lessonsView.getMeasuredHeight();

            //view animation
            uiAnimation.fadeHover((FrameLayout) findViewById(R.id.hover), false);
            uiAnimation.moveDown((LinearLayout) findViewById(R.id.mainLayout), (ImageButton) findViewById(R.id.showLessonsButton), marginHeight);
        } else showUpgrade(view);
    }

    public void upgradeNow(View view) {
        Log.d("inapp billing", "starting inapp billing");
        Log.d("inapp billing", "checking inapp billing");
        if (mService != null) {
            Log.d("inapp billing", "mService not null");
            ArrayList<String> skuList = new ArrayList<String>();
            skuList.add("fullapp_" + currentCourseID);
            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

            try {
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "fullapp_" + currentCourseID, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                try {
                    Log.d("inapp billing", "Starting billing fullapp_" + currentCourseID);
                    try {
                        startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                        Log.d("inapp billing", "fullapp_" + currentCourseID + " OK, restoring...");
                        restorePurchases(view);
                    } catch (NullPointerException e) {
                        Log.d("inapp billing", "error:" + e);
                        restorePurchases(view);
                    }
                } catch (IntentSender.SendIntentException e) {
                    Log.d("SendIntentException", "" + e);
                }
            } catch (RemoteException e) {
                Log.d("RemoteException", "" + e);
            }
        } else {
            Log.d("inapp billing", "Error connecting google play");
            Toast.makeText(this, "Error connecting google play", Toast.LENGTH_SHORT).show();
        }
    }

    public void restorePurchases(View view) {
        Log.d("inapp billing", "restorePurchases fullapp_" + currentCourseID);
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                for (int i = 0; i < ownedSkus.size(); i++) {
                    Log.d("inapp billing", "confirmed " + ownedSkus.get(i));
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ownedSkus.get(i), true);
                    editor.commit();
                }
                //UIAnimation.fadeButton((ImageButton) findViewById(R.id.UpgradeCloseButton), true);
                //showUpgrade(view);
                updateMainScreen();
                showUpgrade(view);
            }
        } catch (RemoteException e) {
            Log.d("inapp billing", "error: " + e);
        }
    }

    public String getUserEmail() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Account[] accounts = AccountManager.get(getBaseContext()).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    return account.name;
                }
            }
        }
        return null;
    }
    public int dp2px(int dp){
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
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

        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    private void disableEnableControls(boolean enable, ViewGroup vg){
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup){
                disableEnableControls(enable, (ViewGroup)child);
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("inapp billing", "onActivityResult");
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Log.d("inapp billing", sku+" bought!");
                    restorePurchases(null);
                }
                catch (JSONException e) {
                    Log.d("inapp billing", "Error! "+e.toString());
                    e.printStackTrace();
                }
            } else Log.d("inapp billing", "result code: "+resultCode);
        } else Log.d("inapp billing", "request code: "+requestCode);
    }
}
