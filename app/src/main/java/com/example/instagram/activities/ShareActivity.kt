package com.example.instagram.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.example.instagram.R
import com.example.instagram.models.FeedPost
import com.example.instagram.models.User
import com.example.instagram.utils.CameraHelper
import com.example.instagram.utils.FirebaseHelper
import com.example.instagram.utils.GlideApp
import com.example.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate")

        mFirebase = FirebaseHelper(this)
        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()

        back_image.setOnClickListener { finish() }
        share_text.setOnClickListener { share() }

        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCamera.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                GlideApp.with(this).load(mCamera.imageUri).centerCrop().into(post_image)
            } else {
                finish()
            }
        }
    }

    private fun share() {
        val imageUri = mCamera.imageUri
        if (imageUri != null) {
            mFirebase.uploadSharePhoto(imageUri) {
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    val uid = mFirebase.currentUid()!!
                    mFirebase.addSharePhoto(uri.toString()) {
                        mFirebase.database.child("feed-posts").child(uid)
                            .push().setValue(mkFeedPost(uid, uri))
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    startActivity(Intent(this, ProfileActivity::class.java))
                                    finish()
                                }
                            }
                    }
                }
            }
        }
    }

    private fun mkFeedPost(
        uid: String,
        uri: Uri
    ): FeedPost {
        return FeedPost(
            uid = uid,
            username = mUser.username,
            image = uri.toString(),
            caption = caption_input.text.toString(),
            photo = mUser.photo
        )
    }
}

