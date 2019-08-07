package com.boss.travelmantics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    private static final String TAG = "DealAdapter";
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ArrayList<TravelDeal> mDeals;

    public DealAdapter(Activity context){
        mContext = context;
        mDeals = new ArrayList<>();
        mDatabaseReference = FirebaseUtil.xDatabaseReference;
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal tDeal = dataSnapshot.getValue(TravelDeal.class);
                tDeal.setId(dataSnapshot.getKey());
                mDeals.add(tDeal);
                Log.d(TAG, "onChildAdded: "+ tDeal.getTitle() + mDeals.size());
                notifyItemInserted(mDeals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.td_row, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal travelDeal = mDeals.get(position);
        holder.bind(travelDeal);
    }

    @Override
    public int getItemCount() {
        return mDeals == null ? 0 : mDeals.size();
    }

    class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mTextTitle;
        TextView mTextPrice;
        TextView mTextDescription;
        ImageView mImageView;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextTitle = itemView.findViewById(R.id.tvTitle);
            mTextPrice = itemView.findViewById(R.id.tvPrice);
            mTextDescription = itemView.findViewById(R.id.tvDescription);
            mImageView = itemView.findViewById(R.id.imageDeal);

            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal travelDeal){
            mTextTitle.setText(travelDeal.getTitle());
            mTextPrice.setText(travelDeal.getPrice());
            mTextDescription.setText(travelDeal.getDescription());
            showImage(travelDeal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            TravelDeal travelDeal = mDeals.get(getAdapterPosition());
            Intent intent = new Intent(mContext, DealActivity.class);
            intent.putExtra(DealActivity.EXTRAS_TRAVEL_DEAL, travelDeal);
            v.getContext().startActivity(intent);
        }

        private void showImage(String url) {
            if (url != null && url.isEmpty()==false) {
                Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.placeholder_image)
                        .resize(160, 160)
                        .centerCrop()
                        .into(mImageView);
            }
        }
    }
}
