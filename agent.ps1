param(
    [ValidateSet('state', 'move', 'claim')][string]$Command = 'state',
    [string]$Move,
    [int]$Version = -1
)
$ErrorActionPreference = 'Stop'
$sessionPath = Join-Path $PSScriptRoot 'target\codex-session.properties'
if (!(Test-Path -LiteralPath $sessionPath)) { throw 'Start a game vs Codex first.' }
$session = Get-Content -LiteralPath $sessionPath -Raw | ConvertFrom-StringData
$headers = @{ Authorization = "Bearer $($session.token)" }
$uri = "http://127.0.0.1:$($session.port)"
if ($Command -eq 'state') {
    Invoke-RestMethod "$uri/state" -Headers $headers -TimeoutSec 8 | ConvertTo-Json -Depth 5 -Compress
} else {
    if ($Version -lt 0) { throw 'Specify -Version from the latest state.' }
    if ($Command -eq 'claim') { $Move = 'claim' }
    Invoke-RestMethod "$uri/move" -Method Post -Headers $headers -Body @{ move = $Move; version = $Version } -TimeoutSec 8 | ConvertTo-Json -Depth 5 -Compress
}
