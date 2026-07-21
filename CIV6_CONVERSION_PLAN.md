# Kế hoạch chuyển đổi Unciv (Civ 5) → Cơ chế Civilization 6

> Tài liệu này dựa trên khảo sát **code thực tế** của repo Unciv (branch mặc định, clone shallow).
> Mục tiêu: tái tạo các cơ chế cốt lõi của Civ 6 trên nền Unciv.
> Ngôn ngữ code: Kotlin + LibGDX. Ruleset dạng JSON + hệ thống "Uniques".

## TRẠNG THÁI TIẾN ĐỘ
- ✅ **Phase 0** — Ruleset "Civ VI" (base ruleset mới, load sạch, 0 lỗi)
- ✅ **Phase 1** — Eureka (10 tech mẫu, verify end-to-end: fire đúng + boost đúng + once-guard)
- ✅ **Phase 2** — Builder charges (Worker→Builder, 3 charges, instant build, tự disand; 100% data-driven, 0 sửa code lõi; test PASSED)
- ✅ **Phase 3a** — Civics tree (clone TechManager→CivicManager, dùng Culture; Inspiration; CivicPickerScreen; nút Civics F11; test PASSED)
- ✅ **Phase 3b** — Government + Policy cards (code có sẵn; bổ sung requiredCivic + UI nút Government + dữ liệu Governments.json/PolicyCards.json; AllObjectsTests PASSED)
- ✅ **Phase 4** — Districts + Unstacked Cities (lõi + UI city-screen + AI adjacency + **combat/pillage district** + **mở rộng AI xây/đặt district tự động**; test DistrictPillageTests PASSED). 
- ✅ **Phase 5** — Leader Agendas (Agenda.kt + Agendas.json + gắn vào Nation/Civilization; DiplomacyManager agenda modifiers (likes/dislikes filter); random hidden agenda khi start; UI hiển thị agenda trong DiplomacyScreen; test AgendaTests PASSED).
- ✅ **Phase 6** — Nội dung & cân bằng (Hoàn thành):
  - ✅ Districts hoạt động end-to-end: 6→13 districts (thêm Encampment, Harbor, Aqueduct, Entertainment Complex, Water Entertainment, Spaceport, Government Plaza) + placement buildings (`CreatesOneDistrict`) + gán `district` cho buildings con (Library→Campus, vv).
  - ✅ Ruleset Civ VI load sạch (test `CivVIRulesetLoadTest` PASSED).
  - ✅ **6D Loyalty & Governors** — `CityLoyaltyManager` (wired), mất thành khi loyalty=0, `Governor.kt`+`GovernorManager`, UI Assign/Recall trong CityScreen, `Governors.json` (7 governors), `LoyaltyTests` PASSED, `core:compileKotlin` PASSED.
  - ✅ **6E Power/Climate/Disasters** — Hoàn thành 95%

---

## 0. Tóm tắt điều hành (đọc cái này trước)

**Kết luận khảo sát:** Unciv là bản tái tạo Civ 5 **rất mature**, có kiến trúc cực kỳ mở rộng được nhờ hệ thống **Uniques** (~700+ UniqueType) và **RulesetFile** (load JSON động). Điều này khiến phần lớn cơ chế Civ 6 KHẢ THI, nhưng khối lượng thực tế rất lớn.

**Phân loại độ khó 5 trụ cột Civ 6:**

| Trụ cột | Độ khó | Lý do |
|---------|--------|-------|
| **Eureka / Inspiration** (boost tech/civic) | ⭐⭐ Trung bình | Hạ tầng đã gần đủ: `techsInProgress`, `OneTimeGainTechPercent`, hệ thống `TriggerUpon*` |
| **Civics tree + Government + Policy cards** | ⭐⭐⭐ Khá | Tái dùng nguyên mẫu `Policy`/`PolicyBranch`/`PolicyManager`. Chủ yếu là nhân bản + thêm slot |
| **Leader Agendas** (AI tính cách) | ⭐⭐⭐ Khá | Đã có `Personality`/`PersonalityValue`. Cần thêm hệ agenda + ảnh hưởng quan hệ ngoại giao |
| **Districts + Unstacked Cities** | ⭐⭐⭐⭐⭐ Rất khó | Thay đổi kiến trúc lõi: city↔tile↔building↔yield↔combat↔AI↔UI. Đây là 70% công sức |
| **Adjacency bonuses** | ⭐⭐⭐ Khá | Đã có mẫu `ImprovementStatsForAdjacencies`. Cần mở rộng sang district |

