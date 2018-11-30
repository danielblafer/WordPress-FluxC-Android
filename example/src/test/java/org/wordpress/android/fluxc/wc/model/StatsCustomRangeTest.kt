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

        statsCustomRange = Mockito.spy(StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY))
    }

    @Test
    fun assertValidDates() {
        val dateTest1 = formatter.parse(START_DATE_STRING)
        val dateTest2 = formatter.parse(END_DATE_STRING)

        Assert.assertEquals(statsCustomRange.getStartDateInDateFormat, dateTest1)
        Assert.assertEquals(statsCustomRange.getEndDateInDateFormat, dateTest2)
    }

    @Test
    fun assertValidStringDates() {
        Assert.assertEquals(statsCustomRange.getStartDate(WEEK_OF_THE_YEAR), START_DATE_STRING)
        Assert.assertEquals(statsCustomRange.getEndDate(WEEK_OF_THE_YEAR), END_DATE_STRING)
    }

    @Test
    fun assertCheckForWrongValuesCorrectCalls() {
        doNothing().`when`(statsCustomRange)!!.checkForSwitchedDates()
        doNothing().`when`(statsCustomRange)!!.checkForOvershotDates()

        statsCustomRange.checkForWrongValues()

        verify(statsCustomRange).checkForSwitchedDates()
        verify(statsCustomRange).checkForOvershotDates()
    }

    @Test
    fun assertGetTimeEnumIsNoneOfTheValues() {
        statsCustomRange.granularity = OrderStatsApiUnit.DEFAULT
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.DAYS)
    }

    @Test
    fun assertGetTimeEnumIsDays() {
        statsCustomRange.granularity = OrderStatsApiUnit.DAY
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.DAYS)
    }

    @Test
    fun assertGetTimeEnumIsWeeks() {
        statsCustomRange.granularity = OrderStatsApiUnit.WEEK
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.WEEKS)
    }

    @Test
    fun assertGetTimeEnumIsMonths() {
        statsCustomRange.granularity = OrderStatsApiUnit.MONTH
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.MONTHS)
    }

    @Test
    fun assertGetTimeEnumIsYears() {
        statsCustomRange.granularity = OrderStatsApiUnit.YEAR
        Assert.assertEquals(statsCustomRange.getTimeEnum, TimeEnum.YEARS)
    }

    @Test
    fun assertCheckForOvershotDatesIsOvershot() {
        statsCustomRange.setEndDate(formatter.parse("2019-09-18"))
        statsCustomRange.checkForOvershotDates()
        Assert.assertEquals(statsCustomRange.getEndDate(WEEK_OF_THE_YEAR), getCurrentDateInStringFormat())
    }

    @Test
    fun assertCheckForOvershotDatesIsSameAsToday() {
        statsCustomRange.setEndDate(formatter.parse(getCurrentDateInStringFormat()))
        statsCustomRange.checkForOvershotDates()
        Assert.assertEquals(statsCustomRange.getEndDate(WEEK_OF_THE_YEAR), getCurrentDateInStringFormat())
    }

    @Test
    fun assertCheckForOvershotDatesIsNotOvershot() {
        statsCustomRange.setEndDate(formatter.parse("2019-09-18"))
        statsCustomRange.checkForOvershotDates()
        Assert.assertNotSame(statsCustomRange.getEndDate(WEEK_OF_THE_YEAR), getCurrentDateInStringFormat())
    }

    @Test
    fun assertCheckForSwitchedDatesActualSwitchedDates() {
        val oldStartDate = statsCustomRange.getStartDate(WEEK_OF_THE_YEAR)
        val oldEndDate = statsCustomRange.getEndDate(WEEK_OF_THE_YEAR)

        statsCustomRange.setEndDate(formatter.parse(START_DATE_STRING))
        statsCustomRange.setStartDate(formatter.parse(END_DATE_STRING))
        statsCustomRange.checkForSwitchedDates()

        Assert.assertEquals(statsCustomRange.getStartDate(WEEK_OF_THE_YEAR), oldStartDate)
        Assert.assertEquals(statsCustomRange.getEndDate(WEEK_OF_THE_YEAR), oldEndDate)
    }

    @Test
    fun assertCheckForSwitchedDatesNonSwitchedDates() {
        statsCustomRange.checkForSwitchedDates()

        Assert.assertEquals(statsCustomRange.getStartDate(WEEK_OF_THE_YEAR), START_DATE_STRING)
        Assert.assertEquals(statsCustomRange.getEndDate(WEEK_OF_THE_YEAR), END_DATE_STRING)
    }

    @Test
    fun assertClipDateBasedOnGranularityYearDate() {
        statsCustomRange.granularity = OrderStatsApiUnit.YEAR
        Assert.assertEquals(
                statsCustomRange.clipDateBasedOnGranularity(START_DATE_STRING, WEEK_OF_THE_YEAR),
                "2018")
    }

    @Test
    fun assertClipDateBasedOnGranularityMonthDate() {
        statsCustomRange.granularity = OrderStatsApiUnit.MONTH
        Assert.assertEquals(
                statsCustomRange.clipDateBasedOnGranularity(START_DATE_STRING, WEEK_OF_THE_YEAR),
                "2018-09"
        )
    }

    @Test
    fun assertClipDateBasedOnGranularityWeekDate() {
        statsCustomRange.granularity = OrderStatsApiUnit.WEEK
        Assert.assertEquals(statsCustomRange.clipDateBasedOnGranularity(
                START_DATE_STRING, WEEK_OF_THE_YEAR),
                "2018-W48"
        )
    }

    @Test
    fun assertClipDateBasedOnGranularityDayDate() {
        Assert.assertEquals(
                statsCustomRange.clipDateBasedOnGranularity(START_DATE_STRING, WEEK_OF_THE_YEAR),
                START_DATE_STRING
        )
    }

    companion object {
        private const val START_DATE_STRING = "2018-09-18"
        private const val END_DATE_STRING = "2018-12-18"
        private const val WEEK_OF_THE_YEAR = 48
    }

    private fun getCurrentDateInStringFormat(): String {
        return SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY).format(Date())
    }
}
