package cc.naratipud.lab.firebase;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailPasswordActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = EmailPasswordActivity.class.getSimpleName();

    private LinearLayout mRootLayout;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);

        bindViews();

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {
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
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_create_account) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (id == R.id.button_sign_in) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (id == R.id.button_sign_out) {
            signOut();
        }
    }

    private void bindViews() {
        // Views
        mRootLayout = (LinearLayout) findViewById(R.id.activity_email_password);
        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);
        mEmailField = (TextInputEditText) findViewById(R.id.field_email);
        mPasswordField = (TextInputEditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_create_account).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        if (user != null) {
            mStatusTextView.setText(getString(R.string.fmt_status_email_password, user.getEmail()));
            mDetailTextView.setText(getString(R.string.fmt_status_firebase, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.button_sign_out).setVisibility(View.GONE);
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                        Snackbar.make(mRootLayout, R.string.auth_failed, Snackbar.LENGTH_SHORT).show();
                        mStatusTextView.setText(R.string.auth_failed);
                    }

                    hideProgressDialog();
                });
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);

        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                task -> {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Snackbar.make(mRootLayout, R.string.auth_failed, Snackbar.LENGTH_SHORT).show();
                    }

                    hideProgressDialog();
                });
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
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
