package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.entity.TranslationMemoryEntity
import net.sharplab.epubtranslator.core.repository.TranslationMemoryRepository
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class TranslationMemoryServiceImpl(private val translationMemoryRepository: TranslationMemoryRepository) : TranslationMemoryService {

    @Transactional
    override fun load(source: String, sourceLang: String, targetLang: String): String? {
        return translationMemoryRepository.find(source, sourceLang, targetLang)?.target
    }

    @Transactional
    override fun save(source: String, target: String, sourceLang: String, targetLang: String) {
        translationMemoryRepository.persist(TranslationMemoryEntity().also { it.source = source; it.target = target; it.sourceLang = sourceLang; it.targetLang = targetLang })
    }
}