package com.velopass.app.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.velopass.app.data.BikeIndexRepository
import com.velopass.app.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BikeIndexViewModelTest {

    @Mock
    private lateinit var repository: BikeIndexRepository

    private lateinit var viewModel: BikeIndexViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        val savedStateHandle = SavedStateHandle()
        viewModel = BikeIndexViewModel(repository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest {
        val state = viewModel.connectionState.first()
        assert(state == BikeIndexConnectionState.UNKNOWN)
    }

    @Test
    fun testLoadConnectionState() = runTest {
        viewModel.loadConnectionState()
        testScheduler.advanceUntilIdle()

        val state = viewModel.connectionState.first()
        assert(state != null)
    }

    @Test
    fun testHandleOAuthCallback_Success() = runTest {
        val testProfile = BikeIndexProfile(
            username = "test_user",
            email = "test@example.com",
            bikeIndexId = 12345L
        )

        whenever(repository.authorizeWithBikeIndex("auth_code"))
            .thenReturn(testProfile)

        viewModel.handleOAuthCallback("auth_code")
        testScheduler.advanceUntilIdle()

        val state = viewModel.connectionState.first()
        val profile = viewModel.profile.first()

        assert(state == BikeIndexConnectionState.CONNECTED)
        assert(profile?.username == "test_user")
        assert(profile?.email == "test@example.com")
    }

    @Test
    fun testSearchBikeBySerial_Success() = runTest {
        val mockResults = listOf(
            BikeIndexSearchResult(
                id = 987654L,
                title = "Trek FX 3 2023",
                brand = "Trek",
                model = "FX 3",
                year = 2023,
                serial = "TEST123",
                color = "Red",
                url = "https://bikeindex.org/bikes/987654"
            )
        )

        whenever(repository.verifyBike("TEST123"))
            .thenReturn(mockResults)

        viewModel.searchBikeBySerial("TEST123")
        testScheduler.advanceUntilIdle()

        val results = viewModel.searchResults.first()
        assert(results.size == 1)
        assert(results[0].brand == "Trek")
    }

    @Test
    fun testSearchBikeBySerial_EmptyResults() = runTest {
        whenever(repository.verifyBike("NONEXISTENT"))
            .thenReturn(emptyList())

        viewModel.searchBikeBySerial("NONEXISTENT")
        testScheduler.advanceUntilIdle()

        val results = viewModel.searchResults.first()
        assert(results.isEmpty())
    }

    @Test
    fun testDisconnectBikeIndex() = runTest {
        viewModel.disconnectBikeIndex()
        testScheduler.advanceUntilIdle()

        val state = viewModel.connectionState.first()
        val profile = viewModel.profile.first()

        assert(state == BikeIndexConnectionState.NOT_CONNECTED)
        assert(profile == null)
    }

    @Test
    fun testSetConnectionState() = runTest {
        viewModel.setConnectionState(BikeIndexConnectionState.EXPIRED)
        testScheduler.advanceUntilIdle()

        val state = viewModel.connectionState.first()
        assert(state == BikeIndexConnectionState.EXPIRED)
    }

    @Test
    fun testClearError() = runTest {
        val errorMsg = "Test error"
        viewModel.setConnectionState(BikeIndexConnectionState.ERROR)
        testScheduler.advanceUntilIdle()

        viewModel.clearError()
        testScheduler.advanceUntilIdle()

        val error = viewModel.error.first()
        assert(error == null)
    }

    @Test
    fun testLoadingState() = runTest {
        viewModel.searchBikeBySerial("TEST123")
        
        val isLoading = viewModel.isLoading.first()
        assert(isLoading == true)
    }
}
