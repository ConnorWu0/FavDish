package com.example.favdish.view.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.favdish.R
import com.example.favdish.databinding.ActivityMainBinding
import com.example.favdish.model.worker.NotifyWorker
import com.example.favdish.utils.Constants
import com.example.favdish.viewmodel.SplashViewModel
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        mNavController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_all_dishes, R.id.navigation_favorite_dishes, R.id.navigation_random_dish
            )
        )
        setupActionBarWithNavController(mNavController, appBarConfiguration)
        binding.navView.setupWithNavController(mNavController)
        if (intent.hasExtra(Constants.NOTIFICATION_ID)){
            val notificationId = intent.getIntExtra(Constants.NOTIFICATION_ID, 0)
            Log.i("Notification Id", "$notificationId")
            binding.navView.selectedItemId = R.id.navigation_random_dish
        }
        startWork()
    }

    private fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresCharging(false)
        .setRequiresBatteryNotLow(true)
        .build()
    private fun createWorkRequest() = PeriodicWorkRequestBuilder<NotifyWorker>(15,TimeUnit.MINUTES)
        .setConstraints(createConstraints())
        .build()

    private fun startWork(){
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "FavDish Notify Work",
                ExistingPeriodicWorkPolicy.KEEP,
                createWorkRequest()
            )
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, null)
    }
    fun hideBottomNavigationView(){
        binding.navView.clearAnimation()
        binding.navView.animate().translationY(binding.navView.height.toFloat()).duration = 300
        binding.navView.visibility = View.GONE
    }
    fun showBottomNavigationView(){
        binding.navView.clearAnimation()
        binding.navView.animate().translationY(0f).duration = 300
        binding.navView.visibility = View.VISIBLE
    }
}