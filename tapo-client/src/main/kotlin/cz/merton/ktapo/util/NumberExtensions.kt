package cz.merton.ktapo.util

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return Math.round(this * multiplier) / multiplier
}