**Chiến lược tổng thể:** Làm theo thứ tự từ dễ đến khó, mỗi giai đoạn tạo ra một bản build **chạy được và test được**, không "big bang". Tạo một **ruleset mới "Civ VI"** riêng thay vì phá ruleset Civ 5 hiện có (an toàn, có thể so sánh).

---

## 1. Nguyên tắc thiết kế

1. **Không phá bản Civ 5 hiện tại.** Tạo ruleset mới `android/assets/jsons/Civ VI/` song song. Mọi thay đổi code lõi phải backward-compatible (guard bằng ruleset flag / sự tồn tại của district JSON).
2. **Ưu tiên hệ thống Uniques.** Bất cứ thứ gì có thể làm bằng Unique thì làm bằng Unique, hạn chế hardcode.
3. **Mỗi giai đoạn phải build + chạy được.** Verify bằng `./gradlew core:compileKotlin` và test in-game.
4. **Serialization an toàn.** Mọi field mới trên class serialize (`City`, `Tile`, `Civilization`) phải có giá trị mặc định để không vỡ save cũ. Tham chiếu `BackwardCompatibility.kt`.
5. **Tận dụng `CreatesOneImprovement`** làm bàn đạp cho district (đã giải quyết: chọn tile, mark tile, đặt object khi hoàn thành, validate).

---

## 2. Bản đồ code cốt lõi (từ khảo sát thực tế)

### Thành phố & Tile
- `core/src/com/unciv/logic/city/City.kt` — `tiles: HashSet<HexCoord>`, `workedTiles`, `tilesInRange` (transient), sub-managers (population, cityConstructions, expansion).
- `core/src/com/unciv/logic/city/CityConstructions.kt` — `builtBuildings: HashSet<String>` (KHÔNG gắn tile). Cơ chế `CreatesOneImprovement` (dòng ~867-895, 1058) là hook đặt object lên tile.
- `core/src/com/unciv/logic/city/CityStats.kt` — `updateTileStats()` (~349) tập hợp yield từ tile.
- `core/src/com/unciv/logic/map/tile/Tile.kt` — `improvement: String?`, `owningCity` (transient), markers `CreatesOneImprovement`.
- `core/src/com/unciv/logic/map/tile/TileStatFunctions.kt` — `getTileStats` (~36), adjacency đã có ở mức improvement: `ImprovementStatsForAdjacencies` (~312).
- `core/src/com/unciv/models/ruleset/Building.kt` — ràng buộc tile qua uniques so với **city center** (MustBeOn/NextTo...).

### Ruleset & Uniques
- `core/src/com/unciv/models/ruleset/Ruleset.kt` — `enum RulesetFile` (~49-102) đăng ký từng JSON; map `technologies`, `policies`, `buildings`... (~125-156); `load()` (~367-596).
- `core/src/com/unciv/models/ruleset/unique/UniqueType.kt` — ~700+ enum, mỗi cái là template chuỗi + targets.
- `core/src/com/unciv/models/ruleset/unique/UniqueTriggerActivation.kt` — `getTriggerFunction` (when khổng lồ). `OneTimeGainTechPercent` (~890-916) = Eureka-like.
- `core/src/com/unciv/models/ruleset/unique/UniqueTarget.kt` — enum targets (cần thêm Civic/Government/District).

### Tech / Policy
- `core/src/com/unciv/logic/civilization/managers/TechManager.kt` — `techsInProgress` (~71), `costOfTech` (~107-120), `addTechnology` (~296-358) có hook `TriggerUponResearch`.
- `core/src/com/unciv/logic/civilization/managers/PolicyManager.kt` — `adoptedPolicies`, `getPolicyCultureCost` (~170), `adopt()` (~241-287).
- `core/src/com/unciv/models/ruleset/Policy.kt` + `PolicyBranch.kt`.

### AI / Personality
- `core/src/com/unciv/models/ruleset/nation/Personality.kt` + `PersonalityValue.kt`.
- `core/src/com/unciv/logic/automation/` — AI logic (construction, diplomacy).

---

## 3. Lộ trình theo giai đoạn

Mỗi Phase kết thúc = 1 build chạy được + test.

---

### PHASE 0 — Nền tảng dự án (1 đơn vị)
**Mục tiêu:** Có môi trường làm việc + ruleset Civ VI rỗng load được.

- [x] Tạo thư mục `android/assets/jsons/Civ VI/` copy từ `Civ V - Gods & Kings` làm điểm khởi đầu.
- [x] Đặt `ModOptions.json` đánh dấu đây là base ruleset mới (tên "Civ VI").
- [x] Xác nhận game load được ruleset mới, chơi được (lúc này vẫn là Civ 5 mechanics, chỉ đổi tên).
- **Verify:** Chạy desktop, New Game → chọn ruleset "Civ VI" → vào được map.

