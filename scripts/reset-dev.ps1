param([string]$Confirmation)
$ErrorActionPreference = 'Stop'
if ($Confirmation -ne 'RESET_DEMO') { throw 'Pass RESET_DEMO to delete local demo volumes.' }
if (!(Test-Path .env) -or !((Get-Content .env) -match '^SPRING_PROFILES_ACTIVE=dev\s*$')) { throw '.env must explicitly select dev.' }
docker compose down --volumes
if ($LASTEXITCODE -ne 0) { throw 'Docker reset failed.' }
docker compose up --build -d
