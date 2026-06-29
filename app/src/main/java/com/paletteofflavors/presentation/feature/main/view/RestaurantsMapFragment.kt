package com.paletteofflavors.presentation.feature.main.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.paletteofflavors.databinding.FragmentRestaurantsMapBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import com.paletteofflavors.R
import com.yandex.mapkit.search.BusinessObjectMetadata

class RestaurantsMapFragment : Fragment(), CameraListener, Session.SearchListener {

    private var _binding: FragmentRestaurantsMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var searchManager: SearchManager
    private var searchSession: Session? = null
    
    private lateinit var clusterizedCollection: ClusterizedPlacemarkCollection

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            enableUserLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestaurantsMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapview

        clusterizedCollection =  mapView.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection { cluster ->
            cluster.appearance.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.baseline_account_circle_24))
            cluster.addClusterTapListener { cl ->
                mapView.mapWindow.map.move(
                    CameraPosition(cl.appearance.geometry,  mapView.mapWindow.map.cameraPosition.zoom + 1, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0.5f),
                    null
                )
                true
            }
        }
        
        mapView.mapWindow.map.addCameraListener(this)
        
        checkPermissions()
        
        binding.fabUserLocation.setOnClickListener {
            val userLocation = userLocationLayer.cameraPosition()
            if (userLocation != null) {
                mapView.mapWindow.map.move(
                    CameraPosition(userLocation.target, 15f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0.5f),
                    null
                )
            } else {
                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            enableUserLocation()
        }
    }

    private fun enableUserLocation() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
    }

    override fun onCameraPositionChanged(
        map: com.yandex.mapkit.map.Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) {
            submitSearchQuery()
        }
    }

    private fun submitSearchQuery() {
        val searchOptions = SearchOptions()
        searchOptions.searchTypes = SearchType.BIZ.value
        searchOptions.resultPageSize = 32

        searchSession = searchManager.submit(
            "ресторан",
            VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            searchOptions,
            this
        )
    }

    override fun onSearchResponse(response: Response) {
        clusterizedCollection.clear()
        
        for (searchResult in response.collection.children) {
            val obj = searchResult.obj
            if (obj != null) {
                val point = obj.geometry[0].point
                if (point != null) {
                    val placemark = clusterizedCollection.addPlacemark(point)
                    placemark.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.ic_map_48dp))

                    val metadata = obj.metadataContainer.getItem(BusinessObjectMetadata::class.java)
                    placemark.userData = metadata
                    placemark.addTapListener { mapObject, _ ->
                        showRestaurantInfo(mapObject.userData as? BusinessObjectMetadata)
                        true
                    }
                }
            }
        }
        clusterizedCollection.clusterPlacemarks(60.0, 15)
    }

    override fun onSearchError(error: com.yandex.runtime.Error) {
        // Handle error
    }


    private fun showRestaurantInfo(metadata: BusinessObjectMetadata?) {
        if (metadata == null) return
        
        binding.restaurantInfoCard.visibility = View.VISIBLE
        binding.restaurantName.text = metadata.name
        binding.restaurantAddress.text = metadata.address.formattedAddress
        binding.restaurantDescription.text = metadata.categories.joinToString { it.name }
        
        binding.restaurantInfoCard.setOnClickListener {
            // For details
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

