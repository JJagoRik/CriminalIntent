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

open class Holder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root){
    open fun bind(crime: Crime){}
}

class CrimeHolder(val bindingCrime: ListItemCrimeBinding) : Holder(bindingCrime){
    override fun bind(crime: Crime){
        bindingCrime.crimeTitle.text = crime.title
        bindingCrime.crimeDate.text = DateFormat.format("EEEE, dd MMMM, yyyy", crime.date).toString()

        bindingCrime.root.setOnClickListener {
            Toast.makeText(
                bindingCrime.root.context,
                "${crime.title} clicked!",
                Toast.LENGTH_SHORT
            ).show()
        }

        bindingCrime.crimeSolved.visibility = if (crime.isSolved) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

class CrimePoliceHolder(val bindingPolice: ChallengeRequiresPoliceBinding) : Holder(bindingPolice){
    override fun bind(crime: Crime){
        bindingPolice.crimeTitle.text = crime.title
        bindingPolice.crimeDate.text = crime.date.toString()

        bindingPolice.root.setOnClickListener {
            Toast.makeText(
                bindingPolice.root.context,
                "${crime.title} clicked!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

class CrimeListAdapter(private val crimes: List<Crime>) : RecyclerView.Adapter<Holder>() {
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
        holder.bind(crime)
    }

//    override fun getItemViewType(position: Int): Int {
//        return if (crimes[position].requiresPolice) 1 else 0
//    }
}