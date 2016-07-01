package ogr.GUDC;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Administrator on 12/23/2015.
 */
public class MapsActivity extends Activity {

    static int DISABLE_ACCEPT_BUTTON = 0;
    static int ENABLE_ACCEPT_BUTTON = 1;
    static int ENABLE_STATUS_BUTTON = 2;
    static int DISABLE_STATUS_BUTTON = 3;
    static int ENABLE_ARRIVED_BUTTON = 4;
    static int DISABLE_ARRIVED_BUTTON = 5;
    static int ENABLE_ROUTE_BUTTON = 6;
    static int DISABLE_ROUTE_BOTTON = 7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapsactivity);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mUserName = getIntent().getStringExtra(getString(R.string.user_name));
        mTeamName = getIntent().getStringExtra(getString(R.string.param_teamname));
        mTaskLocation = getIntent().getStringExtra(getString(R.string.param_tasklocation));
//        if(mTaskLocation == null || mTaskLocation == "")
//        {
//            SetButtonEnabled(false, R.id.btnAcceptTask);
//            SetButtonEnabled(false, R.id.btnTaskStatus);
//        }

        SetButtonEnabled(false, R.id.btnAcceptTask);
        SetButtonEnabled(false, R.id.btnTaskStatus);
        SetButtonEnabled(false, R.id.btnArrived);
        SetButtonEnabled(false, R.id.btnRoute);

        mGPS = new GPSTracker(this);
        mGPS.setWebView(mWebView);
        mGPS.setUserName(mUserName);
        //mGPS.setOnLocationChangedListener();
        String strLocation = "";
        if(mGPS.canGetLocation()) {

            double latitude = mGPS.getLatitude();
            double longitude = mGPS.getLongitude();
            if(latitude == 0 && longitude == 0)
            {
                latitude = 30.56;
                longitude = 31.33;
            }
            strLocation = String.format("%s=%f,%f", getString(R.string.locationParam), latitude, longitude);
            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            mGPS.showSettingsAlert();
        }
        String strUrl = GUDCActivity.SERVER_URL + getString(R.string.url_gudc_maps) + "?username=" + mUserName + "&" + strLocation;
        mWebView.loadUrl(strUrl);
        mWebView.setWebViewClient(new HelloWebViewClient());
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "app");
    }

    private void SetButtonEnabled(boolean enabled, int btnId) {
        Button acceptBtn = (Button)findViewById(btnId);
        acceptBtn.setEnabled(enabled);
        if(enabled)
        {
            acceptBtn.setTextColor(getResources().getColor(R.color.main_text_color));
        }
        else
        {
            acceptBtn.setTextColor(getResources().getColor(R.color.main_background));
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    WebView mWebView;
    String mUserName;
    GPSTracker mGPS;
    private boolean isTaskAccepted = false;
    private String mTeamName = "";
    private String mTaskLocation = "";
    final android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
                if(msg.what== DISABLE_ACCEPT_BUTTON)
                {
                    SetButtonEnabled(false, R.id.btnAcceptTask);
                }
                else if(msg.what == ENABLE_ACCEPT_BUTTON)
                {
                    SetButtonEnabled(true, R.id.btnAcceptTask);
                }
                else if (msg.what == ENABLE_STATUS_BUTTON)
                {
                    SetButtonEnabled(true, R.id.btnTaskStatus);
                }
                else if (msg.what == DISABLE_STATUS_BUTTON)
                {
                    SetButtonEnabled(false, R.id.btnTaskStatus);
                }
                else if (msg.what == ENABLE_ARRIVED_BUTTON)
                {
                    SetButtonEnabled(true, R.id.btnArrived);
                }
                else if (msg.what == DISABLE_ARRIVED_BUTTON)
                {
                    SetButtonEnabled(false, R.id.btnArrived);
                }
                else if (msg.what == ENABLE_ROUTE_BUTTON)
                {
                    SetButtonEnabled(true, R.id.btnRoute);
                }
                else if (msg.what == DISABLE_ROUTE_BOTTON)
                {
                    SetButtonEnabled(false, R.id.btnRoute);
                }
//            super.handleMessage(msg);
        }
    };;

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                view.loadUrl(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public void TestClick(View view)
    {
        mGPS.SendLocation(30.37, 31.99);
    }

    public void onAcceptClick(View view)
    {
        Utilities.yesNoMessageDialog(this, getString(R.string.msg_accept_task), new Runnable() {
            public void run() {
                isTaskAccepted = true;
                sendMessage(DISABLE_ACCEPT_BUTTON);
                sendMessage(ENABLE_ARRIVED_BUTTON);
                mWebView.loadUrl("javascript:TaskAccept()");
//                runOnUiThread(new Runnable(){
//                    @Override
//                    public void run(){
//                        // change UI elements here
//                        SetButtonEnabled(false, R.id.btnAcceptTask);
//                    };
//                });
            }
        }, new Runnable() {
            public void run() {
                isTaskAccepted = false;
            }
        });

    }

    public void sendMessage(int msgWhat)
    {
        Message msg = handler.obtainMessage();
        msg.what = msgWhat;
//                msg.obj = bitmap;
        msg.arg1 = 1;
        handler.sendMessage(msg);
    }

    public void onRouteClick(View view)
    {
        mWebView.loadUrl("javascript:BestRouteClick()");
    }

    public void onArrivedClick(View view)
    {
        String teamLoc = String.format("%f,%f", mGPS.getLatitude(), mGPS.getLongitude());
        mWebView.loadUrl("javascript:IsArrived('" + teamLoc + "')");
    }

    public void onTaskStatusClick(View view)
    {
        Utilities.customYesNoMessageDialog(this, getString(R.string.msg_task_status),
                getString(R.string.task_status_pending), new Runnable() {
                    public void run() {
                        mWebView.loadUrl("javascript:TaskPending()");
                    }
                },
                getString(R.string.task_status_closed), new Runnable() {
                    public void run() {
                        mWebView.loadUrl("javascript:TaskClosed()");
                    }
                });
        sendMessage(DISABLE_STATUS_BUTTON);
    }

    public class WebViewJavaScriptInterface{

        private Context context;

        /*
         * Need a reference to the context in order to sent a post message
         */
        public WebViewJavaScriptInterface(Context context){
            this.context = context;
        }

        /*
         * This method can be called from Android. @JavascriptInterface
         * required after SDK version 17.
         */
        @JavascriptInterface
        public void newTaskAssigned(String taskName){

            Toast.makeText(context, getString(R.string.msg_newtask_assigned), Toast.LENGTH_LONG ).show();
            //sendMessage(ENABLE_ACCEPT_BUTTON);
        }

        @JavascriptInterface
        public void taskSelected(String taskName){

            //Toast.makeText(context, getString(R.string.msg_newtask_assigned), Toast.LENGTH_LONG ).show();
            sendMessage(ENABLE_ACCEPT_BUTTON);
            sendMessage(ENABLE_ROUTE_BUTTON);
        }

        @JavascriptInterface
        public void isArrived(boolean isArrived){

            if(isArrived)
            {
                sendMessage(ENABLE_STATUS_BUTTON);
            }
            else
            {
                Toast.makeText(context, getString(R.string.msg_arrived_task_error), Toast.LENGTH_LONG).show();
            }
        }

    }
}
