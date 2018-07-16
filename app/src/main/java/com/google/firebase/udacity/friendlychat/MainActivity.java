/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/*
* Get code from:
*
* https://github.com/udacity/and-nd-firebase.git
*
* Realtime Database rules
*
* {
* "rules": {
* ".read": true,
* ".write": true
* }
* }
*
* .read Describes whether data can be read by the user
*
* .write Describes whether data can be written by the user
*
* .validate Describes what a correctly formatted value looks like
*
* Predefined variables
*
* now: The current time in millisecond since Unix epoch time (January 1, 1970)
*
* root: Correspond to the current data at the root of the database
*
* newData: Corresponds to the data that will result if the write is successful
*
* data: Corresponds to the current data in Firebase Realtime Database
* at the location of the currently executing rule
*
* $variables A wildcard path used to represent ids and dynamic child keys
*
* auth: Contains the token payload if a user is authenticated, or null if the user isn't
* authenticated.
*
* The auth variable contains the JSON web token for the user.
*
* A JSON Web Token is a standard that defines a way of securely transmitting
* information between parties, like the database and a client, as a JSON object.
*
* Once a user is authenticated, this token contains the provider, the uid, and the
* Firebase Auth ID token.
*
* The provider is the method of authentication, such as email/password, Google Sign In,
* or Facebook Login.
*
* The uid is a unique user ID. This ID is guaranteed to be unique across all providers,
* so a user that authenticates with Google and a user that authenticates with email/password
* do not risk having the same identification.
*
* he Firebase Auth ID is a web token. Yes, this means that there is a web token inside of the
* Auth web token! This token can contain the following data:
*
* email: The email address associated with the account.
*
* email_verified: A boolean that is true if the user has verified they have access to the
* email address.
*
* name: The user's display name if one is set.
*
* sub: The user's Firebase uID
*
* firebase.identities: Dictionary with all the identities that are associated with this user
* account.
*
* firebase.sign_in_provider: The sign_in provider used to obtain this Firebase Auth id token.
*
* Advanced Rules
*
* Sometimes, we don’t want to apply a rule to all users of an app.
*
* We may want to have administrative access for some users, allowing them to access
* data that other users cannot.
*
* We may want to unlock features stored in the database when users reach some target,
* like number of messages sent.
*
* We may want to add premium features to our app that only paying customers can access.
*
* Let’s look at how we can use group-specific rules to enforce premium feature access.
*
* For FriendlyChat we could, for example, give paying customers access to private chat
* rooms.
*
* We'll want to configure the database to include a child of messages that will contain
* the messages from this special chat, and rules so that only the users who paid for
* the service can access private chat rooms.
*
* We will use .read and .write rules to control access those chat rooms.
*
* Let’s compare the structure of a FriendlyChat database that includes private chat
* rooms under the key “special_chat” to the structure of the rules restricting that
* database.
*
* https://classroom.udacity.com/courses/ud0352/lessons/daa58d76-0146-4c52-b5d8-45e32a3dfb08/concepts/60c2664c-3419-4b96-a2cb-42865f29e89f
*
* {
 "rules": {
   "messages": {
     // only authenticated users can read and write the messages node
     ".read": "auth != null",
     ".write": "auth != null",
     "$id": {
       // the read and write rules cascade to the individual messages
       // messages should have a 'name' and 'text' key or a 'name' and 'photoUrl' key
       ".validate": "newData.hasChildren(['name', 'text']) && !newData.hasChildren(['photoUrl']) || newData.hasChildren(['name', 'photoUrl']) && !newData.hasChildren(['text'])"
     }
   }
 }
}
*
*
* FirebaseUI Open source authentication library created by the Firebase team that
* handles the UI flow for authenticate with Firebase implementing the best practises
* for sign in and following the brand guide line for each of the providers.
* */
public class MainActivity extends AppCompatActivity {

   private static final String TAG = "MainActivity";

   public static final String ANONYMOUS = "anonymous";
   public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";
   public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

   private ListView mMessageListView;
   private MessageAdapter mMessageAdapter;
   private ProgressBar mProgressBar;
   private ImageButton mPhotoPickerButton;
   private EditText mMessageEditText;
   private Button mSendButton;

   private FirebaseDatabase mFirebaseDatabase;
   private FirebaseStorage mFirebaseStorage;
   private StorageReference mChatPhotosStorageReference;
   private FirebaseRemoteConfig mFirebaseRemoteConfig;

   // Refers to a portion of the database
   private DatabaseReference mMessagesDatabaseReference;
   // Listener to be notified on database changes
   private ChildEventListener mChildEventListener;

   private FirebaseAuth mFirebaseAuth;
   private FirebaseAuth.AuthStateListener mAuthStateListener;

   private static final int RC_SIGN_IN = 1;
   private static final int RC_PHOTO_PICKER = 2;

   private String mUsername;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mUsername = ANONYMOUS;

      mFirebaseDatabase = FirebaseDatabase.getInstance();
      mFirebaseAuth = FirebaseAuth.getInstance();
      mFirebaseStorage = FirebaseStorage.getInstance();
      mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
      // Root path (si no exiten las crea firebase)
      mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
      mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
      // Initialize references to views
      mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
      mMessageListView = (ListView) findViewById(R.id.messageListView);
      mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
      mMessageEditText = (EditText) findViewById(R.id.messageEditText);
      mSendButton = (Button) findViewById(R.id.sendButton);

      // Initialize message ListView and its adapter
      List<FriendlyMessage> friendlyMessages = new ArrayList<>();
      mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
      mMessageListView.setAdapter(mMessageAdapter);

