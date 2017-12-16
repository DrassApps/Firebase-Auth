# Firebase-Auth
Autentificación de diferentes formas con la herramienta Firebase

La aplicación en Android muestra diferentes formas de autentifiación con la plataforma de Firebase, de tal forma que la podemos incorporar a nuestros proyectos para tener un Login o simplemente para guardar los datos de nuestro usuario mejorando la UX y el conocimiento que tenemos sobre él, para, entre otras cosas, desarollar campañas de marketing más eficiente.

### Autentificación integrada 
* Email & Password
* Número de Teléfono
* Google SingIn
* Facebook
* Tiwtter

Es muy importante que en el archivo 'Strings' modifiqueis las diferentes *KEYs* por las vuestras de Firebase.

### Inicialización de Firebase con Google ID.

Código base para la registrar un usuario en Firebase usando tu cuenta de Google.

```java
  // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
       
  // Llama a la actividad de Google para el registro y esperamos una respuesta
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
   // Recogemos los datos de la vista de SingIn
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        // Si el resultado del registros es correcto
        if (requestCode == RC_SIGN_IN) {

            // Obtenemos el resultado del SignIn con Google
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            // Si el registro ha sido correcto nos autentificamso con FireBase
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Si no mostramos un erorLog.d(TAG, "onComplete: Failed="
                Log.i(TAG,task.getException().getMessage());
                setSnackBar(mLayout,getString(R.string.google_err));
            }

        }else if (resultCode == RESULT_CANCELED){
            setSnackBar(mLayout,getString(R.string.google_err));
        }
    }
    
   // Autentificacion con Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        // Obtenemos los creedenciales
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario logeado y autentificado -> actualizamos UI
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else {
                            // Error detallado
                            Log.d(TAG, "onComplete: Failed="
                                    + task.getException().getMessage());
                            // Si tenemos un error
                            setSnackBar(mLayout,getString(R.string.facebook_singin_err));
                            updateUI(null);
                        }
                    }
                });
    }
```
### Inicialización de Firebase con Usuario & Contraseña.

Código base para la registrar un usuario en Firebase a través de un email y una contraseña pedida al usuario por la UI

```java
  // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
   // Registrarse
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtenemos el texto de los EditTexts
                email = emailEd.getText().toString();
                password = passwordEd.getText().toString();

                // Validamoas los campos
                if (correoValido(email) && password.length() > 5){

                    // Creamos un usuario con Email & Passwrod
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegistroEmailFirebase.this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // Si la creacion del usuario falla, damos un mensaje de error,
                                    // en caso de registro correcto, obtenemos el usuario y
                                    // actualizamos la vista con los datos del usuario

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                    // Solo para el registro con Email, nos guardamos la contraseña
                                    // del usuario en la BD como ejemplo de que si se puede tener
                                    // la contraseña de un usario en la consola de Firebase. Al
                                    // guardarnos datos sensibles de los usuarios, debemos
                                    // tener en cuenta la LOPD y LPI

                                    // Cogemos tambien el token de sesion activa del usuario
                                    String token = FirebaseInstanceId.getInstance().getToken();

                                    // Guardamos los datos
                                    guardarDatosUsuario(email,password,token);

                                    if (!task.isSuccessful()) {
                                        // No ha podido registrarse y nos muestra el fallo que
                                        // devuelve firebase
                                        Log.d(TAG, "onComplete: Failed="
                                                + task.getException().getMessage());

                                        // Manejamos el fallo para que el usuario sepa realmente
                                        // el fallo
                                        if (task.getException().getMessage().
                                                contains("badly formatted")){
                                            setSnackBar(mLayout,"Email incorrecto");
                                        }
                                        updateUI(null);
                                    }

                                }
                            });

                }else{ setSnackBar(mLayout,"Datos de registro incorrectos."); }
            }
        });

        // Iniciar sesion
        inisesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtenemos el texto de los EditTexts
                email = emailEd.getText().toString();
                password = passwordEd.getText().toString();

                // Verificamos los datos de entrada e intentamos logerarnos
                if(correoValido(email) && password.length() > 5){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegistroEmailFirebase.this,
                                    new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // Obtenemos el usuario
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    // Si recibimos un usuario actualizamos la vista
                                    if (user != null) {
                                        updateUI(user);
                                    }

                                    // Si firebase nos devuelve un error, le deimos al usuario que
                                    // se ha equivocado
                                    if (!task.isSuccessful()) {
                                        setSnackBar(mLayout,"Datos de acceso incorrectos");
                                        updateUI(null);
                                    }
                                }
                            });

                }else{ setSnackBar(mLayout,"Datos de acceso incorrectos"); }
            }
        });
```


