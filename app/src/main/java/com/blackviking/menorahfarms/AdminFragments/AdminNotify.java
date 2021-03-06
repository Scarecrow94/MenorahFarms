package com.blackviking.menorahfarms.AdminFragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.blackviking.menorahfarms.Models.DueSponsorshipModel;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminNotify extends Fragment {

    //widgets
    private EditText followedTopic, followedMessage, sponsoredTopic, sponsoredMessage;
    private Button sendFollowedNotiBtn, sendSponsoredNotiBtn;
    private FloatingActionButton broadcastToAllFab, manualActionFab;
    private RelativeLayout directionLayout, followedFarmLayout, sponsoredFarmLayout;
    private ProgressBar sendProgress;

    //firebase
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference notificationRef, userRef, farmRef, followedFarmNotiRef, sponsoredFarmNotiRef, dueSponsorshipsRef, sponsoredFarmRef, runningCycleRef;
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private APIService mService;

    //controller
    private Spinner followedSpinner, sponsoredSpinner;
    private RadioGroup notificationStyle;
    private String selectedFollowedGroup = "", selectedSponsoredGroup = "";


    public AdminNotify() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_notify, container, false);

        
        /*---   FIREBASE   ---*/
        notificationRef = db.getReference(Common.NOTIFICATIONS_NODE);
        userRef = db.getReference(Common.USERS_NODE);
        farmRef = db.getReference(Common.FARM_NODE);
        followedFarmNotiRef = db.getReference(Common.FOLLOWED_FARMS_NOTIFICATION_NODE);
        sponsoredFarmNotiRef = db.getReference(Common.SPONSORED_FARMS_NOTIFICATION_NODE);
        dueSponsorshipsRef = db.getReference(Common.DUE_SPONSORSHIPS_NODE);
        sponsoredFarmRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        runningCycleRef = db.getReference(Common.RUNNING_CYCLE_NODE);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGET   ---*/
        followedTopic = v.findViewById(R.id.followedTopic);
        followedMessage = v.findViewById(R.id.followedMessage);
        sponsoredTopic = v.findViewById(R.id.sponsoredTopic);
        sponsoredMessage = v.findViewById(R.id.sponsoredMessage);
        sendFollowedNotiBtn = v.findViewById(R.id.sendFollowedNotiBtn);
        sendSponsoredNotiBtn = v.findViewById(R.id.sendSponsoredNotiBtn);
        followedSpinner = v.findViewById(R.id.followedSpinner);
        sponsoredSpinner = v.findViewById(R.id.sponsoredSpinner);
        notificationStyle = v.findViewById(R.id.notificationStyle);
        broadcastToAllFab = v.findViewById(R.id.broadcastToAllFab);
        directionLayout = v.findViewById(R.id.directionLayout);
        followedFarmLayout = v.findViewById(R.id.followedFarmLayout);
        sponsoredFarmLayout = v.findViewById(R.id.sponsoredFarmLayout);
        sendProgress = v.findViewById(R.id.sendProgress);
        manualActionFab = v.findViewById(R.id.manualActionFab);
        manualActionFab.hide();


        //notification style
        notificationStyle.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){

                case R.id.followedFarmsNoti:
                    loadFollowedFarms();
                    break;

                case R.id.sponsoredFarmsNoti:
                    loadSponsoredFarms();
                    break;

            }
        });


        broadcastToAllFab.setOnClickListener(v1 -> openBroadcastDialog());


        //manual action fab
        manualActionFab.setOnClickListener(v12 -> {
            runManualOperation();
        });
        
        return v;
    }



    //manual operation
    private void runManualOperation() {

        //convert due sponsorships to stuff
        //new ManualAsyncCallerDue().execute();

        //convert running cycle to stuff
        new ManualAsyncCallerRun().execute();

    }

    private class ManualAsyncCallerDue extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {

            dueSponsorshipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                        DueSponsorshipModel dueObj = snap.getValue(DueSponsorshipModel.class);

                        sponsoredFarmRef.child(dueObj.getUser())
                                .child(dueObj.getSponsorshipId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                SponsoredFarmModel sponsoredObj = dataSnapshot.getValue(SponsoredFarmModel.class);

                                Map<String, Object> notMap = new HashMap<>();
                                notMap.put("timestamp", ServerValue.TIMESTAMP);

                                farmRef.child(sponsoredObj.getFarmId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                FarmModel farmObj = dataSnapshot.getValue(FarmModel.class);

                                                sponsoredFarmNotiRef
                                                        .child(farmObj.getFarmNotiId())
                                                        .child(dueObj.getUser())
                                                        .setValue(notMap);

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }

    private class ManualAsyncCallerRun extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {

            runningCycleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                        String cycleNotiId = snap.getKey();

                        for (DataSnapshot childSnap : snap.getChildren()){

                            RunningCycleModel runObj = childSnap.getValue(RunningCycleModel.class);

                            Map<String, Object> notMap = new HashMap<>();
                            notMap.put("timestamp", ServerValue.TIMESTAMP);

                            sponsoredFarmNotiRef
                                    .child(cycleNotiId)
                                    .child(runObj.getUserId())
                                    .setValue(notMap);

                        }

                    }

                    Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }





    //FOLLOWED FARMS
    private void loadFollowedFarms() {

        //set layout
        directionLayout.setVisibility(View.GONE);
        followedFarmLayout.setVisibility(View.VISIBLE);
        sponsoredFarmLayout.setVisibility(View.GONE);


        //set followed farm list
        final List<String> followedFarmList = new ArrayList<>();
        followedFarmList.add(0, "Followed Farm");

        final ArrayAdapter<String> dataAdapterFollowedFarm;
        dataAdapterFollowedFarm = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, followedFarmList);
        dataAdapterFollowedFarm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                followedFarmNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()){

                            followedFarmList.add(child.getKey());

                        }

                        followedSpinner.setAdapter(dataAdapterFollowedFarm);
                        dataAdapterFollowedFarm.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        }).execute();

        followedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Followed Farm")){

                    selectedFollowedGroup = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //validate
        sendFollowedNotiBtn.setOnClickListener(v -> checkFollowedFarmsNotiParam());

    }

    private void checkFollowedFarmsNotiParam() {

        final String theTopic = followedTopic.getText().toString().trim();
        final String theMessage = followedMessage.getText().toString().trim();


        //validate fields
        if (TextUtils.isEmpty(theTopic)) {

            followedTopic.requestFocus();
            followedTopic.setError("Field required !");

        } else if (TextUtils.isEmpty(theMessage)) {

            followedMessage.requestFocus();
            followedMessage.setError("Field required !");

        } else if (TextUtils.isEmpty(selectedFollowedGroup)) {

            Toast.makeText(getContext(), "Select A Farm", Toast.LENGTH_SHORT).show();

        } else {

            //loading
            sendFollowedNotiBtn.setEnabled(false);
            sendProgress.setVisibility(View.VISIBLE);

            //execute network check async task
            new CheckInternet(getContext(), output -> {

                //check all cases
                if (output == 1){

                    //run in background
                    new FollowedAsyncCaller().execute(theTopic, theMessage, selectedFollowedGroup);

                    //wait 5 seconds then stop loading
                    new Handler().postDelayed(() -> {

                        //stop loading
                        sendFollowedNotiBtn.setEnabled(true);
                        sendProgress.setVisibility(View.GONE);

                        Toast.makeText(getContext(), "DONE", Toast.LENGTH_SHORT).show();
                        followedTopic.setText("");
                        followedMessage.setText("");

                        //set layout
                        Toast.makeText(getContext(), "STARTED", Toast.LENGTH_SHORT).show();

                    }, 5000);

                } else

                if (output == 0){

                    //stop loading
                    sendFollowedNotiBtn.setEnabled(true);
                    sendProgress.setVisibility(View.GONE);

                    //set layout
                    Toast.makeText(getContext(), "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //stop loading
                    sendFollowedNotiBtn.setEnabled(true);
                    sendProgress.setVisibility(View.GONE);

                    //set layout
                    Toast.makeText(getContext(), "Not connected to a network", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        }

    }

    private class FollowedAsyncCaller extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {


            String theTopic = strings[0];
            String theMessage = strings[1];
            String theGroup = strings[2];

            //get user list from user node
            followedFarmNotiRef.child(theGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //create date string
                    final Date todayDate = Calendar.getInstance().getTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
                    String todayString = formatter.format(todayDate);

                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                        //get each user id
                        String userId = snap.getKey();

                        //create notification map
                        final Map<String, Object> notificationMap = new HashMap<>();
                        notificationMap.put("topic", theTopic);
                        notificationMap.put("message", theMessage);
                        notificationMap.put("time", todayString);

                        //push to database
                        notificationRef.child(userId)
                                .push()
                                .setValue(notificationMap)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()){
                                        //send broadcast notification
                                        Map<String, String> dataSend = new HashMap<>();
                                        dataSend.put("title", "Menorah Farms");
                                        dataSend.put("message", theTopic);
                                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/")
                                                .append(userId).toString(), dataSend);

                                        mService.sendNotification(dataMessage)
                                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                                    @Override
                                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {


                                                    }

                                                    @Override
                                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                                    }
                                                });
                                    }

                                });



                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }




    //SPONSORED FARMS
    private void loadSponsoredFarms() {

        directionLayout.setVisibility(View.GONE);
        followedFarmLayout.setVisibility(View.GONE);
        sponsoredFarmLayout.setVisibility(View.VISIBLE);

        //set followed farm list
        final List<String> sponsoredFarmList = new ArrayList<>();
        sponsoredFarmList.add(0, "Sponsored Farm");

        final ArrayAdapter<String> dataAdapterSponsoredFarm;
        dataAdapterSponsoredFarm = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, sponsoredFarmList);
        dataAdapterSponsoredFarm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                sponsoredFarmNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()){

                            sponsoredFarmList.add(child.getKey());

                        }

                        sponsoredSpinner.setAdapter(dataAdapterSponsoredFarm);
                        dataAdapterSponsoredFarm.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        }).execute();

        sponsoredSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Sponsored Farm")){

                    selectedSponsoredGroup = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //validate
        sendSponsoredNotiBtn.setOnClickListener(v -> checkSponsoredFarmsNotiParam());

    }

    private void checkSponsoredFarmsNotiParam() {

        final String theTopic = sponsoredTopic.getText().toString().trim();
        final String theMessage = sponsoredMessage.getText().toString().trim();


        //validate fields
        if (TextUtils.isEmpty(theTopic)) {

            sponsoredTopic.requestFocus();
            sponsoredTopic.setError("Field required !");

        } else if (TextUtils.isEmpty(theMessage)) {

            sponsoredMessage.requestFocus();
            sponsoredMessage.setError("Field required !");

        } else if (TextUtils.isEmpty(selectedSponsoredGroup)) {

            Toast.makeText(getContext(), "Select A Farm", Toast.LENGTH_SHORT).show();

        } else {

            //loading
            sendSponsoredNotiBtn.setEnabled(false);
            sendProgress.setVisibility(View.VISIBLE);

            //run network check
            new CheckInternet(getContext(), output -> {

                //check all cases
                if (output == 1){

                    //run in background
                    new SponsoredAsyncCaller().execute(theTopic, theMessage, selectedSponsoredGroup);

                    //wait 5 seconds then stop loading
                    new Handler().postDelayed(() -> {

                        //stop loading
                        sendSponsoredNotiBtn.setEnabled(true);
                        sendProgress.setVisibility(View.GONE);

                        Toast.makeText(getContext(), "DONE", Toast.LENGTH_SHORT).show();
                        sponsoredTopic.setText("");
                        sponsoredMessage.setText("");

                        //set layout
                        Toast.makeText(getContext(), "STARTED", Toast.LENGTH_SHORT).show();

                    }, 5000);

                } else

                if (output == 0){

                    //stop loading
                    sendSponsoredNotiBtn.setEnabled(true);
                    sendProgress.setVisibility(View.GONE);

                    //set layout
                    Toast.makeText(getContext(), "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //stop loading
                    sendSponsoredNotiBtn.setEnabled(true);
                    sendProgress.setVisibility(View.GONE);

                    //set layout
                    Toast.makeText(getContext(), "No network access", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        }

    }

    private class SponsoredAsyncCaller extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {


            String theTopic = strings[0];
            String theMessage = strings[1];
            String theGroup = strings[2];

            //get user list from user node
            sponsoredFarmNotiRef.child(theGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //create date string
                    final Date todayDate = Calendar.getInstance().getTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
                    String todayString = formatter.format(todayDate);

                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                        //get each user id
                        String userId = snap.getKey();

                        //create notification map
                        final Map<String, Object> notificationMap = new HashMap<>();
                        notificationMap.put("topic", theTopic);
                        notificationMap.put("message", theMessage);
                        notificationMap.put("time", todayString);

                        //push to database
                        notificationRef.child(userId)
                                .push()
                                .setValue(notificationMap)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()){
                                        //send broadcast notification
                                        Map<String, String> dataSend = new HashMap<>();
                                        dataSend.put("title", "Menorah Farms");
                                        dataSend.put("message", theTopic);
                                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/")
                                                .append(userId).toString(), dataSend);

                                        mService.sendNotification(dataMessage)
                                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                                    @Override
                                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {


                                                    }

                                                    @Override
                                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                                    }
                                                });
                                    }

                                });



                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }



    //BROADCAST TO ALL
    private void openBroadcastDialog() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.admin_broadcast_layout,null);

        final EditText broadcastTopic = viewOptions.findViewById(R.id.broadcastTopic);
        final EditText broadcastMessage = viewOptions.findViewById(R.id.broadcastMessage);
        final Button sendBroadcastBtn = viewOptions.findViewById(R.id.sendBroadcastBtn);
        final ProgressBar sendProgress = viewOptions.findViewById(R.id.sendProgress);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        sendBroadcastBtn.setOnClickListener(v -> checkBroadcastNotiParam(sendBroadcastBtn, sendProgress, alertDialog, broadcastTopic, broadcastMessage));


        alertDialog.show();
    }

    private void checkBroadcastNotiParam(final Button sendBroadcastBtn, final ProgressBar sendProgress, final AlertDialog alertDialog, EditText broadcastTopic, EditText broadcastMessage) {

        final String theTopic = broadcastTopic.getText().toString().trim();
        final String theMessage = broadcastMessage.getText().toString().trim();


        //validate fields
        if (TextUtils.isEmpty(theTopic)) {

            broadcastTopic.requestFocus();
            broadcastTopic.setError("Field required !");

        } else if (TextUtils.isEmpty(theMessage)) {

            broadcastMessage.requestFocus();
            broadcastMessage.setError("Field required !");

        } else {

            //loading
            sendBroadcastBtn.setEnabled(false);
            sendProgress.setVisibility(View.VISIBLE);

            //run network check
            new CheckInternet(getContext(), output -> {

                //check all cases
                if (output == 1){

                    //run in background
                    new AsyncCaller().execute(theTopic, theMessage);

                    //wait 5 seconds then stop loading
                    new Handler().postDelayed(() -> {

                        //stop loading
                        sendBroadcastBtn.setEnabled(true);
                        sendProgress.setVisibility(View.GONE);
                        alertDialog.dismiss();

                        //set layout
                        Toast.makeText(getContext(), "STARTED", Toast.LENGTH_SHORT).show();

                    }, 5000);

                } else

                if (output == 0){

                    //stop loading
                    sendBroadcastBtn.setEnabled(true);
                    sendProgress.setVisibility(View.GONE);

                    //set layout
                    Toast.makeText(getContext(), "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //stop loading
                    sendBroadcastBtn.setEnabled(true);
                    sendProgress.setVisibility(View.GONE);

                    //set layout
                    Toast.makeText(getContext(), "No network access", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        }

    }

    private class AsyncCaller extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {


            String theTopic = strings[0];
            String theMessage = strings[1];

            //get user list from user node
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //create date string
                    final Date todayDate = Calendar.getInstance().getTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
                    String todayString = formatter.format(todayDate);

                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                        //get each user id
                        String userId = snap.getKey();

                        //create notification map
                        final Map<String, Object> notificationMap = new HashMap<>();
                        notificationMap.put("topic", theTopic);
                        notificationMap.put("message", theMessage);
                        notificationMap.put("time", todayString);

                        //push to database
                        notificationRef.child(userId)
                                .push()
                                .setValue(notificationMap);

                    }

                    //send broadcast notification
                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", "Menorah Farms");
                    dataSend.put("message", theTopic);
                    DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.GENERAL_NOTIFY).toString(), dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new retrofit2.Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {


                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                }
                            });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }

}
