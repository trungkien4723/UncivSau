# Civ VI Conversion Audit

So sánh giữa project hiện tại và Civilization VI (Gathering Storm) thật.

---

## 1. Technologies (Techs.json)

### 1.1 Công nghệ THỪA — gốc Civ 5, cần xóa

| # | Công nghệ | Era hiện tại | Lý do |
|---|-----------|-------------|-------|
| 1 | Agriculture | Ancient | Starter tech của Civ 5; Civ 6 bắt đầu bằng Pottery/Animal Husbandry |
| 2 | Trapping | Ancient | Civ 5 tech, không có trong Civ 6 |
| 3 | Optics | Classical | Civ 6 dùng Celestial Navigation |
| 4 | Philosophy | Classical | Là **Civic** trong Civ 6 |
| 5 | Drama and Poetry | Classical | Là **Civic** trong Civ 6 |
| 6 | Theology | Medieval | Là **Civic** trong Civ 6 |
| 7 | Civil Service | Medieval | Là **Civic** trong Civ 6 |
| 8 | Guilds | Medieval | Là **Civic** trong Civ 6 |
| 9 | Compass | Medieval | Không có trong Civ 6 |
| 10 | Chivalry | Medieval | Civ 6 dùng Stirrups |
| 11 | Physics | Medieval | Không có trong Civ 6 |
| 12 | Acoustics | Renaissance | Không có trong Civ 6 |
| 13 | Navigation | Renaissance | Civ 6 dùng Celestial Navigation + Cartography |
| 14 | Architecture | Renaissance | Không có trong Civ 6 (là building) |
| 15 | Metallurgy | Renaissance | Không có trong Civ 6 |
| 16 | Archaeology | Industrial | Không phải tech riêng (Archaeologist là unit) |
| 17 | Dynamite | Industrial | Không có trong Civ 6 |
| 18 | Biology | Industrial | Không có trong Civ 6 |
| 19 | Fertilizer | Industrial | Không có trong Civ 6 |
| 20 | Electronics | Modern | Không có trong Civ 6 |
| 21 | Mobile Tactics | Information | Fan-made, không có thật |
| 22 | Particle Physics | Information | Không có trong Civ 6 |
| 23 | Globalization | Information | Là **Civic** trong Civ 6 |
| 24 | Information Warfare | Information | Là **Civic** trong Civ 6 |
| 25 | Climate Change | Information | Cơ chế game (Gathering Storm), không phải tech riêng |
| 26 | Renewable Energy | Information | Không phải tech riêng |
| 27 | Offshore Wind Farms | Information | Không phải tech riêng |
| 28 | Turning Point | Modern | Fan-made |
| 29 | Siege Tactics | Modern | Chuyển xuống Renaissance (xem mục 1.4) |

### 1.2 Công nghệ THIẾU — Civ 6 có, project chưa có

| # | Công nghệ | Era (Civ 6) | Mở khóa |
|---|-----------|-------------|---------|
| 1 | Astrology | Ancient | Holy Site, Religion |
| 2 | Irrigation | Ancient | Farm trên đồng bằng |
| 3 | Celestial Navigation | Classical | Harbor |
| 4 | Shipbuilding | Classical | |
| 5 | Buttress | Medieval | |
| 6 | Military Tactics | Medieval | |
| 7 | Stirrups | Medieval | Knight |
| 8 | Cartography | Renaissance | |
| 9 | Mass Production | Renaissance | |
| 10 | Printing | Renaissance | Thay thế Printing Press |
| 11 | Square Rigging | Renaissance | Frigate |
| 12 | Sanitation | Industrial | Sewer, Neighborhood |
| 13 | Refining | Modern | Oil Well |
| 14 | Advanced Flight | Atomic | |
| 15 | Synthetic Materials | Atomic | |
| 16 | Composites | Information | |
| 17 | Guidance Systems | Information | |
| 18 | Advanced AI | Future | |
| 19 | Advanced Power Cells | Future | |
| 20 | Cybernetics | Future | |
| 21 | Offworld Mission | Future | |
| 22 | Smart Materials | Future | |

