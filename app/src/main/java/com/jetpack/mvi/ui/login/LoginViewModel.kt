package com.jetpack.mvi.ui.login

import com.framework.mvi.base.BaseMviViewModel
import com.framework.mvi.base.MviEffect
import com.framework.mvi.base.MviIntent
import com.framework.mvi.base.MviState
import com.jetpack.mvi.repository.LoginRepository

sealed class LoginIntent : MviIntent {
    data class Login(val username: String, val password: String) : LoginIntent()
    object ClearError : LoginIntent()
}

data class LoginState(
    val loading: Boolean = false,
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val loginSuccess: Boolean = false
) : MviState

sealed class LoginEffect : MviEffect {
    data class ShowToast(val message: String) : LoginEffect()
    object NavigateHome : LoginEffect()
}

class LoginViewModel(
    private val repository: LoginRepository
) : BaseMviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override suspend fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Login -> {
                if (intent.username.isBlank() || intent.password.isBlank()) {
                    setState(currentState().copy(error = "用户名和密码不能为空", loginSuccess = false))
                    sendEffect(LoginEffect.ShowToast("用户名和密码不能为空"))
                    return
                }

                setState(currentState().copy(loading = true, error = null, loginSuccess = false))

                try {
                    val success = repository.login(intent.username, intent.password)
                    if (success) {
                        setState(currentState().copy(loading = false, username = intent.username, password = intent.password, error = null, loginSuccess = true))
                        sendEffect(LoginEffect.ShowToast("登录成功"))
                        sendEffect(LoginEffect.NavigateHome)
                    } else {
                        setState(currentState().copy(loading = false, error = "登录失败", loginSuccess = false))
                        sendEffect(LoginEffect.ShowToast("登录失败"))
                    }
                } catch (e: Exception) {
                    setState(currentState().copy(loading = false, error = e.message ?: "网络异常", loginSuccess = false))
                    sendEffect(LoginEffect.ShowToast("网络异常"))
                }
            }

            LoginIntent.ClearError -> {
                setState(currentState().copy(error = null))
            }
        }
    }
}
