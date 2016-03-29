package hu.ursprung.socialnetworkloginsample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private TextView info;
    private ImageView imageView;

    //facebook login
    private static final int RC_FACEBOOK_LOGIN = 64206;
    private LoginButton loginButton;
    private CallbackManager facebookCallbackManager;

    //google
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private ProgressDialog mProgressDialog;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext(), RC_FACEBOOK_LOGIN);
        facebookCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);

        info = (TextView) findViewById(R.id.info);
        imageView = (ImageView) findViewById(R.id.imageView);

        initFacebookLoginButton();
        initGoogleSignInButton();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    public void onStart() {
        super.onStart();
        handleGoogleSilentSignIn();
    }

    private void handleGoogleSilentSignIn() {
        if (mGoogleApiClient != null) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        /*if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API) // required for App Folder sample
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        }*/
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //START google login
    private void initGoogleSignInButton() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestIdToken(getString(R.string.server_client_id))
                .build();

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
    }

    private void googleSignIn() {
        if (mGoogleApiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail().requestIdToken(getString(R.string.server_client_id))
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            info.setText(getString(R.string.connected));
        } else {
            // Signed out, show unauthenticated UI.
            info.setText(R.string.signed_in_err);
        }
    }

    // [START revokeAccess] for DISCONNECTING
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
    //END google login

    //START facebook login
    private void initFacebookLoginButton() {
        List<String> readPermissionNeeds = Arrays.asList("user_photos", "email", "user_birthday", "public_profile");
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(readPermissionNeeds);

        loginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                openDetailActivity();
                                /*try {
                                    info.setText("Hi, " + object.getString("name"));
                                    object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    String userId = object.getString("id");
                                    Picasso.with(getApplicationContext()).load("https://graph.facebook.com/" + object.getString("id") + "/picture?type=large").into(imageView);
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }*/
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
                info.setText(R.string.signed_in_err);
            }
        });
    }

    //facebook login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_FACEBOOK_LOGIN) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                mGoogleApiClient.connect();
                GoogleSignInAccount acct = result.getSignInAccount();
                Bundle bundle = new Bundle();
                bundle.putString("gDisplayName", acct.getDisplayName());
                bundle.putString("gId", acct.getId());
                bundle.putString("gPhoto", acct.getPhotoUrl().toString());
                bundle.putString("gEmail", acct.getEmail());
                bundle.putString("gIdToken", acct.getIdToken());
                openDetailActivity(bundle);
            }

        }
    }

    //google login
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, RC_SIGN_IN);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    //google login, callback method
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    //google login, callback method
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    private void openDetailActivity() {
        openDetailActivity(null);
    }

    private void openDetailActivity(Bundle bundle) {
        Intent detailActivity = new Intent(this, DetailActivity.class);
        if (bundle != null) {
            detailActivity.putExtras(bundle);
        }
        startActivity(detailActivity);
    }
}
