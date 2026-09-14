package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.ShopItemDto
import com.example.woldeokmoneyverse.data.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopSearchViewModel(
    private val repository: ShopRepository = ShopRepository()
) : ViewModel() {
    private val _results = MutableStateFlow<UiState<List<ShopItemDto>>>(UiState.Empty)
    val results: StateFlow<UiState<List<ShopItemDto>>> = _results.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun search(query: String) {
        val normalized = query.trim()
        _query.value = normalized
        viewModelScope.launch {
            _results.value = UiState.Loading
            repository.getShopItems(normalized).fold(
                onSuccess = { _results.value = UiState.Success(it) },
                onFailure = { _results.value = UiState.Error(it.message ?: "상점 검색 실패") }
            )
        }
    }

    fun clear() {
        _query.value = ""
        _results.value = UiState.Empty
    }
}
