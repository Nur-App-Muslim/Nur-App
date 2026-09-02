$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ($RootDir -match "scripts$") {
    $RootDir = Split-Path -Parent $RootDir
} else {
    $RootDir = Get-Item "."
}

$TmpJdkDir = Join-Path $RootDir ".tmp_jdk"
$ZipPath = Join-Path $RootDir "jdk17.zip"

Write-Host "Root Directory: $RootDir"

# 1. Konfigurasi local.properties
Write-Host "==> Menulis local.properties"
$SdkPath = "C:/Users/Hype AMD/AppData/Local/Android/sdk"
"sdk.dir=$SdkPath" | Out-File (Join-Path $RootDir "local.properties") -Encoding ascii

# 2. Download JDK 17 jika belum ada
if (-not (Test-Path (Join-Path $TmpJdkDir "extracted"))) {
    if (Test-Path $TmpJdkDir) {
        Remove-Item $TmpJdkDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory -Path $TmpJdkDir | Out-Null
    
    Write-Host "==> Mengunduh portable JDK 17 dari Adoptium (±150MB)..."
    $JdkUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
    
    try {
        Start-BitsTransfer -Source $JdkUrl -Destination $ZipPath -ErrorAction Stop
    } catch {
        Write-Host "Start-BitsTransfer gagal, mencoba Invoke-WebRequest..."
        Invoke-WebRequest -Uri $JdkUrl -OutFile $ZipPath
    }
    
    Write-Host "==> Mengekstrak JDK 17..."
    Expand-Archive -Path $ZipPath -DestinationPath $TmpJdkDir
    
    "done" | Out-File (Join-Path $TmpJdkDir "extracted")
    Remove-Item $ZipPath -Force
}

# 3. Temukan folder JDK hasil ekstraksi
$SubDirs = Get-ChildItem -Path $TmpJdkDir -Directory
$JdkHome = $SubDirs | Where-Object { $_.Name -match "jdk" } | Select-Object -First 1
if (-not $JdkHome) {
    $JdkHome = $SubDirs | Select-Object -First 1
}

if (-not $JdkHome) {
    Write-Error "Gagal menemukan folder JDK yang diekstrak."
    exit 1
}
$JdkHomePath = $JdkHome.FullName
$JdkHomePathFormatted = $JdkHomePath.Replace("\", "/")
Write-Host "JDK Home Path: $JdkHomePathFormatted"

# Update org.gradle.java.home di gradle.properties agar gradle tidak memakai path lama yang salah
$GradlePropertiesPath = Join-Path $RootDir "gradle.properties"
if (Test-Path $GradlePropertiesPath) {
    Write-Host "==> Memperbarui org.gradle.java.home di gradle.properties..."
    $Content = Get-Content $GradlePropertiesPath
    $NewContent = @()
    $JavaHomePropSet = $false
    foreach ($line in $Content) {
        if ($line -match "^org.gradle.java.home=") {
            $NewContent += "org.gradle.java.home=$JdkHomePathFormatted"
            $JavaHomePropSet = $true
        } else {
            $NewContent += $line
        }
    }
    if (-not $JavaHomePropSet) {
        $NewContent += "org.gradle.java.home=$JdkHomePathFormatted"
    }
    $NewContent | Out-File $GradlePropertiesPath -Encoding ascii
}

Write-Host "==> Mengonfigurasi environment variables..."
$env:JAVA_HOME = $JdkHomePath
$env:PATH = "$(Join-Path $JdkHomePath 'bin');$env:PATH"

# Verifikasi Java
Write-Host "==> Versi Java yang digunakan:"
java -version

# 4. Gunakan adb.exe secara langsung
$AdbPath = "C:\Users\Hype AMD\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $AdbPath)) {
    $AdbPath = "adb"
}
Write-Host "adb.exe ditemukan di: $AdbPath"

# 5. Build dan Pasang ke Emulator / HP Fisik
Write-Host "==> Menyetel GRADLE_OPTS..."
$env:GRADLE_OPTS = "-Dorg.gradle.jvmargs=-Xmx3072m"

Write-Host "==> Melakukan compile dan instalasi ke emulator / HP Fisik..."
& (Join-Path $RootDir "gradlew.bat") installDebug --stacktrace --console=plain

# 6. Jalankan aplikasi di Emulator / HP Fisik
Write-Host "==> Meluncurkan aplikasi..."
& $AdbPath shell am start -n com.sajda.app/.MainActivity

Write-Host ""
Write-Host "==> Selesai! Aplikasi berhasil dikompilasi dan dijalankan. 🎉"