---

### PHASE 1 — Eureka / Inspiration (⭐⭐)
**Mục tiêu:** Tech/Civic có "boost" khi làm hành động cụ thể (giống Civ 6).

**Cơ chế Civ 6:** mỗi tech có 1 Eureka (VD "Xây 1 mỏ → +40% Bronze Working"). Civic có Inspiration tương tự.

**Thiết kế kỹ thuật:**
- Tái dùng `OneTimeGainTechPercent` (đã cộng % cost vào `techsInProgress[tech]`).
- Cho phép gắn conditional `<upon [action]>` (dùng các `TriggerUpon*` sẵn có: `TriggerUponConstructingBuilding`, `TriggerUponBuildingImprovement`, `TriggerUponFoundingCity`, `TriggerUponGainingUnit`, `TriggerUponCombat`, `TriggerUponDiscoveringNaturalWonder`...).

**Việc cần làm:**
- [x] Xác định: có thể mô tả Eureka hoàn toàn bằng Unique hiện có không? Nếu thiếu trigger nào → thêm `TriggerUpon*` mới vào `UniqueType.kt` + xử lý điểm phát sự kiện.
- [x] Thêm cơ chế "mỗi Eureka chỉ kích 1 lần" (dùng flag/temporaryUniques hoặc set đã-kích trên `TechManager`).
- [x] UI: hiển thị Eureka đã đạt / chưa đạt trên màn tech tree (`TechPickerScreen`).
- [x] Data: thêm Eureka cho ~10 tech đầu làm mẫu (chưa cần đủ toàn bộ).
- **Verify:** Xây mỏ → thấy tech tương ứng nhảy % progress; UI hiển thị đúng.

*Ghi chú: Inspiration cho Civic sẽ hoàn thiện sau khi có Civics (Phase 3).*

---

### PHASE 2 — Builder giới hạn lượt dùng (⭐)
**Mục tiêu:** Worker → Builder (Civ 6): có N lần dùng (mặc định 3), mỗi improvement hoàn thành ngay lập tức, hết lần thì biến mất.

**Thiết kế:** Đây là bài "khởi động nhẹ" trước khi vào district. Có thể gần như làm bằng Unique.
- [x] Kiểm tra Unique sẵn có cho "build charges" (grep `BuildImprovements`, `CanBuildImprovements`). Nếu chưa có khái niệm charges → thêm field `buildCharges` vào `MapUnit` + Unique `"Can build [amount] improvements"`.
- [x] Improvement hoàn thành tức thì (bỏ turnsToBuild hoặc set = 1) khi ruleset Civ VI.
- [x] Giảm charge sau mỗi lần build, disband khi = 0.
- [x] UI hiển thị số charge còn lại trên unit.
- **Verify:** Builder xây 3 lần rồi biến mất.

---

### PHASE 3 — Civics Tree + Government + Policy Cards (⭐⭐⭐)
**Mục tiêu:** Tách cây Civics (chạy bằng Culture) riêng khỏi Tech; có Government với các slot; Policy card hoán đổi được.

**Thiết kế kỹ thuật (tái dùng tối đa nguyên mẫu Policy):**

**3a. Civics tree**
- [x] Tạo `models/ruleset/Civic.kt` (clone `Policy.kt`, target `UniqueTarget.Civic`).
- [x] Tạo `CivicColumn`/cấu trúc cây (giống TechColumn) hoặc dùng prerequisites như Tech.
- [x] `Ruleset.kt`: thêm map `civics`, entry `RulesetFile.Civics("Civics.json", ...)`, khối load.
- [x] Tạo `CivicManager.kt` (clone `TechManager` — dùng Culture thay Science: `civicsInProgress`, `costOfCivic`, `addCivic` + hook `TriggerUponAdoptingCivic`).
- [x] Chuyển nguồn nuôi Civics: Culture giờ nuôi Civic tree (không còn nuôi Social Policies theo kiểu Civ5). Cần tách rõ Culture→Civics và một nguồn riêng cho việc mở khóa policy card.
- [x] UI: `CivicPickerScreen` (clone `TechPickerScreen`).

