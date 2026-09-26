package github.mkbaka.assiya.platform.bukkit.internal.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
val ItemStack?.isAir: Boolean
    get() {
        contract {
            returns(false) implies (this@isAir != null)
        }
        return this == null || type == Material.AIR
    }