# Civ VI Missing Mechanics — Implementation Plan

So sánh các cơ chế còn thiếu giữa project và Civilization VI (Gathering Storm + NFP).

Quy tắc: base game → expansions → game modes.

---

## PHẦN 1: BASE GAME

### 1.1 Tourism + Cultural Victory System ⭐⭐⭐⭐⭐

Trạng thái: ✅ Hoàn thành
Phụ thuộc: Không
File đã sửa:
- ✅ `core/.../models/stats/Stat.kt` — Thêm `Tourism` stat
- ✅ `core/.../models/stats/Stats.kt` — Hỗ trợ Tourism trong tính toán stat
- ✅ `android/assets/jsons/Civ VI/VictoryTypes.json` — Cultural Victory dùng Tourism per turn
- ✅ `core/.../models/ruleset/nation/PersonalityValue.kt` — Thêm Tourism branch
- ✅ `core/.../models/ruleset/unique/Countables.kt` — Thêm Tourism branch
- ✅ `core/.../logic/civilization/NotificationIcons.kt` — Thêm Tourism icon path
- ✅ `core/.../logic/city/CityStats.kt` — Tourism percentage bonus support
- ✅ `android/assets/jsons/Civ VI/Buildings.json` — Theater Square buildings (Amphitheater, Art Museum, Archaeological Museum, Broadcast Center, Film Studio, Marae) + Tourism yields
- ✅ `android/assets/jsons/Civ VI/Buildings.json` — Great Wall wonder + Tourism yield
- ✅ `android/assets/jsons/Civ VI/TileImprovements.json` — Seaside Resort + Tourism yield
- ✅ `android/assets/jsons/Civ VI/TileImprovements.json` — National Park + Tourism yield
- ✅ `core/.../ui/screens/victoryscreen/RankingType.kt` — Thêm Tourism ranking
- ✅ `core/.../logic/civilization/Civilization.kt` — Tourism case in getStatForRanking()
- ✅ `core/.../ui/screens/worldscreen/topbar/WorldScreenTopBarStats.kt` — Tourism display on top bar

Ghi chú:
- Tourism modifiers (open borders, trade routes, government) cần thêm custom unique types và per-civ-pair logic — sẽ triển khai sau khi hoàn thành Great Works System
- Tourism display đã có trên top bar và victory screen rankings

### 1.2 Great Works System ⭐⭐⭐⭐⭐

Trạng thái: ✅ Đã có foundation (data model + manager + building slots)
Phụ thuộc: 1.1 (Tourism stat)
File đã sửa:
- ✅ Tạo `GreatWorkType.kt` — Enum: Writing, Art, Artifact, Music, Relic
- ✅ Tạo `GreatWork.kt` — Data class cho Great Work (id, type, name, creator, era)
- ✅ Tạo `GreatWorksManager.kt` — Quản lý Great Works, tính Tourism/Culture stats
- ✅ `core/.../models/ruleset/Building.kt` — Thêm `greatWorkSlots: Counter<String>` field
- ✅ `core/.../logic/civilization/Civilization.kt` — Thêm `greatWorks` manager + clone + setTransients
- ✅ `core/.../logic/civilization/transients/CivInfoStatsForNextTurn.kt` — Great Works stats aggregation
- ✅ `android/assets/jsons/Civ VI/Buildings.json` — Thêm greatWorkSlots cho:
  - Palace (1 Writing, 1 Art, 1 Music)
  - Amphitheater (1 Writing)
  - Art Museum (2 Art)
  - Archaeological Museum (3 Artifact)
  - Broadcast Center (1 Music)
  - Marae (1 Art)
  - The Great Library (2 Writing)
  - Sistine Chapel (2 Art)
  - Hermitage (3 Art)
  - The Louvre (3 Art, 1 Artifact)
  - Broadway (2 Music)

Ghi chú:
- Cần UI để quản lý Great Works (drag & drop vào slots)
- Cần kết nối Great Person → tạo Great Work khi sử dụng gần thành phố có slot trống
- Cần theming bonuses (cùng era, cùng civ, cùng type) — sẽ triển khai sau

### 1.3 Appeal System ⭐⭐⭐⭐

Trạng thái: ❌ Chưa có
Phụ thuộc: 1.1 (Tourism)
File cần sửa:
- Tạo `TileAppeal.kt` — Tính toán Appeal cho từng tile
- `core/.../models/ruleset/unique/UniqueType.kt` — Appeal uniques
- `core/.../logic/map/TileInfo.kt` — Lưu appeal value
- UI hiển thị appeal lens

