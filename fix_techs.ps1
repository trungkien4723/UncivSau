$file = "D:\lamviec\test\uncivSAU\android\assets\jsons\Civ VI\Units.json"
$lines = Get-Content $file

$techMap = @{
    "Archer" = "Archery"
    "Spearman" = "Bronze Working"
    "Galley" = "Sailing"
    "Heavy Chariot" = "Wheel"
    "Battering Ram" = "Masonry"
    "Horseman" = "Horseback Riding"
    "Swordsman" = "Iron Working"
    "Catapult" = "Engineering"
    "Quadrireme" = "Shipbuilding"
    "Siege Tower" = "Construction"
    "Pikeman" = "Military Tactics"
    "Knight" = "Stirrups"
    "Crossbowman" = "Machinery"
    "Military Engineer" = "Military Engineering"
    "Musketman" = "Gunpowder"
    "Caravel" = "Cartography"
    "Frigate" = "Square Rigging"
    "Privateer" = "Mercantilism"
    "Bombard" = "Metal Casting"
    "Field Cannon" = "Ballistics"
    "Ironclad" = "Steam Power"
    "Medic" = "Sanitation"
    "Ranger" = "Rifling"
    "Artillery" = "Steel"
    "Battleship" = "Steel"
    "AT Crew" = "Chemistry"
    "Infantry" = "Replaceable Parts"
    "Submarine" = "Electricity"
    "Tank" = "Combustion"
    "Machine Gun" = "Advanced Ballistics"
    "Biplane" = "Flight"
    "Observation Balloon" = "Flight"
    "Anti-Air Gun" = "Advanced Ballistics"
    "Bomber" = "Advanced Flight"
    "Fighter" = "Advanced Flight"
    "Aircraft Carrier" = "Combined Arms"
    "Destroyer" = "Combined Arms"
    "Helicopter" = "Synthetic Materials"
    "Mechanized Infantry" = "Satellites"
    "Rocket Artillery" = "Guidance Systems"
    "Mobile SAM" = "Guidance Systems"
    "Jet Fighter" = "Lasers"
    "Missile Cruiser" = "Lasers"
    "Modern Armor" = "Composites"
    "Modern AT" = "Composites"
    "Jet Bomber" = "Stealth Technology"
    "Nuclear Submarine" = "Telecommunications"
    "Spec Ops" = "Advanced Ballistics"
    "Man-At-Arms" = "Apprenticeship"
    "Courser" = "Stirrups"
    "Trebuchet" = "Physics"
    "Pike and Shot" = "Gunpowder"
    "Line Infantry" = "Rifling"
    "Cuirassier" = "Military Science"
    "Drone" = "Advanced Ballistics"
    "Supply Convoy" = "Combined Arms"
    "Skirmisher" = "Horseback Riding"
    "Giant Death Robot" = "Future Technology"
}

$seenUnit = $null
$count = 0
for ($i = 0; $i -lt $lines.Count; $i++) {
    # Check for unit name line
    if ($lines[$i] -match '"name": "([^"]+)"') {
        $seenUnit = $matches[1]
    }
    # Check for requiredTech null line and if we have a tech for this unit
    if ($lines[$i] -match '"requiredTech": null' -and $techMap.ContainsKey($seenUnit)) {
        $lines[$i] = '    "requiredTech": "' + $techMap[$seenUnit] + '",'
        Write-Host "Fixed: $seenUnit -> $($techMap[$seenUnit])"
        $count++
    }
}

Set-Content $file $lines
Write-Host "`nTotal: $count units updated"
