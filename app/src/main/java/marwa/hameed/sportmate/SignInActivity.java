package marwa.hameed.sportmate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.victor.loading.rotate.RotateLoading;

public class SignInActivity extends AppCompatActivity
{
    Button sign_in_btn;
    EditText email,password;
    String email_txt,password_txt;

    RotateLoading rotateLoading;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();

        sign_in_btn = findViewById(R.id.sign_in_btn);
        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        rotateLoading = findViewById(R.id.rotateloading);

        sign_in_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                email_txt = email.getText().toString();
                password_txt = password.getText().toString();

                if (TextUtils.isEmpty(email_txt))
                {
                    Toast.makeText(getApplicationContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password_txt))
                {
                    Toast.makeText(getApplicationContext(), "please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                rotateLoading.start();

                UserLogin(email_txt,password_txt);
            }
        });
    }

    private void UserLogin(String email, String password)
    {
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                            startActivity(intent);
                        } else
                        {
                            rotateLoading.stop();
                            String taskmessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), taskmessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