### 1.4 National Parks / Naturalists ⭐⭐⭐⭐

Trạng thái: ❌ Chưa có
Phụ thuộc: 1.3 (Appeal)
File cần sửa:
- `android/assets/jsons/Civ VI/Units.json` — Thêm Naturalist unit
- `android/assets/jsons/Civ VI/TileImprovements.json` — Thêm National Park improvement
- `core/.../logic/civilization/managers/` — National Park logic
- `UniqueType.kt` — National Park uniques

### 1.5 Corps/Army/Fleet/Armada ⭐⭐⭐⭐

Trạng thái: ❌ Chưa có
Phụ thuộc: Không
File cần sửa:
- `core/.../logic/map/mapunit/` — Unit combining logic
- `UniqueType.kt` — Corps/Army uniques
- `android/assets/jsons/Civ VI/Units.json` — Thêm Corps/Army variants
- UI cho unit combining

---

## PHẦN 2: RISE & FALL EXPANSION

### 2.1 Alliances ⭐⭐⭐

Trạng thái: ✅ Đã có
File đã sửa:
- `TradeOfferType.kt` — Thêm Alliance type
- `Constants.kt` — Thêm 5 loại alliance + hằng số
- `DiplomacyManager.kt` — Thêm DiplomacyFlags, DiplomaticModifiers, signAlliance(), hasAlliance(), getAllianceType(), setAllianceBasedModifier()
- `DiplomacyFunctions.kt` — canSignAllianceWith(), canSignAllianceOfTypeWith()
- `TradeLogic.kt` — Thêm alliance offers + xử lý accept
- `DiplomacyTurnManager.kt` — processAllianceBonuses() mỗi turn, xử lý flag expiry
- `UniqueType.kt` — TriggerUponSigningAlliance
- `DiplomacyAutomation.kt` — offerAlliances() + wantsToSignAlliance()
- `NextTurnAutomation.kt` — Gọi offerAlliances()
- `DiplomacyScreen.kt` — Hiển thị alliance type trong relationship text

5 loại Alliance: Research (science), Military (production + open borders), Economic (gold), Cultural (culture), Religious (faith)
Yêu cầu: Declaration of Friendship + Embassy, thời hạn 30 turn (điều chỉnh theo speed)

### 2.2 Emergencies ⭐⭐⭐

Trạng thái: ✅ Đã có
File đã sửa:
- `EmergencyData.kt` — Data model mới (EmergencyType, EmergencyData)
- `WorldCongressManager.kt` — processEmergenciesEachTurn(), check triggers tự động mỗi turn, resolve + rewards
- `TurnManager.kt` — Gọi processEmergenciesEachTurn() trong endTurn()

4 loại Emergency: Military (chiếm thành), Religious (cải đạo), Nuclear (dùng bom nguyên tử), AidRequest (thiên tai)
Trigger tự động mỗi turn, tracking contribution, rewards bằng Diplomatic Favor

---

## PHẦN 3: GATHERING STORM EXPANSION

### 3.1 Rock Band Unit ⭐⭐⭐

Trạng thái: ⚠️ GameModesManager có, unit chưa có
Phụ thuộc: 1.1 (Tourism)
File cần sửa:
- `android/assets/jsons/Civ VI/Units.json` — Thêm Rock Band unit
- Unique types cho Rock Band promotions

### 3.2 Giant Death Robot ⭐⭐

Trạng thái: ❌ Chưa có
Phụ thuộc: Không
File cần sửa:
- `android/assets/jsons/Civ VI/Units.json` — Thêm GDR
- Tech tree — Thêm công nghệ mở khóa

---

## PHẦN 4: GAME MODES (NFP)

### 4.1 Barbarian Clans Mode ⭐⭐

Trạng thái: ❌ Chưa có
Phụ thuộc: Không
File cần sửa:
- GameModesManager — logic Barbarian Clans
- UI interaction

### 4.2 Tech & Civic Shuffle Mode ⭐⭐

Trạng thái: ❌ Chưa có
Phụ thuộc: Không
File cần sửa:
- Randomize tech/civic tree order
- UI support

---

## LEGEND

- ⭐⭐⭐⭐⭐ = Rất phức tạp, nhiều file
- ⭐⭐⭐⭐ = Phức tạp
- ⭐⭐⭐ = Trung bình
- ⭐⭐ = Đơn giản
- ✅ = Đã có
- ❌ = Chưa có
- ⚠️ = Một phần
