package marwa.hameed.sportmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import marwa.hameed.sportmate.Model.UserModel;

public class RegisterActivity extends AppCompatActivity
{
    CircleImageView profile_image;
    EditText first_name,last_name,email_address,password,mobile_field;
    Button sign_up;
    RadioGroup radioGroup;

    String first_txt,last_txt,email_txt,password_txt,mobile,selectedimageurl,gender;

    Uri photoPath;

    FirebaseAuth auth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);

        first_name = findViewById(R.id.first_name_field);
        last_name = findViewById(R.id.last_name_field);
        email_address = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        mobile_field = findViewById(R.id.mobile_field);
        sign_up = findViewById(R.id.sign_up_btn);
        radioGroup = findViewById(R.id.radio_grp);

        auth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        onRadioButtonClicked();

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                first_txt = first_name.getText().toString();
                last_txt = last_name.getText().toString();
                String fullname = first_txt + " " + last_txt;

                email_txt = email_address.getText().toString();
                password_txt = password.getText().toString();
                mobile = mobile_field.getText().toString();

                if (TextUtils.isEmpty(first_txt))
                {
                    Toast.makeText(getApplicationContext(), "please enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(last_txt))
                {
                    Toast.makeText(getApplicationContext(), "please enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

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

                if (TextUtils.isEmpty(mobile))
                {
                    Toast.makeText(getApplicationContext(), "please enter your mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(gender))
                {
                    Toast.makeText(getApplicationContext(), "please choose gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoPath == null)
                {
                    Toast.makeText(getApplicationContext(), "please add your picture", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setTitle("User Register");
                progressDialog.setMessage("Please Wait Until Creating Account ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);

                CreateAccount(fullname,email_txt,password_txt,mobile,gender);
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(RegisterActivity.this);
            }
        });
    }

    private void uploadImage(final String fullname,final String email, final String mobile, final String gender)
    {
        UploadTask uploadTask;

        final StorageReference ref = storageReference.child("images/" + photoPath.getLastPathSegment());

        uploadTask = ref.putFile(photoPath);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful())
                {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();

                selectedimageurl = downloadUri.toString();

                AddUser(fullname,email,selectedimageurl,mobile,gender);
                progressDialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
                //Toast.makeText(getContext(), "successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void AddUser(String fullname, String email, String imageurl,String mobile, String gender)
    {
        UserModel userModel = new UserModel(fullname,email,imageurl,mobile,gender);

        databaseReference.child("Users").child(getUID()).setValue(userModel);
    }

    private void CreateAccount(final String fullname, final String email, String password, final String mobile,final String gender)
    {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            uploadImage(fullname,email,mobile,gender);
                        } else
                        {
                            String error_message = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private String getUID()
    {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK)
            {
                if (result != null)
                {
                    photoPath = result.getUri();

                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.addphoto)
                            .error(R.drawable.addphoto)
                            .into(profile_image);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home :
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRadioButtonClicked()
    {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.radio_male :
                        gender = "male";
                        break;
                    case R.id.radio_female :
                        gender = "female";
                        break;
                }
            }
        });
    }
}
