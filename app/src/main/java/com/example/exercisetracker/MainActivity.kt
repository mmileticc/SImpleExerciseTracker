package com.example.exercisetracker

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.exercisetracker.data.DatabaseProvider
import com.example.exercisetracker.data.ExerciseDao
import com.example.exercisetracker.data.ExerciseEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class Exercise(val exerciseName: String) {
    PUSHUPS("pushups"),
    PULLUPS("pullups"),
    SITUPS("situps"),
    PLANK("plank"),
    SQUATS("squats")
}

class MainActivity : Activity() {

    private lateinit var tvDay: TextView
    private lateinit var tvMonth: TextView
    private lateinit var tvYear: TextView
    private lateinit var btnPickDate: Button
    private lateinit var tvTotalPushups: TextView
    private lateinit var tvTotalPullups: TextView
    private lateinit var tvTotalSquads: TextView
    private lateinit var tvTotalSitups: TextView
    private lateinit var tvTotalPlank: TextView
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var spinner: Spinner
    private lateinit var btnPlusPushups: Button
    private lateinit var btnMinusPushups: Button
    private lateinit var btnPlusPullups: Button
    private lateinit var btnMinusPullups: Button
    private lateinit var btnPlusSquads: Button
    private lateinit var btnMinusSquads: Button
    private lateinit var btnPlusSitups: Button
    private lateinit var btnMinusSitups: Button
    private lateinit var btnPlusPlank: Button
    private lateinit var btnMinusPlank: Button
    private lateinit var etPushups: EditText
    private lateinit var etPullups: EditText
    private lateinit var etSquads: EditText
    private lateinit var etSitups: EditText
    private lateinit var etPlank: EditText
    private lateinit var viewOptions :Array<String>
    private lateinit var dnevni :String
    private lateinit var mesecni :String
    private lateinit var godisnji :String

    private lateinit var chronometer: Chronometer
    private lateinit var btnStartStop: Button
    private lateinit var btnReset : Button

    private var isRunning = false
    private var pauseOffset: Long = 0

    private var limitPlank = 5
    private var limitPushups = 100
    private var limitPullups = 100
    private var limitSquads = 100
    private var limitSitups = 100

    private fun getHalf(count:Int):Int{
        var c = count / 2
        return if( c *2 == count ) c
        else c + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Povezivanje glavnog layouta
        setContentView(R.layout.activity_main)

        // Povezivanje UI elemenata sa kodom
        initilazeComponents()

        //Postavljanje u calendarView danasnji datum za tekuci
        setCurrentDate()

        //Postavljanje dogadjaja svim elementima
        initilazeListeners()

        val btnShowPopup = findViewById<Button>(R.id.btnShowPopup)

        btnShowPopup.setOnClickListener { showHintPopup(it) }

        readAllExercises()
    }

