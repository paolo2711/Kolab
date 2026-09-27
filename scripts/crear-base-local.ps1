<#
    Crea la base kolab, sus dos roles y las variables de entorno de la aplicacion.
    Las contrasenas se piden al ejecutarlo y no quedan escritas en ningun archivo.

    Uso:
        powershell -ExecutionPolicy Bypass -File scripts\crear-base-local.ps1
#>

param(
    [string] $Psql = "D:\Program Files\PostgreSQL\18\bin\psql.exe",
    [int]    $Puerto = 5433,
    [string] $Servidor = "localhost",
    [string] $Base = "kolab"
)

$ErrorActionPreference = "Stop"

function Leer-Clave {
    param([string] $Rotulo, [switch] $ConConfirmacion)

    while ($true) {
        $primera = Read-Host -Prompt $Rotulo -AsSecureString
        $texto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
            [Runtime.InteropServices.Marshal]::SecureStringToBSTR($primera))

        if ([string]::IsNullOrWhiteSpace($texto)) {
            Write-Host "  Esa clave esta vacia. Otra vez." -ForegroundColor Yellow
            continue
        }
        if (-not $ConConfirmacion) {
            return $texto
        }

        $segunda = Read-Host -Prompt "  Repitela" -AsSecureString
        $repetida = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
            [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segunda))

        if ($texto -ceq $repetida) {
            return $texto
        }
        Write-Host "  No coinciden. Otra vez." -ForegroundColor Yellow
    }
}

try {
    if (-not (Test-Path $Psql)) {
        throw "No encuentro psql en $Psql. Pasa la ruta con -Psql."
    }

    $rolesSql = Join-Path $PSScriptRoot "..\..\2 APF2\entrega\anexos\B base de datos\kolab-roles.sql"
    if (-not (Test-Path $rolesSql)) {
        throw "No encuentro kolab-roles.sql en $rolesSql"
    }

    Write-Host "Base $Base en ${Servidor}:$Puerto" -ForegroundColor Cyan
    Write-Host "Las claves no se muestran ni se guardan en ningun archivo."
    Write-Host ""

    $clavePostgres  = Leer-Clave "Clave del usuario postgres"
    $claveMigracion = Leer-Clave "Clave NUEVA para kolab_migracion" -ConConfirmacion
    $claveApp       = Leer-Clave "Clave NUEVA para kolab_app" -ConConfirmacion

    # psql lee la clave de PGPASSWORD, asi no aparece en la linea de comandos
    $env:PGPASSWORD = $clavePostgres
    $comunes = @("-h", $Servidor, "-p", $Puerto, "-U", "postgres", "-v", "ON_ERROR_STOP=1", "-q")

    Write-Host ""
    Write-Host "Conectando..." -NoNewline
    & $Psql @comunes -d postgres -t -A -c "select 1" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "no pude conectar como postgres. Revisa la clave o el puerto." }
    Write-Host " listo"

    Write-Host "Creando la base..." -NoNewline
    $existe = & $Psql @comunes -d postgres -t -A -c "select 1 from pg_database where datname = '$Base'"
    if ($existe -eq "1") {
        Write-Host " ya existia"
    } else {
        & $Psql @comunes -d postgres -c "create database $Base"
        if ($LASTEXITCODE -ne 0) { throw "no pude crear la base $Base." }
        Write-Host " creada"
    }

    Write-Host "Creando los roles..." -NoNewline
    & $Psql @comunes -d $Base -v "clave_migracion=$claveMigracion" -v "clave_app=$claveApp" -f $rolesSql
    if ($LASTEXITCODE -ne 0) { throw "kolab-roles.sql fallo. Si los roles ya existian, borralos y vuelve a correrlo." }
    Write-Host " listos"

    Write-Host "Guardando las variables..." -NoNewline
    setx KOLAB_DB_URL "jdbc:postgresql://${Servidor}:$Puerto/$Base" | Out-Null
    setx KOLAB_DB_USER "kolab_app" | Out-Null
    setx KOLAB_DB_PASSWORD $claveApp | Out-Null
    setx KOLAB_MIGRACION_USER "kolab_migracion" | Out-Null
    setx KOLAB_MIGRACION_PASSWORD $claveMigracion | Out-Null
    Write-Host " listas"

    Write-Host ""
    Write-Host "Listo. Abre una terminal NUEVA y corre: mvnw.cmd -B clean package; java -jar target\kolab.jar" -ForegroundColor Green
}
catch {
    Write-Host ""
    Write-Host "Fallo: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
finally {
    $env:PGPASSWORD = $null
    $clavePostgres = $null
    $claveMigracion = $null
    $claveApp = $null
    [GC]::Collect()
}
