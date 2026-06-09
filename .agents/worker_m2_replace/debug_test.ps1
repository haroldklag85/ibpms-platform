$content = Get-Content -Raw -Path 'ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java'
# Mimic Java replaceAll
# Java's replaceAll uses regex. 
# /* ... */ is represented by /\*(?s).*?\*/
# //... is represented by //.*
# Let's perform regex replacement in PowerShell
$cleanContent = $content -replace '(?s)/\*.*?\*/', ''
$cleanContent = $cleanContent -replace '//.*', ''
$cleanContent = $cleanContent -replace 'DmnModelRepositoryPort', 'DmnModelPort'

$forbiddenTokens = @('DmnModelEntity', 'DmnModelRepository', 'jakarta.persistence', 'javax.persistence', 'org.springframework.data.jpa')
foreach ($token in $forbiddenTokens) {
    if ($cleanContent.Contains($token)) {
        Write-Host "Match found for token: $token"
        $index = $cleanContent.IndexOf($token)
        $start = [Math]::Max(0, $index - 50)
        $length = [Math]::Min(100, $cleanContent.Length - $index)
        Write-Host "Context: $($cleanContent.Substring($start, $length))"
    }
}
