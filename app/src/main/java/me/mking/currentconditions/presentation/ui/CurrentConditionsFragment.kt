package me.mking.currentconditions.presentation.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.mking.currentconditions.R
import me.mking.currentconditions.databinding.CurrentConditionsFragmentBinding
import me.mking.currentconditions.presentation.viewmodels.CurrentConditionsViewModel
import me.mking.currentconditions.presentation.viewmodels.CurrentConditionsViewState

@AndroidEntryPoint
class CurrentConditionsFragment : Fragment() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkForLocationPermissions(userHasDenied = it)
        }

    companion object {
        fun newInstance() = CurrentConditionsFragment()
    }

    private val viewModel: CurrentConditionsViewModel by viewModels()

    lateinit var viewBinding: CurrentConditionsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = CurrentConditionsFragmentBinding.inflate(inflater).apply {
        viewBinding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.observe(viewLifecycleOwner) { handleState(it) }
        viewBinding.currentConditionsReload.setOnClickListener { viewModel.reload() }
        checkForLocationPermissions()
    }

    private fun handleState(state: CurrentConditionsViewState) {
        viewBinding.currentConditionsProgress.isVisible =
            state is CurrentConditionsViewState.Loading
        viewBinding.currentConditionsErrorMessage.isVisible =
            (state is CurrentConditionsViewState.Error || state is CurrentConditionsViewState.LocationNotAvailable)
        when (state) {
            is CurrentConditionsViewState.Ready -> handleReadyState(state)
            CurrentConditionsViewState.Error -> {
                viewBinding.currentConditionsErrorMessage.text =
                    resources.getString(R.string.current_conditions_error_generic)
            }
            CurrentConditionsViewState.LocationNotAvailable -> {
                viewBinding.currentConditionsErrorMessage.text =
                    resources.getString(R.string.current_conditions_error_location)
            }
            else -> Unit
        }
    }

    private fun handleReadyState(state: CurrentConditionsViewState.Ready) {
        viewBinding.currentConditionsCardView.apply {
            locationText = state.currentWeather.location
            conditionText = state.currentWeather.condition
            temperatureText = state.currentWeather.temperature
            windSpeedText = state.currentWeather.windSpeed
            windDirectionText = state.currentWeather.windDirection
            iconSrc = state.currentWeather.iconUrl
            lastUpdatedText = state.currentWeather.lastUpdated
        }
        viewBinding.currentConditionsProgress.isVisible = state.isRefreshing
        if (state.isOffline) {
            viewBinding.currentConditionsErrorMessage.isVisible = true
            viewBinding.currentConditionsErrorMessage.text =
                resources.getString(R.string.current_conditions_error_offline)
        }
    }

    private fun checkForLocationPermissions(
        userSawRationale: Boolean = false,
        userHasDenied: Boolean = false
    ) {
        when {
            userHasDenied || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.load()
            }
            !userSawRationale && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.location_permission_dialog_title))
            .setMessage(getString(R.string.location_permission_dialog_message))
            .setPositiveButton(
                getString(R.string.location_permission_dialog_continue)
            ) { _, _ -> checkForLocationPermissions(userSawRationale = true) }
            .setNegativeButton(
                getString(R.string.location_permission_dialog_cancel)
            ) { _, _ -> checkForLocationPermissions(userHasDenied = true) }
            .show()
    }

}