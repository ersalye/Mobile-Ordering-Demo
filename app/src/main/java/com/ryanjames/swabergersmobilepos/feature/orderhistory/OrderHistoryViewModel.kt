package com.ryanjames.swabergersmobilepos.feature.orderhistory

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.LoadingDialogBinding
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class OrderHistoryViewModel @Inject constructor(val orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val _onRetrieveOrderHistory = MutableLiveData<Resource<List<Order>>>()
    val onRetrieveOrderHistory: LiveData<Resource<List<Order>>>
        get() = _onRetrieveOrderHistory

    private val _loadingBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingBinding: LiveData<LoadingDialogBinding>
        get() = _loadingBinding

    fun retrieveOrderHistory() {
        compositeDisposable.add(
            orderRepository.getOrderHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    setLoadingViewVisibility(View.VISIBLE)
                }
                .subscribe({ orderList ->
                    _onRetrieveOrderHistory.value = Resource.Success(orderList)
                    setLoadingViewVisibility(View.GONE)
                }, { error ->
                    error.printStackTrace()
                    setLoadingViewVisibility(View.GONE)
                })
        )
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = "Retrieving order history...",
            textColor = R.color.colorWhite
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}