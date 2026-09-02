# watch-deploy.ps1
$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ($RootDir -match "scripts$") {
    $RootDir = Split-Path -Parent $RootDir
} else {
    $RootDir = Get-Item "."
}

$PathToWatch = Join-Path $RootDir "app\src\main"
Write-Host "Memulai pemantau berkas di: $PathToWatch"

$Watcher = New-Object System.IO.FileSystemWatcher
$Watcher.Path = $PathToWatch
$Watcher.IncludeSubdirectories = $true
$Watcher.EnableRaisingEvents = $true

# Debouncing configuration
$global:LastRun = [datetime]::MinValue
$DebounceMs = 3000

$RunBuild = {
    $Now = [datetime]::Now
    $Elapsed = ($Now - $global:LastRun).TotalMilliseconds
    if ($Elapsed -ge $DebounceMs) {
        $global:LastRun = $Now
        Write-Host ""
        Write-Host "==================================================" -ForegroundColor Cyan
        Write-Host "Perubahan terdeteksi! Memulai kompilasi & pasang..." -ForegroundColor Cyan
        Write-Host "==================================================" -ForegroundColor Cyan
        
        try {
            # Panggil skrip compile & run
            & (Join-Path $RootDir "scripts\run-cli.ps1")
        } catch {
            Write-Host "Proses kompilasi/pemasangan gagal: $_" -ForegroundColor Red
        }
    }
}

# Daftarkan event pemantau
$ChangedHandler = Register-ObjectEvent $Watcher "Changed" -Action $RunBuild
$CreatedHandler = Register-ObjectEvent $Watcher "Created" -Action $RunBuild

Write-Host "Memantau perubahan berkas di app/src/main... (Tekan Ctrl+C untuk berhenti)"
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} finally {
    # Bersihkan event listener saat berhenti
    Unregister-Event -SourceIdentifier $ChangedHandler.Name -ErrorAction SilentlyContinue
    Unregister-Event -SourceIdentifier $CreatedHandler.Name -ErrorAction SilentlyContinue
    $Watcher.Dispose()
    Write-Host "Pemantauan dihentikan."
}
