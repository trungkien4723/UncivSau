Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('D:\lamviec\test\uncivSAU\desktop\build\libs\Unciv.jar')
$entries = $zip.Entries | Where-Object { $_.FullName -like '*jsons/Civ VI/*' }
$entries | Select-Object FullName | Select-Object -First 10
$zip.Dispose()