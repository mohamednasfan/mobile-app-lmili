package com.example.imilipocket.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.R
import com.example.imilipocket.databinding.FragmentSettingsBinding
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        viewModel = ViewModelProvider(this, SettingsViewModelFactory(preferenceManager))
            .get(SettingsViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup currency spinner
        val currencies = listOf(
            getString(R.string.currency_usd),
            getString(R.string.currency_eur),
            getString(R.string.currency_gbp),
            getString(R.string.currency_jpy),
            getString(R.string.currency_inr),
            getString(R.string.currency_aud),
            getString(R.string.currency_cad),
            getString(R.string.currency_lkr),
            getString(R.string.currency_cny),
            getString(R.string.currency_sgd),
            getString(R.string.currency_myr),
            getString(R.string.currency_thb),
            getString(R.string.currency_idr),
            getString(R.string.currency_php),
            getString(R.string.currency_vnd),
            getString(R.string.currency_krw),
            getString(R.string.currency_aed),
            getString(R.string.currency_sar),
            getString(R.string.currency_qar)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            currencies
        )

        binding.spinnerCurrency.apply {
            setAdapter(adapter)
            threshold = 1

            // Set current currency
            val currentCurrency = preferenceManager.getSelectedCurrency()
            val currencyIndex = currencies.indexOfFirst { it.startsWith(currentCurrency) }
            if (currencyIndex != -1) {
                setText(currencies[currencyIndex], false)
            }

            setOnItemClickListener { _, _, position, _ ->
                val selectedCurrency = adapter.getItem(position).toString()
                val currencyCode = selectedCurrency.substring(0, 3)
                preferenceManager.setSelectedCurrency(currencyCode)
                Toast.makeText(requireContext(), "Currency updated to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSaveCurrency.setOnClickListener {
                val selectedCurrency = spinnerCurrency.text.toString()
                viewModel.setSelectedCurrency(selectedCurrency)
                Toast.makeText(requireContext(), "Currency saved", Toast.LENGTH_SHORT).show()
            }

            // Handle backup button click
            btnBackup.setOnClickListener {
                backupTransactions()
            }

            // Handle restore button click
            btnRestore.setOnClickListener {
                restoreTransactions()
            }
        }
    }

    private fun observeViewModel() {
        // Implement the logic to observe the ViewModel if necessary
    }

    private fun backupTransactions() {
        try {
            val transactions = preferenceManager.getTransactions() // Get current transactions
            val json = Gson().toJson(transactions) // Convert transactions to JSON

            // Save the JSON string to a file
            val file = File(requireContext().filesDir, "transactions_backup.json")
            file.writeText(json)

            Toast.makeText(requireContext(), "Backup successful", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Backup failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreTransactions() {
        try {
            val file = File(requireContext().filesDir, "transactions_backup.json")

            if (file.exists()) {
                val json = file.readText() // Read the backup JSON file
                val transactionsType = object : TypeToken<List<Transaction>>() {}.type
                val transactions: List<Transaction> = Gson().fromJson(json, transactionsType) // Deserialize JSON to list of transactions

                // Save the restored transactions to SharedPreferences
                preferenceManager.saveTransactions(transactions)

                Toast.makeText(requireContext(), "Restore successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No backup file found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Restore failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
