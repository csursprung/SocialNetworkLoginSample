package hu.ursprung.socialnetworkloginsample;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ucs on 2016.03.06..
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getCanonicalName();

    private ImageView profilePicture;

    private TextView userId;

    private TextView userName;

    private TextView userEmail;

    private Button buttonSignOut;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        userId = (TextView) findViewById(R.id.userId);
        userName = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.userEmail);
        profilePicture = (ImageView) findViewById(R.id.profilePicture);
        buttonSignOut = (Button) findViewById(R.id.sign_out_button);
        ;

        if (AccessToken.getCurrentAccessToken() != null) {
            loggedInByFacebook();
        } else if (getIntent().getExtras() != null) {
            loggedInByGoogle();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Detail Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://hu.ursprung.socialnetworkloginsample/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    private void loggedInByGoogle() {
        Bundle bundle = getIntent().getExtras();
        String displayName = bundle.getString("gDisplayName");
        String gId = bundle.getString("gId");
        String photoUrl = bundle.getString("gPhoto");
        String gEmail = bundle.getString("gEmail");
        String gIdToken = bundle.getString("gIdToken");

        Picasso.with(getApplicationContext()).load(photoUrl).into(profilePicture);
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        User user = getUser(gId, displayName, gEmail, null, gIdToken);

        new UserLoginTask(user).execute();

        //updateUI(gId, displayName, gEmail);
    }


    private void loggedInByFacebook() {
        buttonSignOut.setVisibility(View.VISIBLE);
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                        .Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        LoginManager.getInstance().logOut();
                        openLoginActivity();
                    }
                }).executeAsync();
            }
        });
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String facebookUserId = object.getString("id");

                            User user = getUser(facebookUserId, object.getString("name"), object.getString("email"), AccessToken.getCurrentAccessToken().getToken(), null);
                            new UserLoginTask(user).execute();
                            //updateUI(facebookUserId, object.getString("name"), object.getString("email"));

                            //object.getJSONObject("picture").getJSONObject("data").getString("url");

                            Picasso.with(getApplicationContext()).load("https://graph.facebook.com/" + facebookUserId + "/picture?type=large").into(profilePicture);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture.type(large),id,name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void openLoginActivity() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        startActivity(loginActivity);
    }

    private User getUser(String id, String name, String email, String fbToken, String gToken) {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        user.fbToken = fbToken;
        user.gToken = gToken;
        return user;
    }

    private void updateUI(String id, String name, String email) {
        userId.setText(id);
        userName.setText(name);
        userEmail.setText(email);
    }

    private class User {
        String id;
        String name;
        String email;
        String fbToken;
        String gToken;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mId;
        private final String mUsername;
        private final String mEmail;
        private final String mFbToken;
        private final String mGToken;

        UserLoginTask(User user) {
            mId = user.id;
            mUsername = user.name;
            mEmail = user.email;
            mFbToken = user.fbToken;
            mGToken = user.gToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            try {
                // Simulate network access.
                Thread.sleep(1/*2000*/);


                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://ursprung.hu/auth/index.php");

                try {
                    List nameValuePairs = new ArrayList(1);
                    if (mFbToken != null && mFbToken.length() > 0) {
                        nameValuePairs.add(new BasicNameValuePair("tag", "fbLogin"));
                        nameValuePairs.add(new BasicNameValuePair("fbToken", mFbToken));
                    } else if (mGToken != null && mGToken.length() > 0) {
                        nameValuePairs.add(new BasicNameValuePair("tag", "gLogin"));
                        nameValuePairs.add(new BasicNameValuePair("gToken", mGToken));
                    }

                    nameValuePairs.add(new BasicNameValuePair("userId", mId));
                    nameValuePairs.add(new BasicNameValuePair("username", mUsername));
                    nameValuePairs.add(new BasicNameValuePair("userEmail", mEmail));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);
                    int statusCode = response.getStatusLine().getStatusCode();

                    final String responseBody = EntityUtils.toString(response.getEntity());

                    JSONObject jObj = new JSONObject(responseBody);
                    String tag = String.valueOf(jObj.get("tag"));
                    String success = String.valueOf(jObj.get("success"));
                    String error = String.valueOf(jObj.get("error"));
                    String reFbToken = String.valueOf(jObj.get("reFbToken"));

                    if (mFbToken != null && mFbToken.length() > 0 && reFbToken != null && reFbToken.equals(mFbToken)) {
                        Log.i(TAG, "Signed in as: " + mUsername);
                    }


                } catch (ClientProtocolException e) {
                    Log.e(TAG, "Error sending ID token to backend.", e);
                } catch (IOException e) {
                    Log.e(TAG, "Error sending ID token to backend.", e);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (InterruptedException e) {
                return false;
            }


            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            updateUI(mId, mUsername, mEmail);
           /* if (success) {
                finish();
            } else {

            }*/
        }

        @Override
        protected void onCancelled() {

        }
    }


    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Detail Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://hu.ursprung.socialnetworkloginsample/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
