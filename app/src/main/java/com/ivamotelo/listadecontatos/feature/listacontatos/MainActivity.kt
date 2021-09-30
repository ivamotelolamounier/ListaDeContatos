/**
 *
 */

package com.ivamotelo.listadecontatos.feature.listacontatos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivamotelo.listadecontatos.R
import com.ivamotelo.listadecontatos.application.ContatoApplication
import com.ivamotelo.listadecontatos.bases.BaseActivity
import com.ivamotelo.listadecontatos.feature.contato.ContatoActivity
import com.ivamotelo.listadecontatos.feature.listacontatos.adapter.ContatoAdapter
import com.ivamotelo.listadecontatos.feature.listacontatos.model.ContatosVO
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception


class MainActivity : BaseActivity() {

    private var adapter:ContatoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolBar(toolBar, "Lista de contatos",false)
        setupListView()
        setupOnClicks()
    }

    private fun setupOnClicks(){
        fab.setOnClickListener { onClickAdd() }
        ivBuscar.setOnClickListener { onClickBuscar() }
    }

    private fun setupListView(){
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        onClickBuscar()
    }

    private fun onClickAdd(){
        val intent = Intent(this,ContatoActivity::class.java)
        startActivity(intent)
    }

    private fun onClickItemRecyclerView(index: Int){
        val intent = Intent(this,ContatoActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
    }
    /** Uso de componente 'progressBar',
    * O EditText 'etBuscar', recebe o filtro digitado pelo usuário, o progressBar fica visível
    * Para suspender um thread por um tempo especificado, Java fornece o método de suspensão
    * que é definido na classe Thread.
    * A variável 'listaFiltrada, recebe uma mutableList de ContatosVO
    * Ocorre o tratamento de erro 'try' caso retorne null
    */
    private fun onClickBuscar(){
        val busca = etBuscar.text.toString()
        progress.visibility = View.VISIBLE
        /**
        broqueia outros processamentos do app enquanto realiza a pesquisa por 1500 milisegs
        retira o processamento para uma threads segundária 'Runnnable'
         ATENÇÃO: cuidado para não deixar ocorrer processamentos referentes a 'Views' dentro
         da threads segundária, isso apenas é possível na THREADS PRINCIPAL (MAIN)
        */
        Thread(Runnable {
            /**
            apenas para interação visual em razão da rapidez de pequenas consultas
            não é necessário seu uso, é opcional, para informar o usuário de que o app está
            funcionando
             */
            Thread.sleep(1000)
            var listaFiltrada: List<ContatosVO> = mutableListOf()
            try {
                /**
                Recebe o companion objetc, que recebe a instância do helperDB, com o resultado da
                busca, que será do tipo mutableList. no entanto, se o helper não for encontrado,
                utiliza-se o operador Elvis para retornar uma lista vazia.
                Por padrão, TODA chamada de db deve-se realizar dentro de um try/catch
                */
                listaFiltrada = ContatoApplication.instance.helperDB?.buscarContatos(busca) ?: mutableListOf()
            }catch (ex: Exception){
                ex.printStackTrace()
            }
            /**
             * Para evitar o travamento do app em razão do processamento da View Recicyler (adpter)
             * é necessário o uso da função 'runOnUiThread' como implementado abaixo
             */
            runOnUiThread {
                adapter = ContatoAdapter(this,listaFiltrada) {onClickItemRecyclerView(it)}
                recyclerView.adapter = adapter
                progress.visibility = View.GONE
                Toast.makeText(this,"Buscando por $busca",Toast.LENGTH_SHORT).show()
            }
        }).start()
    }

}
