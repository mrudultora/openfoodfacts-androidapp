package openfoodfacts.github.scrachx.openfood.features.additives

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityAdditivesExplorerBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.Utils
import org.greenrobot.greendao.async.AsyncOperation
import org.greenrobot.greendao.async.AsyncOperationListener
import java.util.*

class AdditiveListActivity : BaseActivity() {
    private var _binding: ActivityAdditivesExplorerBinding? = null
    private val binding get() = _binding!!
    private var additives = mutableListOf<AdditiveName>()
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdditivesExplorerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.additives)
        val daoSession = Utils.daoSession
        val asyncSessionAdditives = daoSession.startAsyncSession()
        val additiveNameDao = daoSession.additiveNameDao
        val languageCode = LocaleHelper.getLanguage(this)
        asyncSessionAdditives.queryList(additiveNameDao.queryBuilder()
                .where(AdditiveNameDao.Properties.LanguageCode.eq(languageCode))
                .where(AdditiveNameDao.Properties.Name.like("E%")).build())
        asyncSessionAdditives.listenerMainThread = AsyncOperationListener { operation: AsyncOperation ->
            additives = operation.result as MutableList<AdditiveName>
            additives.sortWith { additive1: AdditiveName, additive2: AdditiveName ->
                val s1 = additive1.name.toLowerCase(Locale.ROOT).replace('x', '0').split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")).toTypedArray()[1]
                val s2 = additive2.name.toLowerCase(Locale.ROOT).replace('x', '0').split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")).toTypedArray()[1]
                Integer.valueOf(s1).compareTo(Integer.valueOf(s2))
            }
            if (isFinishing) {
                return@AsyncOperationListener
            }
            binding.additiveRecyclerView.layoutManager = LinearLayoutManager(this@AdditiveListActivity)
            binding.additiveRecyclerView.adapter = AdditivesAdapter(additives) { _, name -> onClick(name) }
            binding.additiveRecyclerView.addItemDecoration(DividerItemDecoration(this@AdditiveListActivity, DividerItemDecoration.VERTICAL))
        }
        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(0)
        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)
    }

    fun onClick(name: String) = ProductSearchActivity.start(this, SearchType.ADDITIVE, name)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = this.getSystemService(SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.addtive_search)
        if (searchManager.getSearchableInfo(this.componentName) != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.componentName))
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(query: String): Boolean {
                    val suggestedAdditives = mutableListOf<AdditiveName>()
                    for (additive in additives) {
                        if (additive.name.toLowerCase(Locale.getDefault()).split(" - ").size > 1) {
                            val additiveContent = additive.name.toLowerCase(Locale.getDefault()).split(" - ").toTypedArray()
                            val trimmedQuery = query.trim { it <= ' ' }.toLowerCase(Locale.getDefault())
                            if (additiveContent[0].trim { it <= ' ' }.contains(trimmedQuery)
                                    || additiveContent[1].trim { it <= ' ' }.contains(trimmedQuery)
                                    || "${additiveContent[0]}-${additiveContent[1]}".contains(trimmedQuery)) {
                                suggestedAdditives.add(additive)
                            }
                        }
                    }
                    binding.additiveRecyclerView.adapter = AdditivesAdapter(suggestedAdditives) { _, name -> onClick(name) }
                    binding.additiveRecyclerView.adapter!!.notifyDataSetChanged()
                    return false
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        @JvmStatic
        fun start(context: Context) = context.startActivity(Intent(context, AdditiveListActivity::class.java))
    }
}