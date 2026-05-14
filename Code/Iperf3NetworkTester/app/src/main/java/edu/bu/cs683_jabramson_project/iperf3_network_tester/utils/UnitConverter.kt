package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.annotation.SuppressLint
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.BITS
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.BITS_UNIT
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.GBITS
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.GB_UNIT
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.KBITS
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.KB_UNIT
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.MBITS
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.MB_UNIT
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.TBITS
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.TB_UNIT
import java.util.Locale


data class UnitConvertedData(val value: Double = 0.0, val unit: String = "") {
    override fun toString(): String = if (value <= 0.0) "" else  "$value $unit"
}

object Units {
    const val KBITS = 1024.0
    const val MBITS = 1024.0 * KBITS
    const val GBITS = 1024.0 * MBITS
    const val TBITS = 1024.0 * GBITS
    const val BITS = 1.0

    const val KB_UNIT = "Kbits/sec"
    const val MB_UNIT = "Mbits/sec"
    const val GB_UNIT = "Gbits/sec"
    const val TB_UNIT = "Tbits/sec"
    const val BITS_UNIT = "bits/sec"
}

fun toString(unitConvertedData: UnitConvertedData) = "${unitConvertedData.value} ${unitConvertedData.unit}"


fun toWholeNumber(unitConvertedData: UnitConvertedData): String {
    val ret = "%10.2f %s".format(Locale.US, unitConvertedData.value, unitConvertedData.unit)
    return ret
}


fun fromHumanUnit(value: Double, unit: String): Double {
    val rawBitsPerSec = when (unit) {
        KB_UNIT -> value * KBITS
        MB_UNIT -> value * MBITS
        GB_UNIT -> value * GBITS
        TB_UNIT -> value * TBITS
        else -> value
    }
    return rawBitsPerSec
}

fun toHumanUnit(rawBitsPerSec: Double): UnitConvertedData {
    var perSec = rawBitsPerSec // bits/sec
    var convertedUnit = BITS_UNIT
    if (perSec >= TBITS) {
        perSec = rawBitsPerSec / TBITS
        convertedUnit = TB_UNIT
    } else if (perSec >= GBITS) {
        perSec = rawBitsPerSec / GBITS
        convertedUnit = GB_UNIT
    } else if (perSec >= MBITS) {
        perSec = rawBitsPerSec / MBITS
        convertedUnit = MB_UNIT
    } else if (perSec >= KBITS) {
        perSec = rawBitsPerSec / KBITS
        convertedUnit = KB_UNIT
    }
    return UnitConvertedData(perSec, convertedUnit)
}
