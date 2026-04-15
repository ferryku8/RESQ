package com.uxonauts.resq.controllers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Article(
    val articleId: String = "",
    val judul: String = "",
    val konten: String = "",
    val gambarUrl: String = "",
    val type: String = "article",
    val tglPublish: Timestamp? = null
)

class ArticleController : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    val articles = mutableStateListOf<Article>()
    val banners = mutableStateListOf<Article>()
    var isLoading by mutableStateOf(false)

    init {
        fetchAll()
    }

    fun fetchAll() {
        viewModelScope.launch {
            isLoading = true
            try {
                val snap = db.collection("articles")
                    .orderBy("tglPublish", Query.Direction.DESCENDING)
                    .get().await()

                articles.clear()
                banners.clear()

                for (doc in snap.documents) {
                    val item = Article(
                        articleId = doc.id,
                        judul = doc.getString("judul") ?: "",
                        konten = doc.getString("konten") ?: "",
                        gambarUrl = doc.getString("gambarUrl") ?: "",
                        type = doc.getString("type") ?: "article",
                        tglPublish = doc.getTimestamp("tglPublish")
                    )
                    if (item.type == "banner") banners.add(item)
                    else articles.add(item)
                }
            } catch (e: Exception) {
                // silent fail, list kosong
            } finally {
                isLoading = false
            }
        }
    }
}