# android-mvi-architecture

Kotlin + Android MVI 最简可运行版本。

## 目录结构

```text
android-mvi-architecture/
├── app/
│   ├── src/main/java/com/jetpack/mvi/
│   │   ├── repository/
│   │   │   └── LoginRepository.kt
│   │   └── ui/login/
│   │       ├── LoginActivity.kt
│   │       ├── LoginViewModel.kt
│   │       └── LoginContract.kt
│   ├── src/main/res/layout/activity_login.xml
│   └── src/main/AndroidManifest.xml
├── framework_mvi/
│   ├── src/main/java/com/framework/mvi/
│   │   ├── base/
│   │   │   ├── MviIntent.kt
│   │   │   ├── MviState.kt
│   │   │   ├── MviEffect.kt
│   │   │   ├── BaseMviViewModel.kt
│   │   │   ├── BaseMviActivity.kt
│   │   │   └── BaseMviFragment.kt
│   │   └── repository/
│   │       └── BaseRepository.kt
│   └── build.gradle
├── build.gradle
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/wrapper/gradle-wrapper.properties
└── README.md
```

## 说明

这个版本已经去掉了复杂的 Room / Navigation / Kapt 组合，重点保留：

- MVI 基类
- Activity 展示层
- ViewModel 状态管理
- Repository 数据层
- 最小可运行 Android 代码

这样能够最大限度降低构建失败几率，适合 Android Studio 直接打开并运行。

## 运行方式

1. 安装 JDK 17
2. Android Studio 打开这个项目
3. 等待 Gradle 同步
4. 选择 app 模块
5. 运行到模拟器或真机

## 版本说明

- AGP: 8.5.2
- Kotlin: 1.9.24
- Gradle: 8.7
- JDK: 17

如果本地仍然报构建错误，优先检查：

- JDK 是否为 17
- Android Studio 是否使用的是项目自带 Gradle 配置
- 是否重新同步过项目

Thanks you so much ... END
