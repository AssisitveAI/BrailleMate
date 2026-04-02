package kr.ac.kaist.aailab.braillemate.android.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.BookmarkEntity
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity

@Dao
interface BookmarkDao {

    @Query("""
        SELECT r.* FROM regulations r 
        INNER JOIN bookmarks b ON r.id = b.regulationId 
        ORDER BY b.createdAt DESC
    """)
    fun getBookmarkedRegulations(): Flow<List<RegulationEntity>>

    @Query("SELECT * FROM bookmarks WHERE regulationId = :regulationId LIMIT 1")
    suspend fun getBookmark(regulationId: Int): BookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE regulationId = :regulationId)")
    fun isBookmarked(regulationId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE regulationId = :regulationId")
    suspend fun deleteBookmark(regulationId: Int)
}
