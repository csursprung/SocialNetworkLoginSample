package hu.ursprung.socialnetworkloginsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private TextView info;
    private ImageView imageView;

    //facebook login
    private LoginButton loginButton;
    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);

        info = (TextView) findViewById(R.id.info);
        imageView = (ImageView) findViewById(R.id.imageView);

        initFacebookLoginButton();

    }

    private void initFacebookLoginButton() {
        List<String> readPermissionNeeds = Arrays.asList("user_photos", "email", "user_birthday", "public_profile");
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(readPermissionNeeds);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    info.setText("Hi, " + object.getString("name"));
                                    object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    String userId = object.getString("id");
                                    Picasso.with(getApplicationContext()).load("https://graph.facebook.com/" + object.getString("id") + "/picture?type=large").into(imageView);
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

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
            }
        });
    }

    //facebook login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
