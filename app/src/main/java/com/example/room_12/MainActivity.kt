package com.example.room_12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.room.Update
import com.example.room_12.database.Note
import com.example.room_12.database.NoteDao
import com.example.room_12.database.NoteRoomDatabase
import com.example.room_12.databinding.ActivityMainBinding
import java.sql.RowId
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNiteDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNiteDao = db!!.nodeDao()!!
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            btnAdd.setOnClickListener(View.OnClickListener {
                insert(
                    Note(
                        title = txtTitle.text.toString(),
                        description = txtDescription.text.toString(),
                        date = txtDate.text.toString()
                    )
                )
                resetForm()
            })

            btnUpdate.setOnClickListener{
                update(
                    Note(
                        id = updateId,
                        title = txtTitle.text.toString(),
                        description = txtDescription.text.toString(),
                        date = txtDate.text.toString()
                    )
                )
                updateId = 0
                resetForm()
            }

            lvItem.setOnItemClickListener { adapterView, view, position, id ->
                val item = adapterView.adapter.getItem(position) as Note
                updateId = item.id
                txtTitle.setText(item.title)
                txtDescription.setText(item.description)
                txtDate.setText(item.date)
            }

            lvItem.onItemLongClickListener =
                AdapterView.OnItemLongClickListener{ adapterView, view, position, id ->
                    val item = adapterView.adapter.getItem(position) as Note
                    delete(item)
                    true
                }
        }
    }

    private fun getNotes() {
        mNiteDao.allNotes.observe(this) { notes ->
            val adapter:ArrayAdapter<Note> = ArrayAdapter<Note>(
                this,
                android.R.layout.simple_list_item_1, notes
            )
            binding.lvItem.adapter = adapter
        }
    }

    private fun insert(note: Note) {
        executorService.execute {mNiteDao.insert(note) }
    }

    private fun update(note: Note) {
        executorService.execute {mNiteDao.updates(note) }
    }

    private fun delete(note: Note) {
        executorService.execute {mNiteDao.delete(note) }
    }

    override fun onResume() {
        super.onResume()
        getNotes()
    }

    private fun resetForm() {
        with(binding) {
            txtTitle.setText("")
            txtDescription.setText("")
            txtDate.setText("")
        }
    }
}