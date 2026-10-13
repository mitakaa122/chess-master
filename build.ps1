$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $dependencyFiles = @(
        (Join-Path $env:USERPROFILE '.m2\repository\org\jetbrains\annotations\20.0.0\annotations-20.0.0.jar'),
        (Join-Path $env:USERPROFILE '.m2\repository\commons-codec\commons-codec\1.15\commons-codec-1.15.jar')
    )
    foreach ($file in $dependencyFiles) {
        if (!(Test-Path -LiteralPath $file)) { throw "Missing cached dependency: $file. Run Maven package first, or use an account with the Maven dependencies installed." }
    }
    $fxFiles = @('javafx.base.jar', 'javafx.graphics.jar', 'javafx.controls.jar') | ForEach-Object { Join-Path '.\sdk\javafx-windows\javafx-sdk-11.0.2\lib' $_ }
    $classPath = (@($fxFiles) + $dependencyFiles) -join ';'
    New-Item -ItemType Directory -Force target\classes | Out-Null
    $sources = @(Get-ChildItem src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & javac --release 11 -encoding UTF-8 -cp $classPath -d target\classes @sources
    if ($LASTEXITCODE) { throw 'Java compilation failed.' }
    Copy-Item src\main\resources\* target\classes -Recurse -Force
    & jar --create --file target\chess-1.0-SNAPSHOT.jar -C target\classes .
    if ($LASTEXITCODE) { throw 'JAR packaging failed.' }
    Write-Host 'Built target\chess-1.0-SNAPSHOT.jar'
} finally { Pop-Location }
