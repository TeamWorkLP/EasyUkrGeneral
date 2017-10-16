package com.example.mark0.easyukrainian;

import Hardware.SharedPreferences.UserPreference;
import Hardware.Storage.EasyUkrFiles;
import Hardware.WiFiConnector;
import Infrastructure.AccountSessions.CurrentUser;
import Infrastructure.MainOperations.ResourceDownloader;
import Infrastructure.Serialization.Serializer;
import Infrastructure.Static.Constants;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.File;

import static Infrastructure.Static.EasyUkrApplication.redirectToIntent;


public class InitActivity extends AppCompatActivity {

    View view;
    private TextView header;
    private File mainDirectory;

    //region Permission
    private boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    private void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldAskPermissions()) {
            askPermissions();
        }
        MultiDex.install(this);
        EasyUkrFiles.defaultpath = getExternalFilesDir(null);
        setContentView(R.layout.activity_init);
        header = (TextView) findViewById(R.id.header);
        header.setText("Initialization...");
        view = findViewById(R.id.mainView);
        mainDirectory = getExternalFilesDir(null);
        UserPreference.UserPreferenceInit(getApplication().getSharedPreferences(Constants.Account, 0));
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SplashScreen async = new SplashScreen(this);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                async.execute();
            }
        });
    }

    private class SplashScreen extends AsyncTask<Void, String, Boolean> {
        Activity context = null;
        String end;

        SplashScreen(Activity context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            header.setText(end);
            if (end.contains("Runn")) {

                boolean getedUser = UserPreference.checkSavedAccount();
                if (getedUser)
                    CurrentUser.getInstance().cloneFromMemory(UserPreference.readUserAccount());
                redirectToIntent(context, getedUser
                        ? ProfileNewActivity.class : LoginActivity.class, true, null);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            header.setText(values[0]);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            File word = new File(mainDirectory, EasyUkrFiles.fileWords);
            File topic = new File(mainDirectory, EasyUkrFiles.fileTopics);
            File grammar = new File(mainDirectory, EasyUkrFiles.fileGrammar);
            File recommendation = new File(mainDirectory, EasyUkrFiles.fileRecomendation);
            File recommendationList = new File(mainDirectory, EasyUkrFiles.fileRecomendationList);
            File grammarTask = new File(mainDirectory, EasyUkrFiles.fileGrammarTask);
            File dialogue = new File(mainDirectory, EasyUkrFiles.fileDialogue);

            if (topic.exists() && word.exists() && grammar.exists() &&
                    recommendation.exists() && recommendationList.exists() && grammarTask.exists()
                    && dialogue.exists()) {
                publishProgress("Running...");
            } else {
                if (!(new WiFiConnector(context.getBaseContext()).isConnected())) {
                    SystemClock.sleep(3000);
                    publishProgress("WiFi is not avaible \nfor downloading data");
                    end = "WiFi is not avaible \nfor downloading data";
                    return false;
                } else {
                    final Context context = getApplicationContext();
                    publishProgress("Downloading...");
                    ResourceDownloader.DownloadDictionary(context,
                            new Serializer(EasyUkrFiles.Type.TOPIC, mainDirectory));
                    ResourceDownloader.DownloadDictionary(context,
                            new Serializer(EasyUkrFiles.Type.WORD, mainDirectory));
                    ResourceDownloader.DownloadGrammar(context,
                            new Serializer(EasyUkrFiles.Type.GRAMMAR, mainDirectory));
                    ResourceDownloader.DownloadGrammarTasks(context, new Serializer(EasyUkrFiles.Type.GRAMMARTASK, mainDirectory));
                    ResourceDownloader.DownloadRecommendationCategories(context,
                            new Serializer(EasyUkrFiles.Type.RECOMENDATION, mainDirectory));
                    ResourceDownloader.DownloadRecommendations(context,
                            new Serializer(EasyUkrFiles.Type.RECOMENDATIONLIST, mainDirectory));
                    ResourceDownloader.DownloadDialogues(context,
                            new Serializer(EasyUkrFiles.Type.DIALOGUE, mainDirectory));

                    publishProgress("Data is downloaded");
                }
            }
            end = "Running....";
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            header.setText("Downloading...");
        }
    }
}