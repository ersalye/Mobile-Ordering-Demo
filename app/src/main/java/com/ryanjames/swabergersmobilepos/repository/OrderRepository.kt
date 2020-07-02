package com.ryanjames.swabergersmobilepos.repository

import android.content.SharedPreferences
import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.executeRealmTransaction
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.mappers.toDomain
import com.ryanjames.swabergersmobilepos.mappers.toEntity
import com.ryanjames.swabergersmobilepos.mappers.toRemoteEntity
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import io.reactivex.Single

class OrderRepository(sharedPreferences: SharedPreferences) {

    private val swabergersService = SwabergersService(sharedPreferences)
    private val orderRealmDao = OrderRealmDao()
    private val globalRealmDao = GlobalRealmDao()

    fun getLocalBag(): Single<List<LineItem>> {
        return orderRealmDao.getLineItems().map { it.lineItems.map { lineItem -> lineItem.toDomain() } }
    }

    fun insertLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm ->
            if (orderRealmDao.lineItemsCount(realm) == 0) {
                globalRealmDao.createLocalBagOrderId(realm)
            }
            orderRealmDao.insertLineItem(realm, lineItem.toEntity(realm))
        }
    }

    fun updateLineItem(lineItem: LineItem) {
        executeRealmTransaction { realm -> orderRealmDao.updateLineItem(realm, lineItem.toEntity(realm)) }
    }

    fun postOrder(order: Order): Single<Boolean> {
        var orderId = globalRealmDao.getLocalBagOrderId()
        if (orderId == GlobalRealmDao.NO_LOCAL_ORDER) {
            executeRealmTransaction { realm ->
                orderId = globalRealmDao.createLocalBagOrderId(realm)
            }
        }
        return swabergersService.postOrder(order.toRemoteEntity(orderId)).map { true }
    }

    fun clearLocalBag() {
        executeRealmTransaction { realm ->
            orderRealmDao.deleteAllLineItems(realm)
            globalRealmDao.clearLocalBagOrderId(realm)
        }
    }
}