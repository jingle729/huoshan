package com.effectsar.labcv.demo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.effectsar.labcv.demo.fragment.EBoxConfigPreferenceFragment


open class PreferenceDebugActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_CLAZZ = "eo_debug_fragment_clazz"
        const val TITLE = "eo_debug_fragment_title"
        fun start(activity: Activity, title: String, clazz: Class<out PreferenceFragmentCompat>) {
            activity.startActivity(
                Intent(activity, PreferenceDebugActivity::class.java).apply {
                    putExtra(FRAGMENT_CLAZZ, clazz)
                    putExtra(TITLE, title)
                }
            )
        }
    }

    private lateinit var fragmentClazz: Class<out PreferenceFragmentCompat>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentClazz = intent?.extras?.get(FRAGMENT_CLAZZ) as? Class<out PreferenceFragmentCompat> ?: defaultPreferenceFragment()
        val title = intent.getStringExtra(TITLE) ?: "EffectOne"

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = title
        }

        val currentFragment = supportFragmentManager.findFragmentByTag(fragmentClazz.canonicalName)

        if (currentFragment == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, fragmentClazz.newInstance(), fragmentClazz.canonicalName)
                .commitNowAllowingStateLoss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(android.R.id.home)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    open fun defaultPreferenceFragment(): Class<out PreferenceFragmentCompat> {
        return EBoxConfigPreferenceFragment::class.java
    }
}