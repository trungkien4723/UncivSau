# UncivSau - Civ 6 mechanics for Unciv

![](/extraImages/GithubPreviewImage.jpg)

[![GitHub Release](https://img.shields.io/github/v/release/trungkien4723/UncivSau?logo=github)](https://github.com/trungkien4723/UncivSau/releases)

> **⚠️ Work in progress** - This project is **not finished**. The gameplay mechanics are being ported over one by one, and some assets are still missing - a number of images, icons, and other graphics are placeholders that will be filled in over time. If something looks rough around the edges, that's why :)

## What is this?

A fork of [Unciv](https://github.com/yairm210/Unciv) - the open source, moddability-focused Android and Desktop remake of Civ V, made with [LibGDX](https://github.com/libgdx/libgdx) - whose goal is to bring Civilization VI mechanics into the engine, for everyone who grew up with and loves Civ 6.

## Is this any good?

Depends what you're looking for. If you're in the market for high-res graphics, amazing soundtracks, animations etc, I highly recommend Firaxis's Civilization VI.

If you want a small, fast, moddable, FOSS, in-depth 4X with the Civ 6 ruleset - districts and district-gated buildings, governments and policy cards, envoys and city-states, traders that travel and build roads, espionage missions, era score and golden ages, and more - you've come to the right place :)

## How do I install?

- **Android** - Grab the latest APK from [Releases](https://github.com/trungkien4723/UncivSau/releases) and sideload it
- **Windows** - Grab the MSI from [Releases](https://github.com/trungkien4723/UncivSau/releases) or the Windows zip
- **Linux** - Grab the Linux zip from [Releases](https://github.com/trungkien4723/UncivSau/releases)
- Jars are also in [Releases](https://github.com/trungkien4723/UncivSau/releases) (run jar with `java -jar UncivSau.jar`) - *not recommended* since it updates frequently and you will quickly become out-of-date
- [Build from scratch](https://yairm210.github.io/Unciv/Developers/Building-Locally/) if that's your thing

## What's the roadmap?

The original Unciv roadmap is all about Civ V's Gods & Kings and Brave New World mechanics. This fork follows the same clear vision - but for Civ VI, one mechanic at a time:

* Districts and unique districts
* Governments and policy cards
* Envoys and city-states
* Traders that travel and build roads, trading posts
* Espionage missions
* Era score, golden ages and dark ages
* And more, slowly but surely

### Civ6 Rewrite (clean, no Civ5 loop) - branch `civ6-rewrite`

Master `4.26.14` still Civ5-hotfix. Clean rewrite is on `civ6-rewrite` (orphan, 9 commits):

* **Sprint1 City** - Housing/Amenity (not global happiness), `maxDistrictSlots 1,4,7...`, adjacency `+0.5`, district `cost+15` scaling
* **Sprint2 Tech/Civic** - 2 trees separate, eureka/inspiration `+50%`, Government slots `Military/Economic/Diplomatic/Wildcard`
* **Sprint3 Combat** - walls HP only (not `200/400` strength), siege `-17` vs land only, support/flanking `+2`
* **Sprint4 Loyalty/Era/Governor/Religion** - pressure `pop*(10-dist)/10`, Governor `ArrayList<ArrayList>` fix CCE, Religion pantheon 25/prophet 60
* **Sprint5 Engine v10** - `GameInfoV2` `CURRENT_NUMBER=10` cuts Civ5 saves, `TurnManager`
* **Sprint6 UI** - `Civ6WorldScreen`/`CityScreen` adapters
* **Sprint7 Map/Save** - `TileMapV2` district loses resource, `Civ6Files` only `>10` incompatible (fixed misleading error)
* **Sprint8 AI** - `City/Tech/Government` automation

See `docs/civ6-spec.md` (single truth) + `docs/civ6/ARCHITECTURE.md`.

## FAQ

### Will you implement {feature}?

If it's in Civ VI, then yes!

If not, then the feature won't be added to the base game - possibly it will be added as a way to mod the game, which is constantly expanding.

### Why not? This is its own game, why not add features that weren't in Civ V or Civ VI?

Having a clear vision is important for actually getting things done.

Anyone can make a suggestion. Not all are good, viable, or simple. Not many can actually implement stuff.

As a solo side project, this is done in spare time, of which there isn't much.

### Why Civ 6?

Because it is the second version of the Sid Meier's Civilization game series that I played and loved, so I want to try to develop it - for anyone who loves both Civ 6 and Unciv.

### Aren't you basically making a Civ VI clone? Is that even legal?

The same answer the original Unciv gives: according to the [US Copyright Office FL-108](https://upload.wikimedia.org/wikipedia/commons/9/96/U.S._Copyright_Office_fl108.pdf), intellectual property rights *do not* apply to game mechanics.

It is definitely illegal:
 - To use any assets from the original games (images, sound etc) - they belong to Firaxis

It is probably illegal (no solid sources on this):
 - To use the Civilization name
 - To impersonate the Civ games (so calling yourself civ|zation with a similar logo, for instance)

So no assets, names, or logos from Civ V or Civ VI are used - only the mechanics, recreated from scratch.

### How can I learn to play?

The tutorial information is available in-game at menu > civilopedia > tutorials, and since the rules are Civ 6's, you can search the internet for how to play Civ 6 and there are loads of answers =)

## Contributing

Programmers start [here](https://yairm210.github.io/Unciv/Developers/Building-Locally/)!

Translators start [here](https://yairm210.github.io/Unciv/Translating/Translating/)!

Modders start [here](https://yairm210.github.io/Unciv/Modders/Mods/)!

You can work on any of the open issues, or on improving anything you want - once you're finished, issue a pull request and it'll go into the next version!

If not, you can help by spreading the word - mention it on Reddit or Twitter etc, and share new ideas of how to get the word out!

## [Credits and 3rd parties](docs/Credits.md)