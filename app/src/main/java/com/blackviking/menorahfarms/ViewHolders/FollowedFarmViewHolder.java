package com.blackviking.menorahfarms.ViewHolders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class FollowedFarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView followedFarmImage;
    public TextView followedFarmType, followedFarmPrice, followedFarmState, followedFarmROI;
    public Button unfollowButton;

    public FollowedFarmViewHolder(@NonNull View itemView) {
        super(itemView);

        followedFarmImage = (ImageView)itemView.findViewById(R.id.followedFarmImage);
        followedFarmType = (TextView) itemView.findViewById(R.id.followedFarmType);
        followedFarmPrice = (TextView) itemView.findViewById(R.id.followedFarmPrice);
        followedFarmState = (TextView) itemView.findViewById(R.id.followedFarmState);
        followedFarmROI = (TextView) itemView.findViewById(R.id.followedFarmROI);
        unfollowButton = (Button) itemView.findViewById(R.id.unfollowButton);

        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
