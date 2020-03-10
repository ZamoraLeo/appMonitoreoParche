package com.zamoraleo.appMonitoreoParche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appmonitoreoparche.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zamoraleo.appMonitoreoParche.model.Persona;

import java.util.UUID;
import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        inicializarFirebase();

        final TextInputLayout txtiPwd, txtiEmail, txtiApellidos, txtiEdad, txtiNombre;

        Button btnRegistro;

        txtiPwd = findViewById(R.id.txtiPsw);
        txtiEmail = findViewById(R.id.txtiEmail);
        txtiEdad = findViewById(R.id.txtiEdad);
        txtiApellidos = findViewById(R.id.txtiApellido);
        txtiNombre = findViewById(R.id.txtiNombre);

        btnRegistro = findViewById(R.id.btnRegistrar);

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String pwd = txtiPwd.getEditText().getText().toString();
                final String mail = txtiEmail.getEditText().getText().toString();
                final String apellidos = txtiApellidos.getEditText().getText().toString();
                final String edad = txtiEdad.getEditText().getText().toString();
                final String nombre = txtiNombre.getEditText().getText().toString();

                if (!validarNombre()|!validarApellido()|!validarEdad()|!validarMail()|!validarContraseña()){
                    return;
                }
                Persona p = new Persona();
                p.setUid(UUID.randomUUID().toString());
                p.setNombre(nombre);
                p.setApellidos(apellidos);
                p.setCorreo(mail);
                p.setEdad(Integer.parseInt(edad));
                p.setPassword(pwd);

                databaseReference.child("Persona").child(p.getUid()).setValue(p);
                mAuth.createUserWithEmailAndPassword(mail,pwd).addOnCompleteListener(RegistroActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(RegistroActivity.this, "Se registro correctamente", Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
        });
    }

    //region Validaciones

    private boolean validarNombre() {
        TextInputLayout txtiNombre = findViewById(R.id.txtiNombre);
        final String nombre = txtiNombre.getEditText().getText().toString();

        if(nombre.isEmpty()){
            txtiNombre.setError("Pon tu Nombre");
            txtiNombre.requestFocus();
            return false;
        } else {
            txtiNombre.setError(null);
            return true;
        }
    }

    private boolean validarApellido() {
        TextInputLayout txtiApellido = findViewById(R.id.txtiApellido);
        final String apellido = txtiApellido.getEditText().getText().toString();

        if(apellido.isEmpty()){
            txtiApellido.setError("Pon tu Apellido");
            txtiApellido.requestFocus();
            return false;
        } else {
            txtiApellido.setError(null);
            return true;
        }
    }

    private boolean validarEdad() {
        TextInputLayout txtiEdad = findViewById(R.id.txtiEdad);
        final String edad = txtiEdad.getEditText().getText().toString();

        if(edad.isEmpty()){
            txtiEdad.setError("Pon tu Edad");
            txtiEdad.requestFocus();
            return false;
        } else {
            txtiEdad.setError(null);
            return true;
        }
    }

    private boolean validarMail() {
        TextInputLayout txtiMail = findViewById(R.id.txtiEmail);
        final String mail = txtiMail.getEditText().getText().toString();

        if(mail.isEmpty()){
            txtiMail.setError("Pon un Correo");
            txtiMail.requestFocus();
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            txtiMail.setError("Pon un Correo Valido");
            txtiMail.requestFocus();
            return false;
        } else {
            txtiMail.setError(null);
            return true;
        }
    }

    private boolean validarContraseña() {
        TextInputLayout txtiPwd = findViewById(R.id.txtiPsw);
        final String pwd = txtiPwd.getEditText().getText().toString();

        if(pwd.isEmpty()){
            txtiPwd.setError("Pon una Contraseña");
            txtiPwd.requestFocus();
            return false;
        } else if(!PASSWORD_PATTERN.matcher(pwd).matches()){
            txtiPwd.setError("La contraseña debe tener minimo 8 Digitos, 1 Letra Mayuscula y 1 Número");
            txtiPwd.requestFocus();
            return false;
        } else {
            txtiPwd.setError(null);
            return true;
        }
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

    //endregion

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }
}
