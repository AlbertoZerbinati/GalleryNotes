package com.esp.gallerynotes.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.esp.gallerynotes.R
import com.esp.gallerynotes.database.NoteViewModel
import com.esp.gallerynotes.database.Task
import com.esp.gallerynotes.utils.Priority
import com.esp.gallerynotes.utils.SharedTaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.Instant.now
import java.util.*

class TodoBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var taskEt: EditText
    private lateinit var priorityButton: ImageButton
    private lateinit var priorityRadioGroup: RadioGroup
    private lateinit var saveButton: ImageButton
    private lateinit var taskViewModel: NoteViewModel

    var task: Task? = null


    private lateinit var sharedTaskViewModel: SharedTaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet, container, false)
        taskEt = view.findViewById(R.id.enter_todo_et)
        priorityButton = view.findViewById(R.id.priority_todo_button)
        priorityRadioGroup = view.findViewById(R.id.radioGroup_priority)
        saveButton = view.findViewById(R.id.save_todo_button)

        taskViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        return view
    }

    override fun onResume() {
        super.onResume()

        task = sharedTaskViewModel.selectedTask.value
        sharedTaskViewModel.selectedTask.value = null

        if(task !== null) {
            taskEt.setText(task!!.content)
            priorityRadioGroup.check(when(task!!.priority) {
                Priority.HIGH -> R.id.radioButton_high
                Priority.MEDIUM -> R.id.radioButton_med
                Priority.LOW -> R.id.radioButton_low
            })
        }
        else {
            taskEt.setText("")
        }
    }

    override fun onPause() {
        super.onPause()
        sharedTaskViewModel.selectedTask.value = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedTaskViewModel = ViewModelProvider(requireActivity()).get(SharedTaskViewModel::class.java)

        saveButton.setOnClickListener {
            val text = taskEt.text.toString().trim()
            val priorityId = priorityRadioGroup.checkedRadioButtonId
            val priority: Priority = when (priorityId) {
                R.id.radioButton_high -> Priority.HIGH
                R.id.radioButton_med -> Priority.MEDIUM
                else -> Priority.LOW
            }
            if (text.isNotEmpty()) {
                if (task == null) {
                    // new task
                    task = Task(0, text, priority, Calendar.getInstance().time, false)
                    taskViewModel.insertTask(task!!)
                } else {
                    // update task
                    task = Task(task!!.id, text, priority, Calendar.getInstance().time, task!!.isDone)
                    taskViewModel.updateTask(task!!)
                }
            } else {
                Toast.makeText(activity?.applicationContext, getString(R.string.empty_task), Toast.LENGTH_SHORT).show()
            }


            dismiss() // hides the BSD
        }

        priorityButton.setOnClickListener{
            priorityRadioGroup.visibility = when(priorityRadioGroup.isVisible) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        }
    }
}