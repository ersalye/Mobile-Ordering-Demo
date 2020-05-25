package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LineItem(
    val id: String,
    val product: Product,
    val bundle: ProductBundle?,
    val productsInBundle: HashMap<ProductGroup, List<Product>>,
    val modifiers: HashMap<ProductModifierGroupKey, List<ModifierInfo>>,
    val quantity: Int
) : Parcelable {

    val unitPrice: Float
        get() = bundle?.price ?: product.price

    val price: Float
        get() {
            var price = unitPrice
            for ((_, modifiers) in modifiers) {
                for (modifier in modifiers) {
                    price += modifier.priceDelta
                }
            }
            return price * quantity.coerceAtLeast(1)
        }

    val lineItemName: String
        get() {
            return bundle?.bundleName ?: product.productName
        }

    fun deepCopy(): LineItem {
        return this.copy(productsInBundle = HashMap(this.productsInBundle), modifiers = HashMap(this.modifiers))
    }

    companion object {
        val EMPTY = LineItem("", Product.EMPTY, null, hashMapOf(), hashMapOf(), 1)
    }
}

@Parcelize
data class ProductModifierGroupKey(val product: Product, val modifierGroup: ModifierGroup) : Parcelable