    private fun initilazeListeners() {
        //Postavljanje ponasanja dugmetu za biranje datume
        btnPickDate.setOnClickListener {
            showDatePicker()
        }
        //Postavljanje ponasanja padajucem meniju za izbor vremenskog perioda
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val selectedOption = parentView.getItemAtPosition(position) as String
                updateUIBasedOnSpinnerSelection(selectedOption)
                readAllExercises()
            }
            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing
            }
        }


        //postavljanje ponasanja dugmicima za sklekove
        btnPlusPushups.setOnClickListener {
            Log.d("MainActivity", "BtnPlusPushups clicked")
            addExercise(etPushups.text.toString().toIntOrNull() ?: 0,  Exercise.PUSHUPS.toString())
            etPushups.setText("")
        }
        btnMinusPushups.setOnClickListener {
            addExercise(etPushups.text.toString().toIntOrNull()?.times((-1)) ?: 0, Exercise.PUSHUPS.toString())
            etPushups.setText("")
        }
        //postavljanje ponasanja dugmicima za zgibove
        btnPlusPullups.setOnClickListener {
            addExercise(etPullups.text.toString().toIntOrNull() ?: 0,  Exercise.PULLUPS.toString())
            etPullups.setText("")
        }
        btnMinusPullups.setOnClickListener {
            addExercise(etPullups.text.toString().toIntOrNull()?.times((-1)) ?: 0,  Exercise.PULLUPS.toString())
            etPullups.setText("")
        }
        //postavljanje ponasanja dugmicima za plank
        btnPlusPlank.setOnClickListener {
            addExercise(etPlank.text.toString().toIntOrNull() ?: 0,  Exercise.PLANK.toString())
            etPlank.setText("")
        }
        btnMinusPlank.setOnClickListener {
            addExercise(etPlank.text.toString().toIntOrNull()?.times((-1)) ?: 0,  Exercise.PLANK.toString())
            etPlank.setText("")
        }
        //postavljanje ponasanja dugmicima za cucnjeve
        btnPlusSquads.setOnClickListener {
            addExercise(etSquads.text.toString().toIntOrNull() ?: 0,  Exercise.SQUATS.toString())
            etSquads.setText("")
        }
        btnMinusSquads.setOnClickListener {
            addExercise(etSquads.text.toString().toIntOrNull()?.times((-1)) ?: 0,  Exercise.SQUATS.toString())
            etSquads.setText("")
        }
        //postavljanje ponasanja dugmicima za trbusnjake
        btnPlusSitups.setOnClickListener {
            addExercise(etSitups.text.toString().toIntOrNull() ?: 0,  Exercise.SITUPS.toString())
            etSitups.setText("")
        }
        btnMinusSitups.setOnClickListener {
            addExercise(etSitups.text.toString().toIntOrNull()?.times((-1)) ?: 0,  Exercise.SITUPS.toString())
            etSitups.setText("")
        }
        btnStartStop.setOnClickListener {
            if (!isRunning) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                btnStartStop.text = "Stop"
                isRunning = true
            } else {
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                chronometer.stop()
                btnStartStop.text = "Start"
                isRunning = false
            }
        }

        btnReset.setOnClickListener {
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
            chronometer.stop()
            isRunning = false
            btnStartStop.text = "Start"
        }
    }

    private fun initilazeComponents(){
        exerciseDao = DatabaseProvider.getDatabase(this).exerciseDao()
        spinner = findViewById(R.id.spinnerViewType)

        tvDay = findViewById(R.id.tvDay)
        tvMonth = findViewById(R.id.tvMonth)
        tvYear = findViewById(R.id.tvYear)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvTotalPushups = findViewById(R.id.tvTotalPushups)
        tvTotalPullups = findViewById(R.id.tvTotalPullups)
        tvTotalSquads = findViewById(R.id.tvTotalSquads)
        tvTotalSitups = findViewById(R.id.tvTotalSitups)
        tvTotalPlank = findViewById(R.id.tvTotalPlank)

        btnPlusPushups = findViewById(R.id.btnPlusPushups)
        btnMinusPushups = findViewById(R.id.btnMinusPushups)
        btnPlusPullups = findViewById(R.id.btnPlusPullups)
        btnMinusPullups = findViewById(R.id.btnMinusPullups)
        btnPlusSitups = findViewById(R.id.btnPlusSitups)
        btnMinusSitups = findViewById(R.id.btnMinusSitups)
        btnPlusSquads = findViewById(R.id.btnPlusSquats)
        btnMinusSquads = findViewById(R.id.btnMinusSquats)
        btnPlusPlank = findViewById(R.id.btnPlusPlank)
        btnMinusPlank = findViewById(R.id.btnMinusPlank)

        etPushups = findViewById(R.id.etPushups)
        etPullups = findViewById(R.id.etPullups)
        etSitups = findViewById(R.id.etSitups)
        etSquads = findViewById(R.id.etSquats)
        etPlank = findViewById(R.id.etPlank)

        viewOptions = resources.getStringArray(R.array.view_options)
        dnevni = viewOptions[0]  // "Dnevni"
        mesecni = viewOptions[1]  // "Mesečni"
        godisnji = viewOptions[2]  // "Godišnji"

        chronometer = findViewById(R.id.chronometer)
        btnStartStop = findViewById(R.id.btnStartStop)
        btnReset = findViewById(R.id.btnReset)

    }

    @SuppressLint("SetTextI18n")
    private fun setCurrentDate() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // Meseci su 0-indeksirani
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val selectedCalendar = Calendar.getInstance().apply {
            set(currentYear, currentMonth, currentDay)
        }
        // Formatiraj mesec koristeći SimpleDateFormat
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        val monthAbbreviation = sdf.format(selectedCalendar.time)

        // Postavi vrednosti u EditText polja
        tvDay.setText("$currentDay.")
        tvDay.tag = currentDay
        tvMonth.tag = currentMonth + 1
        tvMonth.setText(monthAbbreviation) // Prikazuje skraćeno ime meseca
        tvYear.tag = currentYear
        tvYear.setText("$currentYear.")

    }

    @SuppressLint("SetTextI18n")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val sdf = SimpleDateFormat("MMM", Locale.getDefault())
                val monthAbbreviation = sdf.format(selectedCalendar.time)
                val numericMonth = selectedMonth + 1  // 1-based

                tvDay.text = "$selectedDay."
                tvDay.tag = selectedDay
                tvMonth.text = monthAbbreviation
                tvMonth.tag = numericMonth  // Čuvamo broj meseca
                tvYear.text = "$selectedYear."
                tvYear.tag = selectedYear
                readAllExercises() // azurira vrednosti svih ispisa jer je promenjen datum
            },
            currentYear,
            currentMonth,
            currentDay
        )
        datePickerDialog.show()

    }

    private fun updateUIBasedOnSpinnerSelection(selection: String) {
        when (selection) {
            dnevni -> {
                // Omogućiti sve: dan, mesec, godina
                tvDay.setEnabled(true)
                tvMonth.setEnabled(true)
                tvYear.setEnabled(true)
                tvDay.setVisibility(View.VISIBLE)
                tvMonth.setVisibility(View.VISIBLE)
                tvYear.setVisibility(View.VISIBLE)
            }
            mesecni -> {
                // Omogućiti samo mesec i godinu
                tvDay.setEnabled(false)
                tvMonth.setEnabled(true)
                tvYear.setEnabled(true)
                tvDay.setVisibility(View.INVISIBLE)
                tvMonth.setVisibility(View.VISIBLE)
                tvYear.setVisibility(View.VISIBLE)
            }
            godisnji -> {
                // Omogućiti samo godinu
                tvDay.setEnabled(false)
                tvMonth.setEnabled(false)
                tvYear.setEnabled(true)
                tvDay.setVisibility(View.INVISIBLE)
                tvMonth.setVisibility(View.INVISIBLE)
                tvYear.setVisibility(View.VISIBLE)
            }
            else -> {
                // Default
                tvDay.setEnabled(true)
                tvMonth.setEnabled(true)
                tvYear.setEnabled(true)
                tvDay.setVisibility(View.VISIBLE)
                tvMonth.setVisibility(View.VISIBLE)
                tvYear.setVisibility(View.VISIBLE)
            }
        }
    }

    /*private fun addExerciseOriginal(countToAdd: Int, exercise: String) {
        val day = tvDay.tag as? Int ?: return
        val numericMonth = tvMonth.tag as? Int ?: return
        val year = tvYear.tag as? Int ?: return
        CoroutineScope(Dispatchers.IO).launch {
            exerciseDao.insertEntry(
                ExerciseEntry(
                    day = day,
                    month = numericMonth,
                    year = year,
                    exerciseType = exercise,
                    count = countToAdd
                )
            )
           readExercise(exercise)
        }
    }*/
    private fun addExercise(countToAdd: Int, exercise: String) {
        val day = tvDay.tag as? Int ?: return
        val numericMonth = tvMonth.tag as? Int ?: return
        val year = tvYear.tag as? Int ?: return

        CoroutineScope(Dispatchers.IO).launch {
            // Pokušaj da dohvatiš postojeći zapis
            val existingEntry = exerciseDao.getEntry(day, numericMonth, year, exercise)
            if (existingEntry != null) {
                // Ako postoji, ažuriraj broj ponavljanja
                val newCount = existingEntry.count + countToAdd
                // Opcionalno, spreči da broj bude negativan:
                val updatedCount = if (newCount < 0) 0 else newCount
                val updatedEntry = existingEntry.copy(count = updatedCount)
                exerciseDao.updateEntry(updatedEntry)
            } else {
                // Ako zapis ne postoji, napravi novi
                exerciseDao.insertEntry(
                    ExerciseEntry(
                        day = day,
                        month = numericMonth,
                        year = year,
                        exerciseType = exercise,
                        count = countToAdd
                    )
                )
            }
            // Nakon ažuriranja/ubacivanja, osveži prikaz
            readExercise(exercise)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun readExercise(exercise: String){
        val selectedPeriod = spinner.selectedItem.toString()
        val day = tvDay.tag as? Int ?: return
        val numericMonth = tvMonth.tag as? Int ?: return
        val year = tvYear.tag as? Int ?: return

        CoroutineScope(Dispatchers.IO).launch {

            val total = when (selectedPeriod) {
                dnevni -> exerciseDao.getTotalForDay(year, numericMonth, day, exercise) ?: 0
                mesecni -> exerciseDao.getTotalForMonth(year, numericMonth, exercise) ?: 0
                godisnji -> exerciseDao.getTotalForYear(year, exercise) ?: 0
                else -> 0
            }

            runOnUiThread {
                when (exercise){
                    Exercise.PUSHUPS.toString() -> {
                        tvTotalPushups.text = total.toString();
                        colorBackground(limitPushups, total, tvTotalPushups)
                    }
                    Exercise.PULLUPS.toString() -> {
                        tvTotalPullups.text = total.toString()
                        colorBackground(limitPullups, total, tvTotalPullups)
                    }
                    Exercise.PLANK.toString() -> {
                        tvTotalPlank.text = total.toString()
                        colorBackground(limitPlank, total, tvTotalPlank)
                    }
                    Exercise.SQUATS.toString() -> {
                        tvTotalSquads.text = total.toString()
                        colorBackground(limitSquads, total, tvTotalSquads)

                    }
                    Exercise.SITUPS.toString() -> {
                        tvTotalSitups.text = total.toString()
                        colorBackground(limitSitups, total, tvTotalSitups)
                    }

                }
            }
        }
    }
    private fun colorBackground(highLimit:Int, count:Int, tv:TextView){
        val mediumLimit = getHalf(highLimit)
        val dnevni = spinner.selectedItem.toString() == dnevni
        if(dnevni){
            if(count < mediumLimit) tv.setBackgroundColor(ContextCompat.getColor(this, R.color.redNotEnough)) //crvena
            else if(count < highLimit) tv.setBackgroundColor(ContextCompat.getColor(this, R.color.orangeHalfDone)) //narandzasta
            else tv.setBackgroundColor(ContextCompat.getColor(this, R.color.greenEnough)) //zelena
        }else tv.setBackgroundColor(Color.TRANSPARENT)
    }
    private fun readAllExercises(){
        readExercise(Exercise.PUSHUPS.toString())
        readExercise(Exercise.PULLUPS.toString())
        readExercise(Exercise.PLANK.toString())
        readExercise(Exercise.SITUPS.toString())
        readExercise(Exercise.SQUATS.toString())
    }

    private fun showHintPopup(view: View) {
        // Inflater za popup layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_hint, null)

        // Kreiranje PopupWindow objekta
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Klik izvan zatvara popup
        )

        // Prikazi popup ispod dugmeta
        popupWindow.showAsDropDown(view, 0, 0)

        // Postavljanje teksta programatski ako treba
        //val tvHintText = popupView.findViewById<TextView>(R.id.tvRedHint)
        //tvHintText.text = "Ovo je moj prilagođeni tekst uputstva!"

        // Dugme za zatvaranje popup-a
        val btnClosePopup = popupView.findViewById<Button>(R.id.btnClosePopup)
        btnClosePopup.setOnClickListener {
            popupWindow.dismiss()
        }
    }
}






