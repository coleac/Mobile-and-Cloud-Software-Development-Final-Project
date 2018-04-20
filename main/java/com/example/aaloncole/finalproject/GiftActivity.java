package com.example.aaloncole.finalproject;

import android.graphics.PorterDuff;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.aaloncole.finalproject.R.id.list_layout_recipient_text;

public class GiftActivity extends AppCompatActivity {

	public String apiString = "Removed for Github";
	public String OAuthString = "Removed for Github";
	
    private AuthorizationService mAuthorizationService;
    private AuthState mAuthState;
    private OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        setContentView(R.layout.activity_gift);
        mAuthorizationService = new AuthorizationService(this);

        Button listButton = (Button) findViewById(R.id.get_list_button);
        listButton.getBackground().setColorFilter(0xFFF49004, PorterDuff.Mode.MULTIPLY);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://finalproject-186021.appspot.com/gifts");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", apiString).build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .addHeader("IdToken", idToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();

                                        try {
                                            List<Map<String, String>> s_lists = new ArrayList<Map<String, String>>();
                                            JSONArray g = new JSONArray(r);
                                            for (int i = 0; i < g.length(); i++) {
                                                HashMap<String, String> m = new HashMap<String, String>();
                                                JSONObject j = g.getJSONObject(i);
                                                m.put("gift", j.getString("gift"));
                                                m.put("price", j.getString("price"));
                                                m.put("recipient", j.getString("recipient"));
                                                m.put("giftwrapped", j.getString("giftwrapped"));
                                                m.put("id", j.getString("id"));
                                                s_lists.add(m);
                                            }
                                            final SimpleAdapter postAdapter = new SimpleAdapter(
                                                    GiftActivity.this,
                                                    s_lists,
                                                    R.layout.santa_list,
                                                    new String[]{"recipient", "gift", "price", "giftwrapped", "id"},
                                                    new int[]{list_layout_recipient_text, R.id.list_layout_gift_text, R.id.list_layout_price_text, R.id.list_layout_giftwrapped_text, R.id.list_layout_id_text});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.santa_list)).setAdapter(postAdapter);
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Clickable list items
        ListView listView1 = (ListView) findViewById(R.id.santa_list);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id){

                TextView txtview1 = (TextView) view.findViewById(R.id.list_layout_recipient_text);
                String item1 = txtview1.getText().toString();
                EditText editText1 = (EditText) findViewById(R.id.recipient_input);
                editText1.setText(String.valueOf(item1));

                TextView txtview2 = (TextView) view.findViewById(R.id.list_layout_gift_text);
                String item2 = txtview2.getText().toString();
                EditText editText2 = (EditText) findViewById(R.id.gift_input);
                editText2.setText(String.valueOf(item2));

                TextView txtview3 = (TextView) view.findViewById(R.id.list_layout_price_text);
                String item3 = txtview3.getText().toString();
                EditText editText3 = (EditText) findViewById(R.id.price_input);
                editText3.setText(String.valueOf(item3));

                TextView txtview4 = (TextView) view.findViewById(R.id.list_layout_giftwrapped_text);
                String item4 = txtview4.getText().toString();
                EditText editText4 = (EditText) findViewById(R.id.giftwrapped_input);
                editText4.setText(String.valueOf(item4));

                TextView txtview5 = (TextView) view.findViewById(R.id.list_layout_id_text);
                String item5 = txtview5.getText().toString();
                EditText editText5 = (EditText) findViewById(R.id.id_input);
                editText5.setText(String.valueOf(item5));

            }
        });

        Button clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.getBackground().setColorFilter(0x00000000, PorterDuff.Mode.MULTIPLY);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editText6 = (EditText) findViewById(R.id.id_input);
                editText6.getText().clear();

                EditText editText7 = (EditText) findViewById(R.id.recipient_input);
                editText7.getText().clear();

                EditText editText8 = (EditText) findViewById(R.id.gift_input);
                editText8.getText().clear();

                EditText editText9 = (EditText) findViewById(R.id.price_input);
                editText9.getText().clear();

                EditText editText0 = (EditText) findViewById(R.id.giftwrapped_input);
                editText0.getText().clear();
            }
        });


        Button addGiftButton = (Button) findViewById(R.id.add_gift_button);
        addGiftButton.getBackground().setColorFilter(0xFF12c70b, PorterDuff.Mode.MULTIPLY);
        addGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://finalproject-186021.appspot.com/gifts");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", apiString).build();
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("recipient", ((EditText)findViewById(R.id.recipient_input)).getText().toString());
                                    obj.put("gift", ((EditText)findViewById(R.id.gift_input)).getText().toString());
                                    obj.put("price", ((EditText)findViewById(R.id.price_input)).getText().toString());
                                    obj.put("giftwrapped", ((EditText)findViewById(R.id.giftwrapped_input)).getText().toString());
                                    String json = obj.toString();
                                    RequestBody body = RequestBody.create(JSON, json);
                                    Request request = new Request.Builder()
                                            .url(reqUrl)
                                            .addHeader("Authorization", "Bearer " + accessToken)
                                            .addHeader("IdToken", idToken)
                                            .post(body)
                                            .build();
                                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            String r = response.body().string();
                                            Integer c = response.code();
                                            if (c == 201) {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Added Gift", Toast.LENGTH_SHORT).show();
                                                    }

                                                });
                                            }
                                            else {

                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Error- Unable to add Gift", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button deleteGiftButton = (Button) findViewById(R.id.delete_gift_button);
        deleteGiftButton.getBackground().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
        deleteGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mOkHttpClient = new OkHttpClient();
                                String id = ((EditText)findViewById(R.id.id_input)).getText().toString();
                                HttpUrl reqUrl = HttpUrl.parse("https://finalproject-186021.appspot.com/gifts/" + id);
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", apiString).build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .addHeader("IdToken", idToken)
                                        .delete()
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        Integer c = response.code();
                                        if (c == 204) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Gift Deleted", Toast.LENGTH_SHORT).show();
                                                }

                                            });
                                        }
                                        else {

                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Error- Unable to delete Gift", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button editGiftButton = (Button) findViewById(R.id.edit_gift_button);
        editGiftButton.getBackground().setColorFilter(0xffffff00, PorterDuff.Mode.MULTIPLY);
        editGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                mOkHttpClient = new OkHttpClient();
                                String id = ((EditText)findViewById(R.id.id_input)).getText().toString();
                                HttpUrl reqUrl = HttpUrl.parse("https://finalproject-186021.appspot.com/gifts/" + id);
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", apiString).build();
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("recipient", ((EditText)findViewById(R.id.recipient_input)).getText().toString());
                                    obj.put("gift", ((EditText)findViewById(R.id.gift_input)).getText().toString());
                                    obj.put("price", ((EditText)findViewById(R.id.price_input)).getText().toString());
                                    obj.put("giftwrapped", ((EditText)findViewById(R.id.giftwrapped_input)).getText().toString());
                                    String json = obj.toString();
                                    RequestBody body = RequestBody.create(JSON, json);
                                    Request request = new Request.Builder()
                                            .url(reqUrl)
                                            .addHeader("Authorization", "Bearer " + accessToken)
                                            .addHeader("IdToken", idToken)
                                            .patch(body)
                                            .build();
                                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            String r = response.body().string();
                                            Integer c = response.code();
                                            if (c == 200) {
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Gift Updated", Toast.LENGTH_SHORT).show();
                                                    }

                                                });
                                            }
                                            else {

                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Error- Unable to edit Gift", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart(){
        mAuthState = getOrCreateAuthState();
        super.onStart();
    }

    AuthState getOrCreateAuthState(){
        AuthState auth = null;
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPreference.getString("stateJson", null);
        if(stateJson != null){
            try {
                auth = AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        if( auth != null && auth.getAccessToken() != null){
            return auth;
        } else {
            updateAuthState();
            Toast.makeText(getApplicationContext(), "Log In", Toast.LENGTH_SHORT).show();
            return null;

        }
    }

    void updateAuthState(){

        Uri authEndpoint = new Uri.Builder().scheme("https").authority("accounts.google.com").path("/o/oauth2/v2/auth").build();
        Uri tokenEndpoint = new Uri.Builder().scheme("https").authority("www.googleapis.com").path("/oauth2/v4/token").build();
        Uri redirect = new Uri.Builder().scheme("com.example.aaloncole.finalproject").path("foo").build();

        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint, null);
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, OAuthString, ResponseTypeValues.CODE, redirect)
                .setScopes("https://www.googleapis.com/auth/plus.me", "https://www.googleapis.com/auth/plus.profile.emails.read")
                .build();

        Intent authComplete = new Intent(this, AuthCompleteActivity.class);
        mAuthorizationService.performAuthorizationRequest(req, PendingIntent.getActivity(this, req.hashCode(), authComplete, 0));
    }
}

//Sources http://www.ezzylearning.com/tutorial/handling-android-listview-onitemclick-event
//https://stackoverflow.com/questions/1521640/standard-android-button-with-a-different-color
//Code used with permission from: CS496-400 Week 8 Content