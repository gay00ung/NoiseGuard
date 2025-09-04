package net.lateinit.noiseguard.domain.label

import net.lateinit.noiseguard.data.ml.ClassifiedLabel

interface LabelLocalizer {
    fun localize(index: Int?, englishName: String): String
    fun localize(label: ClassifiedLabel): String = localize(label.index, label.name)
}

class PassthroughLabelLocalizer : LabelLocalizer {
    override fun localize(index: Int?, englishName: String): String = englishName
}

