# Crea el certificado para probar KOLAB por https en esta maquina.
#
# Es un certificado firmado por si mismo: sirve para ver la aplicacion por https en local, no para
# publicarla. El navegador va a avisar que no conoce quien lo firmo, y esta bien que lo haga.
#
# Deja el archivo en certificados\kolab.p12, que no se sube al repositorio, y guarda la ruta y la
# clave en las variables KOLAB_KEYSTORE y KOLAB_KEYSTORE_CLAVE.

$ErrorActionPreference = 'Stop'

$raiz = Split-Path -Parent $PSScriptRoot
$carpeta = Join-Path $raiz 'certificados'
$archivo = Join-Path $carpeta 'kolab.p12'

# keytool viene con el JDK; si no esta en el PATH se busca donde apunte JAVA_HOME
$keytool = (Get-Command keytool -ErrorAction SilentlyContinue).Source
if (-not $keytool -and $env:JAVA_HOME) {
    $candidato = Join-Path $env:JAVA_HOME 'bin\keytool.exe'
    if (Test-Path $candidato) { $keytool = $candidato }
}
if (-not $keytool) {
    Write-Host 'No encuentro keytool. Viene con el JDK: instala Java 21 o define JAVA_HOME.' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $carpeta)) {
    New-Item -ItemType Directory -Path $carpeta | Out-Null
}

if (Test-Path $archivo) {
    $respuesta = Read-Host 'Ya hay un certificado. Escribe s para rehacerlo, cualquier otra cosa para salir'
    if ($respuesta -ne 's') { exit 0 }
    Remove-Item $archivo
}

# clave al azar: solo letras y digitos, para que entre sin escapes en la propiedad y en setx
$bytes = [byte[]]::new(24)
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$alfabeto = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789'
$clave = -join ($bytes | ForEach-Object { $alfabeto[$_ % $alfabeto.Length] })

& $keytool -genkeypair `
    -alias kolab `
    -keyalg RSA `
    -keysize 2048 `
    -validity 365 `
    -storetype PKCS12 `
    -keystore $archivo `
    -storepass $clave `
    -dname 'CN=localhost, OU=KOLAB, O=KOLAB, L=Lima, C=PE' `
    -ext 'SAN=dns:localhost,ip:127.0.0.1'

if ($LASTEXITCODE -ne 0) {
    Write-Host 'keytool no pudo crear el certificado.' -ForegroundColor Red
    exit 1
}

setx KOLAB_KEYSTORE $archivo | Out-Null
setx KOLAB_KEYSTORE_CLAVE $clave | Out-Null

Write-Host ''
Write-Host "Certificado creado en $archivo" -ForegroundColor Green
Write-Host 'Variables KOLAB_KEYSTORE y KOLAB_KEYSTORE_CLAVE guardadas.'
Write-Host ''
Write-Host 'Cierra esta ventana y abre otra para que las vea, y levanta la aplicacion con:'
Write-Host '  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=https"'
Write-Host 'Despues entra a https://localhost:8443 y acepta el aviso del navegador.'
