# Firebase-Auth
Autentificación de diferentes formas con la herramienta Firebase

La aplicación en Android muestra diferentes formas de autentifiación con la plataforma de Firebase, de tal forma que la podemos incorporar a nuestros proyectos para tener un Login o simplemente para guardar los datos de nuestro usuario mejorando la UX y el conocimiento que tenemos sobre él, para, entre otras cosas, desarollar campañas de marketing más eficiente.

### Autentificación integrada 
* Email & Password
* Google SingIn
* Facebook
* Tiwtter

Es muy importante que en el archivo 'Strings' modifiqueis las diferentes *KEYs* por las vuestras de Firebase.

### Inicialización de Firebase Google.

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
