package com.effectsar.effectsarplatform

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.effectsar.platform.EffectsARPlatform
import com.effectsar.platform.config.EffectsARPlatformConfig
import com.effectsar.platform.struct.Category
import com.effectsar.effectsarplatform.adapter.CategoryListAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EffectsARPlatform.init(
            EffectsARPlatformConfig.Builder()
                .appVersion("4.0.0")
                .language("zh")
                .boeKey("boe_cv_ck_demo")
                .build()
        )

        start_download.setOnClickListener {
            EffectsARPlatform.fetchCategoryList("dp@1oa334f") { result ->
                initList(result)
            }
        }

        start_clear.setOnClickListener {
            loading_text.text = "清理中"
            EffectsARPlatform.clear {
                loading_text.text = "清理完成"
            }
        }
    }

    private fun initList(categoryList: List<Category>) {
        recycler_view.run {
            adapter = CategoryListAdapter(categoryList).apply {
                clickAction = { _, category ->
                    val intent = Intent(this@MainActivity, CategoryActivity::class.java)
                    intent.putExtra("category", category)
                    startActivity(intent)
                }
            }
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }
}