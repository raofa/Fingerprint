package comaidlservice.sunyard.vi218i.fingerprinttest;


import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;

public class FingerprintFragment extends DialogFragment {
    private FingerprintManager mfingerprintManager;
    private CancellationSignal mcancellationSignal;
    private Cipher mcipher;
    private LoginActivity mactivity;
    private TextView errmsg;
    
    private boolean isSefCancel ;
    
    public void setCipher(Cipher cipher){
        mcipher=cipher;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mactivity =(LoginActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mfingerprintManager = getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Material_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.finger_dialog,container,false);
        errmsg = view.findViewById(R.id.error_msg);
        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                stopListening();
            }
        });
        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startListening(mcipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }
    private void startListening(Cipher cipher){
        isSefCancel = false;
        mcancellationSignal = new CancellationSignal();
        mfingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mcancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (!isSefCancel) {
                    errmsg.setText(errString);
                    if(errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                        Toast.makeText(mactivity, errString, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                errmsg.setText(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                Toast.makeText(mactivity, "指纹认证成功", Toast.LENGTH_SHORT).show();
                mactivity.onAuthenticated();
            }

            @Override
            public void onAuthenticationFailed() {
                errmsg.setText("指纹认证失败，请再试一次");
            }
        },null);
    }

    private void stopListening() {
        if (mcancellationSignal != null) {
            mcancellationSignal.cancel();
            mcancellationSignal = null;
            isSefCancel = true;
        }
    }

}
