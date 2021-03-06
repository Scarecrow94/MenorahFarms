package com.blackviking.menorahfarms;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class FarmDetails extends AppCompatActivity {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference farmRef, followedRef, cartRef, followedFarmNotiRef, sponsorshipRef;
    private String currentUid, farmId;

    private ImageView backButton, farmImage;
    private TextView farmType, unitsLeft, farmLocation, farmROI, unitPrice, totalROI, totalDuration, totalPay;
    private ImageView decreaseUnitNumber, increaseUnitNumber;
    private TextView unitNumber, farmDescription;
    private RelativeLayout followFarmBtn, addToCartBtn, followedFarmButton;
    private LinearLayout farmNumber;

    private int unitNumberText = 1;

    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_details);


        /*---   INTENT DATA   ---*/
        farmId = getIntent().getStringExtra("FarmId");


        /*---   FIREBASE   ---*/
        farmRef = db.getReference(Common.FARM_NODE);
        sponsorshipRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        followedRef = db.getReference(Common.FOLLOWED_FARMS_NODE);
        cartRef = db.getReference(Common.CART_NODE);
        followedFarmNotiRef = db.getReference(Common.FOLLOWED_FARMS_NOTIFICATION_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        farmImage = findViewById(R.id.farmDetailImage);
        farmType = findViewById(R.id.farmDetailsType);
        unitsLeft = findViewById(R.id.farmDetailsUnitsLeft);
        farmLocation = findViewById(R.id.farmDetailLocation);
        farmROI = findViewById(R.id.farmDetailROI);
        unitPrice = findViewById(R.id.farmDetailUnitPrice);
        totalROI = findViewById(R.id.totalRoiAndPrice);
        totalDuration = findViewById(R.id.totalDuration);
        totalPay = findViewById(R.id.totalPayback);
        farmDescription = findViewById(R.id.farmDescription);
        followFarmBtn = findViewById(R.id.followFarmButton);
        addToCartBtn = findViewById(R.id.addToCartButton);
        followedFarmButton = findViewById(R.id.followedFarmButton);
        decreaseUnitNumber = findViewById(R.id.decreaseUnitNumber);
        increaseUnitNumber = findViewById(R.id.increaseUnitNumber);
        unitNumber = findViewById(R.id.unitNumber);
        unitNumber.setText(String.valueOf(unitNumberText));
        farmNumber = findViewById(R.id.farmNumber);

        backButton.setOnClickListener(v -> finish());


        //show loading dialog
        showLoadingDialog("Loading farm details . . .");

        //execute network check async task
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                loadCurrentFarm();

            } else

            if (output == 0){

                //set layout
                alertDialog.dismiss();
                Toast.makeText(FarmDetails.this, "No internet connection", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //set layout
                alertDialog.dismiss();
                Toast.makeText(FarmDetails.this, "No network detected", Toast.LENGTH_SHORT).show();

            }

        }).execute();


    }

    private void checkFollowedFarmFollowed() {

        //remove dialog
        alertDialog.dismiss();

        followedRef.child(currentUid)
                .child(farmId)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    followedFarmButton.setVisibility(View.VISIBLE);
                                    followFarmBtn.setVisibility(View.GONE);

                                } else {

                                    followedFarmButton.setVisibility(View.GONE);
                                    followFarmBtn.setVisibility(View.VISIBLE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void loadCurrentFarm() {

        farmRef.child(farmId)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                if (currentFarm != null){

                                    String theFarmType = currentFarm.getFarmType();
                                    String theFarmLocation = currentFarm.getFarmLocation();
                                    final String theFarmROI = currentFarm.getFarmRoi();
                                    final String theFarmUnitPrice = currentFarm.getPricePerUnit();
                                    String theFarmSponsorDuration = currentFarm.getSponsorDuration();
                                    final String theFarmImage = currentFarm.getFarmImageThumb();
                                    final String theFarmState = currentFarm.getFarmState();

                                    if (!theFarmImage.equalsIgnoreCase("")){

                                        Picasso.get()
                                                .load(theFarmImage)
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .placeholder(R.drawable.menorah_placeholder)
                                                .into(farmImage, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Picasso.get()
                                                                .load(theFarmImage)
                                                                .placeholder(R.drawable.menorah_placeholder)
                                                                .into(farmImage);
                                                    }
                                                });

                                    }



                                    farmType.setText(theFarmType);
                                    unitsLeft.setText(currentFarm.getUnitsAvailable());
                                    farmLocation.setText(theFarmLocation);
                                    farmROI.setText("Returns " + theFarmROI + "% in " + theFarmSponsorDuration + " months.");

                                    long priceToLong = Long.parseLong(theFarmUnitPrice);

                                    unitPrice.setText(Common.convertToPrice(FarmDetails.this, priceToLong));

                                    totalROI.setText("Return on investment (" + theFarmROI + "%) - " + Common.convertToPrice(FarmDetails.this, priceToLong));
                                    totalDuration.setText("Total payout after " + theFarmSponsorDuration + " months");

                                    /*---   CALCULATION   ---*/
                                    long theFixedPricePerUnit = Long.parseLong(theFarmUnitPrice);
                                    int theFixedRoi = Integer.parseInt(theFarmROI);

                                    /*---   CALCULATION   ---*/
                                    long theCalculatedPrice = theFixedPricePerUnit * unitNumberText;
                                    long totalCalculation = theCalculatedPrice * theFixedRoi / 100;
                                    long totalResult = totalCalculation + theCalculatedPrice;

                                    totalPay.setText(Common.convertToPrice(FarmDetails.this, totalResult));

                                    if (!theFarmState.equalsIgnoreCase("Now Selling")){

                                        addToCartBtn.setVisibility(View.GONE);
                                        farmNumber.setVisibility(View.GONE);

                                    } else {

                                        addToCartBtn.setVisibility(View.VISIBLE);
                                        farmNumber.setVisibility(View.VISIBLE);

                                    }

                                    /*---   UNIT NUMBERS   ---*/
                                    increaseUnitNumber.setOnClickListener(v -> {

                                        if (unitNumberText < 50) {
                                            unitNumberText++;
                                            unitNumber.setText(String.valueOf(unitNumberText));

                                            calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);

                                        } else {

                                            Toast.makeText(FarmDetails.this, "Sponsorship limit is 50 per attempt", Toast.LENGTH_LONG).show();

                                        }


                                    });

                                    decreaseUnitNumber.setOnClickListener(v -> {

                                        if (unitNumberText == 1){



                                        } else {

                                            unitNumberText --;
                                            unitNumber.setText(String.valueOf(unitNumberText));

                                            calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);

                                        }

                                    });

                                    //follow check
                                    checkFollowedFarmFollowed();


                                    farmDescription.setOnClickListener(v -> {
                                        Intent farmDescIntent = new Intent(FarmDetails.this, FarmDescription.class);
                                        farmDescIntent.putExtra("FarmId", farmId);
                                        startActivity(farmDescIntent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                                    });


                                    followFarmBtn.setOnClickListener(v -> followFarm());

                                    addToCartBtn.setOnClickListener(v -> {

                                        //execute network check async task
                                        new CheckInternet(FarmDetails.this, output -> {

                                            //check all cases
                                            if (output == 1){

                                                addToCart(unitNumberText, theFarmUnitPrice, theFarmROI);

                                            } else

                                            if (output == 0){

                                                //set layout
                                                alertDialog.dismiss();
                                                Toast.makeText(FarmDetails.this, "No internet connection", Toast.LENGTH_SHORT).show();

                                            } else

                                            if (output == 2){

                                                //set layout
                                                alertDialog.dismiss();
                                                Toast.makeText(FarmDetails.this, "No network detected", Toast.LENGTH_SHORT).show();

                                            }

                                        }).execute();

                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void addToCart(final int unitNumberText, final String theFarmUnitPrice, final String theFarmROI) {

        if (Common.checkKYC(FarmDetails.this).equalsIgnoreCase("Profile Complete")) {

            cartRef.child(currentUid)
                    .orderByChild("farmId")
                    .equalTo(farmId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.exists()) {

                                long theFixedPricePerUnit = Long.parseLong(theFarmUnitPrice);
                                int theFixedRoi = Integer.parseInt(theFarmROI);

                                /*---   CALCULATION   ---*/
                                long theCalculatedPrice = theFixedPricePerUnit * unitNumberText;
                                long totalCalculation = theCalculatedPrice * theFixedRoi / 100;
                                long totalResult = totalCalculation + theCalculatedPrice;

                                Map<String, Object> cartMap = new HashMap<>();
                                cartMap.put("totalPrice", theCalculatedPrice);
                                cartMap.put("totalPayout", totalResult);
                                cartMap.put("farmId", farmId);
                                cartMap.put("units", unitNumberText);

                                cartRef.child(currentUid)
                                        .push()
                                        .setValue(cartMap)
                                        .addOnCompleteListener(task -> {

                                            if (task.isSuccessful()){

                                                Toast.makeText(FarmDetails.this, "Added to cart", Toast.LENGTH_SHORT).show();

                                            }

                                        });

                            } else {

                                Toast.makeText(FarmDetails.this, "Already in cart", Toast.LENGTH_SHORT).show();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        } else {

            Toast.makeText(this, Common.checkKYC(FarmDetails.this), Toast.LENGTH_SHORT).show();

        }

    }

    private void followFarm() {

        final long date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy");
        final String dateString = sdf.format(date);

        Map<String, Object> followedMap = new HashMap<>();
        followedMap.put("dateFollowed", dateString);

        followedRef.child(currentUid)
                .child(farmId)
                .setValue(followedMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        subscribeToNotification(farmId);

                        followFarmBtn.setVisibility(View.GONE);
                        followedFarmButton.setVisibility(View.VISIBLE);

                    }

                });

    }

    private void subscribeToNotification(String farmId) {

        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            followedFarmNotiRef.child(theFarm.getFarmNotiId())
                                    .child(currentUid)
                                    .child("timestamp")
                                    .setValue(ServerValue.TIMESTAMP);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void calculateChanges(int unitNumberText, String theFarmUnitPrice, String theFarmRoi) {

        long theFixedPricePerUnit = Long.parseLong(theFarmUnitPrice);
        int theFixedRoi = Integer.parseInt(theFarmRoi);

        /*---   CALCULATION   ---*/
        long theCalculatedPrice = theFixedPricePerUnit * unitNumberText;
        long totalCalculation = theCalculatedPrice * theFixedRoi / 100;
        long totalResult = totalCalculation + theCalculatedPrice;

        unitPrice.setText(Common.convertToPrice(FarmDetails.this, theCalculatedPrice));
        totalPay.setText(Common.convertToPrice(FarmDetails.this, totalResult));
    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            alertDialog.dismiss();
        }
        finish();
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

}
