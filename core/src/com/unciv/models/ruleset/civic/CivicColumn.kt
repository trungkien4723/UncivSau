package com.unciv.models.ruleset.civic

class CivicColumn {
    var columnNumber: Int = 0
    lateinit var era: String
    var civics = ArrayList<Civic>()
    var civicCost: Int = 0
}
