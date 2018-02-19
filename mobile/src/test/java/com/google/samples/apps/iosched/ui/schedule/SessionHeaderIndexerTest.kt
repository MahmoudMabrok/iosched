/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.iosched.ui.schedule

import com.google.samples.apps.iosched.model.TestData
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class SessionHeaderIndexerTest {

    @Test
    fun indexSessions_groupsCorrectly() {
        // Given a list of test sessions with a range of start times (on the hour)
        val session = TestData.session1
        val start = ZonedDateTime.parse("2018-05-08T07:00:00-07:00")
        val sessions = listOf(
            session.copy(startTime = start),
            session.copy(startTime = start),
            session.copy(startTime = start.plusHours(1)),
            session.copy(startTime = start.plusHours(1)),
            session.copy(startTime = start.plusHours(2)),
            session.copy(startTime = start.plusHours(2))
        )

        // Process this list to group by start time, keyed on index
        val grouped = indexSessionHeaders(sessions).toMap()

        // Then verify that the correct groupings are made and indexes used
        assertEquals(3, grouped.size)
        assertEquals(setOf(0, 2, 4), grouped.keys)
        assertEquals(start, grouped[0])
        assertEquals(start.plusHours(1), grouped[2])
        assertEquals(start.plusHours(2), grouped[4])
    }

    @Test
    fun indexSessions_roundsTimeDown() {
        // Given a list of test sessions with start times not on the hour
        val session = TestData.session1
        val start = ZonedDateTime.parse("2018-05-08T07:00:00-07:00")
        val sessions = listOf(
            session.copy(startTime = start),
            session.copy(startTime = start.plusMinutes(10)),
            session.copy(startTime = start.plusMinutes(20)),
            session.copy(startTime = start.plusMinutes(30)),
            session.copy(startTime = start.plusMinutes(40)),
            session.copy(startTime = start.plusMinutes(50)),
            session.copy(startTime = start.plusMinutes(60)),
            session.copy(startTime = start.plusMinutes(70)),
            session.copy(startTime = start.plusMinutes(80)),
            session.copy(startTime = start.plusMinutes(90))
        )

        // Process this list to group by start time, keyed on index
        val grouped = indexSessionHeaders(sessions).toMap()

        // Then verify that the correct groupings are made, rounding down the time to the hour
        assertEquals(2, grouped.size)
        assertEquals(setOf(0, 6), grouped.keys)
        assertEquals(start, grouped[0])
        assertEquals(start.plusHours(1), grouped[6])
    }

    @Test
    fun indexSessions_acrossDays() {
        // Given a list of test sessions which cross into a new day
        val session = TestData.session1
        val start = ZonedDateTime.parse("2018-05-08T22:00:00-07:00")
        val sessions = listOf(
            session.copy(startTime = start),                // 10PM
            session.copy(startTime = start.plusHours(1)),   // 11PM
            session.copy(startTime = start.plusHours(2)),   // Midnight
            session.copy(startTime = start.plusHours(3)),   // 1AM
            session.copy(startTime = start.plusHours(4))    // 2AM
        )

        // Process this list to group by start time, keyed on index
        val grouped = indexSessionHeaders(sessions).toMap()

        // Then verify that the correct groupings are made and in the correct order
        assertEquals(5, grouped.size)
        assertEquals(setOf(0, 1, 2, 3, 4), grouped.keys)
        assertEquals(start, grouped[0])
        assertEquals(start.plusHours(1), grouped[1])
        assertEquals(start.plusHours(2), grouped[2])
        assertEquals(start.plusHours(3), grouped[3])
        assertEquals(start.plusHours(4), grouped[4])
    }
}
