package com.jetpack.mvi.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.jetpack.mvi.R

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: Navigation 组件入口 Activity
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 如果需要默认跳到 userListFragment，可取消下面注释
        // navController.navigate(R.id.userListFragment)
    }
}
