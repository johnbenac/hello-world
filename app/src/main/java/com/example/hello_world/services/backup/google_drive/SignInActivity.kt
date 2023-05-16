package com.example.hello_world.services.backup.google_drive

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hello_world.R


class SignInActivity : AppCompatActivity() {
    private lateinit var googleDriveBackupHelper: GoogleDriveBackupHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        googleDriveBackupHelper = GoogleDriveBackupHelper(this)

        val action = intent.getStringExtra("action")
        if (action == "authenticate") {
            googleDriveBackupHelper.authenticate()
        } else if (action == "restore") {
            googleDriveBackupHelper.startRestore()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GoogleDriveBackupHelper.RC_SIGN_IN) {
            googleDriveBackupHelper.handleSignInResult(resultCode, data)
            finish()
        }
    }


}