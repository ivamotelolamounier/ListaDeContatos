 /**
 Aplication(), é a primeira classe a ser instânciada, responsável por criar
 a aplicação. Primeiro ponto onde o código inicia.
 É o local ideal para instânciar o banco de dados do aplicativo, que será visível
 no sistema, evitando duplicação de código ou de outros instanciamentos equivocados.
 A classe criada obrigatóriamente deve ser herdada da classe pai Application()
 Necessário também informar a existência da classe no arquivo manifest.xml.

 O banco de dados será instânciado dentro da classe Application() para que seja visível
 por toda a aplicação, observando que o mesmo se encontra protegida para métodos 'set'
 ou seja apenas leitura direta, vez que a var é pública, bloqueda para 'set' externos.

 O primeiro método a ser chamado é o 'onCreate(), onde a instância do 'helper' do SQLite
 será criado passando o contexto da activity atual.
*/
package com.ivamotelo.listadecontatos.application

import android.app.Application
import com.ivamotelo.listadecontatos.helpers.HelperDB

class ContatoApplication : Application() {

    var helperDB: HelperDB? = null
        private set

    // criação do Objeto ESTÁTICO para ficar visível na aplicação
    companion object {
        lateinit var instance: ContatoApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this // Armazena o companion objetc para passar para a aplicação
        helperDB = HelperDB(this) // instânciamento do DB, no contexto atual
    }
}