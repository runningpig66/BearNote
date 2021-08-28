package cloud.runningpig.bearnote.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cloud.runningpig.bearnote.logic.model.NoteCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteCategoryDao {

    @Query("SELECT * FROM note_category WHERE sort = :sort")
    fun loadBySort(sort: Int): Flow<List<NoteCategory>>

    @Insert
    suspend fun insertDefault(defaultList: List<NoteCategory>)

    @Query("DELETE FROM note_category")
    suspend fun deleteAll()

//    @Query("SELECT * FROM user")
//    fun getAll(): List<User>
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User
//
//    @Insert
//    fun insertAll(vararg users: User)
//
//    @Delete
//    fun delete(user: User)

}