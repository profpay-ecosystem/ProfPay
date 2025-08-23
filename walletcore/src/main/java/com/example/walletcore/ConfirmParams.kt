package com.example.walletcore

import com.example.walletcore.extension.type
import com.example.walletcore.model.DestinationAddress
import com.example.walletcore.primitives.Account
import com.example.walletcore.primitives.Asset
import com.example.walletcore.primitives.AssetId
import com.example.walletcore.primitives.AssetSubtype
import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
sealed class ConfirmParams {
    abstract val asset: Asset

    abstract val from: Account

    @Serializable(BigIntegerSerializer::class)
    abstract val amount: BigInteger

    val assetId: AssetId get() = asset.id

    class Builder(
        val asset: Asset,
        val from: Account,
        val amount: BigInteger = BigInteger.ZERO,
    ) {
        fun transfer(destination: DestinationAddress, memo: String? = null, isMax: Boolean = false): TransferParams {
            return when (asset.id.type()) {
                AssetSubtype.NATIVE -> TransferParams.Native(
                    asset = asset,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    isMaxAmount = isMax
                )
                AssetSubtype.TOKEN -> TransferParams.Token(
                    asset = asset,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    isMaxAmount = isMax
                )
            }
        }

        fun activate(): Activate {
            return Activate(asset, from)
        }
    }

    @Serializable
    sealed class TransferParams : ConfirmParams() {
        abstract val destination: DestinationAddress
        abstract val memo: String?
        abstract val isMaxAmount: Boolean
        abstract val inputType: InputType?

        override fun isMax(): Boolean {
            return isMaxAmount
        }

        override fun destination(): DestinationAddress {
            return destination
        }

        override fun memo(): String? {
            return memo
        }

        @Serializable
        class Generic(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO,
            override val destination: DestinationAddress = DestinationAddress(""),
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
        ) : TransferParams()

        @Serializable
        class Native(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
        ) : TransferParams()

        @Serializable
        class Token(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
        ) : TransferParams()

        @Serializable
        enum class InputType {
            Signature,
            EncodeTransaction,
        }
    }

    @Serializable
    class Activate(
        override val asset: Asset,
        override val from: Account,
        @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO,
    ) : ConfirmParams()

    open fun destination(): DestinationAddress? = null

    open fun memo(): String? = null

    open fun isMax(): Boolean = false
}