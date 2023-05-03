package com.example.storyapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.api.ListStoryResponse
import com.example.storyapp.databinding.ActivityStoryBinding
import com.example.storyapp.databinding.ItemListStoryBinding
import com.example.storyapp.preferences.AppPreferences
import com.example.storyapp.ui.viewmodel.StoryViewModel

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private lateinit var binding1: ItemListStoryBinding
    private val storyViewModel by viewModels<StoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding1 = ItemListStoryBinding.inflate(layoutInflater)

        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStory.addItemDecoration(itemDecoration)

        storyViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        storyViewModel.message.observe(this) {
            getStoryUser(storyViewModel.story)
        }

        val appPreferences = AppPreferences(this)
        val token = appPreferences.authToken

        if (token != null) {
            storyViewModel.getStories(token)
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.languange_menu -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.logout_menu -> {
                val appPreferences = AppPreferences(this)
                appPreferences.isLoggedIn = false
                appPreferences.authToken = null
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showNoData(isNoData: Boolean) {
        binding.noData.visibility = if (isNoData) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        val appPreferences = AppPreferences(this)
        val token = appPreferences.authToken

        if (token != null) {
            storyViewModel.getStories(token)
        }
    }

    private fun getStoryUser(story: List<ListStoryResponse>) {
        if (story.isEmpty()) {
            showNoData(true)
        } else {
            showNoData(false)
            val listUserAdapter = ListStoryAdapter(story)
            binding.rvStory.adapter = listUserAdapter

            listUserAdapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
                override fun onItemClicked(data: ListStoryResponse) {
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@StoryActivity,
                        androidx.core.util.Pair.create(binding1.tvItemName, ViewCompat.getTransitionName(binding1.tvItemName)),
                        androidx.core.util.Pair.create(binding1.ivItemPhoto, ViewCompat.getTransitionName(binding1.ivItemPhoto))
                    )
                    val intent = Intent(this@StoryActivity, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_STORY, data)
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@StoryActivity).toBundle())
                }
            })
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}