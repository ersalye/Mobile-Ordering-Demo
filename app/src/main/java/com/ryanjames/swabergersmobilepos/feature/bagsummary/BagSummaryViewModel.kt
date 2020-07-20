package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.clearAndAddAll
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(var orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var order: Order = Order(mutableListOf())

    private val _emptyBagVisibility = MutableLiveData<Int>()
    val emptyBagVisibility: LiveData<Int>
        get() = _emptyBagVisibility

    private val _nonEmptyBagVisibility = MutableLiveData<Int>()
    val nonEmptyBagVisibility: LiveData<Int>
        get() = _nonEmptyBagVisibility

    private val _tax = MutableLiveData<String>()
    val tax: LiveData<String>
        get() = _tax

    private val _subtotal = MutableLiveData<String>()
    val subtotal: LiveData<String>
        get() = _subtotal


    private val _total = MutableLiveData<String>()
    val total: LiveData<String>
        get() = _total

    private val _onOrderSucceeded = MutableLiveData<Event<Boolean>>()
    val onOrderSucceeded: LiveData<Event<Boolean>>
        get() = _onOrderSucceeded

    private val _onOrderFailed = MutableLiveData<Event<Boolean>>()
    val orderFailed: LiveData<Event<Boolean>>
        get() = _onOrderFailed

    private val _localBag = MutableLiveData<Order>()
    val getLocalBag: LiveData<Order>
        get() = _localBag

    private val _onClearBag = MutableLiveData<Boolean>()
    val onClearBag: LiveData<Boolean>
        get() = _onClearBag

    var customerInput : String? = null

    init {
        updateBagVisibility()
    }

    fun retrieveLocalBag() {
        compositeDisposable.add(
            orderRepository.getLocalBag()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ lineItems ->
                    order.lineItems.clearAndAddAll(lineItems)
                    _localBag.value = order
                    updateBagVisibility()
                    updatePrices()
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    private fun updateBagVisibility() {
        if (order.lineItems.isEmpty()) {
            _emptyBagVisibility.value = View.VISIBLE
            _nonEmptyBagVisibility.value = View.GONE
        } else {
            _emptyBagVisibility.value = View.GONE
            _nonEmptyBagVisibility.value = View.VISIBLE
        }

    }

    private fun updatePrices() {
        _tax.value = order.tax.toTwoDigitString()
        _subtotal.value = order.subTotal.toTwoDigitString()
        _total.value = "$${order.total.toTwoDigitString()}"
    }

    fun putLineItem(lineItem: LineItem) {
        for ((index, item) in order.lineItems.withIndex()) {
            if (item.id == lineItem.id) {
                order.lineItems[index] = lineItem
                _localBag.value = order
                updatePrices()
                orderRepository.updateLineItem(lineItem)
                return
            }
        }
        updateBagVisibility()
    }

    fun clearBag() {
        order.lineItems.clear()
        _onClearBag.value = true
        updateBagVisibility()
    }

    fun postOrder() {
        compositeDisposable.add(
            orderRepository.postOrder(order.apply { this.customerName = customerInput ?: "" })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("ORDER", "CREATED")
                    orderRepository.clearLocalBag()
                    _onOrderSucceeded.value = Event(true)
                }, {
                    it.printStackTrace()
                    _onOrderFailed.value = Event(true)
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}