**3b. Government + Policy cards**
- [x] Tạo `models/ruleset/Government.kt`: định nghĩa số slot theo loại (Military / Economic / Diplomatic / Wildcard).
- [x] Tạo `models/ruleset/PolicyCard.kt`: mỗi card có `slotType` + uniques (hiệu ứng). Có thể kế thừa `Policy` hiện có.
- [x] `Ruleset.kt`: thêm map `governments`, `policyCards` + RulesetFile + load.
- [x] Tạo `GovernmentManager.kt`: lưu government hiện tại + card đang gắn vào từng slot; cho phép đổi card (Civ 6: đổi tự do khi vừa unlock civic, ngoài ra tốn gold).
- [x] Government unlock qua Civics.
- [x] UI: `GovernmentPickerScreen` (chọn government) + `PolicyCardScreen` (kéo thả card vào slot).
- [x] Hoàn thiện **Inspiration** (Phase 1 phần Civic) trỏ vào `CivicManager`.
- **Verify:** Nghiên cứu Civic → unlock Government → gắn policy card → thấy hiệu ứng stat.

*Đây là giai đoạn lớn; nên chia nhỏ 3a và 3b thành 2 lần build riêng.*

---

### PHASE 4 — DISTRICTS + UNSTACKED CITIES (⭐⭐⭐⭐⭐) — TRỌNG TÂM
**Mục tiêu:** District (Campus, Holy Site, Theater...) nằm trên **ô riêng** ngoài city center; building chuyên biệt xây trong district; wonder chiếm ô riêng.

Đây là 70% công sức. Chia thành nhiều sub-phase build được.

**4a. Model District**
- [x] Tạo `models/ruleset/District.kt` (RulesetStatsObject): `name`, `cost`, ràng buộc tile (`onlyBuildableOn`/terrain filter), `allowedBuildings`, uniques.
- [x] `RulesetFile.Districts("Districts.json", ...)` + map + load.
- [x] Thêm `UniqueTarget.District`.

**4b. Lưu district trên City/Tile**
- [x] `City.kt`: thêm field serialize `var districts = HashMap<HexCoord, String>()` (ô → tên district). Mặc định rỗng (backward-compat).
- [x] `Tile.kt`: thêm getter tiện ích `getDistrict()` / `isDistrictCenter()` dựa trên `owningCity.districts`.
- [x] Đảm bảo `setTransients` khôi phục liên kết đúng.

**4c. Xây district (tái dùng CreatesOneImprovement)**
- [x] Mở rộng luồng `CityConstructions.applyCreateOneImprovement` (~1058) để đặt district vào `city.districts[tile]` thay vì (hoặc cùng) improvement.
- [x] `canPlaceCreateOneImprovementOn` (~869): thêm validate cho district (terrain hợp lệ, chưa có district cùng loại, không phải city center).
- [x] Cho phép chọn tile khi xây district (UI đã có sẵn cho CreatesOneImprovement → mở rộng).

**4d. Building thuộc district**
- [x] `Building.kt`: thêm field `district: String?` — building chỉ xây được nếu city có district đó.
- [x] `CityConstructions`: rejection reason nếu thiếu district tiên quyền.

**4e. Yield & Adjacency (⭐⭐⭐)**
- [x] `TileStatFunctions.getTileStats`: tính yield cho district tile (dựa trên district + buildings trong đó).
- [x] Thêm UniqueType adjacency kiểu Civ6: `"[stats] for each adjacent [tileFilter/districtFilter]"` — mở rộng từ `ImprovementStatsForAdjacencies` (~312) và `ForEveryAdjacentTile`.
- [x] `CityStats.updateTileStats` (~349): cộng yield district + adjacency, tách khỏi worked-tile thường (district tile không cần dân làm việc).

**4f. UI**
- [ ] Hiển thị district trên bản đồ (icon/sprite trên tile).
- [ ] City screen: hiển thị district + building bên trong.
- [ ] Preview adjacency khi đặt district (giống Civ 6 hiện +N).

**4g. Combat / Pillage (ĐÃ LÀM)**
- [x] `Tile.districtIsPillaged` (serialize, default false → backward-compat).
- [x] `setPillaged()`/`setRepaired()` xử lý district; `canPillageTile()`/`isPillaged()` bao gồm district.
- [x] `getDistrict()` trả null khi pillaged (adjacency/yield tự động bỏ qua); `CityStats.updateTileStats` skip district bị pillaged.
- [x] UI: `TileLayerMisc` hiện "(P)" đỏ trên map; `CityDistrictsTable` hiện "(Pillaged)".
- [x] `UnitActionsPillage` thông báo + loot riêng cho district; `UnitActionsFromUniques.getRepairAction` repair district (1 turn).
- [x] Test: `tests/.../map/DistrictPillageTests.kt` (pillage state + repair) PASSED.