### 1.3 Công nghệ cần ĐỔI TÊN

| # | Tên hiện tại | Tên đúng (Civ 6) |
|---|-------------|------------------|
| 1 | The Wheel | Wheel |
| 2 | Printing Press | Printing |
| 3 | Stealth | Stealth Technology |

### 1.4 Công nghệ cần CHUYỂN ERA

| # | Công nghệ | Era hiện tại | Era đúng (Civ 6) |
|---|-----------|-------------|-----------------|
| 1 | Steel | Medieval | Modern |
| 2 | Chemistry | Renaissance | Modern |
| 3 | Siege Tactics | Modern | Renaissance |
| 4 | Apprenticeship | Information | Medieval |
| 5 | Castles | Information | Medieval |
| 6 | Military Engineering | Modern | Medieval |
| 7 | Refrigeration | Modern | Atomic |

---

## 2. Civics (Civics.json)

### 2.1 Civic THỪA — gốc Civ 5 hoặc fan-made, cần xóa

| # | Civic | Era hiện tại | Lý do |
|---|-------|-------------|-------|
| 1 | The Great Green Wall | Renaissance | Không có thật |
| 2 | Reformation | Renaissance | Là in-game process, không phải civic |
| 3 | Trade Confederation | Renaissance | Là policy card |
| 4 | Liberalism | Industrial | Là policy card |
| 5 | Collective Activism | Industrial | Là policy card |
| 6 | New Deal | Industrial | Là policy card |
| 7 | Cultural Heritage Reforms | Modern | (Cultural Heritage là civic thật ở Atomic era) |
| 8 | Suffragette Movement | Modern | (Suffrage là civic thật) |
| 9 | Arctic | Modern | Không có thật |
| 10 | Nuclear Fusion | Modern | Là TECHNOLOGY |
| 11 | Environmental Policy | Modern | Là policy card |
| 12 | Near Future Governance | Information | Fan-made |
| 13 | Near Future Authority | Information | Fan-made |
| 14 | Near Future Engagement | Information | Fan-made |
| 15 | Virtualization | Information | Fan-made |
| 16 | Dataism | Information | Fan-made |

### 2.2 Civic THIẾU — Civ 6 có, project chưa có

| # | Civic | Era (Civ 6) | Ghi chú |
|---|-------|-------------|---------|
| 1 | Civil Service | Medieval | Mở Alliances |
| 2 | The Enlightenment | Renaissance | Mở New Government |
| 3 | Colonialism | Industrial | |
| 4 | Urbanization | Industrial | Mở Neighborhoods |
| 5 | Mass Media | Modern | Mở Broadway, Cristo Redentor |
| 6 | Professional Sports | Atomic | Mở Stadium |
| 7 | Space Race | Atomic | |
| 8 | Information Warfare | Future (GS) | Randomized future civic |
| 9 | Global Warming Mitigation | Future (GS) | Randomized future civic |
| 10 | Exodus Imperative | Future (GS) | Randomized future civic |
| 11 | Venture Politics | Future (GS) | Randomized future civic |
| 12 | Distributed Sovereignty | Future (GS) | Randomized future civic |
| 13 | Optimization Imperative | Future (GS) | Randomized future civic |

### 2.3 Civic cần ĐỔI TÊN

| # | Tên hiện tại | Tên đúng (Civ 6) |
|---|-------------|------------------|
| 1 | Games and Revelry | Games and Recreation |
| 2 | Grand Opera | Opera and Ballet |
| 3 | Smart Power | Smart Power Doctrine |

---

## 3. Tổng quan thay đổi

| Loại | Technologies | Civics |
|------|-------------|--------|
| Cần xóa | 29 | 16 |
| Cần thêm | 22 | 13 |
| Cần đổi tên | 3 | 3 |
| Cần chuyển era | 7 | 0 |
| **Tổng** | **61** | **32** |
