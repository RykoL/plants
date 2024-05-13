package de.rlang.plants.domain

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class IntervalConvert : Converter<String, Interval> {
    override fun convert(source: String): Interval {
        return Interval.valueOf(source.uppercase())
    }
}
