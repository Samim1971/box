param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$gradleFile = Join-Path $projectRoot "app\build.gradle.kts"
$releaseApk = Join-Path $projectRoot "app\build\outputs\apk\release\app-release.apk"
$distDirectory = Join-Path $projectRoot "dist"

$versionMatch = Select-String -LiteralPath $gradleFile -Pattern 'versionName\s*=\s*"([^"]+)"'
if (-not $versionMatch) {
    throw "versionName app/build.gradle.kts icinde bulunamadi."
}
$version = $versionMatch.Matches[0].Groups[1].Value
$outputApk = Join-Path $distDirectory "Kanalixor-Radio-$version.apk"
$checksumFile = "$outputApk.sha256"

Push-Location $projectRoot
try {
    $tasks = @()
    if (-not $SkipTests) {
        $tasks += "testDebugUnitTest"
    }
    $tasks += "assembleRelease"
    & ".\gradlew.bat" @tasks "--no-daemon"
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle derlemesi basarisiz oldu."
    }

    New-Item -ItemType Directory -Path $distDirectory -Force | Out-Null
    Copy-Item -LiteralPath $releaseApk -Destination $outputApk -Force
    $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $outputApk).Hash.ToLowerInvariant()
    "$hash  Kanalixor-Radio-$version.apk" | Set-Content -LiteralPath $checksumFile -Encoding ascii

    Write-Output "APK: $outputApk"
    Write-Output "SHA256: $hash"
} finally {
    Pop-Location
}
