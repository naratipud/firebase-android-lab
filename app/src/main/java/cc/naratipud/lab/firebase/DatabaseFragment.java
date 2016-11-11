package cc.naratipud.lab.firebase;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cc.naratipud.lab.firebase.models.User;

public class DatabaseFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DatabaseFragment.class.getSimpleName();
    private View rootView;
    private RelativeLayout mRootLayout;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    public DatabaseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_database, container, false);
        bindViews();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check auth on start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_sign_in:
                signIn();
                break;
            case R.id.button_sign_up:
                signUp();
                break;
        }
    }

    private void bindViews() {
        // Views
        mRootLayout = (RelativeLayout) rootView.findViewById(R.id.fragment_database_layout);
        mEmailField = (TextInputEditText) rootView.findViewById(R.id.field_email);
        mPasswordField = (TextInputEditText) rootView.findViewById(R.id.field_password);

        // Listeners
        rootView.findViewById(R.id.button_sign_in).setOnClickListener(this);
        rootView.findViewById(R.id.button_sign_up).setOnClickListener(this);
    }

    private void signIn() {
        Log.d(TAG, "signIn");

        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());

                    hideProgressDialog();

                    if (task.isSuccessful()) {
                        onAuthSuccess(task.getResult().getUser());
                    } else {
                        Snackbar.make(mRootLayout, R.string.sign_in_failed, Snackbar.LENGTH_SHORT).show();
                    }
                });

    }

    private void signUp() {
        Log.d(TAG, "signUp");

        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    Log.d(TAG, "createUser:onComplete" + task.isSuccessful());

                    hideProgressDialog();

                    if (task.isSuccessful()) {
                        onAuthSuccess(task.getResult().getUser());
                    } else {
                        Snackbar.make(mRootLayout, R.string.sign_in_failed, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(getActivity(), e -> {
                    Snackbar.make(mRootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private boolean validateForm() {
        boolean valid = true;
        CharSequence email = mEmailField.getText();

        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else if (!isEmailPattern(email)) {
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

    private boolean isEmailPattern(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        String userEmail = user.getEmail();
        String username = usernameFromEmail(userEmail);

        // Write new user
        writeNewUser(user.getUid(), username, userEmail);

        // Go to PostActivity
        startActivity(new Intent(getActivity(), NewPostActivity.class));
        getActivity().finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void writeNewUser(String uid, String name, String email) {
        User user = new User(name, email);
        mDatabaseReference.child("users").child(uid).setValue(user);
    }

}
