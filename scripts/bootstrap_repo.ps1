param(
    [Parameter(Mandatory = $true)][string]$Repo,
    [string]$GhPath
)

# ---- PowerShell 5.x compatible gh.exe resolver ----
function Resolve-Gh {
    param([string]$Override)

    if ($Override -and (Test-Path $Override)) {
        return (Resolve-Path $Override).Path
    }

    $cmd = $null
    try { $cmd = Get-Command gh.exe -ErrorAction Stop } catch {}
    if ($cmd) { return $cmd.Source }

    $candidates = @(
        "$env:ProgramFiles\GitHub CLI\gh.exe",
        "$env:LOCALAPPDATA\Programs\GitHub CLI\gh.exe",
        "$env:LOCALAPPDATA\Microsoft\WindowsApps\gh.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    throw "Cannot find gh.exe. Pass -GhPath 'C:\Program Files\GitHub CLI\gh.exe'."
}

$Gh = Resolve-Gh -Override $GhPath
Write-Host "Using gh: $Gh" -ForegroundColor Cyan

function Ensure-Label {
    param([string]$Name, [string]$Color, [string]$Desc)
    & $Gh label create "$Name" --color $Color --description "$Desc" --repo $Repo 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        & $Gh label edit "$Name" --color $Color --description "$Desc" --repo $Repo | Out-Null
    }
}

Write-Host "== 1) Labels ==" -ForegroundColor Cyan
# Types
Ensure-Label 'type:feat'  '0E8A16' 'Feature'
Ensure-Label 'type:fix'   'D73A4A' 'Bug Fix'
Ensure-Label 'type:chore' 'C2E0C6' 'Chore/Refactor'
Ensure-Label 'type:docs'  '0075CA' 'Docs'
Ensure-Label 'type:test'  '5319E7' 'Testing'
Ensure-Label 'type:ci'    'E4E669' 'CI/CD'
# Scopes
Ensure-Label 'scope:auth'  'BFD4F2' 'Authentication/OAuth2'
Ensure-Label 'scope:codef' 'BFDADC' 'CODEF integration'
Ensure-Label 'scope:api'   'BFDADC' 'API layer'
Ensure-Label 'scope:infra' 'BFDADC' 'Infra/Build/Deploy'
# Priority
Ensure-Label 'prio:P0' 'B60205' 'Critical'
Ensure-Label 'prio:P1' 'D93F0B' 'High'
Ensure-Label 'prio:P2' 'FBCA04' 'Normal'

Write-Host "== 2) Milestones ==" -ForegroundColor Cyan
$milestones = @(
    'M0 - Repo bootstrap',
    'M1 - OAuth2 Login',
    'M2 - CODEF Token & ConnectedId',
    'M3 - Accounts/Transactions Ingest',
    'M4 - Observability'
)
foreach ($m in $milestones) {
    & $Gh api "repos/$Repo/milestones" -f "title=$m" 2>$null | Out-Null
}

function New-Issue {
    param([string]$Title,[string]$Body,[string[]]$Labels,[string]$Milestone)
    $labelArgs = @(); foreach ($l in $Labels) { $labelArgs += @('-l', $l) }
    & $Gh issue create -R $Repo -t $Title -b $Body @labelArgs -m $Milestone | Out-Null
}

Write-Host "== 3) Issues ==" -ForegroundColor Cyan
New-Issue 'Repo/Gradle bootstrap' @'
- settings.gradle.kts / libs.versions.toml / Toolchains(JDK21)
- apps/api and libs/common-* skeletons

AC:
- ./gradlew :apps:api:bootRun success
'@ @('type:chore','scope:infra','prio:P1') 'M0 - Repo bootstrap'

New-Issue 'OAuth2.0 login' @'
- Spring Security + OAuth2 client/session (or JWT)
- /auth/login, /auth/logout, user entity/repo

AC:
- Protected endpoint returns 401 -> 200 after login
'@ @('type:feat','scope:auth','prio:P0') 'M1 - OAuth2 Login'

New-Issue 'CODEF token issue/refresh' @'
- Manage CLIENT_ID/SECRET via env/secret
- Token cache + expiry handling + backoff

AC:
- Sandbox token obtained, auto refresh on 401
'@ @('type:feat','scope:codef','prio:P0') 'M2 - CODEF Token & ConnectedId'

New-Issue 'CODEF connect (connectedId) persistence' @'
- Register user credentials -> store connectedId (encrypted)
- Input DTO validation / exception mapping / audit log (PII masked)

AC:
- connectedId created and stored
'@ @('type:feat','scope:codef','prio:P0') 'M2 - CODEF Token & ConnectedId'

New-Issue 'Accounts API (/api/accounts)' @'
- Fetch accounts via CODEF -> map to standard model

AC:
- 200 with sample accounts
'@ @('type:feat','scope:codef','scope:api','prio:P1') 'M3 - Accounts/Transactions Ingest'

New-Issue 'Transactions ingest (/api/transactions)' @'
- Date range / pagination / retry / idempotent upsert
- Raw store (CSV/Parquet) + RDB keys

AC:
- N months fetched, no duplicates on re-run
'@ @('type:feat','scope:codef','scope:api','prio:P0') 'M3 - Accounts/Transactions Ingest'

New-Issue 'Observability (Actuator/logging/metrics)' @'
- Actuator, common logging (PII mask), error code convention
- Failure/retry metrics

AC:
- /actuator/health 200, metrics visible
'@ @('type:chore','scope:infra','prio:P2') 'M4 - Observability'

Write-Host "Done. Open: https://github.com/$Repo/issues" -ForegroundColor Green
