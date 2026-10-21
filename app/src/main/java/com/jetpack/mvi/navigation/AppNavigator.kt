package com.jetpack.mvi.navigation

import android.app.Activity
import android.content.Intent
import androidx.navigation.NavController
import com.jetpack.mvi.activities.UserDetailActivity
import com.jetpack.mvi.activities.UserListActivity
import com.jetpack.mvi.model.User

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: 页面跳转统一封装（Navigation 版本）
 */
object AppNavigator {

    fun openUserList(activity: Activity) {
        activity.startActivity(Intent(activity, UserListActivity::class.java))
    }

    fun openUserDetail(activity: Activity, user: User) {
        val intent = Intent(activity, UserDetailActivity::class.java)
        intent.putExtra("user_id", user.id)
        intent.putExtra("user_name", user.name)
        intent.putExtra("user_email", user.email)
        activity.startActivity(intent)
    }

    fun openUserDetail(navController: NavController, user: User) {
        val bundle = androidx.core.os.bundleOf(
            "user_id" to user.id,
            "user_name" to user.name,
            "user_email" to user.email
        )
        navController.navigate(com.jetpack.mvi.R.id.userDetailFragment, bundle)
    }
}
