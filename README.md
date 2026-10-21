# Android MVI Architecture

这是一个 Android MVI 示例项目，重点展示 MVI 的设计思想、结构拆分和实际使用方式。它不是一个复杂业务项目，而是一个帮助你快速理解 MVI 的基础工程。

这个项目的目标是让你明确以下几个问题：

- MVI 是什么
- Intent / State / Effect 分别负责什么
- ViewModel 如何驱动 UI
- Activity / Fragment 如何观察状态并渲染页面
- 一个功能如何从“事件”走到“界面更新”和“副作用处理”

---

## 1. MVI 是什么

MVI 是 Model-View-Intent 的缩写，核心方向是：

- 用户触发事件（Intent）
- ViewModel 处理事件，并更新状态（State）
- UI 根据状态渲染界面
- 一次性副作用（Effect）负责处理 Toast、跳转、Dialog 等

它和传统 MVP、MVVM 最大的区别在于：

- UI 不直接处理业务逻辑
- State 是单向流动的
- ViewModel 负责状态管理
- 业务结果通过 State 和 Effect 流向页面

简单说：

- Intent = “用户做了什么”
- State = “页面现在是什么状态”
- Effect = “现在应该做什么副作用”

---

## 2. 这个项目的结构

这个项目把 MVI 的基础能力拆成了两个层次：

- app：业务层，包含具体功能页面，例如登录页
- framework_mvi：基础层，提供 MVI 抽象和通用基类

项目结构大致如下：

```text
android-mvi-architecture/
├── app/
│   └── src/main/java/com/jetpack/mvi/
│       ├── repository/
│       │   └── LoginRepository.kt
│       └── ui/
│           └── login/
│               ├── LoginActivity.kt
│               ├── LoginViewModel.kt
│               └── LoginContract.kt
├── framework_mvi/
│   └── src/main/java/com/framework/mvi/
│       └── base/
│           ├── MviIntent.kt
│           ├── MviState.kt
│           ├── MviEffect.kt
│           ├── BaseMviViewModel.kt
│           ├── BaseMviActivity.kt
│           └── BaseMviFragment.kt
└── README.md
```

这也是最典型的 MVI 工程形式：

- 框架层负责统一抽象
- 业务层只实现具体页面和数据逻辑

---

## 3. MVI 的三大核心

### 3.1 Intent

Intent 表示“用户如何发起一个事件”。

例如：

- 点击登录
- 输入用户名
- 修改密码
- 清空错误提示

示例：

```kotlin
sealed class LoginIntent : MviIntent {
    data class Login(val username: String, val password: String) : LoginIntent()
    object ClearError : LoginIntent()
}
```

这里的意义是：

- 用户点击登录，需要发出一个 Login Intent
- 这个 Intent 包含了用户名和密码
- ViewModel 收到这个 Intent 后去处理登录逻辑

---

### 3.2 State

State 表示“当前页面的状态”。

例如：

- loading：是否正在加载
- error：是否有错误
- loginSuccess：是否登录成功
- username、password：输入状态

示例：

```kotlin
data class LoginState(
    val loading: Boolean = false,
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val loginSuccess: Boolean = false
) : MviState
```

State 的设计原则是：

- 状态尽量不可变
- 状态描述页面当前结果
- UI 只根据 State 进行渲染

例如：
- loading = true 表示正在请求
- error != null 表示展示错误文本
- loginSuccess = true 表示展示成功状态

---

### 3.3 Effect

Effect 表示“一次性副作用”。

常见副作用包括：

- Toast 提示
- 跳转页面
- 展示 Dialog
- 扫描二维码
- 打开相机

示例：

```kotlin
sealed class LoginEffect : MviEffect {
    data class ShowToast(val message: String) : LoginEffect()
    object NavigateHome : LoginEffect()
}
```

它和 State 的区别是：

- State 会一直保留，页面可以持续观察它
- Effect 只发生一次，通常用于一次性的行为

例如：
- 登录成功后弹一个 Toast
- 登录成功后跳转首页

这类行为不应该长期保存在 State 里，而应该通过 Effect 触发。

---

## 4. BaseMviViewModel 是什么

在 framework_mvi 中，最关键的抽象是 BaseMviViewModel。

它负责统一状态管理和副作用发出。

核心思路如下：

- 持有一个 State
- 持有一个 Effect 流
- 通过 dispatch(intent) 处理新事件
- 通过 setState(...) 更新状态
- 通过 sendEffect(...) 发送副作用

