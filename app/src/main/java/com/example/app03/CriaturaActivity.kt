package com.example.app03

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import java.net.URL

// Tela de Busca de Criatura
class CriaturaActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var btnBuscar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvMensagem: TextView
    private lateinit var layoutResultado: LinearLayout
    private lateinit var ivCriatura: ImageView
    private lateinit var tvImagemIndisponivel: TextView
    private lateinit var tvNome: TextView
    private lateinit var hogwartsApi: HogwartsApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_criatura)
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
        ivCriatura = findViewById(R.id.ivCriatura)
        tvImagemIndisponivel = findViewById(R.id.tvImagemIndisponivel)
        tvNome = findViewById(R.id.tvNome)

        // Configura o Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hogwarts-api.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        hogwartsApi = retrofit.create(HogwartsApi::class.java)
    } //Fim onCreate

    // Chamado pelo onClick do botão Buscar.
    // Valida o texto digitado ANTES de consultar a API
    fun buscarCriatura(view: View) {
        // Regra 1: remove os espaços das pontas
        val nome = etNome.text.toString().trim()

        // Regra 2: o campo não pode estar vazio
        if (nome.isEmpty()) {
            etNome.error = "Digite o nome de uma criatura"
            etNome.requestFocus() // com o foco, a mensagem aparece no próprio campo
            return
        }

        // Regra 3: mínimo de 3 caracteres
        if (nome.length < 3) {
            etNome.error = "Digite pelo menos 3 caracteres"
            etNome.requestFocus()
            return
        }

        buscarCriaturaNaApi(nome)
    }

    // Função para buscar a criatura e a sua imagem usando corrotinas
    private fun buscarCriaturaNaApi(nome: String) {
        lifecycleScope.launch {
            try {
                showLoading()
                // Faz a requisição em segundo plano (fora da thread principal)
                val response = withContext(Dispatchers.IO) {
                    hogwartsApi.buscarCriaturas(nome)
                }

                if (response.criaturas.isEmpty()) {
                    showMensagem("Nenhuma criatura encontrada")
                } else {
                    val criatura = response.criaturas[0]

                    // Baixa a imagem também em segundo plano.
                    // O ProgressBar continua ativo até o download terminar
                    val bitmap = withContext(Dispatchers.IO) {
                        baixarImagem(criatura.imagem)
                    }

                    tvNome.text = criatura.nome
                    if (bitmap != null) {
                        ivCriatura.setImageBitmap(bitmap)
                        tvImagemIndisponivel.visibility = View.GONE
                    } else {
                        // Sem imagem ou falha no download: usa a imagem do próprio app
                        ivCriatura.setImageResource(R.drawable.ic_imagem_indisponivel)
                        tvImagemIndisponivel.visibility = View.VISIBLE
                    }
                    layoutResultado.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Sem internet ou erro no servidor: avisa o usuário e o app continua aberto
                Log.e("CriaturaActivity", "Erro ao buscar a criatura", e)
                showMensagem("Não foi possível consultar a API. Verifique sua conexão com a internet e tente novamente.")
            } finally {
                hideLoading()
            }
        } //Fim corrotina
    } //Fim buscarCriaturaNaApi

    // Faz o download da imagem da criatura.
    // Retorna null quando a criatura não tem imagem ou quando o download falha
    private fun baixarImagem(endereco: String?): Bitmap? {
        if (endereco.isNullOrBlank()) {
            return null
        }
        try {
            val url = URL(endereco)
            return BitmapFactory.decodeStream(url.openStream())
        } catch (e: Exception) {
            Log.e("CriaturaActivity", "Erro ao baixar a imagem", e)
            return null
        }
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
