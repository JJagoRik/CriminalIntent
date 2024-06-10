package com.bignerdranch.android.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.launch
import java.util.Date


private const val TAG = "CrimeDeatilFragment"
private const val DATE_FORMAT = "EEE, MMM, dd"


class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val args: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { parseContactSelection(it) }
    }

    private val makePhoneCall = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) {}

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) {isGranted: Boolean ->
            if (isGranted) {
                selectSuspect.launch(null)
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }


    private val binding
        get() = checkNotNull(_binding){
            "Cannont access binding because it is null"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            crimeCall.setOnClickListener {
                if (requireContext().checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                } else {
                    selectSuspect.launch(null)
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${crimeDetailViewModel.suspectPhoneNumber}")
                    startActivity(intent)
                }
            }

            val selectSuspectIntent = selectSuspect.contract.createIntent(requireContext(), null)
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent)
            crimeCall.isEnabled = canResolveIntent(selectSuspectIntent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE){ _, bundle ->
            val newDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    if (binding.crimeTitle.text.toString() == ""){
                        Toast.makeText(requireContext(), "Please enter name", Toast.LENGTH_SHORT).show()
                    } else {
                        findNavController().navigate(
                            CrimeDetailFragmentDirections.showCrimeList()
                        )
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(crime: Crime) {
        binding.apply {
            crimeSolved.isChecked = crime.isSolved
            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject)
                    )
                }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            crimeDate.text = crime.date.toString()
            crimeDate.setOnClickListener {
                findNavController().navigate(CrimeDetailFragmentDirections.selectDate(crime.date))
            }
            crimeSolved.isChecked = crime.isSolved

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }
        }
    }

    private fun getCrimeReport(crime: Crime): String{
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspectText)
    }

    private fun parseContactSelection(contactUri: Uri){
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        var queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }

            val phoneNumber = getPhoneNumber(contactUri)
            if (phoneNumber != null) {
                crimeDetailViewModel.suspectPhoneNumber = phoneNumber
            }
        }
    }

    private fun getPhoneNumber(contactUri: Uri): String? {
        val queryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        val queryCursor = requireActivity().contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                queryFields,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(contactUri.lastPathSegment),
                null
            )

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }

        return null
    }
//    private fun fetchPhoneNumber(contactId: String) {
//        val phoneCursor = requireActivity().contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
//            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
//            arrayOf(contactId),
//            null
//        )
//
//        phoneCursor?.use { pc ->
//            if (pc.moveToFirst()) {
//                val phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                dialPhoneNumber(phoneNumber)
//            }
//        }
//    }
//
//    private fun dialPhoneNumber(phoneNumber: String) {
//        val intent = Intent(Intent.ACTION_DIAL).apply {
//            data = Uri.parse("tel:$phoneNumber")
//        }
//        if (intent.resolveActivity(requireActivity().packageManager) != null) {
//            startActivity(intent)
//        }
//    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        )
        return resolvedActivity != null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.del_crime -> {
                deleteCrime()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteCrime() {
        findNavController().navigate(CrimeDetailFragmentDirections.showCrimeList())
        viewLifecycleOwner.lifecycleScope.launch {
            crimeDetailViewModel.delCrime(crimeDetailViewModel.crime.value!!)
        }
    }
}