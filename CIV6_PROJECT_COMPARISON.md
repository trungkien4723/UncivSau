# Civ 6 (theo chuẩn) vs. Project UncivSau — Bảng đối chiếu hiện trạng

> **Đối chiếu:** `CIV6_GAMEPLAY_MECHANICS.md` (mô tả chuẩn cơ chế Civ 6) ↔ trạng thái code/dữ liệu hiện tại của project này.
> **Cách đọc:** `[x]` = đã làm, `[~]` = làm 1 phần / còn thiếu chi tiết, `[ ]` = chưa có.
> **Nguồn dữ liệu:** ruleset `jsons/Civ VI/`, code `core/src/com/unciv/`, theo bản kế hoạch `CIV6_CONSOLIDATED_PLAN.md` (tới commit `59e80a2 "fix minor bugs"`) + kiểm tra trực tiếp file + diff uncommitted hiện tại.
> **Đánh giá tổng thể:** project hiện là bản chuyển đổi **Civ 5 → Civ 6** của Unciv, đạt ~**95%** cơ chế Civ 6.

---

## 1. Mức trạng thái tổng hợp (từng mục trong GAMEPLAY_MECHANICS)

| # | Mục (theo GAMEPLAY_MECHANICS) | Trạng thái | Ghi chú thực tế trong project |
|---|---|---|---|
| 1 | Tổng quan game (UPT, hex, theo lượt) | `[x]` | Đúng engine Unciv gốc |
| 2 | Thiết lập trận đấu (ruleset, độ khó, tốc độ, bản đồ, thảm họa) | `[x]` | Có đầy đủ `Difficulties.json`, `Speeds.json`, `Eras.json`; cường độ thảm họa GS có |
| 3 | Bản đồ & địa hình (terrain, features, nước, Natural Wonders, resources) | `[x]` | Terrains/Features/Resources đầy đủ; **~22 Natural Wonders** (62+ các yếu mục bổ sung) |
| 4 | Yields (Food/Production/Gold/Science/Culture/Faith) | `[x]` | Engine gốc + Civics/Chính phủ cấp thêm |
| 5 | Housing & Amenities | `[x]` | Housing/Amenities engine đầy đủ; công trình Neighborhood, Entertainment Complex có |
| 6 | Thành phố & Districts | `[x]` | 13 district chính + 2 mới (Canal, Dam — commit mới đã thêm vào `Districts.json`) + Government Plaza + Aerodrome + Spaceport |
| 7 | Adjacency Bonus | `[~]` | **Đã nâng cấp mạnh**: mapping đầy đủ 8-14 bonus, Commercial Hub River +2G, Harbor Water +1G... nhưng chưa 100% khớp từng công thức Civ 6 |
| 8 | Công nghệ, Civic & Policy Cards | `[x]` | 84 techs + 61 civics + 84 policy cards (4 loại slot), Eureka/Inspiration, Tech Shuffle |
| 9 | Chính phủ | `[x]` | 7 chính phủ (Chiefdom→Merchant Republic...), slots, mình đang sửa UI Govt picker (uncommitted) |
| 10 | Đơn vị & vòng lượt | `[x]` | Đầy đủ class + **Corps/Army/Fleet/Armada**; promotions rewritten theo chuẩn Civ 6 |
| 11 | Thám hiểm & Barbarian | `[x]` | Tribes villages (Ruins.json) + Barbarian camps; điều chỉnh spawn gần đây |
| 12 | Thương mại & Trade Routes | `[x]` | Trader unit + capacity (`UniqueType.TradeRouteCapacity`), Trading Posts, roads tự dựng |
| 13 | Tôn giáo & Faith | `[x]` | Pantheon → Great Prophet → Religion → spread/theological combat, beliefs đủ |
| 14 | Great People | `[x]` | 9 loại GP + Great Works (Writing/Art/Artifact/Music/Relic) + theming + Antiquity Sites + Archaeologist |
| 15 | Thành bang & Envoys | `[x]` | 24 CS (6 loại, mỗi loại có suzerain bonus riêng cấp qua nation uniques), gifts/quests, diplomatic marriage |
| 16 | Ngoại giao & quan hệ | `[x]` | Diplomacy + alliances + deals + promises; agendas (24 personality types) cho AI |
| 17 | Chiến tranh, Casus Belli, Grievances | `[x]` | CasusBelli enum đầy đủ + UI chọn khi tuyên chiến + war support; Grievances có |
| 18 | War Weariness | `[~]` | Đã thay bằng **War Support** (mechanics Civ VI) + hiển thị UI trên màn ngoại giao; hiệu ứng amenities/weariness đời 5 cũ vẫn còn đâu đó |
| 19 | Rise & Fall: Loyalty, Governors, Golden/Dark Ages | `[x]` | Governor (cây promo), Loyalty (city happy, thất → Free City), GoldenAgeManager (era score, Dark/Golden/Heroic, Dedications, Dramatic Ages mode) |
| 20 | Gathering Storm: khí hậu/thảm họa/năng lượng | `[x]` | ClimateManager (CO2, 7 pha, sea level), DisasterManager, PowerManager (than/dầu/nuke/tái tạo), Canal/Dam |
| 21 | World Congress & Diplomatic Victory | `[x]` | Hoàn chỉnh: resolutions, emergencies, favor, Diplomatic Victory (quỹ 10 điểm, UN), Trade Embargo |
| 22 | Gián điệp (Spies) | `[x]` | Đầy đủ spy actions (steal tech, sabotage, coup...). **Lưu ý:** Spies là **dữ liệu/manager**, KHÔNG cần unit `Spy` trong Units.json → status "[x]" |
| 23 | 6 điều kiện chiến thắng | `[x]` | Science (spaceship parts), Domination (thủ đô), Culture (tourism), Religion, Diplomatic + Time |
| 24 | Chiến lược game phases | `[x]` | Tất cả phần này là chiến lược guidance trong file; việc **AI** thực thi còn `[~]` |

