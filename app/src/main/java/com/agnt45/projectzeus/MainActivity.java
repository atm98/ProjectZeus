package com.agnt45.projectzeus;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button reqotp,valotp;
    private EditText phone,otp;
    private static final String TAG= "MainActivity";
    private boolean mVerificationInProcess = false;
    private String mVerificationId;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reqotp = findViewById(R.id.reqotp);
        valotp = findViewById(R.id.login);
        phone = findViewById(R.id.phoneno);
        otp = findViewById(R.id.onetimepasswd);
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                mVerificationInProcess = false;
                updateUI(STATE_VERIFY_SUCCESS,phoneAuthCredential);
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mVerificationInProcess = false;
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    phone.setError("Invalid Phone Number:");
                }else if(e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(MainActivity.this,"Quota exceeded",Toast.LENGTH_LONG).show();
                }
                updateUI(STATE_VERIFY_FAILED);
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                updateUI(STATE_CODE_SENT);

            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        if(mVerificationInProcess && validatePhoneNumber()){
            startPhoneNumberVerification(phone.getText().toString());
        }
    }

    private boolean validatePhoneNumber() {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS,mVerificationInProcess);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProcess =  savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);

    }

    private void startPhoneNumberVerification(String s) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(s,
                60
        , TimeUnit.SECONDS,
                MainActivity.this,
                mCallbacks);
        mVerificationInProcess = true;
    }

    /*private boolean validatePhoneNumber() {

        return fa
    }*/

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            updateUI(STATE_SIGNIN_SUCCESS, currentUser);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState) {
        updateUI(uiState,mAuth.getCurrentUser(),null);

    }

    private void updateUI(int uiState, FirebaseUser currentUser, PhoneAuthCredential o) {

    }

    private void signInWithPhoneAuthCredentials(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS,user);
                        }else{
                            if (task.getException() instanceof  FirebaseAuthInvalidCredentialsException){
                                otp.setError("INVALID OTP ");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private void updateUI(int stateVerifySuccess, PhoneAuthCredential phoneAuthCredential) {
        updateUI(stateVerifySuccess, null, phoneAuthCredential);
    }
    private void updateUI(int stateVerifySuccess, FirebaseUser user) {
        updateUI(stateVerifySuccess,user,null);
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredentials(credential);
    }
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }
}
