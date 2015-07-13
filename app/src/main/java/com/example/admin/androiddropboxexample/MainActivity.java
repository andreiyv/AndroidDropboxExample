package com.example.admin.androiddropboxexample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class MainActivity extends Activity implements OnClickListener {

    private DropboxAPI<AndroidAuthSession> dropbox;

    private final static String FILE_DIR = "/MySampleFolder/";
    private final static String DROPBOX_NAME = "YetAnotherUploader";
    private final static String ACCESS_KEY = "pbzt6tzpran9oil";
    private final static String ACCESS_SECRET = "pk53kqbm581o64z";
    private boolean isLoggedIn;
    private Button login;
    private Button uploadFile;


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button) findViewById(R.id.dropbox_login);
        login.setOnClickListener(this);
        uploadFile = (Button) findViewById(R.id.upload_file);
        uploadFile.setOnClickListener(this);


        loggedIn(false);

        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER, token);
        } else {
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER);
        }

        dropbox = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropbox.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();

                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        uploadFile.setEnabled(isLogged);
        login.setText(isLogged ? "Logout" : "Login");
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dropbox_login:

                if (isLoggedIn) {
                    dropbox.getSession().unlink();
                    loggedIn(false);
                } else {
                    dropbox.getSession().startAuthentication(MainActivity.this);
                }
                break;
            case R.id.upload_file:
                UploadFileToDropbox upload = new UploadFileToDropbox(this, dropbox,
                        FILE_DIR);
                upload.execute();
                break;

            default:
                break;
        }
    }
}