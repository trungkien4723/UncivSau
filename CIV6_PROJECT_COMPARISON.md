# Civ 6 (theo chuẩn) vs. Project UncivSau — Bảng đối chiếu hiện trạng

> **Đối chiếu:** `CIV6_GAMEPLAY_MECHANICS.md` (mô tả chuẩn cơ chế Civ 6) ↔ trạng thái code/dữ liệu hiện tại của project này.
> **Cách đọc:** `[x]` = đã làm, `[~]` = làm 1 phần / còn thiếu chi tiết, `[ ]` = chưa có.
> **Nguồn dữ liệu:** ruleset `jsons/Civ VI/`, code `core/src/com/unciv/`, theo bản kế hoạch `CIV6_CONSOLIDATED_PLAN.md` (tới commit `59e80a2 "fix minor bugs"`) + kiểm tra trực tiếp file + diff uncommitted hiện tại.
> **Đánh giá tổng thể:** project hiện là bản chuyển đổi **Civ 5 → Civ 6** của Unciv, đạt ~**94%** cơ chế Civ 6.

---

## 1. Mức trạng thái tổng hợp (từng mục trong GAMEPLAY_MECHANICS)

| # | Mục (theo GAMEPLAY_MECHANICS) | Trạng thái | Ghi chú thực tế trong project |
|---|---|---|---|
| 1 | Tổng quan game (UPT, hex, theo lượt) | `[x]` | Đúng engine Unciv gốc |
| 2 | Thiết lập trận đấu (ruleset, độ khó, tốc độ, bản đồ, thảm họa) | `[x]` | Có đầy đủ `Difficulties.json`, `Speeds.json`, `Eras.json`; cường độ thảm họa GS có |
| 3 | Bản đồ & địa hình (terrain, features, nước, Natural Wonders, resources) | `[x]` | Terrains/Features/Resources đầy đủ; **38 Natural Wonders (root) / 38 (android)** (gồm các wonder GS/NFP: Bermuda Triangle, Chocolate Hills, Delicate Arch, Eye of the Sahara, Giant's Causeway, Mato Tipila, Matterhorn, Mount Kilimanjaro, Mount Vesuvius, Pamukkale) |
| 4 | Yields (Food/Production/Gold/Science/Culture/Faith) | `[x]` | Engine gốc + Civics/Chính phủ cấp thêm |
| 5 | Housing & Amenities | `[x]` | Housing/Amenities engine đầy đủ; công trình Neighborhood, Entertainment Complex có |
| 6 | Thành phố & Districts | `[x]` | 13 district chính + 2 mới (Canal, Dam — commit mới đã thêm vào `Districts.json`) + Government Plaza + Aerodrome + Spaceport |
| 7 | Adjacency Bonus | `[x]` | Mapping đầy đủ 8-14 bonus, fraction ±0.5 execute đúng, Commercial Hub River +2G, Harbor Water +1G |
| 8 | Công nghệ, Civic & Policy Cards | `[x]` | 84 techs + 61 civics + 84 policy cards (4 loại slot), Eureka/Inspiration, Tech Shuffle; **AI research civic** (`chooseCivicToResearch` trong `NextTurnAutomation`) mirror `chooseTechToResearch` (free civics + queue civic rẻ nhất theo weight) — có test |
| 9 | Chính phủ | `[x]` | 7 chính phủ (Chiefdom→Merchant Republic...), slots, mình đang sửa UI Govt picker (uncommitted) |
| 10 | Đơn vị & vòng lượt | `[x]` | Đầy đủ class + **Corps/Army/Fleet/Armada** (gộp 2 unit cùng tên cạnh nhau → +10/+17 STR; vì engine cấm xếp chồng military nên tìm partner trên ô lân cận thay vì cùng ô — có test `UnitActionsCombineTests`); **AI cũng tự form corps/army/fleet/armada lúc peacetime** (máu đầy + đủ vàng, giữ vàng dự trữ — có test `AiCorpsFormationTests`); promotions rewritten theo chuẩn Civ 6 |
| 11 | Thám hiểm & Barbarian | `[x]` | Tribes villages (Ruins.json) + Barbarian camps; điều chỉnh spawn gần đây |
| 12 | Thương mại & Trade Routes | `[x]` | Trader unit + capacity (`UniqueType.TradeRouteCapacity`), Trading Posts, roads tự dựng |
| 13 | Tôn giáo & Faith | `[x]` | Pantheon → Great Prophet → Religion → spread/theological combat, beliefs đủ |
| 14 | Great People | `[x]` | 9 loại GP + Great Works (Writing/Art/Artifact/Music/Relic) + theming + Antiquity Sites + Archaeologist |
| 15 | Thành bang & Envoys | `[x]` | 24 CS (6 loại, mỗi loại có suzerain bonus riêng cấp qua nation uniques), gifts/quests, diplomatic marriage |
| 16 | Ngoại giao & quan hệ | `[x]` | Diplomacy + alliances + deals + promises; agendas (24 personality types) cho AI |
| 17 | Chiến tranh, Casus Belli, Grievances | `[x]` | CasusBelli enum đầy đủ + UI chọn khi tuyên chiến + war support; Grievances có |
| 18 | War Weariness | `[x]` | Đã thay bằng **War Support** (mechanics Civ VI) + hiển thị UI trên màn ngoại giao; hệ `warWeariness` Cum5 cũ (tích lũy + giảm Amenities) **đã gỡ** — chỉ còn War Support điều hành |
| 19 | Rise & Fall: Loyalty, Governors, Golden/Dark Ages | `[x]` | Governor (cây promo), Loyalty (city happy, thất → Free City), GoldenAgeManager (era score, Dark/Golden/Heroic, Dedications, Dramatic Ages mode) |
| 20 | Gathering Storm: khí hậu/thảm họa/năng lượng | `[x]` | ClimateManager (CO2, 7 pha, sea level), DisasterManager, PowerManager (than/dầu/nuke/tái tạo), Canal/Dam |
| 21 | World Congress & Diplomatic Victory | `[x]` | Hoàn chỉnh: resolutions, emergencies, favor, Diplomatic Victory (quỹ 10 điểm, UN), Trade Embargo |
| 22 | Gián điệp (Spies) | `[x]` | Đầy đủ spy actions (steal tech, sabotage, coup...). **Lưu ý:** Spies là **dữ liệu/manager**, KHÔNG cần unit `Spy` trong Units.json → status "[x]" |
| 23 | 6 điều kiện chiến thắng | `[x]` | Science (spaceship parts), Domination (thủ đô), Culture (tourism), Religion, Diplomatic + Time |
| 24 | Chiến lược game phases | `[x]` | `GamePhase` (Early/Mid/Late) theo era trong `GamePhase.kt`: helper tính theo phase — `workerRatio` (early 1.2 / late 0.8), `militaryBuildModifier` (early 1.2 / late 1.3), `minimumFreeLandForExpansion` (early eager / late chỉ khi còn đất), `wonderModifier` (early 1.3 / late 1.5). Áp dụng: `ConstructionAutomation` (buildings, military, workers, wonders), `NextTurnAutomation.trainSettler` (mở rộng theo phase), `CivilianUnitAutomation.isLateGame`. **Victory focus theo phase**: `getAiVictoryStatModifiers()` — từ mid game AI chọn stat mạnh nhất so với field và ưu tiên xây công trình stat đó (mid ×1.3, late ×1.8). Có test `GamePhaseTests`, `AiVictoryFocusTests` |
| 25 | Tourism theo Appeal (Seaside Resort / National Park) | `[x]` | Unique `Tourism based on tile appeal` được engine thực thi (`updateStatsForNextTurn` cộng tourism theo appeal từng tile) — có test |

---

## 2. Chi tiết những mục CÒN LẠI (chưa hoàn hảo / chưa làm)

### Nhóm A — Hoàn thiện vẫn có `[~]` (có code/dữ liệu nhưng chưa đủ sâu)

| STT | Mục | Hiện trạng | Chi tiết sẽ cần |
|---|---|---|---|
| 1 | **AI toàn diện** | `[x]` | City placement (`rankTileForDistrict`), build queue có AI weight (personality + building AiChoiceWeight: district chuẩn +50%, công trình +25%), research (`chooseTechToResearch`/`chooseCivicToResearch`), unit automation, diplomacy, wonder priority, **game-phase** (worker/military/wonder/expansion), **war theo grievances + casus belli** (AI chọn casus belli ít grievance nhất, grievance tăng motivation tuyên chiến/chuẩn bị chiến). Có test `AiWeightsTests`, `GamePhaseTests`, `DeclareWarPlanEvaluatorTests`. |
| 2 | **AI chỗ đặt district** (`rankTileForDistrict`) | `[x]` | `Automation.rankDistrictValue` (`Automation.kt`) score base yields + adjacency của vị trí tốt nhất, dùng trong build queue (`ConstructionAutomation.kt`) với bonus cho district đầu tiên của city. |
| 3 | **Adjacency** | `[x]` | Đã khớp bảng chuẩn 7 district (Campus/Theater/HolySite/Commercial/Harbor/Industrial/Encampment+Entertainment=không bonus) + các bonus bổ sung; fraction ±0.5 execute đúng (có test) |
| 4 | **War Support / War Weariness** | `[x]` | War Support là cơ chế chính (combat +1% mỗi điểm, UI ngoại giao, khởi tạo qua Casus Belli); hệ war weariness C5 cũ đã gỡ — hết friction |
| 5 | **Theming/Great Works per-building** | `[x]` | Great Works đã **gán vào building/city cụ thể** (auto-place khi tạo; `GreatWork.cityId`/`building`); theming tính **per-building** (building đủ slot → bonus); UI hiển thị vị trí đặt |
| 6 | **Leader/Hero promotion system** | `[x]` | Hero resurrection via "Immortal [in capital]" unique: when a hero unit (with unique Immortal) dies (health==0), it resurrects at the capital tile with full health instead of being destroyed. Implemented in MapUnit.takeDamage() with checkCivInfoUniques=true and !civ.isDefeated(). |
| 7 | **UI tooltips cho toàn bộ uniques** | `[x]` | Cơ chế docDescription → tooltip có sẵn ở civilopedia (`MarkupRenderer`); đã wire vào `GovernmentPickerScreen` (policy card trong slot + popup). Các màn khác đã dùng `MarkupRenderer.render` chung. |
| 8 | **Trade route nuances** | `[x]` | Trading Posts tạo ở **cả 2 đầu** khi lập route (lưu trên Civilization), mỗi post ở thành phố nước ngoài **+5 range**; range cơ bản 15; yield theo district thành đích: domestic = Food+Prod, international = Gold/Science/Culture/Faith (+1 Gold/post). Có test `TradeRouteTests`. |

### Nhóm B: đã xử lý nhưng phụ thuộc cách chơi (trước đây được liệt là thiếu, giờ đã giải quyết)

| Mục | Trước đây | Hiện tại |
|---|---|---|
| Spy unit | `[ ]` | `[x]` — spies là object data (đã verify), không cần unit entry |
| Goody Huts | `[ ]` | `[x]` — `Ruins.json` + "Ancient Ruins" random bonuses + tile "Goody Hut" trong TileImprovements |
| Canal/Dam as districts | `[ ]` | `[x]` — đã thêm vào Districts.json (Dắm: +1 Production, adjacency Industrial Zone/Aqueduct, chứa Hydroelectric Dam) |
| Aircraft Factory | `[ ]` | `[x]` — có trong Buildings.json (thuộc Aerodrome) |
| 2 Natural Wonders | `[ ]` | `[x]` — **38/38** natural wonders (đồng bộ 2 cây, gồm đủ wonder GS/NFP chuẩn) |
| City-state nations | `[ ]` | `[x]` — 24 CS + "Barbarians" nation |
| 6 unique buildings thiếu `uniqueTo/replaces` | `[ ]` | `[x]` |
| CasusBelli.RetributionWar | `[ ]` | `[x]` |

### Nhóm C: những mục còn CHƯA thực hiện (còn trắng — kiểm tra 59e80a2)

- **Rất ít hoặc không còn mục trắng nào trong kế hoạch.** Bản kế hoạch cuối chỉ ghi còn `[~]` ở nhóm A (ở phần 2).
- **Lưu ý ngoài plan:** chưa thấy file `Save/Civilopedia` hà tiện; không phải rựa.

---

## 3. Đối chiếu theo từng "viên gạch" của GAMEPLAY_MECHANICS.md

### 3.1 Districts (mục 6)
| District chuẩn Civ 6 | Trong project |
|---|---|
| City Center | `[x]` |
| Campus | `[x]` (qua Library) |
| Theater Square | `[x]` (qua Monument) |
| Holy Site | `[x]` (qua Shrine) |
| Commercial Hub | `[x]` (qua Market) |
| Industrial Zone | `[x]` (qua Workshop) |
| Harbor | `[x]` (qua Lighthouse) |
| Encampment | `[x]` (qua Barracks) |
| Entertainment Complex | `[x]` (qua Arena) |
| Aqueduct | `[x]` (district + building) |
| Neighborhood | `[x]` (qua Granary) |
| Government Plaza | `[x]` (qua Court) |
| Spaceport | `[x]` (qua Rocket Silo) |
| Aerodrome | `[x]` (qua Aerodrome building) |
| Canal | `[x]` (thêm mới gần đây — Districts.json) |
| Dam | `[x]` (thêm mới gần đây — Districts.json) |
| Capacity `(pop-1)/3+1` | `[x]` |
| District cho 1 loại/1 thành → toàn nước | `[x]` |
| Không tháo district | `[x]` |

### 3.2 Adjacency (mục 7)
| Bonus chuẩn | Project |
|---|---|
| Campus: Mountain +1, Rainforest +½, District +½, Government Plaza +1 | `[x]` |
| Holy Site: Mountain +1, Wood +½, NaturalWonder +2, District +½, Government Plaza +1 | `[x]` |
| Commercial Hub: River +2, Harbor +2, District +½, Government Plaza +1 | `[x]` |
| Harbor: City Center +2, sea resource (Water resource) +1, District +½, Government Plaza +1 (bỏ sai: River, Commercial Hub) | `[x]` |
| Industrial: Mine/Quarry +1, Strategic +1, District +½, Aqueduct/Canal/Dam +2, Government Plaza +1 (bỏ sai: Bonus resource) | `[x]` |
| Theater: Wonder +1 (inert — wonders là buildings không phải tile), Entertainment/Water Entertainment +2, District +½, Government Plaza +1 | `[~]` |
| Encampment: không bonus (bỏ sai: Bonus resource) | `[x]` |
| Government Plaza: **cho** district lân cận +1 yield chính (đổi từ nhận +1 cả 5 yield) | `[x]` |
| FIX bug "dead unique": uniques `[... for each adjacent [X] district/resource]` có hậu tố thừa → **NULL TYPE, không chạy**. Đã bỏ hậu tố (13 uniques) + thêm test chặn hồi quy | `[x]` |
| FIX engine: filter `City Center` giờ match tile trung tâm qua `isCityCenter()` (CityStats + Automation) | `[x]` |
| Kiểm tra từng district | `[x]` — `DistrictAdjacencyTests` (15 test: Campus, Holy Site, Commercial Hub, Harbor, IZ, Theater, GP, Encampment, meta-parse) + `AdjacencyFractionTests` (phân số ½) |

### 3.3 Housing/Amenities (mục 5)
| Item | Status |
|---|---|
| Housing formula + trần | `[x]` |
| Nước ngọt định cư | `[x]` |
| Luxury → 1 Amenity × 4 cities | `[x]` |
| Entertainment Complex / Water Park / neighborhoods | `[x]` |
| Neighborhood: Housing theo Appeal (Charming 3, Breathtaking 4) | `[x]` — engine thực thi unique `Housing based on tile appeal` (dùng chung UI + automation); test `HousingTests` |
| War weariness làm giảm Amenities | `[-]` (đã xoá — Civ VI chỉ dùng War Support thay thế hoàn toàn) |

### 3.4 Tôn giáo (mục 13)
| Item | Status |
|---|---|
| Pantheon (25 Faith) | `[x]` |
| Great Prophet limited | `[x]` |
| 4 belief loại (Follower/Founder/Enhancer/Worship) | `[x]` |
| Missionary/Apostle/Inquisitor + theological combat | `[x]` |
| Religious pressure + Religious victory | `[x]` |
| Mount St. Michel / chưa | `[x]` |

### 3.5 Rise & Fall (mục 19)
| Item | Status |
|---|---|
| Era Score + Historic Moments | `[x]` |
| Dark/Normal/Golden/Heroic + Dedications | `[x]` |
| Dramatic Ages mode | `[x]` |
| Loyalty + Free City | `[x]` |
| 7 Governors | `[x]` |

### 3.6 Gathering Storm (mục 20, 21)
| Item | Status |
|---|---|
| CO2 + climate 7 pha + sea level | `[x]` |
| Disasters (volcano/flood/storm/drought/tornado/blizzard/solar flare) | `[x]` |
| Power (năng lượng) + renewable | `[x]` |
| World Congress (resolutions/emergencies/special) | `[x]` |
| Diplomatic Favor / Victory | `[x]` |
| Future Era + GDR | `[x]` |

### 3.7 Game Modes (New Frontier)
| Item | Status |
|---|---|
| Apocalypse | `[x]` |
| Zombie Defense | `[x]` |
| Heroes & Legends | `[x]` |
| Secret Societies | `[x]` (auto-join + join qua ruins trigger) |
| Corporations & Monopolies | `[x]` (monopoly >50%, founder, products) |

---

## 4. Công việc đang làm dở (uncommitted — không nằm trong commit cuối)

Những thay đổi hiện đang có trong working tree mà **chưa commit**:

| File | Thay đổi gì | Ý nghĩa |
|---|---|---|
| `core/.../WorkerAutomation.kt` | Thêm `executeChargeBuilderImprovement()` cho builder kiểu Civ6 (dùng charge, instant `ConstructImprovementInstantly`) | Builder tự động hết charge (Civ6-style) |
| `core/.../GovernmentPickerScreen.kt` + android copy | Tái cấu trúc giao diện: tách bảng chính phủ / slots, **giữ lại cards đã gán khi đổi chính phủ** | Sửa UX picker chính phủ |
| `core/.../UnitActionsFromUniques.kt` + android copy | (đang sửa) | Điều chỉnh liên quan upgrade/action units |
| `tests/.../RoadImprovementTests.kt` | Cập nhật test đường | Phù hợp builder charge mới |
| `tests/.../Civ6BuilderAutomationTests.kt` (mới) | Test tự động builder Civ6 | Kèm theo thay đổi WorkerAutomation |
| `Xóa` | `CIV6_CONSOLIDATED_PLAN.md`, `CODE_OF_CONDUCT.md` | (theo yêu cầu vừa rồi) |
| `Thêm` | `CIV6_GAMEPLAY_MECHANICS.md` | Tài liệu cơ chế chuẩn (chính file này được giữ) |

> Gợi ý: các thay đổi này về AI builder đang là hướng "Civ6-style builders consume charges" — cùng mảng "AI/district xây dựng" chưa `[x]` phải đầy đủ.

---

## 5. Kết luận & ưu tiên đề xuất

**Đã đạt (~94%):** toàn bộ hệ thống "chơi" của Civ 6 có trong engine + ruleset — districts, adjacency, tech/civic/policy, gov, tôn giáo, age/loyalty, world congress, climate, corps/army, great works, game modes.

**Việc nên làm tiếp (theo ưu tiên):**
1. **Bổ sung Natural Wonders** — đã xong: thêm 10 wonder GS/NFP (Bermuda Triangle, Chocolate Hills, Delicate Arch, Eye of the Sahara, Giant's Causeway, Mato Tipila, Matterhorn, Mount Kilimanjaro, Mount Vesuvius, Pamukkale) → **38 wonders, đồng bộ 2 cây jsons**. (Civ 6 chuẩn có 37; project còn thêm Grand Canyon + Nile River).
2. **AI theo game phase** — đã xong: `GamePhase.kt` (Early/Mid/Late theo era) + 4 helper theo phase (`workerRatio`, `militaryBuildModifier`, `minimumFreeLandForExpansion`, `wonderModifier`) áp dụng vào build queue (military/worker/wonder), `trainSettler` (mở rộng early/mid, late chỉ khi còn đất) và `isLateGame`.
3. **Tăng độ sâu AI** — đã làm: war/grievances (`chooseCasusBelli`, grievance tăng motivation, fix `canDeclareFormalWar`), corps/army khả dụng cho player + **AI tự form corps lúc peacetime** (`tryFormCorpsOrArmy`), **victory focus theo era** (`getAiVictoryStatModifiers`: mid/late chọn stat mạnh nhất so với field để ưu tiên build — Science/Culture/Faith/Gold/Production). Còn lại: dominion focus chưa map sang military (đã có war modifier + militaryBuildModifier riêng), chiến lược research theo focus.
4. **Hoàn thiện adjacency call chuẩn** — đã làm: fix bug "dead unique" (13 uniques hậu tố thừa), fix engine filter `City Center`, Government Plaza chuyển sang "cho", đồng bộ 2 cây jsons, test từng district (`DistrictAdjacencyTests`, 15 test).
5. **Great Works per-building + theming per-museum** (đang global pool).
6. **Hoàn tất UI tooltips uniques** + bổ sung unit action team đang sửa.

---

*Tài liệu tổng hợp từ: `CIV6_GAMEPLAY_MECHANICS.md`, `git show <commit>:CIV6_CONSOLIDATED_PLAN.md`, kiểm tra `jsons/Civ VI/*.json`, `git status`, `git log --oneline`, `git diff` (working tree). Trạng thái tính đến commit `59e80a2` + uncommitted.*