package com.hamidat.nullpointersapp;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing.FollowingCallback;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FirestoreFollowingTest {

    private static FirebaseFirestore firestore;
    private static FirestoreHelper firestoreHelper;

    /**
     * Waits up to 5 seconds for the provided CountDownLatch to reach zero.
     * If the latch does not count down within the timeout period, a debug log is printed.
     *
     * @param latch the CountDownLatch to wait on.
     */
    public static void latch5seconds(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d(TAG, "Continue...");
        }
    }

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        firestore = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper();
    }

    @Test
    public void sendFriendRequestTest() {
        // Seed the database with two test users
        String user1Id = "user1";
        String user1Name = "User1";
        String user1Password = "password1";

        String user2Id = "user2";
        String user2Name = "User2";
        String user2Password = "password2";

        // Create user1 with following field initialized
        CountDownLatch user1Latch = new CountDownLatch(1);
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("username", user1Name);
        user1Data.put("password", user1Password);
        user1Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user1Id).set(user1Data);
        latch5seconds(user1Latch);

        // Create user2 with following field initialized
        CountDownLatch user2Latch = new CountDownLatch(1);
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("username", user2Name);
        user2Data.put("password", user2Password);
        user2Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user2Id).set(user2Data);
        latch5seconds(user2Latch);

        // Send friend request from user1 to user2
        CountDownLatch requestLatch = new CountDownLatch(1);
        firestoreHelper.sendFriendRequest(user1Id, user2Id, new FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                requestLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error sending friend request: " + e.getMessage());
                requestLatch.countDown();
            }
        });

        latch5seconds(requestLatch);

        // Verify the friend request was created
        CountDownLatch verificationLatch = new CountDownLatch(1);
        firestore.collection("friend_requests")
                .whereEqualTo("fromUserId", user1Id)
                .whereEqualTo("toUserId", user2Id)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    assertTrue(querySnapshot.size() > 0);
                    verificationLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Error verifying friend request: " + e.getMessage());
                    verificationLatch.countDown();
                });

        latch5seconds(verificationLatch);
    }

    @Test
    public void acceptFriendRequestTest() {
        // Seed the database with two test users
        String user1Id = "user3";
        String user1Name = "User3";
        String user1Password = "password3";

        String user2Id = "user4";
        String user2Name = "User4";
        String user2Password = "password4";

        // Create user1 with following field initialized
        CountDownLatch user1Latch = new CountDownLatch(1);
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("username", user1Name);
        user1Data.put("password", user1Password);
        user1Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user1Id).set(user1Data)
                .addOnSuccessListener(aVoid -> user1Latch.countDown())
                .addOnFailureListener(e -> {
                    fail("Error creating user1: " + e.getMessage());
                    user1Latch.countDown();
                });
        latch5seconds(user1Latch);

        // Create user2 with following field initialized
        CountDownLatch user2Latch = new CountDownLatch(1);
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("username", user2Name);
        user2Data.put("password", user2Password);
        user2Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user2Id).set(user2Data)
                .addOnSuccessListener(aVoid -> user2Latch.countDown())
                .addOnFailureListener(e -> {
                    fail("Error creating user2: " + e.getMessage());
                    user2Latch.countDown();
                });
        latch5seconds(user2Latch);

        // Send friend request from user1 to user2
        CountDownLatch requestLatch = new CountDownLatch(1);
        firestoreHelper.sendFriendRequest(user1Id, user2Id, new FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                requestLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error sending friend request: " + e.getMessage());
                requestLatch.countDown();
            }
        });

        latch5seconds(requestLatch);

        // Accept the friend request
        CountDownLatch acceptLatch = new CountDownLatch(1);
        firestore.collection("friend_requests")
                .whereEqualTo("fromUserId", user1Id)
                .whereEqualTo("toUserId", user2Id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String requestId = querySnapshot.getDocuments().get(0).getId();
                        firestoreHelper.acceptFriendRequest(requestId, new FollowingCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                // Verify following relationship was established
                                firestore.collection("users").document(user1Id).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            ArrayList<String> following = (ArrayList<String>) documentSnapshot.get("following");
                                            assertTrue(following.contains(user2Id));

                                            firestore.collection("users").document(user2Id).get()
                                                    .addOnSuccessListener(documentSnapshot2 -> {
                                                        ArrayList<String> following2 = (ArrayList<String>) documentSnapshot2.get("following");
                                                        assertTrue(following2.contains(user1Id));
                                                        acceptLatch.countDown();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        fail("Error getting user2 following list: " + e.getMessage());
                                                        acceptLatch.countDown();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            fail("Error getting user1 following list: " + e.getMessage());
                                            acceptLatch.countDown();
                                        });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Error accepting friend request: " + e.getMessage());
                                acceptLatch.countDown();
                            }
                        });
                    } else {
                        fail("No friend request found to accept");
                        acceptLatch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    fail("Error finding friend request: " + e.getMessage());
                    acceptLatch.countDown();
                });

        latch5seconds(acceptLatch);
    }

    @Test
    public void declineFriendRequestTest() {
        // Seed the database with two test users
        String user1Id = "user5";
        String user1Name = "User5";
        String user1Password = "password5";

        String user2Id = "user6";
        String user2Name = "User6";
        String user2Password = "password6";

        // Create user1 with following field initialized
        CountDownLatch user1Latch = new CountDownLatch(1);
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("username", user1Name);
        user1Data.put("password", user1Password);
        user1Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user1Id).set(user1Data);
        latch5seconds(user1Latch);

        // Create user2 with following field initialized
        CountDownLatch user2Latch = new CountDownLatch(1);
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("username", user2Name);
        user2Data.put("password", user2Password);
        user2Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user2Id).set(user2Data);
        latch5seconds(user2Latch);

        // Send friend request from user1 to user2
        CountDownLatch requestLatch = new CountDownLatch(1);
        firestoreHelper.sendFriendRequest(user1Id, user2Id, new FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                requestLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error sending friend request: " + e.getMessage());
                requestLatch.countDown();
            }
        });

        latch5seconds(requestLatch);

        // Decline the friend request
        CountDownLatch declineLatch = new CountDownLatch(1);
        firestore.collection("friend_requests")
                .whereEqualTo("fromUserId", user1Id)
                .whereEqualTo("toUserId", user2Id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String requestId = querySnapshot.getDocuments().get(0).getId();
                        firestoreHelper.declineFriendRequest(requestId, new FollowingCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                declineLatch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Error declining friend request: " + e.getMessage());
                                declineLatch.countDown();
                            }
                        });
                    } else {
                        fail("No friend request found to decline");
                        declineLatch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    fail("Error finding friend request: " + e.getMessage());
                    declineLatch.countDown();
                });

        latch5seconds(declineLatch);

        // Verify the friend request status is declined
        CountDownLatch verificationLatch = new CountDownLatch(1);
        firestore.collection("friend_requests")
                .whereEqualTo("fromUserId", user1Id)
                .whereEqualTo("toUserId", user2Id)
                .whereEqualTo("status", "declined")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    assertTrue(querySnapshot.size() > 0);
                    verificationLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Error verifying friend request status: " + e.getMessage());
                    verificationLatch.countDown();
                });

        latch5seconds(verificationLatch);
    }

    @Test
    public void removeFollowingTest() {
        // Seed the database with two test users
        String user1Id = "user7";
        String user1Name = "User7";
        String user1Password = "password7";

        String user2Id = "user8";
        String user2Name = "User8";
        String user2Password = "password8";

        // Create user1 with following field initialized
        CountDownLatch user1Latch = new CountDownLatch(1);
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("username", user1Name);
        user1Data.put("password", user1Password);
        user1Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user1Id).set(user1Data)
                .addOnSuccessListener(aVoid -> user1Latch.countDown())
                .addOnFailureListener(e -> {
                    fail("Error creating user1: " + e.getMessage());
                    user1Latch.countDown();
                });
        latch5seconds(user1Latch);

        // Create user2 with following field initialized
        CountDownLatch user2Latch = new CountDownLatch(1);
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("username", user2Name);
        user2Data.put("password", user2Password);
        user2Data.put("following", new ArrayList<String>()); // Initialize following as empty list
        firestore.collection("users").document(user2Id).set(user2Data)
                .addOnSuccessListener(aVoid -> user2Latch.countDown())
                .addOnFailureListener(e -> {
                    fail("Error creating user2: " + e.getMessage());
                    user2Latch.countDown();
                });
        latch5seconds(user2Latch);

        // Send and accept friend request to establish following relationship
        CountDownLatch requestLatch = new CountDownLatch(1);
        firestoreHelper.sendFriendRequest(user1Id, user2Id, new FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                firestore.collection("friend_requests")
                        .document((String) result)
                        .update("status", "accepted")
                        .addOnSuccessListener(aVoid -> {
                            // Verify following relationship was established
                            firestore.collection("users").document(user1Id).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        ArrayList<String> following = (ArrayList<String>) documentSnapshot.get("following");
                                        assertTrue(following.contains(user2Id));
                                        requestLatch.countDown();
                                    })
                                    .addOnFailureListener(e -> {
                                        fail("Error verifying following after accept: " + e.getMessage());
                                        requestLatch.countDown();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            fail("Error accepting friend request: " + e.getMessage());
                            requestLatch.countDown();
                        });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error sending friend request: " + e.getMessage());
                requestLatch.countDown();
            }
        });

        latch5seconds(requestLatch);

        // Remove following relationship
        CountDownLatch removeLatch = new CountDownLatch(1);
        firestoreHelper.removeFollowing(user1Id, user2Id, new FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                removeLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error removing following: " + e.getMessage());
                removeLatch.countDown();
            }
        });

        latch5seconds(removeLatch);

        // Verify following relationship was removed
        CountDownLatch verificationLatch = new CountDownLatch(1);
        firestore.collection("users").document(user1Id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    ArrayList<String> following = (ArrayList<String>) documentSnapshot.get("following");
                    assertTrue(!following.contains(user2Id));
                    verificationLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Error verifying following removal: " + e.getMessage());
                    verificationLatch.countDown();
                });

        latch5seconds(verificationLatch);
    }
}