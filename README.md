### Project Structure

This is a Kotlin Multiplatform project targeting Android, iOS, Web, and Server.

* [/composeApp](./composeApp/src) contains shared code for your Compose Multiplatform applications:
    - **commonMain** (`./composeApp/src/commonMain/kotlin`): code shared across all targets
    - Platform-specific folders (e.g., **iosMain**, **jvmMain**) contain code for specific targets such as iOS
      CoreCrypto or JVM desktop features

* [/iosApp](./iosApp/iosApp) contains the iOS application entry point. Even if the UI is shared via Compose
  Multiplatform, SwiftUI code and Xcode configuration live here.

* [/server](./server/src/main/kotlin) contains the Ktor server application.

* [/shared](./shared/src) contains shared code for all targets. The main folder is **commonMain** (
  `./shared/src/commonMain/kotlin`). Platform-specific code can also live here if needed.

### Setup

Environment variables for development are stored in `Developer.env`. **You should rename this file to `.env`** to
use it with Docker Compose.
> Don't commit your personal `.env` with secrets to version control.
