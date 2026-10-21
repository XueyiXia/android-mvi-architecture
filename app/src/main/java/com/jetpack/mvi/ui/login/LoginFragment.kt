package com.jetpack.mvi.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.framework.mvi.base.BaseMviFragment
import com.jetpack.mvi.databinding.FragmentLoginBinding
import com.jetpack.mvi.repository.LoginRepository
import com.jetpack.mvi.viewmodel.LoginEffect
import com.jetpack.mvi.viewmodel.LoginIntent
import com.jetpack.mvi.viewmodel.LoginState
import com.jetpack.mvi.viewmodel.LoginViewModel

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: Fragment 版本登录页示例
 */
class LoginFragment : BaseMviFragment<
    FragmentLoginBinding,
    LoginState,
    LoginIntent,
    LoginEffect,
    LoginViewModel
>() {

    override val viewModel: LoginViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(LoginRepository()) as T
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
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
        if (state.loginSuccess) {
            mBindingView.tvSuccess.text = "登录成功"
            mBindingView.tvSuccess.visibility = View.VISIBLE
        } else {
            mBindingView.tvSuccess.visibility = View.GONE
        }
    }

    override fun handleEffect(effect: LoginEffect) {
        when (effect) {
            is LoginEffect.ShowToast -> {
                // Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            }
            LoginEffect.NavigateHome -> {
                // 可通过 NavController 跳转到 userListFragment
            }
        }
    }
}