      // Initialize progress bar
      mProgressBar.setVisibility(ProgressBar.INVISIBLE);

      // ImagePickerButton shows an image picker to upload a image for a message
      mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action"), RC_PHOTO_PICKER);
         }
      });

      // Enable Send button when there's text to send
      mMessageEditText.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
         }

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().trim().length() > 0) {
               mSendButton.setEnabled(true);
            } else {
               mSendButton.setEnabled(false);
            }
         }

         @Override
         public void afterTextChanged(Editable editable) {
         }
      });
      mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

      // Send button sends a message and clears the EditText
      mSendButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            // TODO: Send messages on click

            FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);

            mMessagesDatabaseReference.push().setValue(friendlyMessage);

            // Clear input box
            mMessageEditText.setText("");

            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
         }
      });

      mAuthStateListener = new FirebaseAuth.AuthStateListener() {
         @Override
         public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
               // user is signed in
               onSignedInInitialize(firebaseUser.getDisplayName());
            } else {
               // user is signed out
               onSignedOutCleanup();

//               List<AuthUI.IdpConfig> provider = Collections
//                    .singletonList(new AuthUI.IdpConfig.EmailBuilder().build());
               List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().setScopes(Arrays.asList(Scopes.FITNESS_ACTIVITY_READ)).build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build()
//                    new AuthUI.IdpConfig.TwitterBuilder().build()
               );

// Create and launch sign-in intent
               startActivityForResult(
                    AuthUI.getInstance()
                         .createSignInIntentBuilder()
                         // saves credentials for automatic login
                         .setIsSmartLockEnabled(false)
                         .setAvailableProviders(providers)
                         .setLogo(R.drawable.pep_portrait)
                         .setTheme(R.style.GreenTheme)
                         .setTosUrl("https://twitter.com/tos")
                         .setPrivacyPolicyUrl("https://termsfeed.com/blog/privacy-policy-url-facebook-app/")
                         .build(),
                    RC_SIGN_IN);
            }
         }
      };

      FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings
           .Builder()
           .setDeveloperModeEnabled(BuildConfig.DEBUG)
           .build();
      mFirebaseRemoteConfig.setConfigSettings(settings);

      Map<String, Object> defaultConfigMap = new HashMap<>();

      defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);

      mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

      fetchConfig();

   }

   /* This method is called before onResume() method  */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == RC_SIGN_IN) {
         IdpResponse response = IdpResponse.fromResultIntent(data);
         if (resultCode == RESULT_OK) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String name = firebaseUser.getDisplayName();
            Toast.makeText(this, name + " signed in!", Toast.LENGTH_SHORT).show();
         } else {
            String error = response.getError().getMessage();
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            finish();
         }
      } else if (requestCode == RC_PHOTO_PICKER) {
         if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // From this ref 'content:/local_image/foo/ninel.jpg' lastsegment'd be 'ninel.jpg'
            StorageReference photoRef = mChatPhotosStorageReference
                 .child(selectedImageUri.getLastPathSegment());
            // Upload file to Firebase storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this,
                 new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                       downloadUri.addOnCompleteListener(new OnCompleteListener<Uri>() {
                          // Save the uri to the database
                          @Override
                          public void onComplete(@NonNull Task<Uri> task) {
                             FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, task.getResult().toString());
                             mMessagesDatabaseReference.push().setValue(friendlyMessage);
                          }
                       });
                    }
                 });
         }
      }
   }

   private void attachDatabaseReadListener() {

      if (mChildEventListener == null) {

         mChildEventListener = new ChildEventListener() {

            /* This method will be called once for every entry present in the collection */
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
               mMessageAdapter.add(friendlyMessage);
            }

            /* This method will be called when the contents of an existing document get changed */
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            /* This method will be called when there are any database error: no permission to */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
         };
         mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
      }
   }

   private void onSignedInInitialize(String displayName) {
      mUsername = displayName;
      attachDatabaseReadListener();
   }

   private void detachDatabaseReadListener() {
      if (mChildEventListener != null) {
         mMessagesDatabaseReference.removeEventListener(mChildEventListener);
         mChildEventListener = null;
      }

   }

   private void onSignedOutCleanup() {
      mUsername = ANONYMOUS;
      mMessageAdapter.clear();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.sign_out_menu:
            // logout
            AuthUI
                 .getInstance()
                 //.delete(this)
                 .signOut(this)
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       Toast.makeText(getApplicationContext(), "Come back soon", Toast.LENGTH_SHORT).show();
                    }
                 });
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      mFirebaseAuth.addAuthStateListener(mAuthStateListener);
   }

   @Override
   protected void onPause() {
      super.onPause();
      if (mAuthStateListener != null) {
         mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
      }
      detachDatabaseReadListener();
      mMessageAdapter.clear();
   }

   private void fetchConfig() {
      long cacheExpiration = 3600;

      if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
         cacheExpiration = 0;
      }

      mFirebaseRemoteConfig.fetch(cacheExpiration)
           .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                 mFirebaseRemoteConfig.activateFetched();
                 applyRetrievedLengthLimit();
              }
           })
           .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                 Log.d(TAG, "Error fetching config", e);
                 applyRetrievedLengthLimit();
              }
           });

   }

   private void applyRetrievedLengthLimit() {
      Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
      mMessageEditText
           .setFilters(new InputFilter[]{new InputFilter
                .LengthFilter(friendly_msg_length
                .intValue())});
      Log.d(TAG, FRIENDLY_MSG_LENGTH_KEY + " = " + friendly_msg_length);
   }

}
