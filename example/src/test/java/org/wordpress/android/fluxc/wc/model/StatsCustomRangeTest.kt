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
    fun assertValidStringDatesWhenGranularityIsDay() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY)

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), START_DATE_STRING)
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), END_DATE_STRING)
    }

    @Test
    fun assertValidStringDatesWhenGranularityIsWeek() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK)

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), "2018-W48")
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), "2018-W48")
    }

    @Test
    fun assertValidStringDatesWhenGranularityIsMonth() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.MONTH)

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), "2018-09")
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), "2018-12")
    }

    @Test
    fun assertValidStringDatesWhenGranularityIsYear() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.YEAR)

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), "2018")
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), "2018")
    }

    @Test
    fun assertValidStringDatesWhenGranularityIsCustom() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.CUSTOM)

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), START_DATE_STRING)
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), END_DATE_STRING)
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
    fun assertCheckForSwitchedDatesActualSwitchedDates() {
        // NOTE: The Granularity is irrelevant for this test
        statsCustomRange = Mockito.spy(StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY))

        val oldStartDate = statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR)
        val oldEndDate = statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR)

        statsCustomRange.endDate = formatter.parse(START_DATE_STRING)
        statsCustomRange.startDate = formatter.parse(END_DATE_STRING)
        statsCustomRange.checkForSwitchedDates()

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), oldStartDate)
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), oldEndDate)
    }

    @Test
    fun assertCheckForSwitchedDatesNonSwitchedDates() {
        // NOTE: The Granularity is irrelevant for this test
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY)

        statsCustomRange.checkForSwitchedDates()

        Assert.assertEquals(statsCustomRange.getStartDateAsStringForGranularity(WEEK_OF_THE_YEAR), START_DATE_STRING)
        Assert.assertEquals(statsCustomRange.getEndDateAsStringForGranularity(WEEK_OF_THE_YEAR), END_DATE_STRING)
    }

    @Test
    fun assertClipDateBasedOnGranularityYearDate() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.YEAR)
        Assert.assertEquals(
                statsCustomRange.clipDateBasedOnGranularity(START_DATE_STRING, WEEK_OF_THE_YEAR),
                "2018")
    }

    @Test
    fun assertClipDateBasedOnGranularityMonthDate() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.MONTH)
        Assert.assertEquals(
                statsCustomRange.clipDateBasedOnGranularity(START_DATE_STRING, WEEK_OF_THE_YEAR),
                "2018-09"
        )
    }

    @Test
    fun assertClipDateBasedOnGranularityWeekDateWeekBiggerThan10() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK)
        Assert.assertEquals(statsCustomRange.clipDateBasedOnGranularity(
                START_DATE_STRING, WEEK_OF_THE_YEAR),
                "2018-W48"
        )
    }

    @Test
    fun assertClipDateBasedOnGranularityWeekDateWeekSmallerThan10() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.WEEK)
        Assert.assertEquals(statsCustomRange.clipDateBasedOnGranularity(
                START_DATE_STRING, 1),
                "2018-W01"
        )
    }


    @Test
    fun assertClipDateBasedOnGranularityDayDate() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.DAY)
        Assert.assertEquals(
                statsCustomRange.clipDateBasedOnGranularity(START_DATE_STRING, WEEK_OF_THE_YEAR),
                START_DATE_STRING
        )
    }

    @Test
    fun assertClipDateBasedOnGranularityCustomDate() {
        statsCustomRange = StatsCustomRange(startDate!!, endDate!!, OrderStatsApiUnit.CUSTOM)
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
}
