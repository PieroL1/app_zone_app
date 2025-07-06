package com.example.app_s10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_s10.model.Game
import com.example.app_s10.model.GameAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    
    // Views
    private lateinit var tvWelcome: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var cardStats: CardView
    private lateinit var cardAchievements: CardView
    private lateinit var cardProfile: CardView
    private lateinit var cardSettings: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private var listaJuegos = mutableListOf<Game>()

    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvJuegos)
        gameAdapter = GameAdapter(listaJuegos)
        recyclerView.adapter = gameAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Verificar autenticación
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Usuario no autenticado, redirigir al login
            redirectToLogin()
            return
        }
        
        // Configurar UI
        setupUI()
        setupWindowInsets()
        
        // Cargar información del usuario
        loadUserInfo(currentUser)
        
        // Configurar listeners
        setupClickListeners()
        
        Log.d(TAG, "MainActivity iniciado para usuario: ${currentUser.email}")


        // Para ESCRIBIR un dato simple:
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("mensaje")
        ref.setValue("¡Hola, Firebase! :)")

        // Para LEER ese dato:
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java)
                Log.d("Firebase", "Valor leído: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Error al leer: ", error.toException())
            }
        })

        leerJuegosUI()

        val btnAgregarJuego = findViewById<Button>(R.id.btnAgregarJuego)
        btnAgregarJuego.setOnClickListener {
            mostrarDialogoAgregarJuego()
        }


    }

    private fun mostrarDialogoAgregarJuego() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_juego, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etGenero = dialogView.findViewById<EditText>(R.id.etGenero)
        val etAnio = dialogView.findViewById<EditText>(R.id.etAnio)

        AlertDialog.Builder(
                this,
            com.google.android.material.R.style.Theme_Material3_Dark_Dialog // usa este theme
        )
            .setTitle("Agregar Nuevo Juego")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = etNombre.text.toString()
                val genero = etGenero.text.toString()
                val anio = etAnio.text.toString().toIntOrNull() ?: 0
                if (nombre.isNotBlank() && genero.isNotBlank() && anio > 0) {
                    crearJuego(nombre, genero, anio)
                } else {
                    Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun setupUI() {
        // Inicializar views
        tvWelcome = findViewById(R.id.tv_welcome)
        tvUserEmail = findViewById(R.id.tv_user_email)
        btnLogout = findViewById(R.id.btn_logout)
        cardStats = findViewById(R.id.card_stats)
        cardAchievements = findViewById(R.id.card_achievements)
        cardProfile = findViewById(R.id.card_profile)
        cardSettings = findViewById(R.id.card_settings)
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun loadUserInfo(user: FirebaseUser) {
        // Personalizar saludo según el tipo de usuario
        val welcomeMessage = if (user.isAnonymous) {
            "¡Hola, Invitado!"
        } else {
            "¡Hola, ${user.displayName ?: "Gamer"}!"
        }
        
        tvWelcome.text = welcomeMessage
        
        // Mostrar email o indicar usuario anónimo
        tvUserEmail.text = if (user.isAnonymous) {
            "Usuario invitado"
        } else {
            user.email ?: "Sin email"
        }
        
        // Verificar estado de verificación de email
        if (!user.isAnonymous && user.email != null && !user.isEmailVerified) {
            showEmailVerificationDialog()
        }
    }
    
    private fun setupClickListeners() {
        // Botón logout
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        
        // Cards de navegación (placeholder por ahora)
        cardStats.setOnClickListener {
            showFeatureComingSoon("Estadísticas del Jugador")
        }
        
        cardAchievements.setOnClickListener {
            showFeatureComingSoon("Logros")
        }
        
        cardProfile.setOnClickListener {
            showFeatureComingSoon("Perfil")
        }
        
        cardSettings.setOnClickListener {
            showFeatureComingSoon("Configuración")
        }
    }
    
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Usuario desconectado")
        redirectToLogin()
    }
    
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showEmailVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Verificar Email")
            .setMessage(getString(R.string.auth_email_verification_required))
            .setPositiveButton("Enviar verificación") { _, _ ->
                sendEmailVerification()
            }
            .setNegativeButton("Más tarde", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }
    
    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.auth_verification_email_sent), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error al enviar verificación", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun showFeatureComingSoon(featureName: String) {
        AlertDialog.Builder(this)
            .setTitle("Próximamente")
            .setMessage("La función '$featureName' será implementada en futuras versiones.")
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }
    
    override fun onStart() {
        super.onStart()
        // Verificar autenticación cada vez que la actividad se vuelve visible
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d(TAG, "Usuario no autenticado en onStart, redirigiendo...")
            redirectToLogin()
        }
    }

    fun crearJuego(nombre: String, genero: String, anio: Int) {
        val database = FirebaseDatabase.getInstance()
        val juegosRef = database.getReference("juegos")
        val id = juegosRef.push().key   // Genera un ID único
        if (id != null) {
            val juego = Game(id, nombre, genero, anio)
            juegosRef.child(id).setValue(juego)
                .addOnSuccessListener {
                    Log.d("CRUD", "Juego creado exitosamente")
                }
                .addOnFailureListener {
                    Log.e("CRUD", "Error al crear juego", it)
                }
        }
    }

    fun leerJuegosUI() {
        val database = FirebaseDatabase.getInstance()
        val juegosRef = database.getReference("juegos")
        juegosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevosJuegos = mutableListOf<Game>()
                for (juegoSnapshot in snapshot.children) {
                    val juego = juegoSnapshot.getValue(Game::class.java)
                    juego?.let { nuevosJuegos.add(it) }
                }
                listaJuegos.clear()
                listaJuegos.addAll(nuevosJuegos)
                gameAdapter.actualizarLista(listaJuegos)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    fun actualizarJuego(juego: Game) {
        val database = FirebaseDatabase.getInstance()
        val juegosRef = database.getReference("juegos")
        if (juego.id != null) {
            juegosRef.child(juego.id!!).setValue(juego)
                .addOnSuccessListener {
                    Log.d("CRUD", "Juego actualizado correctamente")
                }
                .addOnFailureListener {
                    Log.e("CRUD", "Error al actualizar juego", it)
                }
        }
    }

    fun eliminarJuego(id: String) {
        val database = FirebaseDatabase.getInstance()
        val juegosRef = database.getReference("juegos")
        juegosRef.child(id).removeValue()
            .addOnSuccessListener {
                Log.d("CRUD", "Juego eliminado correctamente")
            }
            .addOnFailureListener {
                Log.e("CRUD", "Error al eliminar juego", it)
            }
    }


}