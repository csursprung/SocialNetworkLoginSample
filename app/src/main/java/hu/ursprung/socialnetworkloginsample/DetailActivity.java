package hu.ursprung.socialnetworkloginsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ucs on 2016.03.06..
 */
public class DetailActivity extends AppCompatActivity {

    private ImageView profilePicture;

    private TextView userId;

    private TextView userName;

    private TextView userEmail;

    private LoginButton loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        userId = (TextView) findViewById(R.id.userId);
        userName = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.userEmail);
        profilePicture = (ImageView) findViewById(R.id.profilePicture);

        if (AccessToken.getCurrentAccessToken() != null) {
            loggedInByFacebook();
        } else {

        }
    }

    private void loggedInByFacebook() {
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
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
                            userId.setText(facebookUserId);
                            userName.setText(object.getString("name"));
                            userEmail.setText(object.getString("email"));

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
}
