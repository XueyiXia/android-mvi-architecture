# Android MVI Architecture

这是一个用来学习 MVI 结构的 Android Demo，重点不是做一个完整业务系统，而是把状态、意图、副作用这些概念拆清楚。项目里最常见的场景是登录页：用户输入用户名和密码，ViewModel 处理登录逻辑，然后用 State 更新页面状态，登录成功之后再通过 Effect 去做 Toast 或跳转。

我在实际看代码的时候，最容易忽略的地方就是这里：很多同学只看到了 `loginSuccess = true`，但没有继续处理 `NavigateHome` 这个副作用，结果页面虽然提示了“登录成功”，但并没有真正跳转。这个 Demo 里就是典型例子。我们已经把这部分补上了，真正让登录成功后进入用户列表页面。

这个项目的目标不是炫技，而是想把几个基础点讲明白：

- MVI 是什么
- Intent / State / Effect 各自负责什么
- ViewModel 如何驱动 UI
- Activity / Fragment 如何监听状态变化
- 登录成功后应该如何处理跳转这样的副作用

---

## 1. 这个项目里最关键的地方

如果只是照着 Demo 复制代码，最容易出现的问题就是“看起来成功了，但实际上没跳过去”。

举个例子，登录成功时代码会这样处理：

```kotlin
if (success) {
    setState(currentState().copy(loading = false, loginSuccess = true))
    sendEffect(LoginEffect.ShowToast("登录成功"))
    sendEffect(LoginEffect.NavigateHome)
}
```

这里的重点不是 `loginSuccess`，而是 `NavigateHome`。这个副作用必须在页面层处理，否则只是状态变化，没有实际动作。

Fragment 里需要这样写：

```kotlin
override fun handleEffect(effect: LoginEffect) {
    when (effect) {
        is LoginEffect.ShowToast -> {
            Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
        }
        LoginEffect.NavigateHome -> {
            findNavController().navigate(R.id.userListFragment)
        }
    }
}
```

这段代码看起来不复杂，但它决定了你是否真的完成了跳转。

---

## 2. 为什么这个项目适合初学 MVI

如果你第一次接触 MVI，最难的不是语法，而是理解状态和副作用的边界。

- State：页面现在是什么状态，比如 loading、error、loginSuccess
- Intent：用户做了什么，比如点击登录
- Effect：一次性的动作，比如 Toast、Dialog、跳转

很多时候，UI 代码里直接写 `startActivity()`、`finish()`、`Toast` 会让逻辑散落在各处。MVI 的目标就是把这些 “动作” 统一收口，做到页面只负责展示和交互，真正的业务处理交给 ViewModel。

---

## 3. 目录结构

这个项目整体结构比较简单：

```text
android-mvi-architecture/
├── app/
│   └── src/main/java/com/jetpack/mvi/
│       ├── repository/
│       │   └── LoginRepository.kt
│       ├── ui/
│       │   ├── login/
│       │   │   ├── LoginActivity.kt
│       │   │   ├── LoginFragment.kt
│       │   │   └── LoginViewModel.kt
│       │   ├── userlist/
│       │   └── userdetail/
│       └── activities/
│           └── MainActivity.kt
├── framework_mvi/
│   └── src/main/java/com/framework/mvi/
│       └── base/
│           ├── BaseMviActivity.kt
│           ├── BaseMviFragment.kt
│           ├── BaseMviViewModel.kt
│           ├── MviIntent.kt
│           ├── MviState.kt
│           └── MviEffect.kt
├── README.md
└── app/src/main/res/navigation/nav_graph.xml
```

---

## 4. 登录功能怎么跑起来

这个 Demo 里，启动页就是登录页，用户输入账号密码之后点击“登录”。

流程大致是：

1. 点击登录按钮
2. 读取 EditText 中的用户名和密码
3. 发出 `LoginIntent.Login(username, password)`
4. ViewModel 收到 Intent
5. 校验用户名和密码是否为空
6. 调用 Repository 中的 `login()`
7. 若成功，更新 `State`，并发送 `NavigateHome` 这个 Effect
8. 页面层收到 Effect 后执行跳转

这里比较容易失误的地方在第 8 步。很多人只写了 `sendEffect(LoginEffect.NavigateHome)`，但没在页面里接收它，所以页面就一直停住。

---

## 5. 这份代码里，导航是怎么接通的

如果使用的是 Fragment 版本，页面层需要拿到 `NavController`，再执行：

```kotlin
findNavController().navigate(R.id.userListFragment)
```

如果你是 Activity 场景，则直接启动新页面：

```kotlin
val intent = Intent(this, MainActivity::class.java)
startActivity(intent)
finish()
```

这就是“登录成功后跳转”的实际落地方式。Demo 里原本写的是注释，实际上缺的就是这一段实现。

---

## 6. 为什么不要把跳转逻辑写进 ViewModel

我觉得这个点很重要。

ViewModel 负责处理业务和状态，它不应该直接去操作 `Activity` 或 `Fragment`。真正应该做的是：

- 业务成功后更新状态
- 发送一个副作用
- 让页面层选择跳转，弹 Toast，或者弹 Dialog

这样职责分离更清晰，也更容易测试。

---

## 7. 结论

这个 Demo 的价值，不在于“有多少页面”，而在于它把 MVI 的基础思路展示得很清楚：

- 事件来自 Intent
- 页面状态来自 State
- 一次性动作来自 Effect
- ViewModel 是中间协调者
- UI 层负责渲染和副作用处理

如果你是第一次学 MVI，建议不要只看类名，重点看这几个动作的流转：

登录按钮点击 -> Intent -> ViewModel -> State -> Effect -> 页面跳转

这条链路，才是 MVI 最核心的东西。

---

## 8. 补充说明：这个项目里已经修正的部分

原始项目里，`LoginEffect.NavigateHome` 是存在的，但页面层的 `handleEffect()` 里被注释掉了。也就是说，`ViewModel` 已经发出了跳转命令，但 UI 层没有真正去执行它。这样看起来是“登录成功”，但实际没有发生页面跳转。

我们已经把这部分连起来了：登录成功以后，页面会真正执行导航，进入用户列表页。

这也是这个项目最容易踩坑的点，如果只是看状态变化，容易误以为功能已经完成；但真正上线前，必须把副作用接通，否则只是“表面成功”。

