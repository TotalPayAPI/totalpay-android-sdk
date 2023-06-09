/*
 * Property of TotalPay (https://totalpay.global).
 */

package com.totalpay.sample.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.get
import com.totalpay.sample.R
import com.totalpay.sample.app.TotalPayTransactionStorage
import com.totalpay.sample.app.preattyPrint
import com.totalpay.sample.databinding.ActivityRecurringSaleBinding
import com.totalpay.sample.databinding.ActivitySaleBinding
import com.totalpay.sdk.core.TotalPaySdk
import com.totalpay.sdk.model.request.card.TotalPayTestCard
import com.totalpay.sdk.model.request.options.TotalPayRecurringOptions
import com.totalpay.sdk.model.request.options.TotalPaySaleOptions
import com.totalpay.sdk.model.request.order.TotalPayOrder
import com.totalpay.sdk.model.request.order.TotalPaySaleOrder
import com.totalpay.sdk.model.request.payer.TotalPayPayer
import com.totalpay.sdk.model.request.payer.TotalPayPayerOptions
import com.totalpay.sdk.model.response.base.error.TotalPayError
import com.totalpay.sdk.model.response.sale.TotalPaySaleCallback
import com.totalpay.sdk.model.response.sale.TotalPaySaleResponse
import com.totalpay.sdk.model.response.sale.TotalPaySaleResult
import io.kimo.lib.faker.Faker
import java.text.DecimalFormat
import java.util.*

class TotalPayRecurringSaleActivity : AppCompatActivity(R.layout.activity_recurring_sale) {

    private lateinit var binding: ActivityRecurringSaleBinding
    private lateinit var totalPayTransactionStorage: TotalPayTransactionStorage

    private var selectedTransaction: TotalPayTransactionStorage.Transaction? = null
    private var transactions: List<TotalPayTransactionStorage.Transaction>? = null

    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        totalPayTransactionStorage = TotalPayTransactionStorage(this)
        binding = ActivityRecurringSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureView()
    }

    private fun configureView() {
        binding.btnLoadRecurringSale.setOnClickListener {
            transactions = totalPayTransactionStorage.getRecurringSaleTransactions()
            invalidateSpinner()
        }
        binding.btnLoadAll.setOnClickListener {
            transactions = totalPayTransactionStorage.getAllTransactions()
            invalidateSpinner()
        }
        binding.btnRandomize.setOnClickListener {
            randomize()
        }
        binding.btnAuth.setOnClickListener {
            executeRequest(true)
        }
        binding.btnSale.setOnClickListener {
            executeRequest(false)
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
                this@TotalPayRecurringSaleActivity,
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

        binding.btnAuth.isEnabled = selectedTransaction != null
        binding.btnSale.isEnabled = selectedTransaction != null

        binding.etxtRecurringFirstTransId.setText(selectedTransaction?.id)
        binding.etxtRecurringToken.setText(selectedTransaction?.recurringToken)
    }

    private fun randomize() {
        binding.etxtOrderId.setText(UUID.randomUUID().toString())
        binding.etxtOrderAmount.setText(DecimalFormat("#.##").format(random.nextDouble() * 10_000))
        binding.etxtOrderDescription.setText(Faker.Lorem.sentences())

        binding.txtResponse.text = ""
    }

    private fun onRequestStart() {
        binding.progressBar.show()
        binding.txtResponse.text = ""
    }

    private fun onRequestFinish() {
        binding.progressBar.hide()
    }

    private fun executeRequest(isAuth: Boolean) {
        selectedTransaction?.let { selectedTransaction ->
            val amount = try {
                binding.etxtOrderAmount.text.toString().toDouble()
            } catch (e: Exception) {
                0.0
            }

            val order = TotalPayOrder(
                id = binding.etxtOrderId.text.toString(),
                amount = amount,
                description = binding.etxtOrderDescription.text.toString()
            )

            val recurringOptions = TotalPayRecurringOptions(
                firstTransactionId = binding.etxtRecurringFirstTransId.text.toString(),
                token = binding.etxtRecurringToken.text.toString()
            )

            val transaction = TotalPayTransactionStorage.Transaction(
                payerEmail = selectedTransaction.payerEmail,
                cardNumber = selectedTransaction.cardNumber
            )

            onRequestStart()
            TotalPaySdk.Adapter.RECURRING_SALE.execute(
                order = order,
                options = recurringOptions,
                payerEmail = selectedTransaction.payerEmail,
                cardNumber = selectedTransaction.cardNumber,
                auth = isAuth,
                callback = object : TotalPaySaleCallback {
                    override fun onResponse(response: TotalPaySaleResponse) {
                        super.onResponse(response)
                        onRequestFinish()
                        binding.txtResponse.text = response.preattyPrint()
                    }

                    override fun onResult(result: TotalPaySaleResult) {
                        transaction.fill(result.result)
                        transaction.isAuth = isAuth
                        if (result is TotalPaySaleResult.Recurring) {
                            transaction.recurringToken = result.result.recurringToken
                        }

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
