package com.sang.metamap.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.sang.metamap.R
import com.sang.metamap.databinding.ActivityMainBinding
import com.sang.metamap.presentation.viewmodel.MainViewModel
import com.sang.metamap.utils.CloudDataUtil
import com.sang.metamap.utils.ToastUtil


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        // TODO
        // 1. Khi mới vào app lần đầu, get ds tất cả building từ Firebase
        // 2. So sánh với list local, nếu khác thì lấy ds từ Firebase lưu vào local
        // 3. Khi search, user chỉ check ở local
        mainViewModel.getAllBuildingsInHCMUT {
            ToastUtil.showMess(applicationContext, "An error occurred")
        }
    }
}