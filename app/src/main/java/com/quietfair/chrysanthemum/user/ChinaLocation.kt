package com.quietfair.chrysanthemum.user

class ChinaLocation {
    var name: String? = null
    var city: List<City>? = null

    inner class City {
        var name: String? = null
        var area: List<String>? = null
    }
}
