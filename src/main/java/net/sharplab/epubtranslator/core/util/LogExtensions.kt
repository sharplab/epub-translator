package net.sharplab.epubtranslator.core.util

fun String.logSubString(endIndex: Int = 20): String = substring(0, endIndex.coerceAtMost(length))