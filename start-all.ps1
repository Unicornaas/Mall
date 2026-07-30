[CmdletBinding()]
param(
    [string]$RunService,
    [string]$MavenPath,
    [switch]$RunFrontend,
    [string]$NpmPath
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot

# Child mode: keep each long-running process attached to its own hidden
# PowerShell process so stdout and stderr can be written to separate logs.
if ($RunService) {
    Set-Location -LiteralPath $projectRoot
    & $MavenPath '-Dfile.encoding=UTF-8' '-DskipTests' '-pl' $RunService 'spring-boot:run'
    exit $LASTEXITCODE
}

if ($RunFrontend) {
    Set-Location -LiteralPath (Join-Path $projectRoot 'mall-front')
    & $NpmPath 'run' 'dev' '--' '--host' '0.0.0.0'
    exit $LASTEXITCODE
}

function Test-LocalPort {
    param(
        [Parameter(Mandatory = $true)][int]$Port,
        [int]$TimeoutMilliseconds = 500
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $connection = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
        if (-not $connection.AsyncWaitHandle.WaitOne($TimeoutMilliseconds, $false)) {
            return $false
        }
        $client.EndConnect($connection)
        return $client.Connected
    }
    catch {
        return $false
    }
    finally {
        $client.Close()
    }
}

function Wait-LocalPort {
    param(
        [Parameter(Mandatory = $true)][int]$Port,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-LocalPort -Port $Port) {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

function Resolve-MavenPath {
    $pathCommand = Get-Command 'mvn.cmd' -ErrorAction SilentlyContinue
    if ($pathCommand) {
        return $pathCommand.Source
    }

    $wrapperCache = Join-Path $env:USERPROFILE '.m2\wrapper\dists'
    if (Test-Path -LiteralPath $wrapperCache) {
        $cachedMaven = Get-ChildItem -Path $wrapperCache -Filter 'mvn.cmd' -File -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -match '\\apache-maven-[^\\]+\\bin\\mvn\.cmd$' } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($cachedMaven) {
            return $cachedMaven.FullName
        }
    }

    $projectWrapper = Join-Path $projectRoot 'mvnw.cmd'
    if (Test-Path -LiteralPath $projectWrapper) {
        return $projectWrapper
    }

    throw 'Maven was not found. Install Maven or run mvnw.cmd once, then retry.'
}

function Start-LoggedChild {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Arguments,
        [Parameter(Mandatory = $true)][string]$LogDirectory
    )

    $standardOutput = Join-Path $LogDirectory "$Name.out.log"
    $standardError = Join-Path $LogDirectory "$Name.err.log"
    return Start-Process -FilePath 'powershell.exe' `
        -ArgumentList $Arguments `
        -WorkingDirectory $projectRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $standardOutput `
        -RedirectStandardError $standardError `
        -PassThru
}

function Quote-ProcessArgument {
    param([Parameter(Mandatory = $true)][string]$Value)
    return '"' + $Value.Replace('"', '\"') + '"'
}

$services = @(
    [PSCustomObject]@{ Name = 'mall-gateway';   Port = 8080 },
    [PSCustomObject]@{ Name = 'mall-user';      Port = 8081 },
    [PSCustomObject]@{ Name = 'mall-product';   Port = 8082 },
    [PSCustomObject]@{ Name = 'mall-order';     Port = 8083 },
    [PSCustomObject]@{ Name = 'mall-cart';      Port = 8084 },
    [PSCustomObject]@{ Name = 'mall-inventory'; Port = 8085 },
    [PSCustomObject]@{ Name = 'mall-payment';   Port = 8086 }
)

try {
    Write-Host '=== Mall one-click startup ===' -ForegroundColor Cyan

    $preferredJdk = 'C:\Program Files\Java\jdk-17'
    if (Test-Path -LiteralPath (Join-Path $preferredJdk 'bin\java.exe')) {
        $env:JAVA_HOME = $preferredJdk
        $env:Path = (Join-Path $preferredJdk 'bin') + ';' + $env:Path
    }
    elseif (-not (Get-Command 'java.exe' -ErrorAction SilentlyContinue)) {
        throw 'Java 17 was not found. Configure JAVA_HOME and retry.'
    }

    $logRoot = Join-Path $projectRoot '.run-logs'
    $logDirectory = Join-Path $logRoot (Get-Date -Format 'yyyyMMdd-HHmmss')
    New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

    $nacosHome = if ($env:NACOS_HOME) { $env:NACOS_HOME } else { 'D:\nacos' }
    $nacosStartup = Join-Path $nacosHome 'bin\startup.cmd'
    if (Test-LocalPort -Port 8848) {
        Write-Host '[SKIP] Nacos is already listening on port 8848.' -ForegroundColor DarkGray
    }
    else {
        if (-not (Test-Path -LiteralPath $nacosStartup)) {
            throw "Nacos startup script was not found: $nacosStartup"
        }

        Write-Host '[START] Nacos (standalone mode)...' -ForegroundColor Yellow
        $nacosOutput = Join-Path $logDirectory 'nacos-startup.out.log'
        $nacosError = Join-Path $logDirectory 'nacos-startup.err.log'
        $nacosArguments = '/d /c ""{0}" -m standalone"' -f $nacosStartup
        $nacosProcess = Start-Process -FilePath $env:ComSpec `
            -ArgumentList $nacosArguments `
            -WorkingDirectory (Split-Path -Parent $nacosStartup) `
            -WindowStyle Hidden `
            -RedirectStandardOutput $nacosOutput `
            -RedirectStandardError $nacosError `
            -PassThru `
            -Wait

        if (-not (Wait-LocalPort -Port 8848 -TimeoutSeconds 90)) {
            throw "Nacos did not start on port 8848. Check logs in $logDirectory"
        }
        Write-Host '[OK] Nacos is ready.' -ForegroundColor Green
    }

    if (-not (Test-LocalPort -Port 3306)) {
        Write-Warning 'MySQL port 3306 is not listening. Backend services may fail to start.'
    }

    $serviceStates = @{}
    $servicesToStart = @()
    foreach ($service in $services) {
        if (Test-LocalPort -Port $service.Port) {
            $serviceStates[$service.Name] = 'ALREADY RUNNING'
            Write-Host ("[SKIP] {0} is already listening on port {1}." -f $service.Name, $service.Port) -ForegroundColor DarkGray
        }
        else {
            $servicesToStart += $service
            $serviceStates[$service.Name] = 'STARTING'
        }
    }

    if ($servicesToStart.Count -gt 0) {
        $resolvedMavenPath = Resolve-MavenPath
        Write-Host '[BUILD] Installing the shared mall-common module...' -ForegroundColor Yellow
        Push-Location -LiteralPath $projectRoot
        try {
            & $resolvedMavenPath '-Dfile.encoding=UTF-8' '-DskipTests' '-pl' 'mall-common' 'install'
            if ($LASTEXITCODE -ne 0) {
                throw "mall-common build failed with exit code $LASTEXITCODE."
            }
        }
        finally {
            Pop-Location
        }

        $quotedScript = Quote-ProcessArgument -Value $PSCommandPath
        $quotedMaven = Quote-ProcessArgument -Value $resolvedMavenPath
        foreach ($service in $servicesToStart) {
            Write-Host ("[START] {0} (port {1})..." -f $service.Name, $service.Port) -ForegroundColor Yellow
            $childArguments = "-NoProfile -ExecutionPolicy Bypass -File $quotedScript -RunService $($service.Name) -MavenPath $quotedMaven"
            $process = Start-LoggedChild -Name $service.Name -Arguments $childArguments -LogDirectory $logDirectory
            Write-Host ("        PID {0}" -f $process.Id) -ForegroundColor DarkGray
        }
    }

    $frontendState = 'STARTING'
    if (Test-LocalPort -Port 5173) {
        $frontendState = 'ALREADY RUNNING'
        Write-Host '[SKIP] mall-front is already listening on port 5173.' -ForegroundColor DarkGray
    }
    else {
        $resolvedNpmPath = (Get-Command 'npm.cmd' -ErrorAction SilentlyContinue).Source
        if (-not $resolvedNpmPath) {
            throw 'npm.cmd was not found. Install Node.js and retry.'
        }

        $frontendDirectory = Join-Path $projectRoot 'mall-front'
        if (-not (Test-Path -LiteralPath (Join-Path $frontendDirectory 'node_modules'))) {
            Write-Host '[INSTALL] Installing frontend dependencies...' -ForegroundColor Yellow
            Push-Location -LiteralPath $frontendDirectory
            try {
                & $resolvedNpmPath 'install'
                if ($LASTEXITCODE -ne 0) {
                    throw "npm install failed with exit code $LASTEXITCODE."
                }
            }
            finally {
                Pop-Location
            }
        }

        Write-Host '[START] mall-front (port 5173)...' -ForegroundColor Yellow
        $quotedScript = Quote-ProcessArgument -Value $PSCommandPath
        $quotedNpm = Quote-ProcessArgument -Value $resolvedNpmPath
        $frontendArguments = "-NoProfile -ExecutionPolicy Bypass -File $quotedScript -RunFrontend -NpmPath $quotedNpm"
        $frontendProcess = Start-LoggedChild -Name 'mall-front' -Arguments $frontendArguments -LogDirectory $logDirectory
        Write-Host ("        PID {0}" -f $frontendProcess.Id) -ForegroundColor DarkGray
    }

    Write-Host 'Waiting for services to become ready...' -ForegroundColor Cyan
    $deadline = (Get-Date).AddSeconds(150)
    do {
        $pendingCount = 0
        foreach ($service in $services) {
            if ($serviceStates[$service.Name] -eq 'STARTING') {
                if (Test-LocalPort -Port $service.Port) {
                    $serviceStates[$service.Name] = 'RUNNING'
                }
                else {
                    $pendingCount++
                }
            }
        }

        if ($frontendState -eq 'STARTING') {
            if (Test-LocalPort -Port 5173) {
                $frontendState = 'RUNNING'
            }
            else {
                $pendingCount++
            }
        }

        if ($pendingCount -gt 0) {
            Start-Sleep -Seconds 2
        }
    } while ($pendingCount -gt 0 -and (Get-Date) -lt $deadline)

    $result = @()
    $result += [PSCustomObject]@{ Module = 'nacos'; Port = 8848; Status = 'RUNNING' }
    foreach ($service in $services) {
        $status = $serviceStates[$service.Name]
        if ($status -eq 'STARTING') {
            $status = 'FAILED - CHECK LOG'
        }
        $result += [PSCustomObject]@{ Module = $service.Name; Port = $service.Port; Status = $status }
    }
    if ($frontendState -eq 'STARTING') {
        $frontendState = 'FAILED - CHECK LOG'
    }
    $result += [PSCustomObject]@{ Module = 'mall-front'; Port = 5173; Status = $frontendState }

    Write-Host ''
    $result | Format-Table -AutoSize
    Write-Host 'Mall:  http://localhost:5173' -ForegroundColor Green
    Write-Host 'Nacos: http://localhost:8848/nacos' -ForegroundColor Green
    Write-Host ("Logs:  {0}" -f $logDirectory) -ForegroundColor Cyan

    if ($result.Status -contains 'FAILED - CHECK LOG') {
        exit 1
    }
    exit 0
}
catch {
    Write-Host ''
    Write-Host ("Startup failed: {0}" -f $_.Exception.Message) -ForegroundColor Red
    exit 1
}
