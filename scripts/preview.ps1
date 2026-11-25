$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/java.exe'))) {
    throw 'Configura JAVA_HOME con la ruta de tu JDK 21 antes de iniciar la vista previa.'
}

Push-Location $projectRoot
try {
    & .\mvnw.cmd test-compile dependency:build-classpath '-Dmdep.outputFile=target/preview-classpath.txt' '-DincludeScope=test'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar el classpath de la vista previa.' }
    $dependencies = (Get-Content -Raw 'target/preview-classpath.txt').Trim()
    $previewClasspath = "target/test-classes;target/classes;$dependencies"
    # Evitar los argfiles del plugin Spring Boot: su codificacion falla en algunas rutas con tildes de Windows.
    & (Join-Path $env:JAVA_HOME 'bin/java.exe') '-Dspring.devtools.restart.enabled=false' -cp $previewClasspath org.esfe.preview.FrontendPreviewApplication
    if ($LASTEXITCODE -ne 0) { throw 'La vista previa termino con un error.' }
}
finally {
    Pop-Location
}
