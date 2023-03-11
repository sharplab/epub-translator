package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.entity.TranslationMemoryEntity
import net.sharplab.epubtranslator.core.repository.TranslationMemoryRepository
import net.sharplab.epubtranslator.core.util.logSubString
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class TranslationMemoryServiceImpl(private val translationMemoryRepository: TranslationMemoryRepository) : TranslationMemoryService {

    private val logger = LoggerFactory.getLogger(TranslationMemoryServiceImpl::class.java)

    @Transactional
    override fun load(source: String, sourceLang: String, targetLang: String): String? {
        return translationMemoryRepository.find(source, sourceLang, targetLang)?.target
            .also { if (doLog) logger.info("load: {}, translated: {}", source.logSubString(), it?.logSubString()) }
    }

    @Transactional
    override fun save(source: String, target: String, sourceLang: String, targetLang: String) {
        if (doLog) logger.info("save: {}, translated: {}", source.logSubString(), target.logSubString())
        translationMemoryRepository.persist(TranslationMemoryEntity().also { it.source = source; it.target = target; it.sourceLang = sourceLang; it.targetLang = targetLang })
    }

    companion object {
        private const val doLog = false
    }
}