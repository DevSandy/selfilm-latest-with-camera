package com.example.selfilm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment implements View.OnClickListener {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

     ImageView setting_btn;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_account, container, false);
        final TextView username,username2_txt,video_count_txt,biotxt,follow_count_txt,fan_count_txt,heart_count_txt,draft_count_txt;
        final CircleImageView userimage;
        userimage = view.findViewById(R.id.user_image);
        username = view.findViewById(R.id.username);
        username2_txt = view.findViewById(R.id.username2_txt);
        follow_count_txt = view.findViewById(R.id.follow_count_txt);
        video_count_txt = view.findViewById(R.id.video_count_txt);
        fan_count_txt = view.findViewById(R.id.fan_count_txt);
        heart_count_txt = view.findViewById(R.id.heart_count_txt);
        biotxt = view.findViewById(R.id.biotext);
        draft_count_txt = view.findViewById(R.id.draft_count_txt);
        FirebaseUser user = mAuth.getCurrentUser();
        String Uid = user.getUid();
        setting_btn=view.findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(this);



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

                           username.setText(newobj.getString("User_Name"));
                           username2_txt.setText(newobj.getString("User_Name"));
                           fan_count_txt.setText(newobj.getString("Followers_Count"));
                           follow_count_txt.setText(newobj.getString("Following_Count"));
                           heart_count_txt.setText(newobj.getString("Likes_Count"));
                           biotxt.setText("Bio : "+newobj.getString("Bio"));
                           draft_count_txt.setText("0");
                           video_count_txt.setText("0 Videos");
                           String Profilepicurl = newobj.getString("Profile_Picture");
                            Glide.with(getApplicationContext())
                                    .load(Profilepicurl) // image url
                                    .placeholder(R.drawable.personplaceholder) // any placeholder to load at start
                                    .error(R.drawable.logo)  // any image in case of error
                                    .override(200, 200) // resizing
                                    .centerCrop()
                                    .into(userimage);  // imageview object



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


        // Inflate the layout for this fragment

        return view;

    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting_btn:
                Open_Setting();
                break;

        }

    }
    public void Open_Setting(){

        Open_menu_tab(setting_btn);


    }
    public void Open_menu_tab(View anchor_view) {
        Context wrapper = new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom);
        PopupMenu popup = new PopupMenu(wrapper, anchor_view);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setGravity(Gravity.TOP | Gravity.RIGHT);
        }
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.edit_Profile_id:
                        Open_Edit_profile();
                        break;

                    case R.id.logout_id:
                        Logout();
                        break;

                }
                return true;
            }
        });
    }

    // this will erase all the user info store in locally and logout the user
    public void Logout(){

        FirebaseAuth.getInstance().signOut();
        getActivity().finish();
        startActivity(new Intent(getActivity(), Login.class));
    }
    public void Open_Edit_profile(){

        startActivity(new Intent(getActivity(), Editprofile.class));
    }
}