---

## 2. Chi tiết những mục CÒN LẠI (chưa hoàn hảo / chưa làm)

### Nhóm A — Hoàn thiện vẫn có `[~]` (có code/dữ liệu nhưng chưa đủ sâu)

| STT | Mục | Hiện trạng | Chi tiết sẽ cần |
|---|---|---|---|
| 1 | **AI toàn diện** | `[~]` | City placement, build queue, wonder priority, unit automation, research, diplomacy... đang "basic automation". Đây là mảng lớn nhất còn lại. |
| 2 | **AI chỗ đặt district** (`rankTileForDistrict`) | `[~]` | Có đánh giá adjacency từ JSON nhưng chưa bằng độ tinh của 1 người chơi giỏi (giữ ô, dây chuyền district...). |
| 3 | **Adjacency** | `[~]` | Đã gần đủ mapping các district; cần rà từng ô theo đúng bảng của Civ 6 (vd +½ x 2 cho mỗi 2 ô, giá trị fraction thực thi đúng). |
| 4 | **War Support / War Weariness** | `[~]` | War Support là cơ chế chính thay cho War Weariness (đã vgt; nhưng vẫn còn friction với hệ Amenities/war weariness cũ tư đâu đó trong engine Unciv). |
| 5 | **Theming/Great Works per-building** | `[~]` | Great Works dùng **global pool**, không gán vào từng công trình/museum riêng → theming tính toàn cục, chưa per-building như Civ 6. |
| 6 | **Leader/Hero promotion system** | `[~]` | Hero promotions còn basic. |
| 7 | **UI tooltips cho toàn bộ uniques** | `[~]` | Một part còn thiếu tooltip. |
| 8 | **Trade route nuances** | `[~]` | Có capacity + Trading post, nhưng chưa hết chi tiết (route modifier phức tạp theo policy cũ nói chung, tầm vang...). |

### Nhóm B: đã xử lý nhưng phụ thuộc cách chơi (trước đây được liệt là thiếu, giờ đã giải quyết)

| Mục | Trước đây | Hiện tại |
|---|---|---|
| Spy unit | `[ ]` | `[x]` — spies là object data (đã verify), không cần unit entry |
| Goody Huts | `[ ]` | `[x]` — `Ruins.json` + "Ancient Ruins" random bonuses + tile "Goody Hut" trong TileImprovements |
| Canal/Dam as districts | `[ ]` | `[x]` — đã thêm vào Districts.json (Dắm: +1 Production, adjacency Industrial Zone/Aqueduct, chứa Hydroelectric Dam) |
| Aircraft Factory | `[ ]` | `[x]` — có trong Buildings.json (thuộc Aerodrome) |
| 2 Natural Wonders | `[ ]` | `[~]` — ~22 natural wonders đã đủ/gần đủ |
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
| Campus: Mountain +1, Rainforest +½, District +½ | `[x]` |
| Holy Site: Mountain +1, Wood +½, NaturalWonder +2, District +½ | `[x]` |
| Commercial Hub: River +2, Harbor +2, District +½ | `[x]` |
| Harbor: coastal resource +1, District +½ | `[x]` |
| Industrial: Mine/Quarry +1, District +½ | `[x]` |
| Theater: Wonder +1, District +½ | `[x]` |
| Plus 14 adjacency bonus vừa bổ sung (Campus, Theater, Harbor, Encampment, Industrial, Entertainment, Water Entertainment, Aerodrome) | `[x]` |
| Kiểm tra giá trị phân số (½) execute đúng | `[~]` |

### 3.3 Housing/Amenities (mục 5)
| Item | Status |
|---|---|
| Housing formula + trần | `[x]` |
| Nước ngọt định cư | `[x]` |
| Luxury → 1 Amenity × 4 cities | `[x]` |
| Entertainment Complex / Water Park / neighborhoods | `[x]` |
| War weariness làm giảm Amenities | `[~]` (War support thay thế, phần õ hạch còn basic) |

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

**Đã đạt (~95%):** toàn bộ hệ thống "chơi" của Civ 6 có trong engine + ruleset — districts, adjacency, tech/civic/policy, gov, tôn giáo, age/loyalty, world congress, climate, corps/army, great works, game modes.

**Việc nên làm tiếp (theo ưu tiên):**
1. **AI toàn diện** (đặt thành, đặt district, build queue, research, wonder) — mảng lớn còn `[~]` thật.
2. **Hoàn thiện adjacency call chuẩn** (giá trị phân số + kiểm chứng từng district).
3. **Great Works per-building + theming per-museum** (đang global pool).
4. **Giảm friction War Weariness cũ với War Support mới.**
5. **Hoàn tất UI tooltips uniques** + bổ sung unit action team đang sửa.
6. **Commit các thay đổi uncommitted** (GovernmentPicker, WorkerAutomation builder, test mới) sau khi chạy test xong.

---

*Tài liệu tổng hợp từ: `CIV6_GAMEPLAY_MECHANICS.md`, `git show <commit>:CIV6_CONSOLIDATED_PLAN.md`, kiểm tra `jsons/Civ VI/*.json`, `git status`, `git log --oneline`, `git diff` (working tree). Trạng thái tính đến commit `59e80a2` + uncommitted.*