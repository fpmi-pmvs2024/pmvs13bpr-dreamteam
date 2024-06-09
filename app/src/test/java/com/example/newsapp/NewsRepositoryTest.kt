package com.example.newsapp
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.newsapp.NewsModel
import com.example.newsapp.architecture.NewsDao
import com.example.newsapp.architecture.NewsDatabase
import com.example.newsapp.architecture.NewsRepository
import com.example.newsapp.retrofit.Article
import com.example.newsapp.retrofit.NewsApi
import com.example.newsapp.retrofit.NewsDataFromJson
import com.example.newsapp.retrofit.RetrofitHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class NewsRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var newsDatabase: NewsDatabase

    @Mock
    private lateinit var newsDao: NewsDao

    @Mock
    private lateinit var newsApi: NewsApi

    @Mock
    private lateinit var call: Call<NewsDataFromJson>

    private lateinit var newsRepository: NewsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        newsRepository = NewsRepository()
        `when`(newsDatabase.newsDao()).thenReturn(newsDao)
        `when`(RetrofitHelper.getInstance().create(NewsApi::class.java)).thenReturn(newsApi)
    }

    @Test
    fun testInsertNews() = runBlocking {
        val news = NewsModel("Title", "ImageURL", "Description", "URL", "Source", "PublishedAt", "Content")

        doNothing().`when`(newsDao).insertNews(news)

        NewsRepository.insertNews(context, news)

        verify(newsDao, times(1)).insertNews(news)
    }

    @Test
    fun testDeleteNews() = runBlocking {
        val news = NewsModel("Title", "ImageURL", "Description", "URL", "Source", "PublishedAt", "Content")

        doNothing().`when`(newsDao).deleteNews(news)

        NewsRepository.deleteNews(context, news)

        verify(newsDao, times(1)).deleteNews(news)
    }

    @Test
    fun testGetAllNews() {
        val newsList = MutableLiveData<List<NewsModel>>()
        `when`(newsDao.getNewsFromDatabase()).thenReturn(newsList)

        val result: LiveData<List<NewsModel>> = NewsRepository.getAllNews(context)

        Assert.assertEquals(newsList, result)
    }



    @Test
    fun testGetNewsApiCallFailure() {
        `when`(newsApi.getNews(anyString(), anyString(), anyString())).thenReturn(call)

        doAnswer {
            val callback: Callback<NewsDataFromJson> = it.getArgument(0)
            callback.onFailure(call, Throwable("Network error"))
            null
        }.`when`(call).enqueue(any())

        val newsList: MutableLiveData<List<NewsModel>> = newsRepository.getNewsApiCall("category")

        Assert.assertNull(newsList.value)
    }
}