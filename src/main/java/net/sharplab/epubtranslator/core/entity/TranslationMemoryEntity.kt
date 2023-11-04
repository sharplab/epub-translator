package net.sharplab.epubtranslator.core.entity

import jakarta.persistence.*

@Table(indexes = [
    Index(name = "idx_source_sourceLang_targetLang", columnList = "source, sourceLang, targetLang")
])
@Entity(name = "translation_memory")
open class TranslationMemoryEntity {
    @Id
    @GeneratedValue
    open var id: Int? = null
    @Column(length = 65535)
    open lateinit var source: String
    @Column(length = 65535)
    open lateinit var target: String
    open lateinit var sourceLang: String
    open lateinit var targetLang: String
}
