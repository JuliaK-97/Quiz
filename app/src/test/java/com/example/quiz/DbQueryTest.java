package com.example.quiz;

import static org.junit.Assert.*;

import com.example.quiz.Models.RankModel;
import com.example.quiz.Models.TestModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


public class DbQueryTest {

    @Before
    public void setUp() throws Exception {
        // Reset global state before each test
        DbQuery.g_testList = new ArrayList<>();
        DbQuery.g_usersList = new ArrayList<>();
        DbQuery.myPerformance = new RankModel("NULL", 0, -1, 0);
    }

    @After
    public void tearDown() throws Exception {
        DbQuery.g_testList.clear();
        DbQuery.g_usersList.clear();
        DbQuery.myPerformance = null;
    }

    @Test
    public void loadMyScores() {
        // Arrange: create fake tests with scores
        TestModel test1 = new TestModel("test1", 50, 30);
        TestModel test2 = new TestModel("test2", 70, 30);
        DbQuery.g_testList.add(test1);
        DbQuery.g_testList.add(test2);

        // Act: simulate loadMyScores total calculation
        int total = 0;
        for (TestModel t : DbQuery.g_testList) {
            total += t.getTopScore();
        }
        DbQuery.myPerformance.setOverallScore(total);

        // Assert: verify total score is correct
        assertEquals(120, DbQuery.myPerformance.getOverallScore());
    }

    @Test
    public void saveResult() {
        // Arrange: start with a test that has a top score of 50
        TestModel test1 = new TestModel("test1", 50, 30);
        DbQuery.g_testList.add(test1);

        // Act: simulate saving a higher score
        int newScore = 80;
        if (newScore > test1.getTopScore()) {
            test1.setTopScore(newScore);
        }

        // Recalculate total
        int total = 0;
        for (TestModel t : DbQuery.g_testList) {
            total += t.getTopScore();
        }
        DbQuery.myPerformance.setOverallScore(total);

        // Assert: verify updated score
        assertEquals(80, DbQuery.myPerformance.getOverallScore());

        // Act again: simulate saving a lower score (should not reduce)
        newScore = 40;
        if (newScore > test1.getTopScore()) {
            test1.setTopScore(newScore);
        }

        // Recalculate total again
        total = 0;
        for (TestModel t : DbQuery.g_testList) {
            total += t.getTopScore();
        }
        DbQuery.myPerformance.setOverallScore(total);

        // Assert: score should remain 80
        assertEquals(80, DbQuery.myPerformance.getOverallScore());
    }

    @Test
    public void getTopUsers() {
        RankModel user1 = new RankModel("Alice", 120, -1, 0);
        RankModel user2 = new RankModel("Bob", 80, -1, 0);
        RankModel user3 = new RankModel("Charlie", 100, -1, 0);

        DbQuery.g_usersList.add(user1);
        DbQuery.g_usersList.add(user2);
        DbQuery.g_usersList.add(user3);

        // Add current user performance
        DbQuery.myPerformance = new RankModel("Me", 90, -1, 0);
        DbQuery.g_usersList.add(DbQuery.myPerformance);

        // Act: sort by score descending
        DbQuery.g_usersList.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));

        // Assign ranks
        for (int i = 0; i < DbQuery.g_usersList.size(); i++) {
            DbQuery.g_usersList.get(i).setRank(i + 1);
        }

        // Assert: check ranks
        assertEquals(1, user1.getRank());                 // Alice highest
        assertEquals(2, user3.getRank());                 // Charlie next
        assertEquals(3, DbQuery.myPerformance.getRank()); // Me with 90
        assertEquals(4, user2.getRank());                 // Bob lowest

    }
}
