package com.example.expensetracker.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.Repository.ExpenseRepository
import com.example.expensetracker.model.Expense
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

annotation class composable
// from here it starts all the controls for UI operations
// also it loads,displays expense list
// handling for add,edit,delete operations etc
class ExpenseViewModel : ViewModel() { // viewmodel coordinates with everything
    // Our data source for expenses
    private val repository = ExpenseRepository() // the repository does all the data work

    // this one holds complete list of users expenses
    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List< Expense>> = _expenses

    // this one sums up all the expenses
    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    // this one is for handling add,edit,delete operations etc
    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    init {
        loadExpenses()
        loadTotalSpent()
    }

    // here it loads the expenses from database
    // and also connects to repository
    @composable
    private fun loadExpenses() { // this is private because only viewmodel should trigger this and not the activity
        viewModelScope.launch {
            repository.getAllExpenses()
                .catch { e ->
                    // here if Flow errors, it will  show error state
                    _operationState.value = OperationState.Error(
                        e.message ?: "Failed to load expenses"
                    )
                }
                .collect { expenses ->
                    // here new data arrived! it will Update the LiveData
                    _expenses.value = expenses
                }
        }
    }

    // this is for calculate the total spent
    // also updates the total of live data
    @composable
    private fun loadTotalSpent() {
        viewModelScope.launch {
            val total = repository.getTotalSpent()
            _totalSpent.value = total
        }
    }
@composable
    // this one is for adding new expense
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val result = repository.addExpense(expense)

            if (result.isSuccess) { // if you succeeded this will be the message
                _operationState.value = OperationState.Success("Expense added!")
                // here it will refresh total list updates automatically via Flow
                loadTotalSpent()
            } else {
                _operationState.value = OperationState.Error( // if you failed then this message will appear
                    result.exceptionOrNull()?.message ?: "Failed to add expense"
                )
            }
        }
    }
@composable
    // here user can update its expense
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val result = repository.updateExpense(expense)

            if (result.isSuccess) { // if you succeeded this will be the message
                _operationState.value = OperationState.Success("Expense updated!")
                loadTotalSpent()
            } else {
                _operationState.value = OperationState.Error( // if you failed then this message will appear
                    result.exceptionOrNull()?.message ?: "Failed to update expense"
                )
            }
        }
    }

    // here user can delete its expense
    @composable
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val result = repository.deleteExpense(expenseId)

            if (result.isSuccess) { // if you succeeded this will be the message
                _operationState.value = OperationState.Success("Expense deleted!")
                loadTotalSpent()
            } else {
                _operationState.value = OperationState.Error( // if you failed then this message will appear
                    result.exceptionOrNull()?.message ?: "Failed to delete expense"
                )
            }
        }
    }

    //
  @composable
    // here it will manually refresh the  data for expense list and total spent
    fun refresh() {
        loadExpenses()
        loadTotalSpent()
    }
}

// this one keep the tracks for create read update delete(CRUD)
sealed class OperationState {
    object Idle : OperationState() // here no operations are happening
    object Loading : OperationState() // here operations are in progress
    data class Success(val message: String) : OperationState() // here  the operation is completed
    data class Error(val message: String) : OperationState() // here operation is failed
}