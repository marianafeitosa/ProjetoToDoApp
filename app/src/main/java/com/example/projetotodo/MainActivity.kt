package com.example.projetotodo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var inputTask: EditText
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    private var editingId: String? = null
    private val taskList = ArrayList<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputTask = findViewById(R.id.inputTask)
        btnAdd = findViewById(R.id.btnAdd)
        recyclerView = findViewById(R.id.taskList)

        adapter = TaskAdapter(taskList,
            onEdit = { task ->
                editingId = task.id
                inputTask.setText(task.title)
                btnAdd.text = "✔"
            },
            onDelete = { task ->
                db.collection("tasks").document(task.id).delete()
                loadTasks()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val text = inputTask.text.toString().trim()

            if (text.isEmpty()) return@setOnClickListener

            if (editingId == null) {
                db.collection("tasks")
                    .add(mapOf("title" to text))
            } else {
                db.collection("tasks")
                    .document(editingId!!)
                    .update("title", text)

                editingId = null
                btnAdd.text = "+"
            }

            inputTask.setText("")
            loadTasks()
        }

        loadTasks()
    }

    private fun loadTasks() {
        db.collection("tasks").get().addOnSuccessListener { result ->
            taskList.clear()
            for (doc in result) {
                taskList.add(Task(doc.id, doc.getString("title") ?: ""))
            }
            adapter.notifyDataSetChanged()
        }
    }
}