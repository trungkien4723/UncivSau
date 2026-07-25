package com.unciv.models.ruleset

import yairm210.purity.annotations.Readonly

enum class GreatWorkType {
    Writing,
    Art,
    Artifact,
    Music,
    Relic;

    @Readonly fun getTourism(): Int = when (this) {
        Writing -> 2
        Art -> 3
        Artifact -> 3
        Music -> 4
        Relic -> 4
    }

    @Readonly fun getCulture(): Int = when (this) {
        Writing -> 2
        Art -> 2
        Artifact -> 1
        Music -> 3
        Relic -> 2
    }
}
