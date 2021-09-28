package com.ivamotelo.listadecontatos.singleton

import com.ivamotelo.listadecontatos.feature.listacontatos.model.ContatosVO

object ContatoSingleton {
    var lista: MutableList<ContatosVO> = mutableListOf()
}