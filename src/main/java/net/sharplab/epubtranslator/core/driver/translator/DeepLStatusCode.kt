package net.sharplab.epubtranslator.core.driver.translator

/**
 * deepl error declares here
 * https://www.deepl.com/zh/docs-api/accessing-the-api/error-handling/
 */
enum class DeepLStatusCode (var code : Int) {
    TOO_MANY_REQUESTS(429)
}
