# Security, files and operations

## Session and input controls

Passwords use BCrypt (cost 12). Sessions are HttpOnly and SameSite=Strict; Secure cookies default on. The `dev` profile explicitly permits HTTP localhost use. CSRF protection remains enabled: fetch `/api/auth/csrf` before login, fetch it again after login, and attach its named header to every write. The frontend does this automatically. No tokens or provider keys are shipped in static JavaScript.

The server validates allowed values, identifier relationships, dates, decimal precision and transitions. Missing/unauthorized record access returns 404 or 403. The default error body is `{ "code": "..." }`; the frontend translates domain error codes. SQL, credentials and raw exception details are not exposed in domain responses. Static assets have a restrictive Content Security Policy and render user text with escaping.

Important changes append audit rows, including payments, reversals, leases, maintenance transitions, files and access grants. This is an application audit trail, not tamper-evident external audit storage. Production operators should restrict direct database access and retain backups.

## Protected upload storage and scanning

`STORAGE_PATH` points outside public static directories. Uploads accept PDF, JPEG or PNG signatures, up to 8 MiB, with generated storage keys. The original name is sanitized for the download header. All downloads are attachments with no-store caching and server authorization. Identity/CR documents require a linked tenant; maintenance photos inherit the request's location and tenancy. Vendors can download photos for assigned jobs only.

Production upload scanning fails closed unless `UPLOAD_SCAN_COMMAND` points to an executable scanner wrapper. The application invokes that executable directly with one argument: the absolute uploaded file path. No shell interpolation occurs. Return exit code 0 only for a clean file; any other result rejects the upload. The process has a 20-second limit. A typical wrapper can invoke a maintained ClamAV installation with current signatures. Provision the executable and database in your deployment image or host; the base demo Docker image does not include an antivirus engine.

Example Linux wrapper, installed by your system administrator:

```sh
#!/bin/sh
exec /usr/bin/clamdscan --no-summary -- "$1"
```

The `dev` profile permits `DEV_UNSCANNED` uploads for synthetic examples and labels them in the interface. Never enable that profile with real identity documents. Scanning does not replace content-security review; magic bytes alone are not a complete file parser. Protect the file volume, encrypt host storage/backups, restrict filesystem access and define your document retention policy before using real records. A failed database transaction removes its uploaded file; interrupted processes can leave orphaned keys, which should be reconciled during maintenance.

## Production bootstrap

Do not enable `dev`. On an empty database, provide `BOOTSTRAP_ADMIN_USERNAME` and `BOOTSTRAP_ADMIN_PASSWORD` (12–72 characters). The bootstrap creates only a platform administrator, who then creates owner accounts through Access management. Owners provision their portfolio users. Remove bootstrap variables after first use. The admin does not inherit owners' record permissions.

Use HTTPS at a trusted reverse proxy, retain Secure cookies, supply unique database credentials and bind MySQL to a private network. Run the app with the supplied non-root Docker user. Configure a scanner before enabling real uploads. Keep the migration database account under operator control; a separate schema-migration deployment step and least-privilege runtime account are recommended for production.

## Backup and restore

Back up both MySQL and the protected-file volume as one operational checkpoint. Pause writes for a coordinated recovery point. Store exports outside the repository, encrypt them and verify restoration in a separate environment.

Linux/macOS, with the Docker database running:

```sh
mkdir -p backups
# Prompts for the MySQL root password; do not place it in shell history.
docker compose exec mysql mysqldump -uroot -p --single-transaction --routines --triggers --no-tablespaces bayt --result-file=/tmp/bayt-backup.sql
docker compose cp mysql:/tmp/bayt-backup.sql backups/bayt-backup.sql
docker compose cp app:/data backups/protected-files
```

The same `docker compose exec` and `docker compose cp` commands work in PowerShell; use `New-Item -ItemType Directory backups` for the directory. Keep the resulting SQL and directory in the same encrypted backup set.

For a restore, stop app writes, restore into an empty database using your MySQL client (`SOURCE /path/to/bayt-backup.sql;`), restore file keys into `STORAGE_PATH`, and start the matching application version. Confirm Flyway history, counts, a sample authorized document download and a financial reconciliation before reopening access. Do not run development reset commands against a live database.

## Development reset

The optional `scripts/reset-dev.sh` and `scripts/reset-dev.ps1` require the literal `RESET_DEMO` argument. They also require `.env` to declare the `dev` profile. They delete the local Compose MySQL and file volumes, then rebuild a synthetic demo. This loses all local demo records. They never run automatically.
