Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('D:\lamviec\test\uncivSAU\desktop\build\libs\Unciv.jar')
$zip.Entries | Where-Object { $_.FullName -like '*jsons*' } | Select-Object FullName | Select-Object -First 20
$zip.Dispose()