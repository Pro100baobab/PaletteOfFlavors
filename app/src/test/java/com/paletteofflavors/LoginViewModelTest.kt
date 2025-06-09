package com.paletteofflavors

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.paletteofflavors.logIn.viewmodels.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel()
    }

    @Test
    fun `setUserName updates username LiveData`() {
        val testUsername = "testUser"
        viewModel.setUserName(testUsername)
        assertEquals(testUsername, viewModel.username.value)
    }

    @Test
    fun `setPassword updates password LiveData`() {
        val testPassword = "testPass123"
        viewModel.setPassword(testPassword)
        assertEquals(testPassword, viewModel.password.value)
    }
}