package com.example.instagram.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.models.User
import com.example.instagram.utils.FirebaseHelper
import com.example.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity(4) {
    private lateinit var mUser: User
    private val TAG = "ProfileActivity"
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        edit_profile_btn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        settings_image.setOnClickListener {
            val intent = Intent(this, ProfileSettignsActivity::class.java)
            startActivity(intent)
        }
        add_friedns_image.setOnClickListener {
            val intent = Intent(this, AddFriendsActivity::class.java)
            startActivity(intent)
        }

        mFirebase = FirebaseHelper(this)
        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
            profile_image.loadUserPhoto(mUser.photo)
            username_text.text = mUser.username
            followers_count_text.text = mUser.followers.size.toString()
            following_count_text.text = mUser.follows.size.toString()
            profile_name.text = mUser.name
            profile_bio.text = mUser.bio
            setupProfileSiteText()
            setupProfileInfoVisibility()
        })

        images_recycler.layoutManager = GridLayoutManager(this, 3)
        mFirebase.database.child("images").child(mFirebase.currentUid()!!)
            .addValueEventListener(ValueEventListenerAdapter {
                val images = it.children.map { it.getValue(String::class.java)!! }
                images_recycler.adapter = ImagesAdapter(images)
                posts_count_text.text = images.size.toString()
            })
    }

    private fun setupProfileSiteText() {
        profile_site.text = mUser.website
        profile_site.setOnClickListener {
            var url = mUser.website.toString()
            if (!URLUtil.isNetworkUrl(url)) url = "http://$url"
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
        }
    }

    private fun setupProfileInfoVisibility() {
        setTextViewVisibleOrGone(profile_name)
        setTextViewVisibleOrGone(profile_bio)
        setTextViewVisibleOrGone(profile_site)
    }

    private fun setTextViewVisibleOrGone(textView: TextView) {
        if (textView.text.isEmpty())
            textView.visibility = View.GONE
        else
            textView.visibility = View.VISIBLE
    }
}

class ImagesAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false) as ImageView
        return ViewHolder(image)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.loadImage(images[position])
    }

    override fun getItemCount(): Int = images.size
}

class SquareImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