伪代码示意：

```kotlin
abstract class BaseMviViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<E>()
    val effect: SharedFlow<E> = _effect.asSharedFlow()

    fun dispatch(intent: I) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    protected abstract suspend fun handleIntent(intent: I)

    protected fun setState(newState: S) {
        _state.value = newState
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
```

理解这段代码非常重要：

- state 是状态流
- effect 是事件流
- UI 订阅它们
- ViewModel 是状态和副作用的中枢

---

## 5. BaseMviActivity 是什么

页面层不直接写复杂逻辑，而是继承 BaseMviActivity。

它负责：

- 绑定视图
- 观察 ViewModel 的 State
- 观察 ViewModel 的 Effect
- 调用 renderState() 渲染页面
- 调用 handleEffect() 处理副作用

伪代码示意：

```kotlin
abstract class BaseMviActivity<
    BINDING : ViewBinding,
    S : MviState,
    I : MviIntent,
    E : MviEffect,
    VM : BaseMviViewModel<S, I, E>
> : AppCompatActivity() {

    protected abstract val viewModel: VM

    protected abstract fun inflateBinding(): BINDING
    protected abstract fun renderState(state: S)
    protected open fun handleEffect(effect: E) = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = inflateBinding()
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        renderState(state)
                    }
                }

                launch {
                    viewModel.effect.collect { effect ->
                        handleEffect(effect)
                    }
                }
            }
        }
    }
}
```

这样页面会变成非常简单：

- 只负责 UI 和用户交互
- 状态变化交给 ViewModel
- 副作用交给 Effect

---

## 6. 登录功能怎么用 MVI

这个项目里最典型的示例就是登录模块。

### 6.1 定义 Intent

```kotlin
sealed class LoginIntent : MviIntent {
    data class Login(val username: String, val password: String) : LoginIntent()
    object ClearError : LoginIntent()
}
```

意思：
- 当用户点击登录时，发出 Login Intent
- 当用户清空错误时，发出 ClearError

---

### 6.2 定义 State

```kotlin
data class LoginState(
    val loading: Boolean = false,
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val loginSuccess: Boolean = false
) : MviState
```

这表示页面状态中需要知道：
- 是否在加载
- 当前输入值
- 错误信息
- 是否登录成功

---

### 6.3 定义 Effect

```kotlin
sealed class LoginEffect : MviEffect {
    data class ShowToast(val message: String) : LoginEffect()
    object NavigateHome : LoginEffect()
}
```

这里的副作用包括：
- 显示提示框
- 登录后跳转首页

---

### 6.4 ViewModel 实现

```kotlin
class LoginViewModel(
    private val repository: LoginRepository
) : BaseMviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override suspend fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Login -> {
                if (intent.username.isBlank() || intent.password.isBlank()) {
                    setState(currentState().copy(error = "用户名和密码不能为空"))
                    sendEffect(LoginEffect.ShowToast("用户名和密码不能为空"))
                    return
                }

                setState(currentState().copy(loading = true, error = null))

                val success = repository.login(intent.username, intent.password)

                if (success) {
                    setState(
                        currentState().copy(
                            loading = false,
                            username = intent.username,
                            password = intent.password,
                            error = null,
                            loginSuccess = true
                        )
                    )

                    sendEffect(LoginEffect.ShowToast("登录成功"))
                    sendEffect(LoginEffect.NavigateHome)
                } else {
                    setState(
                        currentState().copy(
                            loading = false,
                            error = "登录失败",
                            loginSuccess = false
                        )
                    )

                    sendEffect(LoginEffect.ShowToast("登录失败"))
                }
            }

            LoginIntent.ClearError -> {
                setState(currentState().copy(error = null))
            }
        }
    }
}
```

这一段非常关键，它说明了 MVI 的核心流程：

- 接收 Intent
- 执行业务逻辑
- 更新 State
- 发送 Effect

---

### 6.5 Activity 处理 UI

