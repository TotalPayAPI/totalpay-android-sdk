/*
 * Property of TotalPay (https://totalpay.global).
 */

package com.totalpay.sdk.model.response.sale

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.totalpay.sdk.model.api.TotalPayAction
import com.totalpay.sdk.model.api.TotalPayResult
import com.totalpay.sdk.model.api.TotalPayStatus
import com.totalpay.sdk.model.response.base.result.IDetailsTotalPayResult
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * The SALE recurring init result of the [TotalPaySaleResult].
 * @see TotalPaySaleResponse
 * @see com.totalpay.sdk.feature.adapter.TotalPayRecurringSaleAdapter
 *
 * @param recurringToken recurring token (get if account support recurring sales and was initialization transaction
 * for following recurring)
 */
data class TotalPaySaleRecurring(
    @NonNull
    @SerializedName("action")
    override val action: TotalPayAction,
    @NonNull
    @SerializedName("result")
    override val result: TotalPayResult,
    @NonNull
    @SerializedName("status")
    override val status: TotalPayStatus,
    @NonNull
    @SerializedName("order_id")
    override val orderId: String,
    @NonNull
    @SerializedName("trans_id")
    override val transactionId: String,
    @NonNull
    @SerializedName("trans_date")
    override val transactionDate: Date,
    @Nullable
    @SerializedName("descriptor")
    override val descriptor: String?,
    @NonNull
    @SerializedName("recurring_token")
    val recurringToken: String,
    @NonNull
    @SerializedName("amount")
    override val orderAmount: Double,
    @NonNull
    @SerializedName("currency")
    override val orderCurrency: String,
) : IDetailsTotalPayResult, Serializable