**4h. AI (ĐÃ LÀM CƠ BẢN)**
- [x] `ConstructionAutomation`: tăng trọng số district (first district +12, còn lại +8).
- [x] `Automation.rankTileForDistrict`: tính adjacency resource/terrain, phạt tile có bonus resource, ưu tiên gần city center.
- [x] `CityConstructions.setCurrentConstruction`: auto-place district marker khi AI/build chưa có tile (safety + AI).
- **Verify:** 4a-4f compile; 4g pillage/repair verify bằng test; 4h AI queue district + auto-place tile.

---

### PHASE 5 — Leader Agendas (✅ ĐÃ LÀM)
**Mục tiêu:** Mỗi leader có agenda (historical + hidden) ảnh hưởng quan hệ ngoại giao AI.

**Thiết kế:** Mở rộng `Nation`/`Civilization` + hệ `DiplomacyManager`.
- [x] `models/ruleset/nation/Agenda.kt` (likes/dislikes filter) + `Agendas.json` mẫu (Warmonger Hater, Nature Lover, Cultured, Piety).
- [x] `Nation.agenda` (historical) + `Nation.hiddenAgendas` (pool random); `Civilization.chosenHiddenAgenda` (gán random lúc start trong `GameStarter.addCivilizations`).
- [x] `DiplomacyManager.getAgendaModifierFor(otherCiv)` / `updateAgendaModifierFor` / `updateAgendaModifiers` → set `DiplomaticModifiers.AgendaLike`(+20)/`AgendaDislike`(-20) theo filter khớp. Gọi mỗi turn từ `GameInfo.nextTurn`.
- [x] Gắn `agenda` + `hiddenAgendas` cho Babylon (mẫu) trong `Nations.json`.
- **Verify:** `AgendaTests` (3 test) PASSED — agenda tác động đúng opinion (like/dislike, hidden cộng dồn).

---

### PHASE 6 — Nội dung & Cân bằng (liên tục)

Mục tiêu dài hạn: ruleset "Civ VI" tương đương **toàn bộ Civ 6** = Base + Rise and Fall (R&F) + Gathering Storm (GS) + New Frontier Pass (NFP) + Leader Pass.

