package com.boss.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.core.Path;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_IMAGE = 345;
    private EditText mTextTitle;
    private EditText mTextPrice;
    private EditText mTextDescription;

    public static final String EXTRAS_TRAVEL_DEAL = "com.boss.travelmantics.extras.EXTRAS_TRAVEL_DEAL";
    private TravelDeal mDeal;
    private DatabaseReference mDatabaseReference;
    private ImageView mImageView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mImageView = findViewById(R.id.imageView);
        mTextTitle = findViewById(R.id.textTitle);
        mTextPrice = findViewById(R.id.textPrice);
        mTextDescription = findViewById(R.id.textDescription);

        mDatabaseReference = FirebaseUtil.xDatabaseReference;
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

        Intent intent = getIntent();
//        if (intent != null)
        TravelDeal travelDeal = (TravelDeal) intent.getSerializableExtra(EXTRAS_TRAVEL_DEAL);
        mDeal = new TravelDeal();
        if (travelDeal != null){
            this.mDeal = travelDeal;
            Log.d("show me love", "onCreate: " + mDeal.getId());
            mTextTitle.setText(mDeal.getTitle());
            mTextPrice.setText(mDeal.getPrice());
            mTextDescription.setText(mDeal.getDescription());
            showImage(mDeal.getImageUrl());
        }

        mButton = findViewById(R.id.btnImage);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "choose a picture"), REQUEST_CODE_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK){

            final LinearLayout linearLayout = findViewById(R.id.linear_overlay);
            linearLayout.setVisibility(View.VISIBLE);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            final ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            Uri imageUri = data.getData();

            StorageReference ref = FirebaseUtil.xStorageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final String pictureName = taskSnapshot.getStorage().getPath();
                    Log.d("show me love", "onSuccess: picture name: " + pictureName);
                    mDeal.setImageName(pictureName);
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            showImage(task.getResult().toString());
                            mDeal.setImageUrl(task.getResult().toString());
                            Log.d("show me love", "onSuccess: url: "
                                    + task.getResult().toString() + " - "
                                    + mDeal.getImageUrl()
                            );
                        }
                    });

                    progressBar.setVisibility(View.INVISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    linearLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        MenuItem deleteMenuItem = menu.findItem(R.id.delete_deal_menu);
        MenuItem saveMenuItem = menu.findItem(R.id.save_menu);
        if(FirebaseUtil.isAdmin){
            deleteMenuItem.setVisible(true);
            saveMenuItem.setVisible(true);
            enableEditText(true);
            mButton.setEnabled(true);
        }else{
            deleteMenuItem.setVisible(false);
            saveMenuItem.setVisible(false);
            enableEditText(false);
            mButton.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                clean();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG)
                        .show();
                return true;
            case R.id.delete_deal_menu:
                deleteDeal();
                backToList();
                return true;
        default:
            return super.onOptionsItemSelected(item);
        }

    }

    private void enableEditText(boolean isEnabled){
        mTextTitle.setEnabled(isEnabled);
        mTextPrice.setEnabled(isEnabled);
        mTextDescription.setEnabled(isEnabled);
    }

    private void deleteDeal() {
        if (mDeal == null) {
            Toast.makeText(this, "You need to save this deal first", Toast.LENGTH_LONG)
                    .show();
        } else {
            mDatabaseReference.child(mDeal.getId()).removeValue();

            String imageName = mDeal.getImageName();
            if(imageName != null && !imageName.isEmpty()){
                FirebaseUtil.xFirebaseStorage.getReference().child(imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("show me love", "onSuccess: successfully deleted file");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("show me love", "onFailure: failed to delete file");
                    }
                });
            }
        }

    }

    private void backToList() {
        startActivity(new Intent(this, ListActivity.class));
    }

    private void saveDeal() {
        mDeal.setTitle(mTextTitle.getText().toString());
        mDeal.setDescription(mTextDescription.getText().toString());
        mDeal.setPrice(mTextPrice.getText().toString());
        if(mDeal.getId()==null) {
            mDatabaseReference.push().setValue(mDeal);
        }
        else {
//            DatabaseReference databaseReference = mDatabaseReference.child(mDeal.getId());
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
            Log.d("show me love", "saveDeal: " + mDeal.getId());
            Log.d("show me love", "saveDeal: " + mDeal.getImageUrl());
        }

    }

    private void clean() {
        mTextTitle.setText("");
        mTextPrice.setText("");
        mTextDescription.setText("");
        mImageView.setImageDrawable(null);
        mTextTitle.requestFocus();
    }

    private void showImage(String url) {
        Log.d("show me love", "showImage: " + url);
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(mImageView);
        }
    }
}
