/*
 * Property of TotalPay (https://totalpay.global).
 */

package com.totalpay.sample.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.totalpay.sample.R
import com.totalpay.sample.databinding.ActivityCaptureBinding
import com.totalpay.sample.app.TotalPayTransactionStorage
import com.totalpay.sample.app.preattyPrint
import com.totalpay.sdk.core.TotalPaySdk
import com.totalpay.sdk.model.response.base.error.TotalPayError
import com.totalpay.sdk.model.response.capture.TotalPayCaptureCallback
import com.totalpay.sdk.model.response.capture.TotalPayCaptureResponse
import com.totalpay.sdk.model.response.capture.TotalPayCaptureResult
import java.util.*

class TotalPayCaptureActivity : AppCompatActivity(R.layout.activity_capture) {

    private lateinit var binding: ActivityCaptureBinding
    private lateinit var totalPayTransactionStorage: TotalPayTransactionStorage

    private var selectedTransaction: TotalPayTransactionStorage.Transaction? = null
    private var transactions: List<TotalPayTransactionStorage.Transaction>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        totalPayTransactionStorage = TotalPayTransactionStorage(this)
        binding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureView()
    }

    private fun configureView() {
        binding.btnLoadCapture.setOnClickListener {
            transactions = totalPayTransactionStorage.getCaptureTransactions()
            invalidateSpinner()
        }
        binding.btnLoadAll.setOnClickListener {
            transactions = totalPayTransactionStorage.getAllTransactions()
            invalidateSpinner()
        }
        binding.btnCapture.setOnClickListener {
            executeRequest()
        }

        invalidateSpinner()
    }

    private fun invalidateSpinner() {
        binding.spinnerTransactions.apply {
            val prettyTransactions = transactions
                .orEmpty()
                .map { it.toString() }
                .toMutableList()
                .apply {
                    add(0, "Select Transaction")
                }

            adapter = object : ArrayAdapter<String>(
                this@TotalPayCaptureActivity,
                android.R.layout.simple_spinner_dropdown_item,
                prettyTransactions
            ) {
                override fun isEnabled(position: Int): Boolean {
                    return position != 0
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return super.getDropDownView(position, convertView, parent).apply {
                        alpha = if (position == 0) 0.5F else 1.0F
                    }
                }
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    parent?.get(0)?.alpha = if (position == 0) 0.5F else 1.0F

                    if (transactions.isNullOrEmpty()) {
                        invalidateSelectedTransaction()
                        return
                    }

                    selectedTransaction = if (position == 0) {
                        null
                    } else {
                        transactions?.get((position - 1).coerceAtLeast(0))
                    }

                    invalidateSelectedTransaction()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedTransaction = null
                    invalidateSelectedTransaction()
                }
            }

            invalidateSelectedTransaction()
        }

    }

    private fun invalidateSelectedTransaction() {
        binding.txtSelectedTransaction.text = selectedTransaction?.preattyPrint()
        binding.btnCapture.isEnabled = selectedTransaction != null
    }

    private fun onRequestStart() {
        binding.progressBar.show()
        binding.txtResponse.text = ""
    }

    private fun onRequestFinish() {
        binding.progressBar.hide()
    }

    private fun executeRequest() {
        selectedTransaction?.let { selectedTransaction ->
            val amount = try {
                binding.etxtAmount.text.toString().toDouble()
            } catch (e: Exception) {
                0.00
            }

            val transaction = TotalPayTransactionStorage.Transaction(
                payerEmail = selectedTransaction.payerEmail,
                cardNumber = selectedTransaction.cardNumber
            )

            onRequestStart()
            TotalPaySdk.Adapter.CAPTURE.execute(
                transactionId = selectedTransaction.id,
                payerEmail = selectedTransaction.payerEmail,
                cardNumber = selectedTransaction.cardNumber,
                amount = amount,
                callback = object : TotalPayCaptureCallback {
                    override fun onResponse(response: TotalPayCaptureResponse) {
                        super.onResponse(response)
                        onRequestFinish()
                        binding.txtResponse.text = response.preattyPrint()
                    }

                    override fun onResult(result: TotalPayCaptureResult) {
                        transaction.fill(result.result)

                        totalPayTransactionStorage.addTransaction(transaction)
                    }

                    override fun onError(error: TotalPayError) = Unit

                    override fun onFailure(throwable: Throwable) {
                        super.onFailure(throwable)
                        onRequestFinish()
                        binding.txtResponse.text = throwable.preattyPrint()
                    }
                }
            )
        } ?: invalidateSelectedTransaction()
    }
}
