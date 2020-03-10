package com.zamoraleo.appMonitoreoParche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.app.Person;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zamoraleo.appMonitoreoParche.model.Persona;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ListaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private List<Persona> listPerson = new ArrayList<Persona>();
    ArrayAdapter<Persona> arrayAdapterPersona;

    ListView lstPersonas;
    Persona personaSelected;

    String tit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        mAuth = FirebaseAuth.getInstance();

        lstPersonas = findViewById(R.id.lstPersonas);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                if(mFirebaseUser == null){
                    Intent intent = new Intent(ListaActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        inicializarFirebase();
        listaDatos();

        lstPersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                personaSelected = (Persona) parent.getItemAtPosition(i);
                dialogoGuardar();
            }
        });
    }

    private void dialogoGuardar() {
        final EditText txtNombre, txtPwd, txtEmail, txtApellidos, txtEdad;
        TextView titulo;
        Button btnRegistro, btnBorrar;

        AlertDialog.Builder builder = new AlertDialog.Builder(ListaActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View viewG = inflater.inflate(R.layout.activity_login, null);


        builder.setView(viewG);

        final AlertDialog dialog = builder.create();
        dialog.show();

        txtNombre = viewG.findViewById(R.id.txtNombre);
        txtPwd = viewG.findViewById(R.id.txtPsw);
        txtEmail = viewG.findViewById(R.id.txtEmail);
        txtApellidos = viewG.findViewById(R.id.txtApellido);
        txtEdad = viewG.findViewById(R.id.txtEdad);
        titulo = viewG.findViewById(R.id.txtRegistro);

        btnRegistro = viewG.findViewById(R.id.btnRegistrar);
        btnBorrar = viewG.findViewById(R.id.btnBorrar);
        tit = "Modificar";

        titulo.setText(tit);
        txtNombre.setText(personaSelected.getNombre());
        txtPwd.setText(personaSelected.getPassword());
        txtEmail.setText(personaSelected.getCorreo());
        txtApellidos.setText(personaSelected.getApellidos());
        txtEdad.setText(String.valueOf(personaSelected.getEdad()));

        btnRegistro.setText(tit);
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewG) {
                Persona p = new Persona();

                final String pwd = txtPwd.getText().toString();
                final String mail = txtEmail.getText().toString();
                final String nombre = txtNombre.getText().toString();
                final String apellidos = txtApellidos.getText().toString();
                final String edad = txtEdad.getText().toString();

                if(nombre.isEmpty()){
                    txtNombre.setError("Pon tu Nombre");
                    txtNombre.requestFocus();
                } else if(apellidos.isEmpty()){
                    txtApellidos.setError("Pon tus Apellidos");
                    txtApellidos.requestFocus();
                } else if(mail.isEmpty()){
                    txtEmail.setError("Pon un Correo");
                    txtEmail.requestFocus();
                } else if(edad.isEmpty()){
                    txtEdad.setError("Pon tu Edad");
                    txtEdad.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    txtEmail.setError("Pon un Correo Valido");
                    txtEmail.requestFocus();
                } else if(pwd.isEmpty()){
                    txtPwd.setError("Pon una Contraseña");
                    txtPwd.requestFocus();
                } else if(!PASSWORD_PATTERN.matcher(pwd).matches()){
                    txtPwd.setError("La contraseña debe tener minimo 8 Digitos, 1 letra Mayuscula y 1 Número");
                    txtPwd.requestFocus();
                } else{

                    p.setUid(personaSelected.getUid());
                    p.setApellidos(txtApellidos.getText().toString().trim());
                    p.setEdad(Integer.parseInt(txtEdad.getText().toString().trim()));
                    p.setCorreo(txtEmail.getText().toString().trim());
                    p.setPassword(txtPwd.getText().toString().trim());
                    p.setNombre(txtNombre.getText().toString().trim());

                    databaseReference.child("Persona").child(p.getUid()).setValue(p);
                    Toast.makeText(ListaActivity.this, "Se modificó correctamente", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminar();
                dialog.dismiss();
            }
        });
    }

    private void listaDatos() {
        databaseReference.child("Persona").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listPerson.clear();
                for(DataSnapshot objSnapshot : dataSnapshot.getChildren()){
                    Persona p = objSnapshot.getValue(Persona.class);
                    listPerson.add(p);

                    arrayAdapterPersona = new ArrayAdapter<Persona>(ListaActivity.this, android.R.layout.simple_list_item_1, listPerson);
                    lstPersonas.setAdapter(arrayAdapterPersona);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    public void onBackPressed() {
        cerrarSesion();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.icLogout:{
                cerrarSesion();
            }
            break;
            case R.id.icAdd:{
                onRegistrar();
            }
            break;
        }
        return true;
    }

    private void onRegistrar() {
        Intent intent = new Intent(ListaActivity.this, RegistroActivity.class);
        startActivity(intent);
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
    }

    private void dialogoRegistro(){
        final EditText txtNombre, txtPwd, txtEmail, txtApellidos, txtEdad;
        final TextInputLayout txtiNombre, txtiPwd, txtiEmail, txtiApellidos, txtiEdad;
        TextView Titulo;

        Button btnRegistro;

        AlertDialog.Builder builder = new AlertDialog.Builder(ListaActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_login, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        txtNombre = view.findViewById(R.id.txtNombre);
        txtPwd = view.findViewById(R.id.txtPsw);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtApellidos = view.findViewById(R.id.txtApellido);
        txtEdad = view.findViewById(R.id.txtEdad);

        txtiNombre = view.findViewById(R.id.txtiNombre);
        txtiPwd = view.findViewById(R.id.txtiPsw);
        txtiEmail = view.findViewById(R.id.txtiEmail);
        txtiApellidos = view.findViewById(R.id.txtiApellido);
        txtiEdad = view.findViewById(R.id.txtiEdad);

        Titulo = view.findViewById(R.id.txtRegistro);

        btnRegistro = view.findViewById(R.id.btnRegistrar);
        tit = "Registrar";

        Titulo.setText(tit);

        btnRegistro.setText(tit);
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String pwd = txtPwd.getText().toString();
                final String mail = txtEmail.getText().toString();
                final String nombre = txtNombre.getText().toString();
                final String apellidos = txtApellidos.getText().toString();
                final String edad = txtEdad.getText().toString();

                if(nombre.isEmpty()){
                    txtiNombre.setError("Pon tu Nombre");
                    txtiNombre.requestFocus();
                } else if(apellidos.isEmpty()){
                    txtiApellidos.setError("Pon tus Apellidos");
                    txtiApellidos.requestFocus();
                } else if(mail.isEmpty()){
                    txtiEmail.setError("Pon un Correo");
                    txtiEmail.requestFocus();
                } else if(edad.isEmpty()){
                    txtiEdad.setError("Pon tu Edad");
                    txtiEdad.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    txtiEmail.setError("Pon un Correo Valido");
                    txtiEmail.requestFocus();
                } else if(pwd.isEmpty()){
                    txtiPwd.setError("Pon una Contraseña");
                    txtiPwd.requestFocus();
                } else if(!PASSWORD_PATTERN.matcher(pwd).matches()){
                    txtiPwd.setError("La contraseña debe tener minimo 8 Digitos, 1 letra Mayuscula y 1 Número");
                    txtiPwd.requestFocus();
                } else{
                    Persona p = new Persona();
                    p.setUid(UUID.randomUUID().toString());
                    p.setNombre(nombre);
                    p.setApellidos(apellidos);
                    p.setCorreo(mail);
                    p.setEdad(Integer.parseInt(edad));
                    p.setPassword(pwd);

                    databaseReference.child("Persona").child(p.getUid()).setValue(p);
                    mAuth.createUserWithEmailAndPassword(mail,pwd).addOnCompleteListener(ListaActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Toast.makeText(ListaActivity.this, "Se registro correctamente", Toast.LENGTH_LONG).show();
                        }
                    });
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    private void eliminar() {
        Persona p = new Persona();
        p.setUid(personaSelected.getUid());
        databaseReference.child("Persona").child(p.getUid()).removeValue();
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
