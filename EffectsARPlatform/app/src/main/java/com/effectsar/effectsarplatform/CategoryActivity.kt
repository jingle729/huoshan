package com.effectsar.effectsarplatform

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.effectsar.platform.EffectsARPlatform
import com.effectsar.platform.api.MaterialDownloadListener
import com.effectsar.platform.struct.Category
import com.effectsar.platform.struct.Material
import com.effectsar.platform.struct.PlatformError
import com.effectsar.effectsarplatform.adapter.CategoryListAdapter
import com.effectsar.effectsarplatform.adapter.ItemListAdapter
import kotlinx.android.synthetic.main.layout_category.*

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category)

        intent.getParcelableExtra<Category>("category")?.let { category ->
            one_recycler_view.run {
                adapter = CategoryListAdapter(category.childCategory).apply {
                    clickAction = { _, category ->
                        if (category.childCategory.isNotEmpty()) {
                            val intent = Intent(this@CategoryActivity, CategoryActivity::class.java)
                            intent.putExtra("category", category)
                            startActivity(intent)
                        } else {
                            initTwoRecyclerView(category)
                        }
                    }
                }
                layoutManager = LinearLayoutManager(this@CategoryActivity, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
            }
        }
    }

    private fun initTwoRecyclerView(category: Category) {
        EffectsARPlatform.fetchMaterialList("dp@1oa334f", category) {
            two_recycler_view.run {
                layoutManager = GridLayoutManager(this@CategoryActivity, 4)
                adapter = ItemListAdapter(it).apply {
                    clickAction = { _, mediaResponse ->
                        EffectsARPlatform.fetchMaterial(mediaResponse, object : MaterialDownloadListener {
                            override fun onSuccess(material: Material, path: String) {
                                Log.e("gaojin", "onSuccess:${Thread.currentThread().name}")
                                Toast.makeText(this@CategoryActivity, "下载成功", Toast.LENGTH_SHORT).show()
                            }

                            override fun onProgress(material: Material, process: Int) {
                                Log.e("gaojin", "onProgress:${process} ThreadName:${Thread.currentThread().name}")
                            }

                            override fun onFailed(material: Material, e: Exception, platformError: PlatformError) {
                            }
                        })
                    }
                }
                setHasFixedSize(true)
            }
        }
    }
}