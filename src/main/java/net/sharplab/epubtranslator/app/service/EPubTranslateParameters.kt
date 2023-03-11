package net.sharplab.epubtranslator.app.service

import java.io.File

class EPubTranslateParameters(val srcFile: File, val dstFile: File, val srcLang: String, val dstLang: String, val limitCredits: Int, val abortOnError: Boolean)