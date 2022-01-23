package net.sharplab.epubtranslator.core.repository

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import javax.inject.Inject

@QuarkusTest
internal class TranslationMemoryRepositoryTest {

    @Inject
    private lateinit var translationMemoryRepository: TranslationMemoryRepository

    @Test
    fun findAll_test(){
        assertDoesNotThrow {
            translationMemoryRepository.findAll()
        }
    }

    @Test
    fun find_test(){
        assertDoesNotThrow {
            translationMemoryRepository.find("test", "en", "ja")
        }
    }



}