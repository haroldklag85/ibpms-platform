$bytes = [System.IO.File]::ReadAllBytes('ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java')
$encodings = @(
    [System.Text.Encoding]::UTF8,
    [System.Text.Encoding]::Unicode, # UTF-16LE
    [System.Text.Encoding]::BigEndianUnicode, # UTF-16BE
    [System.Text.Encoding]::GetEncoding('ISO-8859-1')
)

foreach ($enc in $encodings) {
    $text = $enc.GetString($bytes)
    Write-Host "Encoding: $($enc.WebName)"
    $lines = $text -split '\r?\n'
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match 'persistence|jakarta|DmnModelEntity|DmnModelRepository') {
            Write-Host "Line $($i + 1): $($lines[$i])"
        }
    }
}
