package com.example.gruppe5


import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class MainActivity : AppCompatActivity() {

    lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //assignId()
        assignId()
        checkMyPermission()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_map, R.id.navigation_favorites, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)

        // setter opp navbar
        navView.setupWithNavController(navController)
        navView.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    private fun checkMyPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(
            object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse){
                    Toast.makeText(applicationContext, "ACCESS GRANTED", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
//                    val intent = Intent()
//                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    val uri = Uri.fromParts("package", getPackageName(),"");
//                    intent.setData(uri);
//                    startActivity(intent);

                    checkMyPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
//                        token?.continuePermissionRequest()
                }
            }).check()

    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
    }

}
