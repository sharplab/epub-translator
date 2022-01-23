package net.sharplab.epubtranslator.core.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import net.sharplab.epubtranslator.core.entity.TranslationMemoryEntity
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class TranslationMemoryRepository : PanacheRepository<TranslationMemoryEntity> {

    fun find(source: String, sourceLang: String, targetLang: String): TranslationMemoryEntity? {
        return find("from net.sharplab.epubtranslator.core.entity.TranslationMemoryEntity where source = ?1 and sourceLang = ?2 and targetLang = ?3", source, sourceLang, targetLang).firstResult()
    }
}
