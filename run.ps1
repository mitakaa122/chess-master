
param([switch]$Build)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if ($Build) { & .\build.ps1 }
    if (!(Test-Path -LiteralPath '.\target\chess-1.0-SNAPSHOT.jar')) { throw 'Run .\build.ps1 first with Java 11 selected.' }
    & java "-Dchess.home=$PSScriptRoot" --module-path '.\sdk\javafx-windows\javafx-sdk-11.0.2\lib' --add-modules 'javafx.controls,jdk.httpserver' -cp '.\target\chess-1.0-SNAPSHOT.jar' funny.co.Main
    if ($LASTEXITCODE) { throw 'Chess exited with an error.' }
} finally { Pop-Location }
