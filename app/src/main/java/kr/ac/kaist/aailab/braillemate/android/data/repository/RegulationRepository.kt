package kr.ac.kaist.aailab.braillemate.android.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.BookmarkDao
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.RegulationDao
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.BookmarkEntity
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class RegulationSection(
    val section: String,
    val chapters: List<RegulationChapter>
)

@Serializable
data class RegulationChapter(
    val chapter: String,
    val articles: List<RegulationArticle>
)

@Serializable
data class RegulationArticle(
    val articleNumber: String,
    val content: String,
    val subSection: String? = null,
    val examples: List<BrailleExample>? = null,
    val notes: String? = null
)

@Serializable
data class BrailleExample(
    val text: String,
    val braille: String
)

@Singleton
class RegulationRepository @Inject constructor(
    private val regulationDao: RegulationDao,
    private val bookmarkDao: BookmarkDao,
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllRegulations(): Flow<List<RegulationEntity>> =
        regulationDao.getAllRegulations()

    fun getSections(): Flow<List<String>> =
        regulationDao.getSections()

    fun getChapters(section: String): Flow<List<String>> =
        regulationDao.getChapters(section)

    fun getRegulationsBySection(section: String): Flow<List<RegulationEntity>> =
        regulationDao.getRegulationsBySection(section)

    fun getRegulationsBySectionAndChapter(section: String, chapter: String): Flow<List<RegulationEntity>> =
        regulationDao.getRegulationsBySectionAndChapter(section, chapter)

    suspend fun getRegulationById(id: Int): RegulationEntity? =
        regulationDao.getRegulationById(id)

    fun searchRegulations(query: String): Flow<List<RegulationEntity>> {
        val ftsQuery = query.trim().split("\\s+".toRegex()).joinToString(" ") { "$it*" }
        return regulationDao.searchRegulations(ftsQuery)
    }

    // Bookmarks
    fun getBookmarkedRegulations(): Flow<List<RegulationEntity>> =
        bookmarkDao.getBookmarkedRegulations()

    fun isBookmarked(regulationId: Int): Flow<Boolean> =
        bookmarkDao.isBookmarked(regulationId)

    suspend fun toggleBookmark(regulationId: Int) {
        val existing = bookmarkDao.getBookmark(regulationId)
        if (existing != null) {
            bookmarkDao.deleteBookmark(regulationId)
        } else {
            bookmarkDao.insertBookmark(BookmarkEntity(regulationId = regulationId))
        }
    }

    // Data initialization
    suspend fun initializeData() {
        if (regulationDao.getCount() > 0) return

        try {
            val inputStream = context.assets.open("regulations.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val sections = json.decodeFromString<List<RegulationSection>>(jsonString)

            var orderIndex = 0
            val entities = mutableListOf<RegulationEntity>()

            for (section in sections) {
                for (chapter in section.chapters) {
                    for (article in chapter.articles) {
                        entities.add(
                            RegulationEntity(
                                section = section.section,
                                chapter = chapter.chapter,
                                subSection = article.subSection,
                                articleNumber = article.articleNumber,
                                content = article.content,
                                examples = article.examples?.let { exList -> json.encodeToString(ListSerializer(BrailleExample.serializer()), exList) },
                                notes = article.notes,
                                orderIndex = orderIndex++
                            )
                        )
                    }
                }
            }

            regulationDao.insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
