param([string]$BaseUrl = "http://localhost:8080")

$ErrorActionPreference = "Stop"

function Testar-Http {
    param([string]$Id, [string]$Method, [string]$Path,
          [int]$ExpectedStatus, [hashtable]$Headers = @{}, $Body = $null)
    $params = @{ Method = $Method; Uri = "$BaseUrl$Path"; Headers = $Headers;
                 UseBasicParsing = $true }
    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = $Body | ConvertTo-Json
    }
    $response = $null
    try {
        $response = Invoke-WebRequest @params
        $status = [int]$response.StatusCode
    } catch {
        if ($null -eq $_.Exception.Response) { throw }
        $status = [int]$_.Exception.Response.StatusCode
    }
    if ($status -ne $ExpectedStatus) {
        throw "$Id FALHOU: esperado HTTP $ExpectedStatus, recebido $status ($Path)"
    }
    Write-Host "$Id OK: HTTP $status ($Path)"
    if ($null -ne $response -and $response.Content) {
        return $response.Content | ConvertFrom-Json
    }
}

$admin = Testar-Http -Id "AU01/admin" -Method Post -Path "/auth/login" `
    -ExpectedStatus 200 -Body @{ email = "admin@biblioteca.com"; senha = "password" }
if (-not $admin.token -or $admin.perfil -ne "administrador") { throw "Resposta de login invalida" }
$adminHeaders = @{ Authorization = "Bearer $($admin.token)" }
$null = Testar-Http -Id "AU01/painel" -Method Get -Path "/admin/painel" -ExpectedStatus 200 -Headers $adminHeaders
$null = Testar-Http -Id "CT01" -Method Post -Path "/auth/login" -ExpectedStatus 401 `
    -Body @{ email = "admin@biblioteca.com"; senha = "senha-incorreta" }
$null = Testar-Http -Id "AU05" -Method Get -Path "/admin/painel" -ExpectedStatus 401

$leitor = Testar-Http -Id "AU01/leitor" -Method Post -Path "/auth/login" `
    -ExpectedStatus 200 -Body @{ email = "joao@biblioteca.com"; senha = "password" }
$null = Testar-Http -Id "CT02" -Method Get -Path "/admin/usuarios" -ExpectedStatus 403 `
    -Headers @{ Authorization = "Bearer $($leitor.token)" }
$null = Testar-Http -Id "AU06" -Method Get -Path "/atendente/painel" -ExpectedStatus 200 -Headers $adminHeaders

$atendente = Testar-Http -Id "AU01/atendente" -Method Post -Path "/auth/login" `
    -ExpectedStatus 200 -Body @{ email = "carlos@biblioteca.com"; senha = "password" }
$null = Testar-Http -Id "AU10" -Method Get -Path "/admin/painel" -ExpectedStatus 403 `
    -Headers @{ Authorization = "Bearer $($atendente.token)" }

Write-Host "Verificacao rapida concluida. Execute os demais casos do documento de evidencias."
