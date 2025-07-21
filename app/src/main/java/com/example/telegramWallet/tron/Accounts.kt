package com.example.telegramWallet.tron

import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.key.KeyPair
import org.tron.trident.proto.Response
import java.math.BigInteger

// Обработка Tron API раздела Accounts.
class Accounts {
    // Получаем ресурсы аккаунта.
    fun getAccountResource(ownerAddress: String, privateKey: String): Response.AccountResourceMessage {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        return wrapper.getAccountResource(ownerAddress)
    }

    // Информация об учетной записи, включая баланс TRX, балансы TRC-10, информацию о ставках,
    // информация о голосовании и разрешениях и т. д.
    fun getAccount(ownerAddress: String, privateKey: String): Response.Account {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        return wrapper.getAccount(ownerAddress)
    }

    fun allowance(spender: String, ownerAddress: String, privateKey: String): BigInteger? {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val contract: Contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
        val token = Trc20Contract(contract, ownerAddress, wrapper)
        val result = token.allowance(ownerAddress, spender)

        wrapper.close()
        return result
    }

    fun isAllowanceUnlimited(
        spender: String,
        ownerAddress: String,
        privateKey: String,
    ): Boolean {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val contract: Contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
        val token = Trc20Contract(contract, ownerAddress, wrapper)
        val result = token.allowance(ownerAddress, spender)

        wrapper.close()

        val max = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(10L).pow(6))
        return result >= max
    }

    fun hasEnoughBandwidth(address: String?, requiredBandwidth: Long): Boolean {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", KeyPair.generate().toPrivateKey())
        val resources = wrapper.getAccountResource(address)

        val freeNetRemaining: Long = resources.freeNetLimit - resources.freeNetUsed
        val paidNetRemaining: Long = resources.netLimit - resources.netUsed

        val totalAvailableBandwidth = freeNetRemaining + paidNetRemaining

        wrapper.close()
        return totalAvailableBandwidth >= requiredBandwidth
    }
}