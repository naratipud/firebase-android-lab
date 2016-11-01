package cc.naratipud.lab.firebase;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AnonymousAuthActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AnonymousAuthActivity.class.getSimpleName();

    private RelativeLayout mRootLayout;
    private TextView mStatusIdText;
    private TextView mEmailText;
    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mLinkAccountButton;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_auth);

        bindViews();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }

            updateUI(user);
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_anonymous_sign_in:
                signInAnonymously();
                break;
            case R.id.button_anonymous_sign_out:
                signOut();
                break;
            case R.id.button_link_account:
                linkAccount();
                break;
        }
    }

    private void bindViews() {
        // Views
        mRootLayout = (RelativeLayout) findViewById(R.id.activity_anonymous_auth);
        mStatusIdText = (TextView) findViewById(R.id.anonymous_status_id);
        mEmailText = (TextView) findViewById(R.id.anonymous_status_email);
        mEmailField = (TextInputEditText) findViewById(R.id.field_email);
        mPasswordField = (TextInputEditText) findViewById(R.id.field_password);

        // Buttons
        mSignInButton = (Button) findViewById(R.id.button_anonymous_sign_in);
        mSignOutButton = (Button) findViewById(R.id.button_anonymous_sign_out);
        mLinkAccountButton = (Button) findViewById(R.id.button_link_account);

        // Listeners
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mLinkAccountButton.setOnClickListener(this);
    }

    private void signInAnonymously() {
        Log.d(TAG, "signInAnonymously()");

        showProgressDialog();

        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
                Log.w(TAG, "signInAnonymously", task.getException());
                Snackbar.make(mRootLayout, R.string.auth_failed, Snackbar.LENGTH_SHORT).show();
            }

            hideProgressDialog();
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void linkAccount() {
        Log.d(TAG, "linkAccount()");

        if (!validateLinkForm()) {
            return;
        }

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        showProgressDialog();

        mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, task -> {
            Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
                Snackbar.make(mRootLayout, R.string.auth_failed, Snackbar.LENGTH_SHORT).show();
            }

            hideProgressDialog();
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        boolean isSignedIn = (user != null);

        if (isSignedIn) {
            mStatusIdText.setText(getString(R.string.id_fmt, user.getUid()));
            mEmailText.setText(getString(R.string.email_fmt, user.getEmail()));
        } else {
            mStatusIdText.setText(R.string.signed_out);
            mEmailText.setText(null);
        }

        // Button visibility
        mSignInButton.setEnabled(!isSignedIn);
        mSignOutButton.setEnabled(isSignedIn);
        mLinkAccountButton.setEnabled(isSignedIn);
    }

    private boolean validateLinkForm() {
        boolean valid = true;
        CharSequence email = mEmailField.getText();

        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else if (!emailValidator(email)) {
            mEmailField.setError("Invalid email pattern.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText())) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private boolean emailValidator(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
