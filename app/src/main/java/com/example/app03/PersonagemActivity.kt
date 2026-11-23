package com.example.app03

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Tela de Busca de Personagem
class PersonagemActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var btnBuscar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvMensagem: TextView
    private lateinit var layoutResultado: LinearLayout
    private lateinit var tvNome: TextView
    private lateinit var tvEspecie: TextView
    private lateinit var tvCasa: TextView
    private lateinit var hogwartsApi: HogwartsApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personagem)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNome = findViewById(R.id.etNome)
        btnBuscar = findViewById(R.id.btnBuscar)
        progressBar = findViewById(R.id.progressBar)
        tvMensagem = findViewById(R.id.tvMensagem)
        layoutResultado = findViewById(R.id.layoutResultado)
        tvNome = findViewById(R.id.tvNome)
        tvEspecie = findViewById(R.id.tvEspecie)
        tvCasa = findViewById(R.id.tvCasa)

        // Configura o Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hogwarts-api.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        hogwartsApi = retrofit.create(HogwartsApi::class.java)
    } //Fim onCreate

    // Chamado pelo onClick do botão Buscar.
    // Valida o texto digitado ANTES de consultar a API
    fun buscarPersonagem(view: View) {
        // Regra 1: remove os espaços das pontas
        val nome = etNome.text.toString().trim()

        // Regra 2: o campo não pode estar vazio
        if (nome.isEmpty()) {
            etNome.error = "Digite o nome de um personagem"
            etNome.requestFocus() // com o foco, a mensagem aparece no próprio campo
            return
        }

        // Regra 3: mínimo de 3 caracteres
        if (nome.length < 3) {
            etNome.error = "Digite pelo menos 3 caracteres"
            etNome.requestFocus()
            return
        }

        buscarPersonagemNaApi(nome)
    }

    // Função para buscar o personagem usando corrotinas
    private fun buscarPersonagemNaApi(nome: String) {
        lifecycleScope.launch {
            try {
                showLoading()
                // Faz a requisição em segundo plano (fora da thread principal)
                val response = withContext(Dispatchers.IO) {
                    hogwartsApi.buscarPersonagens(nome)
                }

                if (response.personagens.isEmpty()) {
                    showMensagem("Nenhum personagem encontrado")
                } else {
                    // A busca pode retornar vários personagens: usamos apenas o primeiro
                    val personagem = response.personagens[0]
                    tvNome.text = personagem.nome
                    tvEspecie.text = textoOuNaoInformado(personagem.especie)
                    tvCasa.text = textoOuNaoInformado(personagem.casa)
                    layoutResultado.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Sem internet ou erro no servidor: avisa o usuário e o app continua aberto
                Log.e("PersonagemActivity", "Erro ao buscar o personagem", e)
                showMensagem("Não foi possível consultar a API. Verifique sua conexão com a internet e tente novamente.")
            } finally {
                hideLoading()
            }
        } //Fim corrotina
    } //Fim buscarPersonagemNaApi

    // Quando a API não informa o dado, mostra "Não informado"
    private fun textoOuNaoInformado(texto: String?): String {
        if (texto.isNullOrBlank()) {
            return "Não informado"
        }
        return texto
    }

    // Mostra uma mensagem na tela (nenhum resultado ou erro)
    private fun showMensagem(mensagem: String) {
        tvMensagem.text = mensagem
        tvMensagem.visibility = View.VISIBLE
    }

    // Mostra o ProgressBar e limpa o resultado da busca anterior
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        btnBuscar.isEnabled = false
        tvMensagem.visibility = View.GONE
        layoutResultado.visibility = View.GONE
    }

    // Esconde o ProgressBar
    private fun hideLoading() {
        progressBar.visibility = View.GONE
        btnBuscar.isEnabled = true
    }
} //Fim da activity
