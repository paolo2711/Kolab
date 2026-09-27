<#
    Crea la base kolab, sus dos roles y las variables de entorno de la aplicacion.

    Solo pide la clave del usuario postgres. Las de kolab_migracion y kolab_app se generan al azar
    y se guardan unicamente en las variables de entorno: nadie las escribe ni las necesita a mano.

    Se puede volver a correr. Si los roles ya existen, les cambia la clave por una nueva y
    actualiza las variables.

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

function Nueva-Clave {
    # solo letras y digitos: asi entra sin escapes en la URL de JDBC, en setx y en psql
    $alfabeto = 'abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789'
    $tope = [Math]::Floor(256 / $alfabeto.Length) * $alfabeto.Length
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $clave = ""
        $uno = [byte[]]::new(1)
        while ($clave.Length -lt 32) {
            $rng.GetBytes($uno)
            # los bytes del final se descartan para que ninguna letra salga mas seguido que otra
            if ($uno[0] -lt $tope) {
                $clave += $alfabeto[$uno[0] % $alfabeto.Length]
            }
        }
        return $clave
    }
    finally {
        $rng.Dispose()
    }
}

function Existe-Rol {
    param([string] $Rol)
    $r = & $Psql -h $Servidor -p $Puerto -U postgres -d postgres -t -A -q `
                 -c "select 1 from pg_roles where rolname = '$Rol'"
    return $r -eq "1"
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
    Write-Host "Las claves de los roles se generan solas. No se muestran ni se guardan en archivos."
    Write-Host ""

    $clave = Read-Host -Prompt "Clave del usuario postgres" -AsSecureString
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($clave))
    if ([string]::IsNullOrWhiteSpace($env:PGPASSWORD)) {
        throw "no escribiste ninguna clave."
    }

    $comunes = @("-h", $Servidor, "-p", $Puerto, "-U", "postgres", "-v", "ON_ERROR_STOP=1", "-q")

    Write-Host ""
    Write-Host "Conectando..." -NoNewline
    & $Psql @comunes -d postgres -t -A -c "select 1" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "no pude conectar como postgres. Revisa la clave o el puerto." }
    Write-Host " listo"

    Write-Host "Creando la base..." -NoNewline
    $hay = & $Psql @comunes -d postgres -t -A -c "select 1 from pg_database where datname = '$Base'"
    if ($hay -eq "1") {
        Write-Host " ya existia"
    } else {
        & $Psql @comunes -d postgres -c "create database $Base"
        if ($LASTEXITCODE -ne 0) { throw "no pude crear la base $Base." }
        Write-Host " creada"
    }

    $claveMigracion = Nueva-Clave
    $claveApp = Nueva-Clave

    if ((Existe-Rol "kolab_migracion") -and (Existe-Rol "kolab_app")) {
        Write-Host "Renovando la clave de los roles..." -NoNewline
        & $Psql @comunes -d $Base -c "alter role kolab_migracion password '$claveMigracion'"
        if ($LASTEXITCODE -ne 0) { throw "no pude cambiar la clave de kolab_migracion." }
        & $Psql @comunes -d $Base -c "alter role kolab_app password '$claveApp'"
        if ($LASTEXITCODE -ne 0) { throw "no pude cambiar la clave de kolab_app." }
        Write-Host " listas"
    } else {
        Write-Host "Creando los roles..." -NoNewline
        & $Psql @comunes -d $Base -v "clave_migracion=$claveMigracion" -v "clave_app=$claveApp" -f $rolesSql
        if ($LASTEXITCODE -ne 0) { throw "kolab-roles.sql fallo. Si solo uno de los dos roles existia, borralo y vuelve a correr." }
        Write-Host " listos"
    }

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
    $claveMigracion = $null
    $claveApp = $null
    [GC]::Collect()
}
