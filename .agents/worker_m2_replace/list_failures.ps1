$reports_dir = Join-Path $PSScriptRoot "..\..\backend\ibpms-core\target\surefire-reports"
if (-not (Test-Path -LiteralPath $reports_dir)) {
    Write-Host "Directory not found: $reports_dir"
    exit 1
}

$failures = @()
$errors = @()

$files = Get-ChildItem -LiteralPath $reports_dir -Filter "TEST-*.xml"

foreach ($file in $files) {
    try {
        [xml]$xml = Get-Content -LiteralPath $file.FullName -Raw
        # Handles single testcase or multiple testcases
        $testcases = $xml.testsuite.testcase
        foreach ($testcase in $testcases) {
            $fail = $testcase.failure
            $err = $testcase.error
            if ($null -ne $fail -or $null -ne $err) {
                $node = if ($null -ne $fail) { $fail } else { $err }
                $classname = $testcase.classname
                $name = $testcase.name
                $msg = $node.message
                if ($null -eq $msg) { $msg = $node.InnerText }
                
                $item = [PSCustomObject]@{
                    Classname = $classname
                    Name = $name
                    Message = $msg
                }
                
                if ($null -ne $fail) {
                    $failures += $item
                } else {
                    $errors += $item
                }
            }
        }
    } catch {
        Write-Host "Error parsing $($file.Name): $_"
    }
}

$out_path = Join-Path $PSScriptRoot "fresh_failures.txt"
$out = "TOTAL FRESH FAILURES: $($failures.Count)`r`n"
$out += "TOTAL FRESH ERRORS: $($errors.Count)`r`n`r`n"
$out += "=== FRESH FAILURES ===`r`n"
foreach ($f in ($failures | Sort-Object Classname, Name)) {
    $out += "$($f.Classname).$($f.Name): $($f.Message)`r`n"
}
$out += "`r`n=== FRESH ERRORS ===`r`n"
foreach ($e in ($errors | Sort-Object Classname, Name)) {
    $out += "$($e.Classname).$($e.Name): $($e.Message)`r`n"
}

Set-Content -LiteralPath $out_path -Value $out -Encoding utf8
Write-Host "Wrote $($failures.Count) failures and $($errors.Count) errors to $out_path"
