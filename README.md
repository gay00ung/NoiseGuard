# NoiseGuard

This is a Kotlin Multiplatform project targeting Android, iOS, Web.

## 🚀 프로젝트 설정

### iOS 설정

iOS 프로젝트를 처음 설정하거나 clone 후 실행할 때:

```bash
# 설정 스크립트 실행
./scripts/setup-ios.sh
```

이 스크립트는 다음 작업을 수행합니다:
- `Config.xcconfig` 파일 생성 (템플릿에서)
- CocoaPods 의존성 설치

⚠️ **중요**: `Config.xcconfig` 파일은 `.gitignore`에 등록되어 있어 Git에 커밋되지 않습니다. 
이 파일에는 Team ID, API 키 등 민감한 정보가 포함될 수 있으므로 각 개발자가 로컬에서 관리해야 합니다.

#### 수동 설정 (스크립트를 사용하지 않는 경우)

1. Config 파일 생성:
   ```bash
   cp iosApp/Configuration/Config.xcconfig.template iosApp/Configuration/Config.xcconfig
   ```

2. 필요시 Config.xcconfig 파일 수정:
   - `TEAM_ID`: Apple Developer Team ID
   - `PRODUCT_BUNDLE_IDENTIFIER`: Bundle ID
   - API 키 (추후 추가 예정)

3. CocoaPods 설치:
   ```bash
   cd iosApp && pod install
   ```

4. Xcode에서 `iosApp.xcworkspace` 파일 열기 (`.xcodeproj` 파일이 아님)

## 📁 프로젝트 구조

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.