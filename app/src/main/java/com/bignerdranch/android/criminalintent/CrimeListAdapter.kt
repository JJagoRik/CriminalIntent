package com.bignerdranch.android.criminalintent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.bignerdranch.android.criminalintent.databinding.ChallengeRequiresPoliceBinding
import com.bignerdranch.android.criminalintent.databinding.ListItemCrimeBinding
import android.text.format.DateFormat
import java.util.UUID

open class Holder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root){
    open fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit){}
}

class CrimeHolder(val bindingCrime: ListItemCrimeBinding) : Holder(bindingCrime){
    override fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit){
        bindingCrime.crimeTitle.text = crime.title
        bindingCrime.crimeDate.text = DateFormat.format("EEEE, dd MMMM, yyyy", crime.date).toString()

        bindingCrime.root.setOnClickListener {
            onCrimeClicked(crime.id)
        }

        bindingCrime.crimeSolved.visibility = if (crime.isSolved) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

class CrimePoliceHolder(val bindingPolice: ChallengeRequiresPoliceBinding) : Holder(bindingPolice){
    override fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit){
        bindingPolice.crimeTitle.text = crime.title
        bindingPolice.crimeDate.text = crime.date.toString()

        bindingPolice.root.setOnClickListener {
            onCrimeClicked(crime.id)
        }
    }
}

class CrimeListAdapter(
    private val crimes: List<Crime>,
    private val onCrimeClicked: (crimeId: UUID) -> Unit) : RecyclerView.Adapter<Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == 0) {
            val binding = ListItemCrimeBinding.inflate(inflater, parent, false)
            return CrimeHolder(binding)
        } else {
            val binding = ChallengeRequiresPoliceBinding.inflate(inflater, parent, false)
            return CrimePoliceHolder(binding)
        }
    }

    override fun getItemCount() = crimes.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val crime = crimes[position]
        holder.bind(crime, onCrimeClicked)
    }

//    override fun getItemViewType(position: Int): Int {
//        return if (crimes[position].requiresPolice) 1 else 0
//    }
}