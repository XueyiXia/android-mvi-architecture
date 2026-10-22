package com.jetpack.mvi.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.framework.mvi.base.BaseMviActivity
import com.jetpack.mvi.activities.MainActivity
import com.jetpack.mvi.databinding.ActivityLoginBinding
import com.jetpack.mvi.repository.LoginRepository

class LoginActivity : BaseMviActivity<
    ActivityLoginBinding,
    LoginState,
    LoginIntent,
    LoginEffect,
    LoginViewModel
>() {

    override val viewModel: LoginViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(LoginRepository()) as T
            }
        }
    }

    override fun inflateBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        mBindingView.btnLogin.setOnClickListener {
            val username = mBindingView.etUsername.text?.toString().orEmpty()
            val password = mBindingView.etPassword.text?.toString().orEmpty()
            viewModel.dispatch(LoginIntent.Login(username, password))
        }
    }

    override fun renderState(state: LoginState) {
        mBindingView.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
        mBindingView.tvError.text = state.error ?: ""
        mBindingView.tvError.visibility = if (state.error.isNullOrEmpty()) View.GONE else View.VISIBLE
        mBindingView.tvSuccess.text = if (state.loginSuccess) "登录成功" else ""
        mBindingView.tvSuccess.visibility = if (state.loginSuccess) View.VISIBLE else View.GONE
    }

    override fun handleEffect(effect: LoginEffect) {
        when (effect) {
            is LoginEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            LoginEffect.NavigateHome -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
