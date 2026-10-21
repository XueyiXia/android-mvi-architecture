# android-mvi-architecture

Kotlin 版本的 MVI 开发框架，适用于中大型项目。

这个项目是在 `android-mvvm-architecture` 的基础上进一步演化出来的 MVI 架构示例。它重点强调单向数据流、状态驱动 UI、一次性事件处理，以及基础层、数据层和页面层的分离，适合中大型 Android 项目进行统一架构设计。

> 这个项目不是为了替代成熟的商业级框架，而是在 MVP/MVVM 的基础上，尝试封装一套更加规范、可扩展的 MVI 代码结构，以便在真实项目中逐步落地。

## 项目定位

MVI 的核心思想可以概括为：

- Intent：用户事件输入
- State：页面状态描述
- Effect：一次性事件输出
- ViewModel：处理 Intent，生成 State / Effect
- Repository：负责数据接入和缓存

和 MVVM 相比，MVI 更强调：

- 单向数据流
- UI 完全由 State 驱动
- Event 和 State 明确分离
- 页面状态更稳定，可测试性更高

---

## 架构特点

- 基础层统一封装：`BaseMviViewModel` / `BaseMviActivity` / `BaseMviFragment`
- UI 层仅负责展示和事件触发
- 数据层统一管理：Repository / Room / Retrofit
- 状态管理更加清晰，适合复杂页面演进
- 支持 Activity 与 Fragment 两种页面实现方式
- 已具备统一错误处理与导航封装能力

---

## 设计目标

这个项目的目标是：

- 让页面逻辑更清晰
- 降低 Activity / Fragment 中的耦合
- 把状态和事件统一管理
- 方便中大型项目扩展
- 为后续接入真实网络请求、缓存、导航、表单等能力做准备

---

## 核心模块说明

### 1. BaseMviViewModel

`BaseMviViewModel` 是 MVI 的核心基类，负责统一维护：

- `StateFlow` 页面状态
- `SharedFlow` 一次性事件
- `dispatch()` 事件入口
- `setState()` 更新状态
- `sendEffect()` 发送事件

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

### 2. BaseMviActivity / BaseMviFragment

这两个基类统一封装：

- 页面状态收集
- Effect 事件监听
- 页面初始化流程
- 页面生命周期管理

这样页面代码只需要关注：

- `renderState()`
- `handleEffect()`
- `initView()`

---

## MVI 基础结构

```text
android-mvi-architecture/
├── app/
│   ├── src/main/java/com/jetpack/mvi/
│   │   ├── activities/
│   │   │   ├── LoginActivity.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── UserListActivity.kt
│   │   │   ├── UserDetailActivity.kt
│   │   │   └── UserListFragmentActivity.kt
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── UserDao.kt
│   │   │   └── UserEntity.kt
│   │   ├── error/
│   │   │   └── ErrorHandler.kt
│   │   ├── model/
│   │   │   └── User.kt
│   │   ├── navigation/
│   │   │   └── AppNavigator.kt
│   │   ├── net/
│   │   │   ├── ApiClient.kt
│   │   │   └── NetworkResult.kt
│   │   ├── repository/
│   │   │   ├── LoginRepository.kt
│   │   │   └── UserRepository.kt
│   │   ├── ui/
│   │   │   ├── login/
│   │   │   │   └── LoginFragment.kt
│   │   │   ├── userdetail/
│   │   │   │   └── UserDetailFragment.kt
│   │   │   └── userlist/
│   │   │       ├── UserAdapter.kt
│   │   │       ├── UserListContract.kt
│   │   │       ├── UserListFragment.kt
│   │   │       └── UserListViewModel.kt
│   │   └── viewmodel/
│   │       └── LoginViewModel.kt
│   ├── src/main/res/
│   │   ├── layout/
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_user_list.xml
│   │   │   ├── activity_user_detail.xml
│   │   │   ├── activity_main.xml
│   │   │   ├── fragment_login.xml
│   │   │   ├── fragment_user_list.xml
│   │   │   ├── fragment_user_detail.xml
│   │   │   └── item_user.xml
│   │   └── navigation/
│   │       └── nav_graph.xml
│   └── src/main/AndroidManifest.xml
├── framework_mvi/
│   └── src/main/java/com/framework/mvi/
│       ├── base/
│       │   ├── MviIntent.kt
│       │   ├── MviState.kt
│       │   ├── MviEffect.kt
│       │   ├── BaseMviViewModel.kt
│       │   ├── BaseMviActivity.kt
│       │   └── BaseMviFragment.kt
│       └── repository/
│           └── BaseRepository.kt
├── build.gradle
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 登录页示例

登录页示例使用 MVI 的典型写法：

- 用户点击登录 -> 发送 `LoginIntent.Login`
- ViewModel 处理登录逻辑
- 结果变更为 `LoginState`
- 成功/失败通过 `LoginEffect` 发出

示例结构：

```kotlin
sealed class LoginIntent : MviIntent {
    data class Login(val username: String, val password: String) : LoginIntent()
}

data class LoginState(
    val loading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
) : MviState

sealed class LoginEffect : MviEffect {
    data class ShowToast(val message: String) : LoginEffect()
    object NavigateHome : LoginEffect()
}
```

---

## Retrofit + Room 实战示例

这个项目已经包含：

- Retrofit 网络层
- `NetworkResult` 统一返回
- Room 本地缓存
- Repository 数据接入层

这样处理方式的优点是：

- 网络层和界面层脱离
- 本地缓存和网络请求共存
- 状态更稳定
- 错误处理更统一

---

## 统一错误处理

错误抽象层如下：

```kotlin
sealed class UiError {
    data class NetworkError(val message: String) : UiError()
    data class ApiError(val code: Int, val message: String) : UiError()
    data class UnknownError(val message: String) : UiError()
}
```

在 Repository 层中统一转换错误，页面层只处理状态和提示即可。这样可以避免重复 `try/catch` 粘在 Activity / Fragment 中。

---

## Navigation 组件接入

项目中也接入了 Navigation：

```xml
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:startDestination="@id/loginFragment">

    <fragment
        android:id="@+id/loginFragment"
        android:name="com.jetpack.mvi.ui.login.LoginFragment" />

    <fragment
        android:id="@+id/userListFragment"
        android:name="com.jetpack.mvi.ui.userlist.UserListFragment" />
</navigation>
```

这样页面跳转可以集中在 Navigation 中管理，减少硬编码 `Intent` 和页面耦合。

---

## 适用场景

- 中大型 Android 项目
- 页面多、状态复杂的项目
- 登录、列表、详情、表单等场景
- 需要统一网络层、缓存层、错误层和导航层的项目
- 适合在真实业务中逐步落地架构标准化

---

## 使用方式

1. 页面继承 `BaseMviActivity` 或 `BaseMviFragment`
2. 页面中定义自己的 `Intent / State / Effect`
3. ViewModel 继承 `BaseMviViewModel`
4. 在 Repository 中统一处理网络和缓存
5. 在 Activity / Fragment 中收集 `state` 和 `effect`
6. 通过 `dispatch()` 发送事件

---

## 结语

这个项目已经从最简单的 MVI 结构，扩展到了更接近真实开发场景的工程化版本：

- MVI 基类
- 统一状态管理
- Retrofit 网络封装
- Room 缓存支持
- Navigation 跳转
- 统一错误处理
- 用户列表/详情示例

它适合作为：

- 学习 MVI 架构的参考实现
- 中大型项目的基础架构模板
- 扩展到真正业务代码前的工程化雏形

如果这个框架可以帮助到你，欢迎在 GitHub 上关注和 Star。

Thanks you so much ... END
