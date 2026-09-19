Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('D:\lamviec\test\uncivSAU\desktop\build\libs\Unciv.jar')
$entry = $zip.Entries | Where-Object { $_.FullName -eq 'jsons/Civ VI/Nations.json' }
if ($entry) {
    Write-Host "Nations.json found in JAR"
    Write-Host "Size: $($entry.Length) bytes"
} else {
    Write-Host "Nations.json NOT found in JAR"
}
$zip.Dispose()