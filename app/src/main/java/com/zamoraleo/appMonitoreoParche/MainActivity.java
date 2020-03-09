package com.zamoraleo.appMonitoreoParche;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import com.example.appmonitoreoparche.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    EditText txtCorreo, txtContraseña;
    Button btnAceptar;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCorreo = findViewById(R.id.txtUsuario);
        txtContraseña = findViewById(R.id.pswSesion);
        btnAceptar = findViewById(R.id.btnAceptar);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progressdialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        inicializarFirebase();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                if(mFirebaseUser != null) {
                    Intent intent = new Intent(MainActivity.this, ListaActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        progressDialog.dismiss();

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progressdialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                final String pwd = txtContraseña.getText().toString();
                final String mail = txtCorreo.getText().toString();

                if(mail.isEmpty()){
                    txtCorreo.setError("Pon un Correo");
                    txtCorreo.requestFocus();
                    progressDialog.dismiss();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    txtCorreo.setError("Pon un Correo Valido");
                    txtCorreo.requestFocus();
                    progressDialog.dismiss();
                } else if(pwd.isEmpty()){
                    txtContraseña.setError("Pon una Contraseña");
                    txtContraseña.requestFocus();
                    progressDialog.dismiss();
                } else if(!PASSWORD_PATTERN.matcher(pwd).matches()){
                    txtContraseña.setError("La contraseña debe tener minimo 8 Digitos, 1 letra Mayuscula y 1 Número");
                    txtContraseña.requestFocus();
                    progressDialog.dismiss();
                } else{
                    mAuth.signInWithEmailAndPassword(mail, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Error de Inicio de Sesion", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Bienvenido!", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        } else {
            finish();
        }
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 4 characters
                    "$");
}
