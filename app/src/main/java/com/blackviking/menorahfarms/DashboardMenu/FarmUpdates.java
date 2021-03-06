package com.blackviking.menorahfarms.DashboardMenu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.FarmUpdateModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.FarmUpdateViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FarmUpdates extends AppCompatActivity {

    private ImageView backButton;
    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout;
    private android.app.AlertDialog alertDialog;
    private RecyclerView farmUpdateRecycler;
    private boolean isLoading = false;

    private LinearLayoutManager layoutManager;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference updateRef;
    private FirebaseRecyclerAdapter<FarmUpdateModel, FarmUpdateViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_updates);


        //firebase
        updateRef = db.getReference(Common.FARM_UPDATES_NODE);


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        farmUpdateRecycler = findViewById(R.id.farmUpdateRecycler);


        //show loading dialog
        showLoadingDialog("Loading farm updates . . .");


        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){


                updateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            alertDialog.dismiss();
                            emptyLayout.setVisibility(View.GONE);
                            noInternetLayout.setVisibility(View.GONE);
                            farmUpdateRecycler.setVisibility(View.VISIBLE);
                            loadUpdates();

                        } else {

                            alertDialog.dismiss();
                            emptyLayout.setVisibility(View.VISIBLE);
                            noInternetLayout.setVisibility(View.GONE);
                            farmUpdateRecycler.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else

            if (output == 0){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
                farmUpdateRecycler.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
                farmUpdateRecycler.setVisibility(View.GONE);

            }

        }).execute();

        backButton.setOnClickListener(v -> finish());
    }

    private void loadUpdates() {

        farmUpdateRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(FarmUpdates.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        farmUpdateRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<FarmUpdateModel, FarmUpdateViewHolder>(
                FarmUpdateModel.class,
                R.layout.farm_update_item,
                FarmUpdateViewHolder.class,
                updateRef.limitToLast(10)
        ) {
            @Override
            protected void populateViewHolder(FarmUpdateViewHolder viewHolder, final FarmUpdateModel model, int position) {

                viewHolder.youtubeTitle.setText(model.getVideo_title());
                viewHolder.youtubeView.loadUrl(model.getVideo_embed());
                viewHolder.playInFullScreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent fullscreenIntent = new Intent(FarmUpdates.this, FarmUpdateFullscreen.class);
                        fullscreenIntent.putExtra("VideoUrl", model.getVideo_url());
                        startActivity(fullscreenIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

                    }
                });

            }
        };
        farmUpdateRecycler.setAdapter(adapter);
    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        //loading
        isLoading = true;

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isLoading = false;
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isLoading = false;
            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            alertDialog.dismiss();
        }
        finish();
    }
}
