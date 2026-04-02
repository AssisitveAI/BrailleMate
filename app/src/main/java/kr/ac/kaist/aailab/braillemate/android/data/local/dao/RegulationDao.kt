package kr.ac.kaist.aailab.braillemate.android.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity

@Dao
interface RegulationDao {

    @Query("SELECT * FROM regulations ORDER BY orderIndex ASC")
    fun getAllRegulations(): Flow<List<RegulationEntity>>

    @Query("SELECT * FROM regulations WHERE id = :id")
    suspend fun getRegulationById(id: Int): RegulationEntity?

    @Query("SELECT DISTINCT section FROM regulations ORDER BY orderIndex ASC")
    fun getSections(): Flow<List<String>>

    @Query("SELECT DISTINCT chapter FROM regulations WHERE section = :section ORDER BY orderIndex ASC")
    fun getChapters(section: String): Flow<List<String>>

    @Query("SELECT * FROM regulations WHERE section = :section ORDER BY orderIndex ASC")
    fun getRegulationsBySection(section: String): Flow<List<RegulationEntity>>

    @Query("SELECT * FROM regulations WHERE section = :section AND chapter = :chapter ORDER BY orderIndex ASC")
    fun getRegulationsBySectionAndChapter(section: String, chapter: String): Flow<List<RegulationEntity>>

    @Query("""
        SELECT regulations.* FROM regulations
        JOIN regulations_fts ON regulations.rowid = regulations_fts.rowid
        WHERE regulations_fts MATCH :query
        ORDER BY regulations.orderIndex ASC
    """)
    fun searchRegulations(query: String): Flow<List<RegulationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(regulations: List<RegulationEntity>)

    @Query("SELECT COUNT(*) FROM regulations")
    suspend fun getCount(): Int

    @Query("DELETE FROM regulations")
    suspend fun deleteAll()
}
