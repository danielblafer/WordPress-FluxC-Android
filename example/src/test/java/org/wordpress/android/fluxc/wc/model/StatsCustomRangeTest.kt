package org.wordpress.android.fluxc.wc.model

import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.model.StatsCustomRange
import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit
import org.wordpress.android.fluxc.store.WCStatsStore
import org.wordpress.android.fluxc.utils.TimeEnum
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class StatsCustomRangeTest {
    private lateinit var statsCustomRange: StatsCustomRange
    private var startDate: Date? = null
    private var endDate: Date? = null

    private val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)

    @Before
    fun setUp() {
        startDate = formatter.parse(START_DATE_STRING)
        endDate = formatter.parse(END_DATE_STRING)
    }

    @Test
    fun assertValidDates() {
        val dateTest1 = formatter.parse(START_DATE_STRING)
        val dateTest2 = formatter.parse(END_DATE_STRING)

        // NOTE: The Granularity is irrelevant for this test
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK)

        Assert.assertEquals(statsCustomRange.startDate, dateTest1)
        Assert.assertEquals(statsCustomRange.endDate, dateTest2)
    }

    @Test
    fun assertCheckForWrongValuesCorrectCalls() {
        // NOTE: The Granularity is irrelevant for this test
        statsCustomRange = Mockito.spy(StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK))

        doNothing().`when`(statsCustomRange)!!.checkForSwitchedDates()

        statsCustomRange.checkForSwitchedDates()

        verify(statsCustomRange).checkForSwitchedDates()
    }

    @Test
    fun assertGetTimeEnumIsNoneOfTheValues() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.CUSTOM)
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.DAYS)
    }

    @Test
    fun assertGetTimeEnumIsDays() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY)
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.DAYS)
    }

    @Test
    fun assertGetTimeEnumIsWeeks() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK)
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.WEEKS)
    }

    @Test
    fun assertGetTimeEnumIsMonths() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.MONTH)
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.MONTHS)
    }

    @Test
    fun assertGetTimeEnumIsYears() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.YEAR)
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.YEARS)
    }

    @Test
    fun assertGetCorrectDateFormatIsCustom() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.CUSTOM)
        Assert.assertEquals(statsCustomRange.getCorrectDateFormat, WCStatsStore.DATE_FORMAT_DAY)
    }

    @Test
    fun assertGetCorrectDateFormatIsDay() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY)
        Assert.assertEquals(statsCustomRange.getCorrectDateFormat, WCStatsStore.DATE_FORMAT_DAY)
    }

    @Test
    fun assertGetCorrectDateFormatIsWeek() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK)
        Assert.assertEquals(statsCustomRange.getCorrectDateFormat, WCStatsStore.DATE_FORMAT_WEEK)
    }

    @Test
    fun assertGetCorrectDateFormatIsMonth() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.MONTH)
        Assert.assertEquals(statsCustomRange.getCorrectDateFormat, WCStatsStore.DATE_FORMAT_MONTH)
    }

    @Test
    fun assertGetCorrectDateFormatIsYear() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.YEAR)
        Assert.assertEquals(statsCustomRange.getCorrectDateFormat, WCStatsStore.DATE_FORMAT_YEAR)
    }

    companion object {
        private const val START_DATE_STRING = "2018-09-18"
        private const val END_DATE_STRING = "2018-12-18"
    }
}
