package mobi.devteam.demofalldetector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 243;
    private final String TAG = "LoginActivity";
    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.txtUserName)
    EditText edtUserName;
    @BindView(R.id.txtPassword)
    EditText edtPassword;
    @BindView(R.id.txtForgetPassword)
    TextView txtForgetPassword;
    @BindView(R.id.btnSignUp)
    Button btnSignUp;
    @BindView(R.id.btnSignInWithGoogle)
    SignInButton btnSignInWithGoogle;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.my_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        mAuth = FirebaseAuth.getInstance();

        progressBar.setVisibility(View.GONE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goto_main_activity();
        }
    }

    private void goto_main_activity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btnSignUp)
    void sign_up() {
        final String email = edtUserName.getText().toString();
        final String password = edtPassword.getText().toString();

        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(this, getString(R.string.err_email_password_null), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            goto_main_activity();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @OnClick(R.id.btnSignIn)
    void sign_in() {
        final String email = edtUserName.getText().toString();
        final String password = edtPassword.getText().toString();
        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(this, getString(R.string.err_email_password_null), Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.err_invalid_info_login,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            goto_main_activity();
                        }

                    }
                });
    }

    @OnClick(R.id.btnSignInWithGoogle)
    public void login_with_google() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Log.e(TAG, result.getStatus().toString());
                Toast.makeText(this, getString(R.string.err_login), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
        Toast.makeText(this, R.string.err_login, Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            goto_main_activity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.signin_fail,
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
