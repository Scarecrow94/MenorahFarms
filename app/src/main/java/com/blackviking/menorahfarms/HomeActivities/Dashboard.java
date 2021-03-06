package com.blackviking.menorahfarms.HomeActivities;

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

import com.blackviking.menorahfarms.AdminDash;
import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.DashboardMenu.AccountManager;
import com.blackviking.menorahfarms.DashboardMenu.Faq;
import com.blackviking.menorahfarms.DashboardMenu.FarmUpdates;
import com.blackviking.menorahfarms.DashboardMenu.FollowedFarms;
import com.blackviking.menorahfarms.DashboardMenu.Notifications;
import com.blackviking.menorahfarms.DashboardMenu.SponsoredFarms;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.SignIn;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class Dashboard extends AppCompatActivity {

    private LinearLayout dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView dashboardText, farmstoreText, accountText;

    private TextView welcome, sponsorCycle, totalReturnsText, nextEndOfCycleDate, cartItemCount;
    private ImageView cartButton;
    private CircleImageView userAvatar;
    private RelativeLayout goToFarmstoreButton;
    private RelativeLayout sponsoredFarmsLayout, farmsToWatchLayout, farmUpdatesLayout, allFarmsLayout, projectManagerLayout, notificationLayout, faqLayout, adminLayout;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsoredRef, cartRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUid;
    private UserModel paperUser;
    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;

    //google
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        /*---   FIREBASE   ---*/
        userRef = db.getReference(Common.USERS_NODE);
        sponsoredRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        cartRef = db.getReference(Common.CART_NODE);
        currentUid = Paper.book().read(Common.USER_ID);

        //sub to notification
        FirebaseMessaging.getInstance().subscribeToTopic(Common.GENERAL_NOTIFY);


        /*---   WIDGETS   ---*/
        dashboardSwitch = findViewById(R.id.dashboardLayout);
        farmstoreSwitch = findViewById(R.id.farmShopLayout);
        accountSwitch = findViewById(R.id.accountLayout);
        dashboardText = findViewById(R.id.dashboardText);
        farmstoreText = findViewById(R.id.farmShopText);
        accountText = findViewById(R.id.accountText);


        sponsorCycle = findViewById(R.id.userSponsorCycle);
        welcome = findViewById(R.id.userWelcome);
        totalReturnsText = findViewById(R.id.totalReturns);
        nextEndOfCycleDate = findViewById(R.id.nextEndOfCycleDate);
        cartButton = findViewById(R.id.userCart);
        userAvatar = findViewById(R.id.userAvatar);
        goToFarmstoreButton = findViewById(R.id.goToFarmstoreButton);
        sponsoredFarmsLayout = findViewById(R.id.sponsoredFarmsLayout);
        farmsToWatchLayout = findViewById(R.id.farmsToWatchLayout);
        farmUpdatesLayout = findViewById(R.id.farmUpdatesLayout);
        allFarmsLayout = findViewById(R.id.allFarmsLayout);
        projectManagerLayout = findViewById(R.id.projectManagerLayout);
        notificationLayout = findViewById(R.id.notificationLayout);
        faqLayout = findViewById(R.id.faqLayout);
        adminLayout = findViewById(R.id.adminLayout);
        cartItemCount = findViewById(R.id.cartItemCount);


        /*---   BOTTOM NAV   ---*/
        dashboardText.setTextColor(getResources().getColor(R.color.colorPrimary));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountText.setTextColor(getResources().getColor(R.color.black));



        /*---   GOOGLE INIT   ---*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, connectionResult -> Toast.makeText(Dashboard.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //open farm shop
        farmstoreSwitch.setOnClickListener(v -> {

            Intent farmstoreIntent = new Intent(Dashboard.this, FarmShop.class);
            startActivity(farmstoreIntent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //open account
        accountSwitch.setOnClickListener(v -> {

            Intent accountIntent = new Intent(Dashboard.this, Account.class);
            startActivity(accountIntent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //cart
        cartButton.setOnClickListener(v -> {

            Intent cartIntent = new Intent(Dashboard.this, Cart.class);
            startActivity(cartIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //farmshop
        goToFarmstoreButton.setOnClickListener(v -> {

            Intent farmShopIntent = new Intent(Dashboard.this, FarmShop.class);
            startActivity(farmShopIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //sponsored farms
        sponsoredFarmsLayout.setOnClickListener(v -> {

            Intent sponsoredFarmsIntent = new Intent(Dashboard.this, SponsoredFarms.class);
            startActivity(sponsoredFarmsIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //followed farms
        farmsToWatchLayout.setOnClickListener(v -> {

            Intent followedFarmsIntent = new Intent(Dashboard.this, FollowedFarms.class);
            startActivity(followedFarmsIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //farm updates
        farmUpdatesLayout.setOnClickListener(v -> {

            Intent farmUpdatesIntent = new Intent(Dashboard.this, FarmUpdates.class);
            startActivity(farmUpdatesIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //farm shop again
        allFarmsLayout.setOnClickListener(v -> {

            Intent farmShopIntent = new Intent(Dashboard.this, FarmShop.class);
            startActivity(farmShopIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //project manager
        projectManagerLayout.setOnClickListener(v -> {

            Intent projectManagerIntent = new Intent(Dashboard.this, AccountManager.class);
            startActivity(projectManagerIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //notifications
        notificationLayout.setOnClickListener(v -> {

            Intent notificationIntent = new Intent(Dashboard.this, Notifications.class);
            startActivity(notificationIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //faqs
        faqLayout.setOnClickListener(v -> {

            Intent faqIntent = new Intent(Dashboard.this, Faq.class);
            startActivity(faqIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });


        //show loading dialog
        showLoadingDialog("Fetching details . . .");


        //load user
        loadUser(currentUid);

    }

    private void loadUser(String currentUid) {

        //local user profile
        paperUser = Paper.book().read(Common.PAPER_USER);

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                //always update latest from server
                userRef.child(currentUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                UserModel theUser = dataSnapshot.getValue(UserModel.class);

                                if (theUser != null) {

                                    ((ApplicationClass) (getApplicationContext())).setUser(theUser);

                                    //update current local profile
                                    paperUser = theUser;

                                    //set user profile
                                    setUser(paperUser);

                                    //set other details
                                    setOtherDetails();

                                    //dismiss dialog
                                    alertDialog.dismiss();

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            } else

            if (output == 0){

                if (paperUser != null) {
                    //close loading dialog
                    alertDialog.dismiss();

                    setUser(paperUser);
                    Toast.makeText(Dashboard.this, "No internet", Toast.LENGTH_SHORT).show();

                } else {

                    alertDialog.dismiss();
                    Toast.makeText(this, "Details could not be retrieved, please try again later.", Toast.LENGTH_LONG).show();

                }

            } else

            if (output == 2){

                if (paperUser != null) {

                    //close loading dialog
                    alertDialog.dismiss();

                    setUser(paperUser);
                    Toast.makeText(Dashboard.this, "Please connect to a network", Toast.LENGTH_SHORT).show();

                } else {

                    alertDialog.dismiss();
                    Toast.makeText(this, "Details could not be retrieved, please try again later.", Toast.LENGTH_LONG).show();

                }

            }

        }).execute();

    }

    private void setOtherDetails() {

        //sponsored farm count
        sponsoredRef.child(currentUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    long totalReturn = 0;
                                    int sponsorCount = (int) dataSnapshot.getChildrenCount();
                                    sponsorCycle.setText("Running Cycles : " + sponsorCount);

                                    for (DataSnapshot user : dataSnapshot.getChildren()){

                                        long theReturn = Long.parseLong(user.child("sponsorReturn").getValue().toString());
                                        totalReturn = totalReturn + theReturn;

                                    }

                                    totalReturnsText.setText(Common.convertToPrice(Dashboard.this, totalReturn));


                                } else {

                                    sponsorCycle.setText("Running Cycles : 0");
                                    totalReturnsText.setText("₦ 0.00");

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        //next cycle end
        sponsoredRef.child(currentUid)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                    String theKey = snap.getKey();

                                    if (dataSnapshot.exists()) {

                                        String date = dataSnapshot.child(theKey).child("cycleEndDate").getValue().toString();
                                        nextEndOfCycleDate.setText(date);

                                    } else {

                                        nextEndOfCycleDate.setText("Not Available");

                                    }

                                }



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        //cart item count
        cartRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int count = (int) dataSnapshot.getChildrenCount();

                        cartItemCount.setText(String.valueOf(count));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void setUser(final UserModel paperUser) {

        //welcome message
        welcome.setText("Hi, " + paperUser.getFirstName());

        //user profile pic
        if (!paperUser.getProfilePictureThumb().equalsIgnoreCase("")){

            Picasso.get()
                    .load(paperUser.getProfilePictureThumb())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile)
                    .into(userAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(paperUser.getProfilePictureThumb())
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar);
                        }

                    });

        } else {

            userAvatar.setImageResource(R.drawable.profile);

        }

        //user type
        if (paperUser.getUserType().equalsIgnoreCase("Admin")){

            //show admin portal
            adminLayout.setEnabled(true);
            adminLayout.setVisibility(View.VISIBLE);
            adminLayout.setOnClickListener(v -> {
                Intent adminIntent = new Intent(Dashboard.this, AdminDash.class);
                startActivity(adminIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            });

        } else if (paperUser.getUserType().equalsIgnoreCase("Banned")) {

            if (paperUser.getSignUpMode().equalsIgnoreCase("Google")){

                //revoke access
                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);

                Paper.book().destroy();

                mAuth.signOut();
                ((ApplicationClass)(getApplicationContext())).resetUser();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GENERAL_NOTIFY);
                Intent signoutIntent = new Intent(Dashboard.this, SignIn.class);
                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signoutIntent);

                finish();

            } else {

                Paper.book().destroy();

                mAuth.signOut();
                ((ApplicationClass)(getApplicationContext())).resetUser();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GENERAL_NOTIFY);
                Intent signoutIntent = new Intent(Dashboard.this, SignIn.class);
                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signoutIntent);

                finish();

            }

        } else {

            adminLayout.setVisibility(View.INVISIBLE);
            adminLayout.setEnabled(false);

        }

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
    protected void onResume() {
        super.onResume();
        loadUser(currentUid);
    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            alertDialog.dismiss();
        }
        finish();
    }
}
