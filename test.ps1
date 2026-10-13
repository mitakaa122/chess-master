$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force target\rules-tests | Out-Null
    & javac --release 11 -encoding UTF-8 -d target\rules-tests src\main\java\funny\co\core\Rules.java src\test\java\funny\co\core\RulesTest.java
    if ($LASTEXITCODE) { throw 'Rules test compilation failed.' }
    & java -cp target\rules-tests funny.co.core.RulesTest
    if ($LASTEXITCODE) { throw 'Rules tests failed.' }
} finally { Pop-Location }
