package com.example.quiz;

import android.util.ArrayMap;
import android.util.Log;


import com.example.quiz.Models.CategoryModel;
import com.example.quiz.Models.ProfileModel;
import com.example.quiz.Models.QuestionModel;
import com.example.quiz.Models.RankModel;
import com.example.quiz.Models.TestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DbQuery {
    public static FirebaseFirestore g_firestore;
    //public static FirebaseFirestore g_firestore = FirebaseFirestore.getInstance();

    public static List<CategoryModel> g_catList = new ArrayList<>();
    public static List<TestModel> g_testList = new ArrayList<>();
    public static List<RankModel> g_usersList = new ArrayList<>();
    public static int g_usersCount = 0;
    public static boolean isCurrentUserInTop20 = false;
    public static int g_selected_cat_index = 0;
    public static ProfileModel myProfile = new ProfileModel("NA", null, null, 0);
    public static int g_selected_test_index =0;
    public static List<QuestionModel> g_questionList = new ArrayList<>();
    public static List<String> g_bmIdList = new ArrayList<>();
    public static List<QuestionModel> g_bookmarksList = new ArrayList<>();

    public static long startTime;
    public static RankModel myPerformance = new RankModel("NULL", 0, -1, 0);
    static int var;

    /**
     * createUserData
     * Purpose:
     * This method initializes a new user’s data in Firestore when they first sign up or log in.
     * It creates a document in the "USERS" collection with basic profile information and
     * default values for score and bookmarks. It also increments the global user count.
     * Why this method is used:
     * - Ensures every new user has a properly structured Firestore document.
     * - Sets default values for TOTAL_SCORE and BOOKMARKS so the app can safely reference them later.
     * - Updates the "TOTAL_USERS" document to keep track of how many users exist in the system.
     * - Uses a Firestore batch write so both operations (user creation + count increment) succeed together.
     * How it works:
     * 1. Creates a Map (userData) with the following fields:
     *    - EMAIL_ID: the user’s email address.
     *    - NAME: the user’s display name.
     *    - TOTAL_SCORE: initialized to 0 (no progress yet).
     *    - BOOKMARKS: initialized to 0 (no saved questions yet).
     * 2. Creates a DocumentReference (userDoc) pointing to the new user’s UID in the "USERS" collection.
     * 3. Starts a Firestore WriteBatch:
     *    - Adds the userData map to the userDoc (creates the user document).
     *    - Updates the "TOTAL_USERS" document by incrementing the COUNT field by 1.
     * 4. Commits the batch:
     *    - On success: calls completeListener.onSuccess() so the UI can proceed.
     *    - On failure: calls completeListener.onFailure() so the UI can handle errors.
     * Notes:
     * - Using a batch ensures that the operations are treated as single unit: either both succeed or both fail
     * - This prevents inconsistencies (e.g., a user document created without updating the total count).
     */
    public static void createUserData(String email, String name, MyCompleteListener completeListener) {
        // Step 1: Prepare user data
        Map<String, Object> userData = new ArrayMap<>();
        userData.put("EMAIL_ID", email);
        userData.put("NAME", name);
        userData.put("TOTAL_SCORE", 0); // default score
        userData.put("BOOKMARKS", 0);   // default bookmarks count
        //handle user if not logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            return;
        }

        // Step 2: Reference to the new user document (based on UID)
        DocumentReference userDoc = g_firestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Step 3: Start batch write
        WriteBatch batch = g_firestore.batch();
        batch.set(userDoc, userData); // create user document

        // Reference to the TOTAL_USERS document (global counter)
        DocumentReference countDoc = g_firestore.collection("USERS").document("TOTAL_USERS");
        batch.update(countDoc, "COUNT", FieldValue.increment(1)); // increment user count

        // Step 4: Commit batch
        batch.commit()
                .addOnSuccessListener(unused -> {
                    completeListener.onSuccess(); // notify success
                })
                .addOnFailureListener(e -> {
                    completeListener.onFailure(); // notify failure
                });
    }
    /**
     * loadCategories
     * Purpose:
     * This method retrieves all quiz categories from Firestore and stores them in the global
     * category list (g_catList). Each category contains its ID, name, and the number of tests
     * available under it. It ensures that the app has the latest category data whenever a user
     * logs in successfully.
     * Why this method is used:
     * - Categories define the structure of the quiz app (e.g., Road rules, Vehicle controls, etc).
     * - Each category contains multiple tests, so loading them is essential for navigation.
     * - Ensures the app dynamically adapts to changes in Firestore (new categories, updated names).
     * - Provides a clean, centralized way to initialize categories after login.
     * How it works:
     * 1. Clears the existing g_catList to avoid duplicates.
     * 2. Fetches the "Categories" document from the QUIZ collection.
     *    - This document contains:
     *      - COUNT: total number of categories.
     *      - CAT1_ID, CAT2_ID, … : IDs of each category document.
     * 3. Reads the COUNT field to determine how many categories exist.
     * 4. Loops from 1 to COUNT:
     *    - Retrieves each category ID (CATi_ID).
     *    - Fetches the corresponding category document from Firestore.
     *    - Extracts:
     *      - NAME: the category’s display name.
     *      - No_Of_Tests: how many tests belong to this category.
     *    - Creates a CategoryModel object and adds it to g_catList.
     * 5. Uses an AtomicInteger (loadedCount) to track how many categories have finished loading.
     *    - Only calls completeListener.onSuccess() once, after all categories are loaded.
     * 6. Handles errors gracefully:
     *    - If the "Categories" document doesn’t exist or COUNT <= 0 → calls onFailure().
     *    - If any category document fails to load → calls onFailure().
     * Notes:
     * - This method is asynchronous: Firestore queries run in parallel for each category.
     * - AtomicInteger ensures thread-safe counting of loaded categories.
     * - Called immediately after user login to prepare the app’s category navigation.
     */
    public static void loadCategories(final MyCompleteListener completeListener) {
        // ✅ Clear old categories before loading new ones
        g_catList.clear();

        g_firestore.collection("QUIZ").document("Categories").get()
                .addOnSuccessListener(catListDoc -> {
                    if (catListDoc == null || !catListDoc.exists()) {
                        completeListener.onFailure();
                        return;
                    }

                    Long catCountValue = catListDoc.getLong("COUNT");
                    if (catCountValue == null || catCountValue <= 0) {
                        completeListener.onFailure();
                        return;
                    }

                    // Counter to track how many categories have finished loading
                    AtomicInteger loadedCount = new AtomicInteger(0);

                    for (int i = 1; i <= catCountValue; i++) {
                        String catID = catListDoc.getString("CAT" + i + "_ID");
                        if (catID == null) continue;

                        g_firestore.collection("QUIZ").document(catID).get()
                                .addOnSuccessListener(catDoc -> {
                                    if (catDoc != null && catDoc.exists()) {
                                        String catName = catDoc.getString("NAME");
                                        Long noOfTestsLong = catDoc.getLong("No_Of_Tests");
                                        int noOfTests = (noOfTestsLong != null) ? noOfTestsLong.intValue() : 0;

                                        g_catList.add(new CategoryModel(catID, noOfTests, catName));
                                    }

                                    // ✅ Only call onSuccess once, when all categories are loaded
                                    if (loadedCount.incrementAndGet() == catCountValue) {
                                        completeListener.onSuccess();
                                    }
                                })
                                .addOnFailureListener(e -> completeListener.onFailure());
                    }
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }




    /**
     * loadQuestions
     * Purpose:
     * This method retrieves all quiz questions for the currently selected category and test
     * from Firestore. It builds a list of QuestionModel objects that represent each question,
     * its options, correct answers, and bookmark status.
     * Why this method is used:
     * - Provides the actual content of the quiz (questions + answers).
     * - Ensures questions are dynamically loaded from Firestore instead of being hardcoded.
     * - Supports multiple question types (single choice, multiple choice, true/false).
     * - Integrates bookmark functionality so users can save/revisit questions.
     * How it works:
     * 1. Clears the existing g_questionList to avoid duplicates.
     * 2. Queries the "Questions" collection in Firestore:
     *    - Filters by CATEGORY (based on the selected category index).
     *    - Filters by TEST (based on the selected test index).
     *    - Returns all matching documents.
     * 3. For each question document:
     *    - Extracts fields:
     *      - QUESTION: the question text.
     *      - A, B, C, D: the four possible options.
     *      - QUESTION_TYPE: defines the type (e.g., single_choice, multiple_choice).
     *      - ANSWER: stored as an array of numbers (indices of correct options).
     *    - Converts ANSWER into a List<Integer> (handles both numeric and string values safely).
     *    - Checks if the question ID exists in g_bmIdList (bookmark list).
     *      - If yes → marks isBookmarked = true.
     * 4. Creates a new QuestionModel object with:
     *    - Question text and options (defaulting to empty strings if null).
     *    - Correct answers list.
     *    - Question type (defaults to "single_choice" if missing).
     *    - Initial state = NOT_VISITED (user hasn’t answered yet).
     *    - Bookmark status.
     *    - Firestore document ID.
     * 5. Adds each QuestionModel to g_questionList.
     * 6. Calls completeListener.onSuccess() once all questions are loaded.
     * 7. If the query fails, calls completeListener.onFailure().
     * Notes:
     * - This method is asynchronous: Firestore query runs in the background.
     * - Bookmark integration ensures consistency between saved questions and quiz display.
     * - Called whenever a user starts a test, so the app always loads fresh questions.
     */
    public static void loadQuestions(final MyCompleteListener completeListener) {
        // Step 1: Clear old questions before loading new ones
        g_questionList.clear();

        // Step 2: Query Firestore for questions in the selected category and test
        g_firestore.collection("Questions")
                .whereEqualTo("CATEGORY", g_catList.get(g_selected_cat_index).getDocID())
                .whereEqualTo("TEST", g_testList.get(g_selected_test_index).getTestID())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Step 3: Iterate through each question document
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String question = doc.getString("QUESTION");
                        String optionA = doc.getString("A");
                        String optionB = doc.getString("B");
                        String optionC = doc.getString("C");
                        String optionD = doc.getString("D");
                        String questionType = doc.getString("QUESTION_TYPE");

                        // Step 4: Parse ANSWER field into a list of integers
                        List<?> rawAnswers = (List<?>) doc.get("ANSWER");
                        List<Integer> correctAnswers = new ArrayList<>();
                        if (rawAnswers != null) {
                            for (Object obj : rawAnswers) {
                                if (obj instanceof Number) {
                                    correctAnswers.add(((Number) obj).intValue());
                                } else {
                                    try {
                                        correctAnswers.add(Integer.parseInt(obj.toString()));
                                    } catch (NumberFormatException e) {
                                        // ignore invalid values
                                    }
                                }
                            }
                        }

                        // Step 5: Check bookmark status
                        boolean isBookmarked = g_bmIdList.contains(doc.getId());

                        // Step 6: Create QuestionModel and add to list
                        g_questionList.add(new QuestionModel(
                                question == null ? "" : question,
                                optionA == null ? "" : optionA,
                                optionB == null ? "" : optionB,
                                optionC == null ? "" : optionC,
                                optionD == null ? "" : optionD,
                                correctAnswers,
                                questionType == null ? "single_choice" : questionType,
                                QuestionModel.NOT_VISITED,
                                isBookmarked,
                                doc.getId()
                        ));
                    }

                    // Step 7: Notify success after all questions are loaded
                    completeListener.onSuccess();

                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }




    /**
     * loadTestData
     * Purpose:
     * Loads all tests for the currently selected category from Firestore. Each test has an ID
     * and a time limit. This prepares the g_testList so the user can select and attempt tests.
     * Why this method is used:
     * - Ensures the app dynamically loads tests for each category.
     * - Provides test IDs and time limits needed for quiz functionality.
     * - Clears old test data before loading new ones to avoid duplication.
     * How it works:
     * 1. Clears g_testList.
     * 2. Fetches the "TESTS_INFO" document inside the selected category’s TESTS_LIST subcollection.
     * 3. Reads the number of tests (from g_catList).
     * 4. Loops through each test index:
     *    - Retrieves TESTi_ID and TESTi_TIME.
     *    - Creates a TestModel with testId, default topScore = 0, and time limit.
     *    - Adds it to g_testList.
     * 5. Calls completeListener.onSuccess() once all tests are loaded.
     * 6. Calls onFailure() if Firestore query fails.
     */
    public static void loadTestData(final MyCompleteListener completeListener) {
        g_testList.clear();

        g_firestore.collection("QUIZ")
                .document(g_catList.get(g_selected_cat_index).getDocID())
                .collection("TESTS_LIST").document("TESTS_INFO")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int noOfTests = g_catList.get(g_selected_cat_index).getNoOfTests();
                    for (int i = 1; i <= noOfTests; i++) {
                        String testId = documentSnapshot.getString("TEST" + i + "_ID");
                        Long testTimeLong = documentSnapshot.getLong("TEST" + i + "_TIME");

                        if (testId != null && testTimeLong != null) {
                            g_testList.add(new TestModel(testId, 0, testTimeLong.intValue()));
                        }
                    }
                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }

    /**
     * getUserData
     * Purpose:
     * Loads the current user’s profile and performance data from Firestore. This includes
     * name, email, phone, bookmarks count, and TOTAL_SCORE. Updates both myProfile and
     * myPerformance objects so the app can display user info consistently.
     * Why this method is used:
     * - Ensures the app has the latest user profile data after login.
     * - Keeps myPerformance in sync with Firestore’s TOTAL_SCORE.
     * - Provides bookmark count for Saved Questions feature.
     * How it works:
     * 1. Fetches the current user’s document from the USERS collection.
     * 2. Updates myProfile with:
     *    - NAME
     *    - EMAIL_ID
     *    - PHONE (if present)
     *    - BOOKMARKS count
     * 3. Updates myPerformance with:
     *    - NAME
     *    - TOTAL_SCORE → stored in overallScore (not score).
     *    - Rank will be updated later via getTopUsers().
     * 4. Calls completeListener.onSuccess() if successful.
     * 5. Calls onFailure() if Firestore query fails.
     */
    public static void getUserData(MyCompleteListener completeListener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            completeListener.onFailure();
            return;
        }
        DocumentReference userDoc = g_firestore.collection("USERS").document(user.getUid());
                userDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                    myProfile.setName(documentSnapshot.getString("NAME"));
                    myProfile.setEmail(documentSnapshot.getString("EMAIL_ID"));

                    if (documentSnapshot.getString("PHONE") != null)
                        myProfile.setPhone(documentSnapshot.getString("PHONE"));
                    Long bookmarks = documentSnapshot.getLong("BOOKMARKS");
                    if(bookmarks != null)
                        myProfile.setBookmarksCount(bookmarks.intValue());

                    Long totalScore = documentSnapshot.getLong("TOTAL_SCORE");
                    if(totalScore != null)
                        myPerformance.setOverallScore(totalScore.intValue());

                    myPerformance.setName(documentSnapshot.getString("NAME"));

                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }

    /**
     * saveProfileData
     * Purpose:
     * Updates the current user’s profile information (name and phone) in Firestore.
     * Also updates the local myProfile object so the app reflects changes immediately.
     * Why this method is used:
     * - Allows users to edit their profile details.
     * - Keeps Firestore and local cache (myProfile) in sync.
     * How it works:
     * 1. Creates a map (profileData) with updated fields:
     *    - NAME (always updated).
     *    - PHONE (only if provided).
     * 2. Updates the user’s Firestore document with these values.
     * 3. On success:
     *    - Updates myProfile locally with the new name and phone.
     *    - Calls completeListener.onSuccess().
     * 4. On failure:
     *    - Calls completeListener.onFailure().
     */
    public static void saveProfileData(String name, String phone, MyCompleteListener completeListener) {
       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       if(user == null){
           completeListener.onFailure();
           return;
       }
        Map<String, Object> profileData = new ArrayMap<>();
        profileData.put("NAME", name);
        if (phone != null)
            profileData.put("PHONE", phone);

        g_firestore.collection("USERS").document(user.getUid())
                .update(profileData)
                .addOnSuccessListener(unused -> {
                    myProfile.setName(name);
                    if (phone != null)
                        myProfile.setPhone(phone);
                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }
    /**
     * loadBmIds
     * Purpose:
     * Loads the IDs of all bookmarked questions for the current user from Firestore.
     * These IDs are stored in g_bmIdList for later use (e.g., loading full question details).
     * Why this method is used:
     * - Provides a lightweight way to fetch only bookmark IDs before loading full questions.
     * - Reduces initial data transfer by not fetching entire question documents immediately.
     * How it works:
     * 1. Clears g_bmIdList to avoid duplicates.
     * 2. Fetches the "BOOKMARKS" document inside the user’s USER_DATA subcollection.
     * 3. Reads the total bookmark count from myProfile.
     * 4. Loops through each bookmark index:
     *    - Retrieves BM{i}_ID from Firestore.
     *    - Adds it to g_bmIdList.
     * 5. Calls completeListener.onSuccess() once all IDs are loaded.
     * 6. Calls onFailure() if Firestore query fails.
     * Notes:
     * - This method only loads IDs, not full question data.
     * - Full question details are loaded later in loadBookMarks().
     */
    public static void loadBmIds(MyCompleteListener completeListener) {
        g_bmIdList.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            completeListener.onFailure();
            return;
        }

        g_firestore.collection("USERS").document(user.getUid())
                .collection("USER_DATA").document("BOOKMARKS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int count = myProfile.getBookmarksCount();
                    for (int i = 0; i < count; i++) {
                        String bmID = documentSnapshot.getString("BM" + (i + 1) + "_ID");
                        g_bmIdList.add(bmID);
                    }
                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }

    /**
     * loadBookMarks
     * Purpose:
     * Loads full question details for all bookmarked questions using IDs from g_bmIdList.
     * Builds a list of QuestionModel objects (g_bookmarksList) for display in the Saved Questions screen.
     * Why this method is used:
     * - Allows users to revisit and practice bookmarked questions.
     * - Ensures bookmark list stays consistent with Firestore data.
     * How it works:
     * 1. Clears g_bookmarksList.
     * 2. Iterates through each bookmark ID in g_bmIdList.
     * 3. For each ID:
     *    - Fetches the corresponding question document from Firestore.
     *    - Checks if the document exists (question may have been deleted).
     *    - Parses the ANSWER field into a List<Integer>.
     *    - Creates a QuestionModel with:
     *      - Question text, options A–D, correct answers, type, NOT_VISITED state, and doc ID.
     *    - Adds it to g_bookmarksList.
     * 4. Uses a counter (var) to track how many questions have been loaded.
     *    - Calls completeListener.onSuccess() once all bookmarks are loaded.
     * 5. Calls onFailure() if any query fails.
     * Notes:
     * - Bookmarked questions are marked as NOT_VISITED initially.
     * - If a question no longer exists in Firestore, it is skipped.
     */
    public static void loadBookMarks(MyCompleteListener completeListener) {
        g_bookmarksList.clear();
        var = 0;
        if(g_bmIdList.isEmpty())
            completeListener.onSuccess();

        for (int i = 0; i < g_bmIdList.size(); i++) {
            String docID = g_bmIdList.get(i);

            g_firestore.collection("Questions").document(docID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Parse correct answers
                            List<?> rawAnswers = (List<?>) documentSnapshot.get("ANSWER");
                            List<Integer> correctAnswers = new ArrayList<>();
                            if (rawAnswers != null) {
                                for (Object obj : rawAnswers) {
                                    if (obj instanceof Number) {
                                        correctAnswers.add(((Number) obj).intValue());
                                    } else {
                                        try {
                                            correctAnswers.add(Integer.parseInt(obj.toString()));
                                        } catch (NumberFormatException e) {
                                            // ignore invalid values
                                        }
                                    }
                                }
                            }

                            // Add question to bookmarks list
                            g_bookmarksList.add(new QuestionModel(
                                    documentSnapshot.getString("QUESTION"),
                                    documentSnapshot.getString("A"),
                                    documentSnapshot.getString("B"),
                                    documentSnapshot.getString("C"),
                                    documentSnapshot.getString("D"),
                                    correctAnswers,
                                    documentSnapshot.getString("QUESTION_TYPE"),
                                    QuestionModel.NOT_VISITED,
                                    false, // bookmarked flag not needed here
                                    documentSnapshot.getId()
                            ));
                        }

                        // Track progress
                        var++;
                        if (var == g_bmIdList.size()) {
                            completeListener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> completeListener.onFailure());
        }
    }


    /**
     * getTopUsers
     * Purpose:
     * Retrieves the top 20 users from Firestore based on TOTAL_SCORE and stores them in g_usersList.
     * Also checks if the current user is in the top 20 and updates myPerformance accordingly.
     * Why this method is used:
     * - Powers the leaderboard by showing the highest scoring users.
     * - Updates myPerformance with rank and score if the current user is in the top 20.
     * How it works:
     * 1. Clears g_usersList to avoid duplicates.
     * 2. Queries Firestore for users with TOTAL_SCORE > 0, ordered descending by TOTAL_SCORE.
     * 3. Limits results to top 20 users.
     * 4. Iterates through each user document:
     *    - Creates a RankModel with name, TOTAL_SCORE, rank, and overallScore.
     *    - Adds it to g_usersList.
     *    - If the document belongs to the current user:
     *      - Sets isMe0nTopList = true.
     *      - Updates myPerformance with rank and TOTAL_SCORE.
     * 5. Calls completeListener.onSuccess() when done.
     * 6. Calls onFailure() if Firestore query fails.

     */
    public static void getTopUsers(MyCompleteListener completeListener) {
        g_usersList.clear();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not signed in
            completeListener.onFailure();
            return;
        }
        String myUID = currentUser.getUid();

        g_firestore.collection("USERS")
                .whereGreaterThan("TOTAL_SCORE", 0)
                .orderBy("TOTAL_SCORE", Query.Direction.DESCENDING)
                .limit(20) // only check top 20 to preserve memory
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Long totalScoreLong = doc.getLong("TOTAL_SCORE");

                        int totalScore;
                        if(totalScoreLong != null){
                            totalScore = totalScoreLong.intValue();
                        } else{
                            totalScore = 0;
                        }
                        g_usersList.add(new RankModel(
                                doc.getString("NAME"),
                                totalScore,
                                rank,
                                totalScore // overallScore set equal to TOTAL_SCORE
                        ));

                        // ✅ Update myPerformance if current user is in top 20
                        if (myUID.compareTo(doc.getId()) == 0) {
                            isCurrentUserInTop20 = true;
                            myPerformance.setRank(rank);
                            myPerformance.setOverallScore(totalScore);
                            myPerformance.setName(doc.getString("NAME"));
                        }
                        rank++;
                    }
                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }
    /**
     * getUsersCount
     * Purpose:
     * Retrieves the total number of users from Firestore and stores it in g_usersCount.
     * Why this method is used:
     * - Needed for rank calculation when the current user is not in the top 20.
     * - Provides context for leaderboard scaling.
     * How it works:
     * 1. Fetches the "TOTAL_USERS" document from the USERS collection.
     * 2. Reads the COUNT field and stores it in g_usersCount.
     * 3. Calls completeListener.onSuccess() if successful.
     * 4. Calls onFailure() if Firestore query fails.
     */


    public static void getUsersCount(MyCompleteListener completeListener)
    {
        g_firestore.collection("USERS").document("TOTAL_USERS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long countLong = documentSnapshot.getLong("COUNT");
                    if (countLong != null) {
                        g_usersCount = countLong.intValue();
                    } else {
                        g_usersCount = 0;
                    }

                    /*g_usersCount = (countLong != null) ? countLong.intValue() : 0;
                    * you can replace the if else statement with this ternary operator*/
                    completeListener.onSuccess();

                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }
    /**
     * loadData
     * Purpose:
     * Loads all essential data for the app after login:
     * - Categories
     * - User profile and performance
     * - Total user count
     * - Bookmark IDs
     * Why this method is used:
     * - Provides a single entry point to initialize the app’s data.
     * - Ensures categories, user data, and bookmarks are loaded in sequence.
     * How it works:
     * 1. Calls loadCategories().
     *    - On success → calls getUserData().
     *    - On failure → calls completeListener.onFailure().
     * 2. Calls getUserData().
     *    - On success → calls getUsersCount().
     *    - On failure → calls completeListener.onFailure().
     * 3. Calls getUsersCount().
     *    - On success → calls loadBmIds().
     *    - On failure → calls completeListener.onFailure().
     * 4. Calls loadBmIds().
     *    - On success → calls completeListener.onSuccess().
     *    - On failure → calls completeListener.onFailure().
     * Notes:
     * - This method orchestrates multiple data-loading calls.
     * - Since getUserData now correctly sets myPerformance.overallScore,
     *   loadData will initialize both profile and performance consistently.
     */
    public static void loadData(MyCompleteListener completeListener) {
        loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                getUserData(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        getUsersCount(new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                loadBmIds(completeListener);
                            }

                            @Override
                            public void onFailure() {
                                completeListener.onFailure();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        completeListener.onFailure();
                    }
                });
            }

            @Override
            public void onFailure() {
                completeListener.onFailure();
            }
        });
    }

    /**
     * loadMyScores
     * Purpose:
     * Loads the user's saved test scores from Firestore. Each test has a "top score"
     * (the highest score the user has achieved for that test). These scores are stored in the
     * "MY_SCORES" document under USER_DATA.
     * Why this method is used:
     * - Ensures that when the app starts or refreshes, the user's progress for each test is
     *   correctly loaded into memory (g_testList).
     * - Allows the TestAdapter to display accurate progress bars for each test.
     * - Provides the data needed to calculate the user's TOTAL_SCORE (sum of all top scores),
     *   which is used for ranking and account statistics.
     * How it works:
     * 1. Queries Firestore for the "MY_SCORES" document belonging to the current user.
     * 2. If the document exists:
     *    - Iterates through all tests in g_testList.
     *    - For each test, retrieves its saved top score (if any) and updates the local model.
     * 3. If the document does not exist:
     *    - Initializes all test scores to 0 (user has not attempted any tests yet).
     * 4. After loading, calculates the TOTAL_SCORE by summing all test top scores and updates
     *    myPerformance.overallScore with this value.
     * 5. Calls completeListener.onSuccess() so the UI knows loading is finished.
     * 6. On failure, logs the error, resets scores to 0, and calls completeListener.onFailure().
     */
    public static void loadMyScores(MyCompleteListener completeListener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // If user is not signed in, scores can't load
            completeListener.onFailure();
            return;
        }
        String myUID = currentUser.getUid();

        g_firestore.collection("USERS")
                .document(myUID)
                .collection("USER_DATA").document("MY_SCORES")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, load scores
                        for (int i = 0; i < g_testList.size(); i++) {
                            int top = 0;
                            Long scoreLong = documentSnapshot.getLong(g_testList.get(i).getTestID());
                            if (scoreLong != null) {
                                top = scoreLong.intValue();
                            }
                            g_testList.get(i).setTopScore(top);
                        }
                    } else {
                        // No scores yet, set all top scores to 0
                        for (int i = 0; i < g_testList.size(); i++) {
                            g_testList.get(i).setTopScore(0);
                        }
                    }

                    // Calculate TOTAL_SCORE (sum of all top scores)
                    int total = 0;
                    for (TestModel test : g_testList) {
                        total += test.getTopScore();
                    }
                    myPerformance.setOverallScore(total);

                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("DbQuery", "Error loading scores", e);
                    for (int i = 0; i < g_testList.size(); i++) {
                        g_testList.get(i).setTopScore(0);
                    }
                    myPerformance.setOverallScore(0);
                    completeListener.onFailure();
                });
    }

    /**
     * saveResult
     * Purpose:
     * Saves the result of a completed test into Firestore. Updates:
     * - The user's bookmarks.
     * - The user's TOTAL_SCORE (sum of all top scores).
     * - The user's per-test top score (only if the new score is higher).
     * Why this method is used:
     * - Ensures user progress is persisted in Firestore.
     * - Keeps both per-test progress and overall leaderboard ranking consistent.
     * - Uses a Firestore batch write so all updates succeed or fail together.
     * How it works:
     * 1. Creates a batch write operation.
     * 2. Updates the BOOKMARKS document with the current list.
     * 3. Updates the USER document with TOTAL_SCORE and bookmark count.
     * 4. If the new score > saved top score, updates MY_SCORES for that test.
     * 5. Commits the batch:
     *    - On success: updates local cache (g_testList and myPerformance).
     *    - On failure: calls completeListener.onFailure().
     * Notes:
     * - ✅ FIX: Use myPerformance.setOverallScore() instead of setScore().
     * - This ensures AccountFragment and LeaderboardFragment display the correct score.
     */
    public static void saveResult(int score, MyCompleteListener completeListener) {
        WriteBatch batch = g_firestore.batch();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            completeListener.onFailure();
            return;
        }

        // Bookmarks
        Map<String, Object> bmData = new ArrayMap<>();
        for (int i = 0; i < g_bmIdList.size(); i++) {
            bmData.put("BM" + (i + 1) + "_ID", g_bmIdList.get(i));
        }
        DocumentReference bmDoc = g_firestore.collection("USERS")
                .document(currentUser.getUid())
                .collection("USER_DATA").document("BOOKMARKS");
        batch.set(bmDoc, bmData);

        // Reference to the user document
        DocumentReference userDoc = g_firestore.collection("USERS")
                .document(currentUser.getUid());

        // Update per-test top score if needed
        if (score > g_testList.get(g_selected_test_index).getTopScore()) {
            DocumentReference scoreDoc = userDoc.collection("USER_DATA").document("MY_SCORES");
            Map<String, Object> testData = new HashMap<>();
            testData.put(g_testList.get(g_selected_test_index).getTestID(), score);
            batch.set(scoreDoc, testData, SetOptions.merge());
        }

        // ✅ Calculate TOTAL_SCORE
        final int total = g_testList.stream()
                .mapToInt(TestModel::getTopScore)
                .sum()
                + (score > g_testList.get(g_selected_test_index).getTopScore()
                ? score - g_testList.get(g_selected_test_index).getTopScore()
                : 0);

        Map<String, Object> userData = new ArrayMap<>();
        userData.put("TOTAL_SCORE", total);
        userData.put("BOOKMARKS", g_bmIdList.size());
        batch.update(userDoc, userData);

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(unused -> {
                    if (score > g_testList.get(g_selected_test_index).getTopScore()) {
                        g_testList.get(g_selected_test_index).setTopScore(score);
                    }
                    if (myPerformance != null) {
                        myPerformance.setOverallScore(total); // FIX: use overallScore
                    }
                    completeListener.onSuccess();
                })
                .addOnFailureListener(e -> completeListener.onFailure());
    }

}
