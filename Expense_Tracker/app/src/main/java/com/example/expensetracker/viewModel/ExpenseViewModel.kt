package com.example.expensetracker.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.Repository.ExpenseRepository
import com.example.expensetracker.model.Category
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.service.LocationData
import com.example.expensetracker.service.LocationService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

annotation class composable

// from here it starts all the controls for UI operations
// also it loads,displays expense list
// handling for add,edit,delete operations etc
class ExpenseViewModel(application: Application) : AndroidViewModel(application) { //Changed to AndroidViewModel so we can access GPS location services

    // Our data source for expenses
    private val repository = ExpenseRepository() // the repository does all the data work

    //This is our GPS helper that finds where you are right now
    // Think of it as a location detective that works with your phone's GPS
    private val locationService = LocationService(application)

    // this one holds complete list of users expenses
    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List< Expense>> = _expenses

    // this one sums up all the expenses
    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    // this one is for handling add,edit,delete operations etc
    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    // This holds your current GPS location (latitude, longitude, and address)
    // When you tap "Get Location", this gets filled with where you are
    private val _currentLocation = MutableLiveData<LocationData?>()
    val currentLocation: LiveData<LocationData?> = _currentLocation

    // This remembers which currency you selected (like $, €, ₹)
    // So you don't have to select it every time you add an expense
    private val _selectedCurrency = MutableLiveData<Currency>()
    val selectedCurrency: LiveData<Currency> = _selectedCurrency

    // This shows a loading spinner while GPS is finding your location
    // true = "Searching for location...", false = "Location found!"
    private val _isLoadingLocation = MutableLiveData<Boolean>()
    val isLoadingLocation: LiveData<Boolean> = _isLoadingLocation

    init {
        // Here we set Euro as the default currency when app starts
        _selectedCurrency.value = Currency.EUR // You can change this to your local currency

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

    // This function gets your current GPS location with one tap!
    // It's like asking your phone "Where am I right now?"
    fun getCurrentLocation() {
        viewModelScope.launch {
            // Show the loading spinner while GPS is working
            _isLoadingLocation.value = true

            // Ask the location service to find where you are
            val result = locationService.getCurrentLocation()

            // If GPS successfully found your location, save it
            result.onSuccess { locationData ->
                _currentLocation.value = locationData
                _operationState.value = OperationState.Success("Location detected!")
            }

            // If GPS couldn't find location (maybe you're indoors), show error
            result.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Failed to get location"
                )
            }

            // Hide the loading spinner
            _isLoadingLocation.value = false
        }
    }

    // This lets you pick which currency to use (like switching from $ to ₹)
    // Your choice gets saved so you don't have to select it every time
    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    // here the smart expense creation happens automatically
    @composable
    fun addExpenseWithLocation(
        amount: Double,
        description: String,
        category: Category? = null  // If you don't pick a category, we'll guess it for you!
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            // Get the location you saved earlier (or null if you didn't tap "Get Location")
            val location = _currentLocation.value

            // Smart magic! If you didn't pick a category, we detect it from your description
            // Example: "Lunch at McDonald's" → Automatically picks FOOD category
            val finalCategory = category ?: Category.autoDetect(description)

            // Create the expense with all the smart features
            val expense = Expense(
                amount = amount,
                currency = _selectedCurrency.value?.code ?: "EUR",  // Use your selected currency
                category = finalCategory.name,                       // Use smart-detected category
                description = description,
                location = location?.address ?: "",                  // Add GPS address if available
                latitude = location?.latitude,                       // Add GPS coordinates
                longitude = location?.longitude
            )

            val result = repository.addExpense(expense)

            if (result.isSuccess) { // if you succeeded this will be the message
                _operationState.value = OperationState.Success("Expense added!")
                loadTotalSpent()
            } else {
                _operationState.value = OperationState.Error( // if you failed then this message will appear
                    result.exceptionOrNull()?.message ?: "Failed to add expense"
                )
            }
        }
    }

    // this one is for adding new expense
    @composable
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

    // here user can update its expense
    @composable
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

    // here it will manually refresh the  data for expense list and total spent
    @composable
    fun refresh() {
        loadExpenses()
        loadTotalSpent()
    }

    // This filters your expenses to show only the ones with GPS location
    // Perfect for showing on a map! Only expenses with latitude & longitude show up
    fun getExpensesWithLocation(): List<Expense> {
        return _expenses.value?.filter {
            it.latitude != null && it.longitude != null
        } ?: emptyList()
    }

    // Returns true if we can access location, false if we need to ask for permission
    fun hasLocationPermission(): Boolean {
        return locationService.hasLocationPermission()
    }
}

// this one keep the tracks for create read update delete(CRUD)
sealed class OperationState {
    object Idle : OperationState() // here no operations are happening
    object Loading : OperationState() // here operations are in progress
    data class Success(val message: String) : OperationState() // here  the operation is completed
    data class Error(val message: String) : OperationState() // here operation is failed
}