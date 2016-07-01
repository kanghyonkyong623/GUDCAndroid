package ogr.GUDC;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 12/23/2015.
 */
public class LoginActivity extends Activity {
    public static final String PREFS_NAME = "LoginInfo";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getUserInputForServerIP();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String userid = settings.getString("UserName", "");
        String password = settings.getString("Password", "");

        if(userid != null && userid != "") {
            EditText userNameCtrl = (EditText) findViewById(R.id.username);
            EditText passwordCtrl = (EditText) findViewById(R.id.password);
            CheckBox rememberChk = (CheckBox)findViewById(R.id.chkRemember);

            userNameCtrl.setText(userid);
            passwordCtrl.setText(password);
            rememberChk.setChecked(true);
        }

    }

    private void getUserInputForServerIP()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String serverIP = settings.getString("ServerIP", getString(R.string.url_server_ip));
        GUDCActivity.SERVER_URL = "http://" + serverIP + getString(R.string.url_server_path);
        //editor.commit();

        userInput.setText(serverIP);

        TextView msgTextView = (TextView) promptsView.findViewById(R.id.messageText);
        msgTextView.setText(getString(R.string.msg_input_server_ip));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // get user input and set it to result
                                // edit text
                                GUDCActivity.SERVER_URL = "http://" + userInput.getText() + getString(R.string.url_server_path);
                                SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("ServerIP", userInput.getText().toString());
                                editor.commit();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    public void login(View view) throws IOException {
        EditText userName = (EditText)findViewById(R.id.username);
        EditText password = (EditText)findViewById(R.id.password);
        CheckBox rememberMe = (CheckBox)findViewById(R.id.chkRemember);
        String postString = "UserName=" + userName.getText() + "&Password=" + password.getText() + "&RememberMe=" + String.valueOf(rememberMe.isChecked());

        try {
            String strResult = NetworkUtilities.sendPost(this, GUDCActivity.SERVER_URL + getString(R.string.url_login), postString, null, null, true);
            if (strResult != null && !strResult.equals(""))
            {
                JSONObject jsonObject = new JSONObject(strResult);
                String result = jsonObject.getString("result");
                if (result != null && result.equals("Succeed"))
                {
                    Intent data = new Intent();
                    data.putExtra(getString(R.string.login_result), GUDCActivity.LOGIN_SUCCEED);
                    data.putExtra(getString(R.string.user_name), jsonObject.getString(getString(R.string.user_name)));
                    data.putExtra(getString(R.string.param_teamname), jsonObject.getString(getString(R.string.param_teamname)));
                    if(!jsonObject.isNull(getString(R.string.param_tasklocation)))
                        data.putExtra(getString(R.string.param_tasklocation), jsonObject.getString(getString(R.string.param_tasklocation)));

                    //data.putExtra()
                    setResult(Activity.RESULT_OK, data);
                    if(rememberMe.isChecked())
                    {
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("UserName", userName.getText().toString());
                        editor.putString("Password", password.getText().toString());
                        editor.commit();
                    }
                    finish();
                }
                else
                {
                    Utilities.messageDialog(this, getString(R.string.incorrect_user), null);
                }
            }
            else
            {
                Utilities.messageDialog(this, getString(R.string.incorrect_user), null);
            }

        } catch (Exception e) {
            Utilities.errorDialog(this, e, null);
        }

//        URL url = null;
//        try {
//            url = new URL("http://www.android.com/");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//        try {
//            urlConnection.setDoOutput(true);
//            urlConnection.setChunkedStreamingMode(0);
//
//            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//            EditText userName = (EditText)findViewById(R.id.username);
//            EditText password = (EditText)findViewById(R.id.password);
//            CheckBox rememberMe = (CheckBox)findViewById(R.id.chkRemember);
//            String postString = "UserName=" + userName.getText() + "&Password=" + password.getText() + "&RememberMe=" + String.valueOf(rememberMe.isChecked());
//            out.write(postString.getBytes("utf-8"));
//
//            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//            byte[] byteResult = new byte[];
//            in.read(byteResult);
//        }
//        finally{
//            urlConnection.disconnect();
//        }
    }
}