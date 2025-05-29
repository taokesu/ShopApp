package com.shopapp.presentation.common.validation

import android.util.Patterns

/**
 * Утилитарный класс для валидации полей ввода
 */
object ValidationUtils {
    
    /**
     * Валидация имени пользователя
     */
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Имя пользователя не может быть пустым"
            )
            username.length < 3 -> ValidationResult(
                isValid = false,
                errorMessage = "Имя пользователя должно содержать минимум 3 символа"
            )
            username.contains(" ") -> ValidationResult(
                isValid = false,
                errorMessage = "Имя пользователя не должно содержать пробелы"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация пароля
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Пароль не может быть пустым"
            )
            password.length < 6 -> ValidationResult(
                isValid = false,
                errorMessage = "Пароль должен содержать минимум 6 символов"
            )
            !password.any { it.isDigit() } -> ValidationResult(
                isValid = false,
                errorMessage = "Пароль должен содержать хотя бы одну цифру"
            )
            !password.any { it.isLetter() } -> ValidationResult(
                isValid = false,
                errorMessage = "Пароль должен содержать хотя бы одну букву"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация подтверждения пароля
     */
    fun validatePasswordConfirmation(password: String, confirmation: String): ValidationResult {
        return when {
            confirmation.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Подтверждение пароля не может быть пустым"
            )
            password != confirmation -> ValidationResult(
                isValid = false,
                errorMessage = "Пароли не совпадают"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация email
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Email не может быть пустым"
            )
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult(
                isValid = false,
                errorMessage = "Некорректный формат email"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация телефона
     */
    fun validatePhone(phone: String): ValidationResult {
        // Если телефон не обязателен, пропускаем пустую строку
        if (phone.isBlank()) {
            return ValidationResult(isValid = true)
        }
        
        // Простая проверка формата телефона
        val phoneRegex = "^[+]?[0-9]{10,13}\$".toRegex()
        return if (phoneRegex.matches(phone)) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = "Некорректный формат телефона (должен содержать 10-13 цифр)"
            )
        }
    }
    
    /**
     * Валидация полного имени
     */
    fun validateFullName(fullName: String): ValidationResult {
        // Если имя не обязательно, пропускаем пустую строку
        if (fullName.isBlank()) {
            return ValidationResult(isValid = true)
        }
        
        return when {
            fullName.length < 2 -> ValidationResult(
                isValid = false,
                errorMessage = "ФИО должно содержать минимум 2 символа"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация адреса
     */
    fun validateAddress(address: String): ValidationResult {
        // Если адрес не обязателен, пропускаем пустую строку
        if (address.isBlank()) {
            return ValidationResult(isValid = true)
        }
        
        return when {
            address.length < 5 -> ValidationResult(
                isValid = false,
                errorMessage = "Адрес должен содержать минимум 5 символов"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация цены
     */
    fun validatePrice(price: String): ValidationResult {
        return when {
            price.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Цена не может быть пустой"
            )
            price.toDoubleOrNull() == null -> ValidationResult(
                isValid = false,
                errorMessage = "Некорректный формат цены"
            )
            price.toDouble() <= 0 -> ValidationResult(
                isValid = false,
                errorMessage = "Цена должна быть больше нуля"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация количества
     */
    fun validateQuantity(quantity: String): ValidationResult {
        return when {
            quantity.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Количество не может быть пустым"
            )
            quantity.toIntOrNull() == null -> ValidationResult(
                isValid = false,
                errorMessage = "Некорректный формат количества"
            )
            quantity.toInt() < 0 -> ValidationResult(
                isValid = false,
                errorMessage = "Количество не может быть отрицательным"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация названия продукта
     */
    fun validateProductName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Название продукта не может быть пустым"
            )
            name.length < 2 -> ValidationResult(
                isValid = false,
                errorMessage = "Название продукта должно содержать минимум 2 символа"
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * Валидация описания продукта
     */
    fun validateProductDescription(description: String): ValidationResult {
        // Если описание не обязательно, пропускаем пустую строку
        if (description.isBlank()) {
            return ValidationResult(isValid = true)
        }
        
        return when {
            description.length < 10 -> ValidationResult(
                isValid = false,
                errorMessage = "Описание должно содержать минимум 10 символов"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}

/**
 * Результат валидации
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)
