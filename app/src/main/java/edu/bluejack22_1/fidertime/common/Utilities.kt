package edu.bluejack22_1.fidertime.common

import com.google.firebase.auth.FirebaseAuth

class Utilities {

    companion object{

        fun getAuthFirebase(): FirebaseAuth {
            return FirebaseAuth.getInstance();
        }

        fun getImageName(path : String) : String{
            return path.substring(path.lastIndexOf("/") + 1)
        }
    }
}