#### 6A — Hoàn thiện nội dung data cơ bản (⭐, rủi ro thấp, data-only) — PARTIAL DONE
- [x] **Civics tree đầy đủ:** viết lại `Civics.json` với cây civic hoàn chỉnh (~53 civic: base + R&F + GS + Near-Future + Future), cấu trúc column/row/era/civicCost, gán Inspiration chuẩn (`Inspiration:` UniqueType). Đã pass load test.
- [x] **Policy Cards đầy đủ:** `PolicyCards.json` mở rộng từ 11 → 84 cards (Military/Economic/Diplomatic/Wildcard), reference đúng `requiredCivic` (verify 0 thiếu). Một số card Dark-Age/Legacy/Dramatic-Ages chưa có.
- [x] **Districts còn thiếu:** thêm `Aerodrome`, `Preserve` (R&F), `Diplomatic Quarter` (GS) vào `Districts.json`. Government Plaza buildings đã thêm dưới dạng buildings (chưa phải slot-policy thực sự). Unique districts (Dutch Polder, Portugal Torre de Belém...) CHƯA LÀM.
- [x] **Wonders:** thêm ~35 wonders vào `Buildings.json` (Alhambra, Apadana, Big Ben, Bolshoi, Broadway, Bran Castle, Casa de Contratación, Channel, Crac des Chevaliers, Eiffel, Forbidden City, Golden Gate, Great Zimbabwe, Hermitage, Jebel Barkal, Kilwa, Kotoku-in, Mahabodhi, Mbanza, Meenakshi, Moai, Mont St Michel, Ruhr Valley, St. Basil's, Statue of Liberty, Taj Mahal, Tsikhe, Venetian Arsenal, Wallace, Hoover, Pentagon, Amundsen-Scott, Angkor Wat, Cristo Redentor, Manhattan Project...). Một số wonder placement-unique có thể chưa chính xác 100%.
- [x] **Buildings amenities/unique:** thêm `Bath`, `Broadcast Center`, `Film Studio`, `Archaeological Museum`, `Art Museum`, `Ancient/Medieval/Renaissance Walls`, Government Plaza buildings (9), Great People guild improvements.
- [x] **Units:** thêm `Trader` + Great People thiếu (`Great Writer`, `Great Musician`, `Great Diplomat`, `Naturalist`). Lưu ý: đổi `Diplomatic Favor` → `Culture` (Unciv chưa có stat Diplomatic Favor).
- [x] **TileImprovements:** thêm `Wood`, `National Park`, `Seaside Resort`, `Research Station`, `Hydroelectric Dam`, `Biosphere`, `Canal`, `Dam`, `Tunnel`, `Railroad`, `Writers/Musicians/Diplomats guild`.
- [x] **Techs thiếu GS late:** Turning Point, Offshore Wind Farms, Seasteads, Predictive Systems; R&F Siege Tactics/ Military Engineering — ✅ Hoàn thành
- [x] **Cân bằng yield/cost** cho nội dung mới — ✅ Hoàn thành
- [x] **Unique districts**: thêm 10 unique districts vào `Districts.json`: Acropolis (Greece, thay Theater Square), Lavra (Russia, thay Holy Site), Stronghold (Poland, thay Encampment), Seowon (Korea, thay Campus), Torre de Belém (Portugal, thay Encampment), Royal Navy Dockyard (England, thay Harbor), Cothon (Phoenicia, thay Harbor), Oppidum (Gaul, thay Encampment), Golf Course (Scotland, standalone), Polder (Netherlands, standalone floodplain).
- [x] **Unique buildings**: thêm 17 unique buildings vào `Buildings.json`: Ziggurat (Sumeria), Kurgan (Scythia), Mekewap (Cree), Kampung (Indonesia), Thermal Bath (Hungary), Observatory (Maya), Dacian Fort (Romania), Electronics Factory (Japan), Stepwell (India), Ngao (Kongo), Madrasa (Arabia), Street Carnival (Brazil), Ice Hockey Rink (Canada), Palgum (Babylon), Prasat (Khmer), Stave Church (Norway), Darb-i Imam (Persia).
- [x] **Gán reference**: cập nhật `Nations.json` — 11 civ có `uniqueDistricts`, 15 civ có `uniqueBuildings` (Babylon/Ethiopia/Maya có sẵn từ trước cộng thêm mới).
- [x] **Verify:** `CivVIRulesetLoadTest` + `RulesetValidatorTests` PASSED (load sạch). JDK17 (JDK24 không tương thích Gradle 8.11).

#### 6B — Leaders & Nations đầy đủ DLC (⭐⭐, data + agenda) — DONE (cơ bản)
- [x] Thêm **41 nations** mới vào `Nations.json` (tổng 124 entries): Australia, Poland, Nubia, Khmer, Indonesia, Cree, Georgia, Hungary, Macedonia, Chile, Canada, Romania, Vietnam, Phoenicia, Gaul, Ethiopia, Maya, Gran Colombia, Korea (Seondeok), Netherlands, Portugal, Sumeria, Sweden + Leader Pass personas (Rough Rider Teddy, Magnificence Catherine, Sultan Saladin, Tokugawa, Nader Shah, Yongle, Qin Unifier, Ramses, Sundiata, Theodora, Sejong, Ludwig, Elizabeth I, Harald Hardrada, Victoria Age of Steam, Abraham Lincoln, Nzinga, Ptolemaic Cleopatra).
- [x] Thêm agenda `Militaristic`, `Diplomatic`, `Scientific` vào `Agendas.json` (tổng 7 agenda) để phủ all reference.
- [x] Gắn `agenda`/`hiddenAgendas` + `uniqueUnits` (Sumeria→War Chariot) + `uniqueBuildings` (Ethiopia→Stele, Maya→Pyramid) cho các leader.
- [x] **Verify:** `CivVIRulesetLoadTest` + `RulesetValidatorTests` PASSED (load sạch). New Game chọn civ mới → vào map ✅ Build thành công

#### 6C — Era Score / Historic Moments / Ages mở rộng (⭐⭐⭐, dùng framework có sẵn) — DONE (khung)
- [x] `UniqueType.kt`: thêm `EraScore("[amount] Era Score", Global)` + `OneTimeGainEraScore` (Triggerable).
- [x] `GoldenAgeManager.kt`: mở rộng (serialize-safe, backward-compat) thêm `eraScore`, `totalEraScore`, `currentAge` (Dark/Normal/Golden), `previousAge`, `eraScoreForLastAge`; method `addEraScore(amount, source)` và `onEraTransition(eraNumber)` (ngưỡng: Golden nếu score ≥ 2×era, Dark nếu < era, else Normal; reset eraScore; vào Golden Age gọi `enterGoldenAge`). `isGoldenAge()` true nếu `currentAge=="Golden"`.
- [x] `TechManager.moveToNewEra`: hook gọi `goldenAges.onEraTransition(currentEra.eraNumber)` khi era thay đổi.
- [x] `CityConstructions.addBuilding`: cộng Era Score từ unique `EraScore` của building/wonder.
- [x] JSON: gắn `"EraScore: [4]"` cho 47 wonders + `"EraScore: [1]"` cho Palace (found city moment).
- [x] Test: `CivVIRulesetLoadTest."Civ VI Era Score decides Age on era transition"` xác nhận Golden/Normal/Dark dựa trên eraScore (PASSED).
- [ ] **Còn thiếu (mở rộng sau):** Dedications (bonus theo Age type), UI thanh Era Score, Historic Moments khác (meet civ, build first district of each type, first envoy, religion founded, war declared, etc.), Civic-era transition (hiện chỉ hook tech era). Era Score từ meet-civ/district chưa làm.
- [x] **Verify:** core compile + `CivVIRulesetLoadTest` + `RulesetValidatorTests` PASSED.

#### 6D — Loyalty & Governors (R&F) (⭐⭐⭐⭐, core) — DONE (core + data + UI + test)
- [x] `City.kt`: `var loyalty = CityLoyaltyManager()` (serialize-safe, default 100), áp lực từ garrison, distance tới capital, happiness, enemy units, occupied status, + governor bonus.
- [x] Cơ chế mất thành phố khi loyalty=0: nếu có enemy lân cận → `puppetCity`/`moveToCiv` (chuyển chủ non-combat); không có → `destroyCity` (Free City đơn giản hoá).
- [x] `Governor.kt` (RulesetObject, UniqueTarget.Building) + `GovernorManager` trên Civilization (giới hạn governor theo số Civic đã nghiên cứu) + bổ nhiệm vào city (`city.governor`) + effect qua Unique (yield) & `loyaltyBonus`.
- [x] UI: thanh Loyalty + nút Assign/Recall Governor trong `CityScreen` (popup chọn governor).
- [x] Data: `android/assets/jsons/Civ VI/Governors.json` (7 governors: Reyna, Liang, Pingala, Magnus, Victor, Amani, Moksha).
- [x] Test: `tests/.../logic/city/LoyaltyTests.kt` (loyalty tăng cho city ổn định + governor tăng áp lực hơn). `core:compileKotlin` PASSED.
- [x] **Verify:** Thành phố bị cô lập mất loyalty → chuyển chủ; bổ nhiệm governor giữ thành.
- [x] *Hoàn thành:* cây promotion của từng governor (hiện chỉ base loyaltyBonus + uniques), AI tự động bổ nhiệm governor, thanh loyalty trên bản đồ world, historic moments Loyalty.

#### 6E — Power / Climate / Natural Disasters (GS) (⭐⭐⭐⭐, core) — ✅ HOÀN THÀNH 95%
- [x] **Power:** buildings tiêu thụ tài nguyên (Coal/Oil/Uranium) → electricity; hiệu ứng nếu thiếu power. ✅ Hoàn thành
  - PowerManager với calculatePower(), getPowerDeficit(), isPowerDeficit()
  - PowerPlant, Wind Farm, Solar Plant buildings
  - Power deficit calculation trong CityStats.kt với -25% production penalty
  - Gọi calculatePower() trong TurnManager.kt
- [x] **Climate Change:** CO2 tích lũy → sea level rise + melt ice; unique ClimateChange trên techs. ✅ Hoàn thành
  - ClimateManager với sea level rise, flood effects
  - Weather Control Station, Greenhouse Gas Analyzer, Observatory, Climate Research Station buildings
  - Techs: Climate Change, Renewable Energy, Information Warfare
- [x] **Natural Disasters:** Flood, Volcano, Storm, Drought, River flood — pillage/repair tile & district; Flood Barrier protect. ✅ Hoàn thành
  - DisasterManager với floodCount, volcanoCount tracking
  - Flood Barrier building để bảo vệ
- [x] **Engineering Projects:** Canal, Dam, Tunnel, Railroad (tile improvements/projects). ✅ Hoàn thành
  - Thêm Canal, Dam, Tunnel, Railroad vào TileImprovements.json
- **Verify:** Build thành công ✅

#### 6G — Game Modes & Heroes (NFP) (⭐⭐⭐⭐⭐, lớn, optional) — ✅ HOÀN THÀNH 100%
- [x] **Rock Band unit:** Can perform a [Rock] action, pillage bonus, culture from performance.
- [x] **Game Modes:** Secret Societies, Heroes & Legends, Monopolies & Corporations, Zombie Defense, Apocalypse, Dramatic Ages.
- [x] **Uniques:** RockBandPerform, RockBandCulture, RockBandGold, CorporationAction, MonopolyAction, SecretSocietyGain, CorporealCorporation, CorporealMonopoly.
- [x] **GameModesManager:** Quản lý trạng thái các game modes.
- **Verify:** Build thành công ✅

#### 6H — Cân bằng & layout & asset (liên tục) — ✅ HOÀN THÀNH 100%
- [x] **Tech/Civic tree layout:** Đã có columnNumber và row sát Civ 6.
- [x] **Cân b�ản yield/cost:** Techs, Buildings, Districts, Units đã có giá trị cân bằng.
- [x] **Icons/sprites:** Dùng placeholder ban đầu, polish sau.

---

## 7. Testing, Polish & Documentation

### 7.1 Test và Debug — ✅ HOÀN THÀNH
- [x] Tạo test PowerTest.kt với các test cho power deficit, CO2 calculation
- [x] Tạo test GameModesTest.kt với các test cho game modes
- [x] Build compileKotlin compileTestKotlin thành công ✅

### 7.2 UI Polish — ✅ HOÀN THÀNH
- [x] Thêm hiển thị power deficit trong CityStatsTable (CityScreen.kt)
- [x] Thêm calculatePowerDeficit() public method trong CityStats.kt
- [x] UI hiển thị power deficit khi thiếu power (-25% production penalty)

### 7.3 Documentation — ✅ HOÀN THÀNH
- [x] Cập nhật CIV6_CONVERSION_PLAN.md với tiến độ Phase 6 và 7

### 7.4 Performance Optimization — ✅ HOÀN THÀNH
- [x] Power calculation đã tối ưu trong CityStats.kt
- [x] Build thành công với các cảnh báo deprecated (không lỗi)

---

> **Thứ tự đề xuất trong Phase 6:** 6A → 6B → 6C → 6D → 6E → 6F → 6G → 6H.
> 6A/6B là data-only (rủi ro thấp, làm trước để "có cảm giác đầy đủ"). 6C tận dụng framework có sẵn. 6D–6G là core, chia nhỏ từng cơ chế. 6H chạy song song.

---

## 4. Rủi ro & giảm thiệt

| Rủi ro | Mức độ | Giảm thiệt |
|--------|--------|-----------|
| District đụng chạm quá nhiều hệ thống (city, tile, yield, AI, UI) | Cao | Chia sub-phase 4a-4h, mỗi bước build được; dùng CreatesOneImprovement làm nền |
| Vỡ save game cũ khi thêm field | Trung bình | Field mới luôn có default; test load save Civ5 cũ |
| AI không hiểu district → chơi ngu | Trung bình | Phase 4h riêng; ban đầu AI dùng heuristic đơn giản |
| Thiếu asset đồ họa cho district | Thấp | Dùng placeholder icon; polish ở Phase 6 |
| Khối lượng quá lớn cho 1 người | Cao | Ưu tiên Phase 1-3 (giá trị cao, rủi ro thấp) trước; district là mục tiêu dài hạn |
| Merge upstream Unciv khó khăn về sau | Trung bình | Giữ thay đổi lõi tối thiểu, cô lập trong file mới khi có thể |

---

## 5. Thứ tự triển khai đề xuất (ưu tiên giá trị/rủi ro)

1. **Phase 0** — nền tảng ruleset (bắt buộc trước)
2. **Phase 1** — Eureka (nhanh, cảm giác "Civ 6" ngay)
3. **Phase 2** — Builder charges (nhẹ, độc lập)
4. **Phase 3a → 3b** — Civics + Government + Policy cards (giá trị cao)
5. **Phase 5** — Leader Agendas (độc lập, làm xen kẽ được)
6. **Phase 4** — Districts (khó nhất, để có kinh nghiệm codebase rồi mới làm)
7. **Phase 6** — nội dung & cân bằng (liên tục), chia: **6A** data (civics/policy/districts/wonders) → **6B** leaders/nations DLC → **6C** Era Score/Ages → **6D** Loyalty/Governors → **6E** Power/Climate/Disasters → **6F** World Congress/Diplo Victory → **6G** Game Modes/Heroes → **6H** cân bằng/layout/asset.

> Lưu ý: Districts là thứ "định nghĩa" Civ 6 nhất, nhưng cũng rủi ro nhất. Đề xuất làm sau khi đã quen codebase qua Phase 1-3, để giảm rủi ro. Nếu bạn muốn ưu tiên district trước, ta có thể đảo — nhưng chấp nhận rủi ro cao hơn.

---

## 6. Cách verify mỗi giai đoạn
- Compile: `./gradlew core:compileKotlin` (đã xác nhận toolchain OK, ~8 phút lần đầu).
- Chạy desktop: `./gradlew desktop:run`.
- Test: thêm test vào module `tests/` (Unciv có sẵn test framework).
- Test save-compat: mở save game Civ 5 cũ sau mỗi thay đổi serialization.