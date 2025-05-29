package com.shopapp.presentation.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shopapp.presentation.common.validation.ValidationResult

/**
 * Компонент текстового поля с валидацией
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    validate: (String) -> ValidationResult,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    isRequired: Boolean = false,
    validateOnFocusLoss: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }
    
    // Валидация при изменении значения, если поле уже было "тронуто"
    LaunchedEffect(value) {
        if (touched) {
            val result = validate(value)
            showError = !result.isValid
            errorMessage = result.errorMessage
        }
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            label = label,
            isError = showError,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            supportingText = if (showError) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else {
                supportingText
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        touched = true
                    } else if (validateOnFocusLoss && touched) {
                        val result = validate(value)
                        showError = !result.isValid
                        errorMessage = result.errorMessage
                    }
                }
        )
    }
}

/**
 * Вспомогательная функция для удобного создания обязательного поля
 */
@Composable
fun RequiredValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    validate: (String) -> ValidationResult,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    validateOnFocusLoss: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label*") },
        modifier = modifier,
        validate = validate,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        isRequired = true,
        validateOnFocusLoss = validateOnFocusLoss,
        supportingText = supportingText
    )
}

/**
 * Вспомогательная функция для удобного создания необязательного поля
 */
@Composable
fun OptionalValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    validate: (String) -> ValidationResult,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    validateOnFocusLoss: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        validate = validate,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        isRequired = false,
        validateOnFocusLoss = validateOnFocusLoss,
        supportingText = supportingText
    )
}
