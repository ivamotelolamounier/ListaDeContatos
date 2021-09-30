/**
 * Uma classe auxiliar para gerenciar a criação de banco de dados e gerenciamento de versão.
 * Você cria uma subclasse de implementação e onCreate, onUpgradeopcionalmente onOpen,
 * essa classe se encarrega de abrir o banco de dados se ele existir, criá-lo se não existir
 * e atualizá-lo conforme necessário. As transações são usadas para garantir que o
 * banco de dados esteja sempre em um estado razoável.
 * https://developer.android.com/reference/kotlin/android/database/sqlite/SQLiteOpenHelper
 *
 * Tem que obrigatóriamente herdar da classe SQLiteOpenHelper()
 */
package com.ivamotelo.listadecontatos.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.ivamotelo.listadecontatos.feature.listacontatos.model.ContatosVO

class HelperDB(
    context: Context
) : SQLiteOpenHelper(context, NOME_BANCO, null, VERSAO_ATUAL) {

    /**
    Os parâmetros 'name', 'factory' e 'version', por padrão instânciados no construtor do
    HelperDB, são movidos e delegados para o 'HelperDB'. Isto evita que o nome do DB, e
    sua versão, sejam acessadas por métodos set externos, protegendo o DB de acessos fora da
    classe Helper, através do método ESTÁTICO companion object, (para ficar visível em toda aplicação)
     uma vez que está sendo chamado pelo construtor do HelperDB
    */
    companion object {
        private val NOME_BANCO = "contato.db"
        private val VERSAO_ATUAL = 2
    }

    /** Comando SQL para criar a tabela 'contato', com os campos 'id', 'nome' e 'telefone', através
     * da manipulação das val 'TABLE_NAME', 'COLUMNS_ID', 'COLUMNS_NOME', 'COLUMNUS_TELEFONE',
     * que concactenadas será:
     * "CREATE TABLE (contato id INTEGER NOT NULL, nome INTEGER NOT NULL, telefone INTEGER NOT NULL),
     * PRIMARY KEY (id AUTOINCREMENT)"
     */
    val TABLE_NAME = "contato"
    val COLUMNS_ID = "id"
    val COLUMNS_NOME = "nome"
    val COLUMNS_TELEFONE = "telefone"
    val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
            "$COLUMNS_ID INTEGER NOT NULL," +
            "$COLUMNS_NOME TEXT NOT NULL," +
            "$COLUMNS_TELEFONE TEXT NOT NULL," +
            "" +
            "PRIMARY KEY($COLUMNS_ID AUTOINCREMENT)" +
            ")"
    /**
    Membro obrigatório do método SQLiteOpenHelper, usado para criar a instância do banco de dados
    assim, toda vez que o DB for criado pela primeira vez o método será chamado, onde serão criadas
    todas as tabelas do DB, conforme variáveis inicializadas acima
    */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }
    /**
    Membro obrigatório do método SQLiteOpenHelper, semelhante ao OnCreate, é chamado todas as
    vezes que a versão (val VERSAO_ATUAL) do DB for alterada, comparando a versão antiga com a nova,
    IF as versões forem diferentes, então o DB antigo é excluido (DROP_TABLE) e uma nova versão
    é criada, caso contrário, o banco de dados é instânciado (onCreate(db) é recriado (UPDATE, CREATE)
    */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(oldVersion != newVersion) {
            db?.execSQL(DROP_TABLE)
        }
        onCreate(db)
    }

    /**
     * Função utilizada para implementar as buscas no banco de dados, com a passagem de parâmetros
     * 'busca', que é uma String utilizada para filtrar os registros escolhidos,
     * O retorno da busca é uma Lista<ContatosVO>
     * 1 - Primeiramente instância-se o BD através da val 'db' que retorna 'readableDatabase', que
     * Crie e / ou abra um banco de dados. Este será o mesmo objeto retornado por getWritableDatabase
     * https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper
     * por segurança, utiliza-se o operador Elvis (?:) para caso o db retorne nulo, então será
     * retornada uma mutableList vazia.
     * 2- Após, cria-se e instância-se uma lista de contatos através da var 'lista' que recebe uma
     * mutableList do tipo 'ContatosVO'
     * 3- Existem diversas maneiras de fazer um SELECT, (ex: db.query) porém a forma implementada
     * facilita futuras atualizaçoes ou novas implementações no código de busca, além de mais
     * visibilidade e controle
     */
    fun buscarContatos(busca: String, isBuscaPorID: Boolean = false) : List<ContatosVO> {
        val db = readableDatabase ?: return mutableListOf() //Utiliza-se o readableDatabase
        var lista = mutableListOf<ContatosVO>()
        var where: String? = null
        var args: Array<String> = arrayOf()
        if(isBuscaPorID){                       // busca pelo ID
            where = "$COLUMNS_ID = ?"           //SQL 'WHERE'
            args = arrayOf("$busca")
        }else{                                  // busca pela coluna selecionada
            where = "$COLUMNS_NOME LIKE ?"
            args = arrayOf("%$busca%")
        }
        /**
         * Verifica se o cursor retornou nulo, se verdadeiro, fecha o db e retorna uma tabela vazia
         * caso contrário, faz a pesquisa (query) na tabela "TABLE_NAME" = 'contato' e as condições
         * do filtro da pesquisa "where" = 'id LIKE' + o valor do filtro %$busca% = qualquer sequência
         * de caracteres digitados no EditText.
         * O método utilizado para pesquisar foi o SQL puro, existem outras formas como 'content'
         */
        var cursor = db.query(TABLE_NAME,null,where,args,null,null,null)
        if (cursor == null){
            db.close()
            return mutableListOf()
        }
        /**
         * implementação para percorrer a lista de contatos conforme a seleção
        Uso da classe Cursor: Essa interface fornece acesso aleatório de leitura e gravação
        ao conjunto de resultados retornado por uma consulta ao banco de dados.
        As implementações do cursor não precisam ser sincronizadas, portanto, o código que usa
        um Cursor de vários threads deve realizar sua própria sincronização ao usar o Cursor.
        As implementações devem ter uma subclasse AbstractCursor.
         */
        while(cursor.moveToNext()){
            var contato = ContatosVO(
                cursor.getInt(cursor.getColumnIndex(COLUMNS_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMNS_NOME)),
                cursor.getString(cursor.getColumnIndex(COLUMNS_TELEFONE))
            )
            lista.add(contato) // adiciona o contato
        }
        db.close() // fecha o db e retorna a lista atualizada (novo contato)
        return lista
    }

    /**
     * Função implementada para inserir um novo contato, utilizado o método nativo 'writeableDatabase
     * ContenValues: Cria um conjunto vazio de valores usando o tamanho inicial padrão
     * Aqui a forma de implementar é diferente, usa-se o método 'content' em vez de SQL puro.
     */
    fun salvarContato(contato: ContatosVO) {
        val db = writableDatabase ?: return // Utiliza-se o writeableDatabase
        var content = ContentValues()
        content.put(COLUMNS_NOME,contato.nome)
        content.put(COLUMNS_TELEFONE,contato.telefone)
        db.insert(TABLE_NAME,null,content)
        db.close()
    }

    /**
     MÉTODO SQL PURO

    fun deletarContato(id: Int) {
        val db = writableDatabase ?: return
        val sql = "DELETE FROM $TABLE_NAME WHERE $COLUMNS_ID = ?"
        val arg = arrayOf("$id")
        db.execSQL(sql,arg)
        db.close()
    }
    */

    /**
     * MÉTODO FUNÇÔES NATIVAS DB
     */
    fun deletarContato(id: Int){
        val db: SQLiteDatabase = writableDatabase ?: return
        val where = "id = ?"
        val args = arrayOf("$id")
        db.delete(TABLE_NAME, where, args)
        db.close()
    }

    /**
     * Na função para atualizar, foi utilizado o MÈTODO SQL PURO
    */

    fun updateContato(contato: ContatosVO) {
        val db = writableDatabase ?: return
        val sql = "UPDATE $TABLE_NAME SET $COLUMNS_NOME = ?, $COLUMNS_TELEFONE = ? WHERE $COLUMNS_ID = ?"
        val arg = arrayOf(contato.nome,contato.telefone,contato.id)
        db.execSQL(sql,arg)
        db.close()
    }

    /**
     * Mesma função 'updateContato()', MÈTODO DB NATIVO

    fun updateContato(contato: ContatosVO){
        val db: SQLiteDatabase = writableDatabase ?: return
        val content = contentValuesOf()
        content.put(COLUMNS_NOME, contato.nome)
        content.put(COLUMNS_TELEFONE, contato.telefone)
        val where = "id = ?"
        val args = arrayOf("${contato.id}")
        db.update(TABLE_NAME, content, where,args)
        db.close()
    }
    */
}