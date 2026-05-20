# Agentic Rules & Skill Installation Specification
## WeatherSnap Cognitive Operating System

This document serves as the formal Agent Operating System (OS) specification for the Antigravity Agent operating on the WeatherSnap project. It defines the required engineering skills, behavioral constraints, and absolute operating rules for all future Android development tasks. The AI must internalize these instructions and execute all operations at a Staff/Principal engineering level.

---

## 1. Skill Installations

The following skill modules are hereby installed and activated for all WeatherSnap development operations:

### 1.1 System Architecture Skill
**Directive:** Design modular, scalable, loosely coupled systems. Enforce strict feature boundaries.
*   **Actionable Behaviors:**
    *   Maintain the defined Gradle multi-module topology (`:app`, `:feature:*`, `:core:*`).
    *   Never create circular dependencies.
    *   Ensure feature modules (`:feature:weather`, `:feature:camera`, `:feature:report`, `:feature:history`, `:feature:settings`) never depend on each other.
    *   All feature cross-communication must be routed via `:core:domain` or Navigation routes in the `:app` shell.
    *   Hilt dependency injection must be used to wire implementations to interfaces.

### 1.2 Clean Architecture Skill
**Directive:** Enforce Presentation → Domain → Data separation with zero cross-layer leakage.
*   **Actionable Behaviors:**
    *   **Presentation Layer (`:feature:*`)**: Knows only about Domain models and Android UI state. Never imports Room or Retrofit classes.
    *   **Domain Layer (`:core:domain`)**: Pure Kotlin. Zero Android dependencies. Defines Use Cases, Repository interfaces, and Domain Models.
    *   **Data Layer (`:core:database`, `:core:network`, `:core:file`)**: Implements Domain Repository interfaces. Maps DTOs/Entities to Domain Models before returning them.

### 1.3 Android Production Engineering Skill
**Directive:** Utilize modern, production-grade Android frameworks and patterns correctly.
*   **Actionable Behaviors:**
    *   **State Management:** Use `StateFlow` exclusively in ViewModels. Expose immutable `StateFlow` to the UI.
    *   **Process Death:** Persist all critical UI state (e.g., active drafts, coordinates) using `SavedStateHandle`.
    *   **Dependency Injection:** Use Hilt for all DI. Utilize `@HiltViewModel` and `@HiltWorker`. Provide bindings via `@Module`.
    *   **Compose:** Use lifecycle-aware flow collection (`collectAsStateWithLifecycle()`).
    *   **Storage & Networking:** Use Room for SQLite and Retrofit/OkHttp for REST APIs.

### 1.4 Reliability Engineering Skill
**Directive:** Build for extreme resilience. Assume the network will fail and the system will kill the app.
*   **Actionable Behaviors:**
    *   **Offline-First:** The local Room database is the Single Source of Truth (SSOT). Network operations only sync the database; the UI only observes the database.
    *   **Crash Recovery:** Catch exceptions gracefully and emit standard `UiState.Error` wrappers.
    *   **Draft Management:** Unsubmitted reports/snaps must be persisted locally. Use WorkManager for deferred, guaranteed background synchronization.

### 1.5 Performance Engineering Skill
**Directive:** Optimize for constrained hardware (8GB RAM optimization target) and buttery-smooth execution.
*   **Actionable Behaviors:**
    *   **Coroutines:** Never block the Main thread. Use injected `DispatcherProvider` to route heavy work to `IO` or `Default` pools.
    *   **Image Handling:** Sandbox and compress camera captures aggressively (e.g., target 1920x1080) in memory-efficient streams *before* saving to disk to prevent OOM errors.
    *   **Compose Performance:** Minimize recompositions. Use stable models, `remember`, and avoid heavy computations in Compose scopes.

### 1.6 Debugging & Refactoring Skill
**Directive:** Actively guard system integrity and continuously improve the codebase.
*   **Actionable Behaviors:**
    *   Detect and refuse architecture violations (e.g., a feature module trying to import a database DAO directly).
    *   Propose refactoring if a class violates the Single Responsibility Principle (SRP).
    *   Rely on verifiable diagnostics (e.g., compile errors, lint warnings) to drive fixes.

---

## 2. Strict Operating Rules

The agent must strictly adhere to the following operating rules. **Any violation is considered a critical failure.**

1.  **Architecture-First Development:**
    *   *Rule:* No implementation code shall be written before the system design, data models, and architectural contracts are defined and agreed upon.
2.  **No UI Generation (Google Stitch MCP Policy):**
    *   *Rule:* The agent must **NOT** write custom Jetpack Compose UI component layouts or design systems from scratch. All UI components, layouts, and design systems will be provided externally via the **Google Stitch MCP**. The agent's role in the Presentation layer is strictly limited to ViewModels, State logic, and Navigation wiring.
3.  **Strict Modular Isolation:**
    *   *Rule:* Feature modules (`:feature:*`) must remain completely isolated from one another.
4.  **Offline-First Enforcement:**
    *   *Rule:* The local Room database is the *only* source of truth. APIs are secondary data sources used strictly to populate or synchronize the local database.
5.  **Prohibition of Global State Misuse:**
    *   *Rule:* No global variables or singletons outside of Hilt-managed scopes. Avoid `companion object` state.
6.  **Mandatory Lifecycle-Safe Design:**
    *   *Rule:* All features must flawlessly survive configuration changes (rotation), backgrounding, and system-initiated process death. `SavedStateHandle` is mandatory for transient state.
7.  **Incremental Development Discipline:**
    *   *Rule:* Develop, build, and verify one module at a time. Do not attempt massive, cross-module rewrites without verifying intermediate compile states.

---

## 3. System Constraints

The agent must operate within these absolute technical constraints:

*   **Memory Constraint:** Optimize all operations for devices with 8GB RAM or less. Aggressive garbage collection avoidance and efficient byte array handling for image processing are mandatory.
*   **Execution Environment:** Make zero assumptions about emulator availability. The code must be hardware-agnostic and robust enough to run on real devices without reliance on specific emulator behaviors.
*   **Coroutine Safety:** Every launched coroutine must execute within a defined, lifecycle-aware scope (`viewModelScope`, `lifecycleScope`). GlobalScope is strictly prohibited.
*   **IO-Bound Isolation:** Disk reads/writes (Room, Files) and Network requests (Retrofit) must *never* execute on the main thread. They must be explicitly shifted to the `Dispatcher.IO` pool via the injected `DispatcherProvider`.
