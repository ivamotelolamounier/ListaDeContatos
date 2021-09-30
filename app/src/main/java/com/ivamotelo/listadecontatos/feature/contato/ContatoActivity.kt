package com.ivamotelo.listadecontatos.feature.contato

import android.os.Bundle
import android.view.View
import com.ivamotelo.listadecontatos.R
import com.ivamotelo.listadecontatos.application.ContatoApplication
import com.ivamotelo.listadecontatos.bases.BaseActivity
import com.ivamotelo.listadecontatos.feature.listacontatos.model.ContatosVO
import kotlinx.android.synthetic.main.activity_contato.*
import kotlinx.android.synthetic.main.activity_contato.toolBar

class ContatoActivity : BaseActivity() {

    private var idContato: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contato)
        setupToolBar(toolBar, "Contato",true)
        setupContato()
        btnSalvarConato.setOnClickListener { onClickSalvarContato() }
    }

    private fun setupContato(){
        idContato = intent.getIntExtra("index",-1)
        if (idContato == -1){                                           // Se igual -1 será uma criação
            btnExcluirContato.visibility = View.GONE                    // Esconde o botão 'excluir'
            return
        }
        /**
         * Uma forma de um processo dividir a si mesmo em duas ou mais tarefas que podem ser
         * executadas concorrencialmente.  API runOnUiThread como um atalho, seguro e recomendado,
         * de acesso à thread principal de um aplicativo Android.
         * Aqui é implementado o conceito de loading
         * O uso de @runnable em 'return' é necessário, uma vez que não retorna nada, voltando
         * para a thread (Runnable)
         */
        progress.visibility = View.VISIBLE                              // Se diferente -1, então Edição ou pesquisa
        Thread(Runnable {                                               // mostra a progressBar
            Thread.sleep(1000)
            var lista = ContatoApplication.instance.helperDB?.buscarContatos("$idContato",true) ?: return@Runnable
            var contato = lista.getOrNull(0) ?: return@Runnable
            runOnUiThread {
                etNome.setText(contato.nome)
                etTelefone.setText(contato.telefone)
                progress.visibility = View.GONE
            }
        }).start()
    }

    private fun onClickSalvarContato(){
        val nome = etNome.text.toString()
        val telefone = etTelefone.text.toString()
        val contato = ContatosVO(
            idContato,
            nome,
            telefone
        )
        progress.visibility = View.VISIBLE
        Thread(Runnable {
            Thread.sleep(1000)
            if(idContato == -1) {
                ContatoApplication.instance.helperDB?.salvarContato(contato)
            }else{
                ContatoApplication.instance.helperDB?.updateContato(contato)
            }
            runOnUiThread {
                progress.visibility = View.GONE
                finish()
            }
        }).start()
    }

    /**
     * Com o uso da biblioteca descontinuada 'kotlinx.android.synthetic', na activity_contato.xml
     * foi configurado o 'android:onClick="onClickExcluirContato"', para a função abaixo
     */
    fun onClickExcluirContato(view: View) {
        if(idContato > -1){                             // verifica se é um novo contato
            progress.visibility = View.VISIBLE
            Thread(Runnable {
                Thread.sleep(1000)
                ContatoApplication.instance.helperDB?.deletarContato(idContato)
                runOnUiThread {
                    progress.visibility = View.GONE
                    finish()
                }
            }).start()
        }
    }
}
