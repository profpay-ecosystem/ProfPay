package com.profpay.wallet.utils.aml

import com.profpay.wallet.ui.screens.wallet.AMLType
import org.server.protobuf.aml.AmlProto

fun AmlProto.GetAmlByTxIdResponse.toAmlType(): AMLType = when {
        riskyScore >= 70.0 -> AMLType.HIGH_RISC
        riskyScore >= 50.0 -> AMLType.MEDIUM_RISC
        else -> AMLType.LOW_RISC
    }