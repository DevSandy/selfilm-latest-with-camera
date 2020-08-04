package com.example.selfilm;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class Editprofile extends AppCompatActivity {

    private static final String TAG = "";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CircleImageView profilepic;
    private EditText username_edit,firstname_edit,lastname_edit,user_bio_edit,insta_id;
    private RadioButton male_btn,female_btn;
    private TextView save_btn;
    private ImageButton Goback,upload_pic_btn;
    private Uri mCropImageUri;
    String imageuri;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("profilepicimages/"+ UUID.randomUUID().toString());
    String Profilepicurl;
    String usernamesr;
    String firstname;
    String lastname;
    String Gender;
    String bio;
    String gender;
    FirebaseUser user = mAuth.getCurrentUser();
    String Uid = user.getUid();
    private RadioGroup genderradiogroup;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        Goback = (ImageButton)findViewById(R.id.Goback);
        upload_pic_btn = (ImageButton)findViewById(R.id.upload_pic_btn);
        profilepic = (CircleImageView)findViewById(R.id.profile_image);
        save_btn = (TextView) findViewById(R.id.save_btn);
        genderradiogroup = (RadioGroup)findViewById(R.id.grnderradiogrp);
        username_edit = (EditText)findViewById(R.id.username_edit);
        firstname_edit = (EditText)findViewById(R.id.firstname_edit);
        lastname_edit = (EditText)findViewById(R.id.lastname_edit);
        user_bio_edit = (EditText)findViewById(R.id.user_bio_edit);
        male_btn = (RadioButton) findViewById(R.id.male_btn);
        female_btn = (RadioButton) findViewById(R.id.female_btn);
        insta_id = (EditText) findViewById(R.id.insta_id);



        Goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateprofile();

//                finish();
            }
        });

        upload_pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectImageClick(view);
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            object.put("UID",Uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Enter the correct url for your api service site
        String url = getResources().getString(R.string.getprofileurl);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            ArrayList<HashMap<String, String>> userList = new ArrayList<>();
                            JSONObject newobj = new JSONObject(String.valueOf(response.getJSONObject("body")));

                             Profilepicurl = newobj.getString("Profile_Picture");
                             usernamesr = newobj.getString("User_Name");
                             firstname = newobj.getString("First_Name");
                             lastname = newobj.getString("Last_Name");
                             Gender = newobj.getString("Gender");
                             bio = newobj.getString("Bio");
                             String instagramid = newobj.getString("Instagram_Id");

                            username_edit.setText(usernamesr);
                            firstname_edit.setText(firstname);
                            lastname_edit.setText(lastname);
                            user_bio_edit.setText(bio);
                            insta_id.setText(instagramid);

                            if (Gender.equals("Male")){
                                male_btn.setChecked(true);
                                gender = "Male";

                            }
                            else{
                                female_btn.setChecked(true);
                                gender = "Female";

                            }

                            Glide.with(getApplicationContext())
                                    .load(Profilepicurl) // image url
                                    .placeholder(R.drawable.personplaceholder) // any placeholder to load at start
                                    .error(R.drawable.logo)  // any image in case of error
                                    .override(200, 200) // resizing
                                    .centerCrop()
                                    .into(profilepic);  // imageview object
                        }
                        catch (JSONException ex){
                            Log.e("JsonParser Example","unexpected JSON exception", ex);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(this);
    }
    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ((ImageView) findViewById(R.id.profile_image)).setImageURI(result.getUri());
                imageuri = String.valueOf(result.getUri());
                uploadimage();
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Start crop image activity for the given image.
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    public void uploadimage(){
        UploadTask uploadTask = imagesRef.putFile(Uri.parse(imageuri));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Profilepicurl = uri.toString();

                            }
                        });
                    }
                }
            }});
    }
    public void updateprofile(){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();

        if (genderradiogroup.getCheckedRadioButtonId()==-1){
            gender="Not_Defined";
        }else {
            if (male_btn.isChecked()){
                gender = "Male";
            }
            if (female_btn.isChecked()){
                gender="Female";
            }
        }


//        if (male_btn.isChecked()){
//            gender = "Male";
//        }
//        if (female_btn.isChecked()){
//            gender = "Female";
//        }else {
//            gender = "Not Defined";
//        }
        try {
            //input your API parameters
            object.put("UID",Uid);
            object.put("First_Name",firstname_edit.getText().toString());
            object.put("Last_Name",lastname_edit.getText().toString());
            object.put("Bio",user_bio_edit.getText().toString());
            object.put("User_Name",username_edit.getText().toString());
            object.put("Instagram_Id",insta_id.getText().toString());
            object.put("Gender",gender);
            object.put("Profile_Picture",Profilepicurl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Enter the correct url for your api service site
        String url = getResources().getString(R.string.updateprofileurl);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(Editprofile.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Editprofile.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(jsonObjectRequest);

    }



}