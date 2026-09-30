<#
    Crea la base kolab, sus dos roles y las variables de entorno de la aplicacion.

    Solo pide la clave del usuario postgres. Las de kolab_migracion y kolab_app se generan al azar
    y se guardan unicamente en las variables de entorno: nadie las escribe ni las necesita a mano.

    Se puede volver a correr: borra la base kolab y sus dos roles, y los crea de cero. Al arrancar,
    la aplicacion crea las tablas y carga los ejemplos.

    Uso: doble clic en instalar-base.cmd, en la raiz del proyecto. Ese archivo llama a este.
#>

param(
    [string] $Psql = "",
    [int]    $Puerto = 0,
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

# el psql de la version mas nueva instalada, en C: o en D:
function Buscar-Psql {
    $encontrados = Get-ChildItem -Path "C:\Program Files\PostgreSQL\*\bin\psql.exe",
                                       "D:\Program Files\PostgreSQL\*\bin\psql.exe" -ErrorAction SilentlyContinue |
        Sort-Object { [int]($_.FullName -replace '.*PostgreSQL\\(\d+)\\.*', '$1') } -Descending
    if ($encontrados) { return $encontrados[0].FullName }
    return ""
}

# el puerto del servidor de esa misma version: lo dice el registro de Windows o su postgresql.conf
function Buscar-Puerto {
    $version = $Psql -replace '.*PostgreSQL\\(\d+)\\.*', '$1'
    $servicio = Get-ItemProperty -Path "HKLM:\SOFTWARE\PostgreSQL\Services\postgresql-x64-$version" `
                                 -ErrorAction SilentlyContinue
    if ($servicio -and $servicio.Port) { return [int]$servicio.Port }
    $conf = Join-Path (Split-Path (Split-Path $Psql)) "data\postgresql.conf"
    $linea = Select-String -Path $conf -Pattern '^\s*port\s*=\s*(\d+)' -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($linea) { return [int]$linea.Matches[0].Groups[1].Value }
    return 5432
}

try {
    if ([string]::IsNullOrWhiteSpace($Psql)) {
        $Psql = Buscar-Psql
    }
    if ([string]::IsNullOrWhiteSpace($Psql) -or -not (Test-Path $Psql)) {
        throw "No encuentro PostgreSQL instalado. Instalalo desde https://www.postgresql.org/download/windows/ y vuelve a correr esto."
    }
    if ($Puerto -eq 0) {
        $version = $Psql -replace '.*PostgreSQL\\(\d+)\\.*', '$1'
        $detectado = Buscar-Puerto
        Write-Host "PostgreSQL $version encontrado, escucha en el puerto $detectado."
        $respuesta = Read-Host -Prompt "Enter para usar el $detectado, o escribe otro puerto"
        $Puerto = if ([string]::IsNullOrWhiteSpace($respuesta)) { $detectado } else { [int]$respuesta }
    }

    $rolesSql = Join-Path $PSScriptRoot "kolab-roles.sql"
    if (-not (Test-Path $rolesSql)) {
        throw "No encuentro kolab-roles.sql junto a este script."
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
    if ($LASTEXITCODE -ne 0) {
        throw "no pude conectar como postgres en el puerto $Puerto. Revisa la clave; si tienes mas de un PostgreSQL instalado, cada uno usa su propio puerto y su propia clave."
    }
    Write-Host " listo"

    $hay = & $Psql @comunes -d postgres -t -A -c "select 1 from pg_database where datname = '$Base'"
    if ($hay -eq "1") {
        Write-Host ""
        Write-Host "La base $Base ya existe. Se borra con todo lo que tenga y se crea de cero." -ForegroundColor Yellow
        $seguir = Read-Host -Prompt "Escribe s para seguir"
        if ($seguir -ne "s") { throw "no se toco nada." }
        Write-Host "Borrando la base anterior..." -NoNewline
        & $Psql @comunes -d postgres -c "drop database $Base with (force)"
        if ($LASTEXITCODE -ne 0) { throw "no pude borrar la base $Base." }
        Write-Host " listo"
    }

    # los permisos de los roles viven dentro de la base: se crean de nuevo junto con ella
    & $Psql @comunes -d postgres -c "drop role if exists kolab_app" -c "drop role if exists kolab_migracion"
    if ($LASTEXITCODE -ne 0) { throw "no pude borrar los roles anteriores." }

    Write-Host "Creando la base..." -NoNewline
    & $Psql @comunes -d postgres -c "create database $Base"
    if ($LASTEXITCODE -ne 0) { throw "no pude crear la base $Base." }
    Write-Host " creada"

    $claveMigracion = Nueva-Clave
    $claveApp = Nueva-Clave

    Write-Host "Creando los roles..." -NoNewline
    & $Psql @comunes -d $Base -v "clave_migracion=$claveMigracion" -v "clave_app=$claveApp" -f $rolesSql
    if ($LASTEXITCODE -ne 0) { throw "kolab-roles.sql fallo." }
    Write-Host " listos"

    Write-Host "Guardando las variables..." -NoNewline
    setx KOLAB_DB_URL "jdbc:postgresql://${Servidor}:$Puerto/$Base" | Out-Null
    setx KOLAB_DB_USER "kolab_app" | Out-Null
    setx KOLAB_DB_PASSWORD $claveApp | Out-Null
    setx KOLAB_MIGRACION_USER "kolab_migracion" | Out-Null
    setx KOLAB_MIGRACION_PASSWORD $claveMigracion | Out-Null
    setx KOLAB_DESARROLLO "true" | Out-Null
    Write-Host " listas"

    Write-Host ""
    Write-Host "Listo." -ForegroundColor Green
    Write-Host "Cierra VS Code (o IntelliJ) y vuelve a abrirlo para que vea las variables nuevas."
    Write-Host "Despues dale Run a KolabApplication: crea las tablas y carga los ejemplos."
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
