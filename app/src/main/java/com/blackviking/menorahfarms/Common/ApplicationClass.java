package com.blackviking.menorahfarms.Common;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.os.Build;

import com.blackviking.menorahfarms.Models.UserModel;
import com.firebase.ui.auth.User;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

/**
 * Created by Scarecrow on 6/18/2018.
 */

public class ApplicationClass extends Application {

    private UserModel user = null;

    public UserModel getUser(){
        return user;
    }

    public void setUser(UserModel user){
        this.user = user;
        Paper.book().write(Common.PAPER_USER, user);
    }

    public void resetUser(){

        user = null;

    }

    public static final String CHANNEL_1_ID = "FeedChannel";
    public static final String CHANNEL_2_ID = "AdminChannel";
    public static final String CHANNEL_3_ID = "SponsoredChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   PAPER   ---*/
        Paper.init(getApplicationContext());


        /*---   FIREBASE OFFLINE   ---*/
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        /*-------PICASSO--------*/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        createNotificationChannels();

    }

    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            /*---   OREO CUSTOM SOUND   ---*/
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();



            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "FeedChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel1.setDescription("Feed Channel");



            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "AdminChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel2.setDescription("Admin Channel");



            NotificationChannel channel3 = new NotificationChannel(
                    CHANNEL_3_ID,
                    "AccountChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel3.setDescription("Account Channel");


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel3);
        }

    }
}
