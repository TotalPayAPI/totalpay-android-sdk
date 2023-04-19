/*
 * Property of TotalPay (https://totalpay.global).
 */

package com.totalpay.sdk.model.api

import com.google.gson.annotations.SerializedName

/**
 * When you make request to Payment Platform, you need to specify action, that needs to be done.
 * Basically, every action is represented by its Adapter.
 * @see com.totalpay.sdk.core.TotalPaySdk
 * @see com.totalpay.sdk.feature.adapter
 * @see com.totalpay.sdk.model.response.base.result.ITotalPayResult
 *
 * @property action the action value.
 */
enum class TotalPayAction(val action: String) {
    /**
     * Creates SALE or AUTH transaction.
     */
    @SerializedName("SALE")
    SALE("SALE"),

    /**
     * Creates CAPTURE transaction.
     */
    @SerializedName("CAPTURE")
    CAPTURE("CAPTURE"),

    /**
     * Creates REVERSAL or REFUND transaction.
     */
    @SerializedName("CREDITVOID")
    CREDITVOID("CREDITVOID"),

    /**
     * Gets status of transaction in Payment Platform.
     */
    @SerializedName("GET_TRANS_STATUS")
    GET_TRANS_STATUS("GET_TRANS_STATUS"),

    /**
     * Gets details of the order from Payment platform.
     */
    @SerializedName("GET_TRANS_DETAILS")
    GET_TRANS_DETAILS("GET_TRANS_DETAILS"),

    /**
     * Creates SALE or AUTH transaction using previously used cardholder data.
     */
    @SerializedName("RECURRING_SALE")
    RECURRING_SALE("RECURRING_SALE"),

    /**
     * Following actions can not be made by request, they are created by Payment Platform in certain circumstances
     * (e.g. issuer initiated chargeback) and you receive callback as a result.
     */
    @SerializedName("CHARGEBACK")
    CHARGEBACK("CHARGEBACK")
}
