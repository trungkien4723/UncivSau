Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('D:\lamviec\test\uncivSAU\desktop\build\libs\Unciv.jar')
$entry = $zip.Entries | Where-Object { $_.FullName -eq 'jsons/Civ VI/Nations.json' }
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$content = $reader.ReadToEnd()
$stream.Close()
$zip.Dispose()
$content.Substring(0, 500)