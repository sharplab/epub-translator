package net.sharplab.epubtranslator.core.entity

import javax.persistence.*

@Table(indexes = [
    Index(name = "idx_source_sourceLang_targetLang", columnList = "source, sourceLang, targetLang")
])
@Entity(name = "translation_memory")
class TranslationMemoryEntity {
    @Id
    @GeneratedValue
    var id: Int? = null
    @Column(length = 65535)
    lateinit var source: String
    @Column(length = 65535)
    lateinit var target: String
    lateinit var sourceLang: String
    lateinit var targetLang: String
}
