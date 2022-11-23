package edu.bluejack22_1.fidertime.common

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack22_1.fidertime.R

class Utilities {

    companion object{

        fun getAuthFirebase(): FirebaseAuth {
            return FirebaseAuth.getInstance();
        }

        fun getImageName(path : String) : String{
            return path.substring(path.lastIndexOf("/") + 1)
        }

        fun getGoogleSignInClient (activity : Activity) : GoogleSignInClient {
            val googleSignInClient : GoogleSignInClient
            val webClientId = "1007275358557-2lh87b42cu38c4svlmmtfv7o6nsai811.apps.googleusercontent.com"
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(activity , gso)

            return googleSignInClient
        }
    }


}