package cloud.runningpig.bearnote.ui.detail

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import cloud.runningpig.bearnote.BearNoteApplication
import cloud.runningpig.bearnote.logic.BearNoteRepository
import cloud.runningpig.bearnote.logic.model.*
import cloud.runningpig.bearnote.logic.utils.showToast
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

/**
 * 明细、图表、账户共用
 */
class DetailViewModel(private val bearNoteRepository: BearNoteRepository) : ViewModel() {

    val date = MutableLiveData(Date())

    val queryByDate: LiveData<List<NoteDetail>> = Transformations.switchMap(date) {
        val startOfDay = getStartOfDay(it)
        val endOfDay = getEndOfDay(it)
        bearNoteRepository.queryByDate(startOfDay, endOfDay).asLiveData()
    }

    fun queryById(noteId: Int) = bearNoteRepository.queryById(noteId).asLiveData()

    fun queryById2(noteId: Int) = bearNoteRepository.queryById(noteId)

    val page = MutableLiveData(0)

    // TODO 图表按钮切换月份
    val month = MutableLiveData(Date())
    val dataList = ArrayList<ChartMonthBean>()

    // 月内所有记账的总金额
    var amount: Double = 0.0

    // 月内前5记账的总金额
    var topFiveAmount = 0.0

    // 月内其他记账笔数（除去前5）
    var otherCountCategoryId = 0

    fun queryByMonth(sort: Int): LiveData<List<ChartMonthBean>> = Transformations.switchMap(month) {
        val startOfMonth = getStartOfMonth(it)
        val endOfMonth = getEndOfMonth(it)
        bearNoteRepository.queryByMonth(sort, startOfMonth, endOfMonth).asLiveData()
    }

    val map = HashMap<LiveData<List<DailyAmount>>, Observer<List<DailyAmount>>>()

    fun queryDailyAmount(date: Date): LiveData<List<DailyAmount>> {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)
        return bearNoteRepository.queryDailyAmount(startOfDay, endOfDay)
    }

    /** 设置参数Date到当月第一天00:00:00 */
    private fun getStartOfMonth(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /** 设置参数Date到当月最后一天23:59:59 */
    private fun getEndOfMonth(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 0) // 这里设置DAY为0确实可以到达上个月最后一天，即当前月最后一天
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /** 设置参数Date到当天00:00:00 */
    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /** 设置参数Date到当天23:59:59 */
    private fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun generateCenterSpannableText(): SpannableString {
        val s = SpannableString("BearNote\n小熊记账")
        s.setSpan(RelativeSizeSpan(1.2f), 0, 8, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 8, s.length, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 8, s.length, 0)
        s.setSpan(RelativeSizeSpan(1.1f), 8, s.length, 0)
//        s.setSpan(StyleSpan(Typeface.ITALIC), 8, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), 8, s.length, 0)
        return s
    }

    fun generateDescriptionText(sort: Int): String {
        val sortString = if (sort == 0) "支出" else "收入"
        return "${sortString}类别排行"
    }

    /**
     * 以下为账户模块代码
     */

    // 验证账户输入信息
    fun accountEntryValid(name: String?, balance: String?): Boolean {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(balance)) {
            return false
        }
        try {
            balance?.toDouble() // 账户余额的文字可以转换成double
        } catch (ignore: Exception) {
            "账户余额输入错误".showToast()
            return false
        }
        return true
    }

    private fun getNewAccount(name: String, icon: String, balance: Double, information: String, order: Int, id: Int = -1): Account {
        if (id == -1) { // 不手动指定id，新增元素
            return Account(
                name = name,
                icon = icon,
                balance = balance,
                information = information,
                order = order,
                uid = BearNoteApplication.uid,
            )
        } else { // 指定id，说明是更新某个id的元素
            return Account(
                id = id,
                name = name,
                icon = icon,
                balance = balance,
                information = information,
                order = order,
                uid = BearNoteApplication.uid,
            )
        }
    }

    private fun insert(account: Account) = viewModelScope.launch {
        bearNoteRepository.insertAccount(account)
    }

    fun addNewAccount(name: String, icon: String, balance: Double, information: String, order: Int) {
        val account = getNewAccount(name, icon, balance, information, order)
        insert(account)
    }

    fun update(account: Account) = viewModelScope.launch {
        bearNoteRepository.update(account)
    }

    suspend fun update2(account: Account) = bearNoteRepository.update(account)


    fun updateAccount(id: Int, name: String, icon: String, balance: Double, information: String, order: Int) {
        val account = getNewAccount(name, icon, balance, information, order, id)
        update(account)
    }

    fun queryMaxOrder2() = bearNoteRepository.queryMaxOrder2().asLiveData()

    lateinit var accountList: List<Account>
    fun loadAccount() = bearNoteRepository.loadAccount().asLiveData()

    fun updateList2(list: List<Account>) = viewModelScope.launch {
        bearNoteRepository.updateList2(list)
    }

    // 转账
    var from: Account? = null // todo 点击完成后设置为null
    var to: Account? = null
    var mDate = Date()

    fun transferEntryValid(fromId: Int?, toId: Int?, amount: String?): Boolean {
        if (fromId != null && toId != null && amount != null) {
            return true
        }
        return false
    }

    private fun insertTransfer(transfer: Transfer) = viewModelScope.launch {
        bearNoteRepository.insertTransfer(transfer)
    }

    private fun newTransfer(fromId: Int, toId: Int, amount: Double, info: String, date: Date): Transfer {
        return Transfer(
            fromId = fromId,
            toId = toId,
            amount = amount,
            information = info,
            date = date
        )
    }

    fun addNewTransfer(fromId: Int, toId: Int, amount: Double, info: String, date: Date) {
        val t = newTransfer(fromId, toId, amount, info, date)
        insertTransfer(t)
    }

    val month2 = MutableLiveData(Date()) // TODO 改月份

    fun queryByMonth2(accountId: Int): LiveData<List<TransferDetail>> = Transformations.switchMap(month2) {
        val startOfMonth = getStartOfMonth(it)
        val endOfMonth = getEndOfMonth(it)
        bearNoteRepository.queryByDate2(accountId, startOfMonth, endOfMonth).asLiveData()
    }

    fun sumBalance() = bearNoteRepository.sumBalance().asLiveData()

    fun queryByAid2(aid: Int) = bearNoteRepository.queryByAid2(aid).asLiveData()

    suspend fun queryByAid3(aid: Int) = bearNoteRepository.queryByAid3(aid)

    fun deleteAccount(aid: Int) = viewModelScope.launch {
        bearNoteRepository.deleteAccount(aid)
    }

    fun deleteNote(noteId: Int) = viewModelScope.launch {
        bearNoteRepository.deleteNote(noteId)
    }

    suspend fun addUser(user: User) = bearNoteRepository.addUser(user)

    suspend fun login(username: String, password: String) = bearNoteRepository.login(username, password)

    fun userEntryValid(username: String, password: String, nickname: String): Boolean {
        return !(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(nickname))
    }

    fun loginEntryValid(username: String, password: String): Boolean {
        return !(TextUtils.isEmpty(username) || TextUtils.isEmpty(password))
    }

    fun insertUser(user: User) = viewModelScope.launch {
        bearNoteRepository.insertUser(user)
    }

    fun selectUser() = bearNoteRepository.selectUser().asLiveData()

    fun deleteUser() = viewModelScope.launch {
        bearNoteRepository.deleteUser()
    }

}

class DetailViewModelFactory(private val bearNoteRepository: BearNoteRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(bearNoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}