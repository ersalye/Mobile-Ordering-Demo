package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

open class MenuRealmEntity(
    var categories: RealmList<CategoryRealmEntity>,
    var createdAt: Date
) : RealmObject() {
    constructor() : this(RealmList(), Date())
}

open class CategoryRealmEntity(
    var categoryId: String,
    var categoryName: String,
    var products: RealmList<ProductRealmEntity>
) : RealmObject() {
    constructor() : this("", "", RealmList())
}

open class ProductRealmEntity(
    var productId: String,
    var productName: String,
    var productDescription: String,
    var price: Float,
    var receiptText: String,
    var modifierGroups: RealmList<ModifierGroupRealmEntity>,
    var bundles: RealmList<ProductBundleRealmEntity>
) : RealmObject() {
    constructor() : this("", "", "", 0f, "", RealmList(), RealmList())
}

open class ModifierInfoRealmEntity(
    var modifierId: String,
    var modifierName: String,
    var priceDelta: Float,
    var receiptText: String
) : RealmObject() {
    constructor() : this("", "", 0f, "")
}

open class ModifierGroupRealmEntity(
    var modifierGroupId: String,
    var modifierGroupName: String,
    var action: String,
    var options: RealmList<ModifierInfoRealmEntity>,
    var defaultSelection: String?,
    var min: Int,
    var max: Int
) : RealmObject() {

    constructor() : this("", "", "", RealmList(), "", 1, 1)

    fun getDefaultSelection(): ModifierInfoRealmEntity? {
        return options.find { it.modifierId == defaultSelection }
    }
}

open class ProductBundleRealmEntity(
    var bundleId: String,
    var bundleName: String,
    var price: Float,
    var receiptText: String,
    var productGroups: RealmList<ProductGroupRealmEntity>
) : RealmObject() {
    constructor() : this("", "", 0f, "", RealmList())
}

open class ProductGroupRealmEntity(
    var productGroupId: String,
    var productGroupName: String,
    var options: RealmList<ProductRealmEntity>,
    var defaultProduct: ProductRealmEntity? = null
) : RealmObject() {
    constructor() : this("", "", RealmList())
}