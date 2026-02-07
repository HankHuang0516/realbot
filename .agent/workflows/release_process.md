---
description: Automate the release process: increment version, build AAB, and organize output with version suffix.
---

# Pre-release Checks
> [!IMPORTANT]
> **Google Play Console Setup**
> Ensure the following are configured in Google Play Console before release:
> - **Product ID**: `e_claw_premium` (Must match `BillingManager.kt` exactly)
> - **Name**: E Claw Premium

## Store Listing Info (Subscription Benefits)
Add these up to 4 benefits in **Monetize > Products > Subscriptions > User Benefits**:

1.  **Unlimited AI Chat** (無限暢聊)
    *   Remove the daily 15-message limit and chat freely.
2.  **Premium Badge** (尊爵徽章)
    *   Exclusive premium status indicator in app settings.
3.  **Support Development** (支持開發)
    *   Directly support the continuous improvement of E-Claw.
4.  **Future Access** (未來功能)
    *   Get priority access to upcoming premium features.

1.  **Check and Increment Version**:
    - Open `app/build.gradle.kts`.
    - Find `versionCode` and `versionName`.
    - **Action**: Increment `versionCode` by 1.
    - **Action**: Increment `versionName` (e.g., 1.0.3 -> 1.0.4).
    - Save the file.

2.  **Build Release Bundle**:
    // turbo
    Run the following command to build the signed AAB:
    ```bash
    ./gradlew.bat bundleRelease
    ```

3.  **Organize Release Artifacts**:
    - Identify the current version name from `app/build.gradle.kts`.
    - Create a new folder named `release_v<versionName>` in the project root.
    - Copy the generated AAB to this folder.
    
    // turbo
    ```powershell
    # Extract version name
    $gradleContent = Get-Content "app/build.gradle.kts" -Raw
    if ($gradleContent -match 'versionName = "(.*?)"') {
        $versionName = $matches[1]
        Write-Host "Detected version: $versionName"
        
        # Create versioned release directory
        $releaseDir = "release_v$versionName"
        New-Item -ItemType Directory -Force -Path $releaseDir
        
        # Copy artifact
        $source = "app/build/outputs/bundle/release/app-release.aab"
        if (Test-Path $source) {
            Copy-Item -Path $source -Destination "$releaseDir/app-release.aab"
            Write-Host "SUCCESS: Release copied to $releaseDir/app-release.aab"
            Invoke-Item $releaseDir
        } else {
            Write-Error "Build artifact not found at $source"
        }
    } else {
        Write-Error "Could not parse versionName from build.gradle.kts"
    }
    ```
