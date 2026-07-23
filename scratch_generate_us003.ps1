$content = Get-Content -Path ".\docs\requirements\epics\epic_B_formularios_bpmn.md" -Encoding UTF8
$inUs003 = $false
$cas = @()

foreach ($line in $content) {
    if ($line -match "^### US-003:") { $inUs003 = $true }
    elseif ($line -match "^### US-") { $inUs003 = $false }
    
    if ($inUs003 -and $line -match "Scenario: .*?\(CA-(\d+)\)") {
        $caNum = [int]$matches[1]
        $title = $line -replace "^\s*Scenario: \[.*?\]\s*", ""
        $title = $title -replace "^\s*Scenario:\s*", ""
        $title = $title -replace "\s*\(CA-\d+\)\s*", ""
        $cas += [PSCustomObject]@{ Num = $caNum; Title = $title }
    }
}

$cas = $cas | Sort-Object Num

$out = @()
$out += "| CA | Título | Back | Front | QA | Notas / Handoff |"
$out += "|----|--------|------|-------|----|-----------------|"

foreach ($ca in $cas) {
    $num = $ca.Num
    $back = "❌"
    $front = "❌"
    $qa = "❌"
    $notes = ""
    
    if ($num -le 20) {
        $back = "⏳"; $front = "⏳"; $notes = "Sin handoff formal (pre-protocolo)"
    } elseif ($num -le 69) {
        $back = "✅"; $front = "✅"
        $rangeStart = [math]::Floor(($num - 1) / 5) * 5 + 1
        $rangeEnd = $rangeStart + 4
        if ($rangeStart -ge 21) {
            $notes = "handoff_*_US003_CA${rangeStart}_CA${rangeEnd}"
        }
    } else {
        if ($num -eq 87) { $back = "✅"; $front = "❌"; $notes = "handoff_backend_us003_rem_ca87" }
        elseif ($num -eq 88) { $back = "✅"; $front = "✅"; $notes = "handoff_frontend_us003_rem_ca88" }
        elseif ($num -eq 90) { $back = "✅"; $front = "✅"; $notes = "handoff_frontend_us003_rem_ca90" }
        elseif ($num -eq 91) { $back = "✅"; $front = "❌"; $notes = "handoff_backend_us003_rem_ca91" }
        elseif ($num -eq 92) { $back = "✅"; $front = "✅"; $notes = "handoff_frontend_us003_rem_ca92" }
        elseif ($num -eq 93) { $back = "✅"; $front = "✅"; $notes = "handoff_frontend_us003_rem_ca93" }
        else { $back = "❌"; $front = "❌"; $notes = "Pendiente" }
    }
    
    $out += "| CA-$num | $($ca.Title) | $back | $front | $qa | $notes |"
}

$out | Out-File ".\scratch_us003_table.md" -Encoding utf8