```kotlin
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
        mBindingView.progressBar.visibility =
            if (state.loading) View.VISIBLE else View.GONE

        mBindingView.tvError.text = state.error ?: ""

        mBindingView.tvError.visibility =
            if (state.error.isNullOrEmpty()) View.GONE else View.VISIBLE

        mBindingView.tvSuccess.text =
            if (state.loginSuccess) "登录成功" else ""

        mBindingView.tvSuccess.visibility =
            if (state.loginSuccess) View.VISIBLE else View.GONE
    }

    override fun handleEffect(effect: LoginEffect) {
        when (effect) {
            is LoginEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            LoginEffect.NavigateHome -> {
                Toast.makeText(this, "跳转首页", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

这里可以看到：

- Activity 负责界面交互
- 用户点击按钮，触发 Intent
- ViewModel 处理结果
- State 改变后，界面刷新
- 副作用通过 Effect 执行

这正是 MVI 的最简单、最标准使用方式。

---

## 7. 为什么这种结构容易维护

MVI 的好处是：

### 7.1 状态清晰

页面行为不再散落在 Activity 中，而是集中在 State 中。

### 7.2 逻辑更容易测试

你可以单独测试：

- 输入不合法时，State 会不会变成 error
- 登录成功时，是否发出 NavigateHome
- 登录失败时，是否保存错误状态

### 7.3 业务逻辑和 UI 分离

Activity 只负责：

- 点击事件
- 绑定视图
- 展示状态
- 响应副作用

业务逻辑放在 ViewModel 和 Repository 中。

### 7.4 更容易扩展功能

新增一个页面，只需要：

- 新增 Intent
- 新增 State
- 新增 Effect
- 新增 ViewModel
- 新增 Activity / Fragment

这会让功能拓展非常稳定。

---

## 8. 新增页面时的标准步骤

如果你要新增一个页面，例如 UserList 页面，可以按下面步骤来：

### 第一步：定义 Intent

```kotlin
sealed class UserListIntent : MviIntent {
    object LoadUsers : UserListIntent()
    data class Refresh(val page: Int) : UserListIntent()
}
```

### 第二步：定义 State

```kotlin
data class UserListState(
    val loading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
) : MviState
```

### 第三步：定义 Effect

```kotlin
sealed class UserListEffect : MviEffect {
    data class ShowToast(val message: String) : UserListEffect()
    data class NavigateToDetail(val userId: String) : UserListEffect()
}
```

### 第四步：创建 ViewModel

```kotlin
class UserListViewModel(
    private val repository: UserRepository
) : BaseMviViewModel<UserListState, UserListIntent, UserListEffect>(UserListState()) {

    override suspend fun handleIntent(intent: UserListIntent) {
        when (intent) {
            UserListIntent.LoadUsers -> {
                setState(currentState().copy(loading = true))
                val users = repository.getUsers()
                setState(currentState().copy(loading = false, users = users))
            }

            is UserListIntent.Refresh -> {
                setState(currentState().copy(loading = true))
                val users = repository.getUsers(page = intent.page)
                setState(currentState().copy(loading = false, users = users))
            }
        }
    }
}
```

### 第五步：创建 Activity / Fragment

```kotlin
class UserListActivity : BaseMviActivity<
    ActivityUserListBinding,
    UserListState,
    UserListIntent,
    UserListEffect,
    UserListViewModel
>() {
    // 绑定视图和状态渲染
}
```

这样就能快速扩展出新的功能页面。

---

## 9. 什么时候应该使用 MVI

MVI 更适合这些场景：

- 页面状态多
- 需要清晰的状态流
- 需要统一管理副作用
- 页面逻辑较复杂
- 业务流程较长
- 需要更容易测试和维护

它不一定适合所有项目。对于小项目，MVI 可能显得稍微重一点。但是一旦页面越来越多、状态越来越复杂，MVI 的价值会越来越明显。

---

## 10. 一个最核心的认识

MVI 的本质不是“写一大堆类”，而是“把 UI 切成三类关注点”：

- 事件：Intent
- 状态：State
- 副作用：Effect

然后让 ViewModel 成为这三者之间的中枢。

理解这一点后，项目会从“代码堆砌”变成“状态驱动设计”。

---

## 11. 结论

这个项目的核心价值，不在于展示炫技的功能，而在于展示 MVI 的最小可用结构：

- 抽象出统一的 MVI 基类
- Activity / Fragment 负责 UI
- ViewModel 负责状态和副作用
- Repository 负责数据
- 页面通过 State 进行驱动，而不是直接写散落的逻辑

如果你想从传统 Activity 代码转向更清晰的架构，这个项目就是一个很好的入口。

你可以把它当成一个“最小 MVI 模板”。以后每新增一个功能页面，都按照这个结构扩展，就能逐渐构建出更稳定的 Android 应用架构。
