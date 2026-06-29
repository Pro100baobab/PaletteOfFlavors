package com.paletteofflavors.presentation.feature.main.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.paletteofflavors.R
import com.paletteofflavors.data.local.SessionManager
import com.paletteofflavors.databinding.FragmentProfileBinding
import com.paletteofflavors.domain.model.NetworkRecipe
import com.paletteofflavors.presentation.feature.main.viewmodel.ProfileViewModel
import com.paletteofflavors.presentation.feature.recipes.view.NetworkRecipeDetailsFragment
import com.paletteofflavors.presentation.feature.recipes.view.adapter.NetworkRecipeAdapter
import com.paletteofflavors.presentation.feature.recipes.viewmodel.RecipeSharedViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileFragment(private val targetUserId: Int? = null) : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by lazy {
        (requireActivity() as MainActivity).profileViewModel
    }
    private val sharedViewModel: RecipeSharedViewModel by activityViewModels()
    private val favoritesViewModel by lazy { (requireActivity() as MainActivity).favoritesViewModel }

    private lateinit var userRecipesAdapter: NetworkRecipeAdapter
    private var currentUserId: Int = -1

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePhoto()
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { uploadImage(it) }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleGalleryImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = (requireActivity() as MainActivity).sessionManager
        currentUserId = sessionManager.getUsersDetailFromSession()[SessionManager.KEY_USER_ID]?.toIntOrNull() ?: -1

        setUpRecyclerView()
        bindData()
        setOnCLickListeners()
        observeProfileData()
        fetchData()
    }

    private fun setUpRecyclerView() {
        userRecipesAdapter = NetworkRecipeAdapter(
            onItemClick = { recipe ->
                sharedViewModel.selectNetworkRecipe(recipe)
                (requireActivity() as MainActivity).replaceMainFragment(NetworkRecipeDetailsFragment("Profile"))
            },
            onSaveOrDeleteButtonClick = { recipe, _ ->
                if (targetUserId == null || targetUserId == currentUserId) {
                    showDeleteOwnRecipeConfirmDialog(recipe)
                }
            },
            isSaved = { _ -> flow { emit(false) } },
            useDeleteIcon = (targetUserId == null || targetUserId == currentUserId)
        )
        binding.userRecipesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.userRecipesRecyclerView.adapter = userRecipesAdapter
    }

    private fun fetchData() {
        val userId = targetUserId ?: currentUserId
        val isOwnProfile = targetUserId == null || targetUserId == currentUserId
        profileViewModel.fetchUserData(userId, isOwnProfile)
    }

    private fun bindData() {
        val activity = requireActivity() as MainActivity
        val isOwnProfile = targetUserId == null || targetUserId == currentUserId

        if (isOwnProfile) {
            val usersDetails = activity.sessionManager.getUsersDetailFromSession()
            binding.profileName.text = usersDetails[SessionManager.KEY_USERNAME]
            binding.profileEmail.text = usersDetails[SessionManager.KEY_EMAIL]
            binding.changeAvatarButton.visibility = View.VISIBLE
            binding.logoutButton.visibility = View.VISIBLE
        } else {
            binding.changeAvatarButton.visibility = View.GONE
            binding.logoutButton.visibility = View.GONE
        }
        
        val isCashing = activity.sessionManagerBaseSettings.usersSession.getBoolean(
            SessionManager.KEY_CASH,
            false
        )
        binding.cashLabel.text = if (isCashing) 
            getString(R.string.do_not_cash_network_recipes) 
        else 
            getString(R.string.do_cash_network_recipes)
    }

    private fun setOnCLickListeners() {
        binding.changeAvatarButton.setOnClickListener { showImageSourceDialog() }
        binding.changeCashFlagButton.setOnClickListener { changeCashSettings() }
        binding.changeLanguageButton.setOnClickListener { changeLanguage() }
        binding.logoutButton.setOnClickListener { showConfirmDialog() }
        binding.addAccountButton.setOnClickListener { addAccount() }
        
        binding.followersLayout.setOnClickListener {
            val userId = targetUserId ?: currentUserId
            (requireActivity() as MainActivity).replaceMainFragment(FollowersFragment(userId, "followers"))
        }
        binding.followingLayout.setOnClickListener {
            val userId = targetUserId ?: currentUserId
            (requireActivity() as MainActivity).replaceMainFragment(FollowersFragment(userId, "following"))
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(context)
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(intent)
    }

    private fun handleGalleryImage(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap?.let { uploadImage(it) }
    }

    private fun uploadImage(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        
        profileViewModel.uploadAndSetAvatar(currentUserId, imageBase64)
    }

    private fun addAccount() {
        val activity = requireActivity() as MainActivity
        activity.run {
            findNavController(R.id.fragmentContainerView).popBackStack()
            navBottomViewModel.setIsContentVisible(false)
            showFullScreenContainer()
        }
    }

    private fun changeLanguage() {
        val languageCode = if (getCurrentLanguageCode() == "ru") "en" else "ru"
        setAppLocale(requireContext(), languageCode)
        restartActivity()
    }

    private fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPref.edit { putString("app_language", languageCode) }
    }

    private fun getCurrentLanguageCode(): String {
        val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPref?.getString("app_language", Locale.getDefault().language)
            ?: Locale.getDefault().language
    }

    private fun restartActivity() {
        val intent = Intent(requireActivity(), requireActivity().javaClass).apply {
            putExtra("SHOW_PROFILE_FRAGMENT", true)
            flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        requireActivity().finish()
        startActivity(intent)
    }

    private fun changeCashSettings() {
        val session = (requireActivity() as MainActivity).sessionManagerBaseSettings.usersSession
        val newFlag = !session.getBoolean(SessionManager.KEY_CASH, true)
        session.edit {
            putBoolean(SessionManager.KEY_CASH, newFlag)
        }
        binding.cashLabel.text = if (newFlag) 
            getString(R.string.do_not_cash_network_recipes) 
        else 
            getString(R.string.do_cash_network_recipes)
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(context)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти из аккаунта ${binding.profileName.text}?")
            .setPositiveButton("Выйти") { dialog, _ ->
                dialog.dismiss()
                logOut()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    private fun logOut() {
        val activity = requireActivity() as MainActivity
        activity.run {
            loginViewModel.clearResults()
            navBottomViewModel.setSelectedNavItem(R.id.navigation_search)
            sessionManager.logoutUserSession()
            navBottomViewModel.setIsContentVisible(false)
            showFullScreenContainer()
        }
    }

    private fun showDeleteOwnRecipeConfirmDialog(recipe: NetworkRecipe) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление рецепта")
        builder.setMessage("Вы уверены, что хотите удалить собственный рецепт ${recipe.title}?")
        builder.setPositiveButton("Удалить") { _, _ ->
            favoritesViewModel.deleteOwnRecipe(recipe)
            fetchData() // Refresh
        }
        builder.setNegativeButton("Отменить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    profileViewModel.recipeCount.collect { count ->
                        binding.recipesCount.text = count.toString()
                    }
                }
                launch {
                    profileViewModel.followersCount.collect { count ->
                        binding.followersCount.text = count.toString()
                    }
                }
                launch {
                    profileViewModel.followingCount.collect { count ->
                        binding.followingCount.text = count.toString()
                    }
                }
                launch {
                    profileViewModel.userRecipes.collect { recipes ->
                        userRecipesAdapter.clearRecipes()
                        userRecipesAdapter.addRecipes(recipes)
                    }
                }
                launch {
                    profileViewModel.targetUser.collect { user ->
                        if (user != null && targetUserId != null && targetUserId != currentUserId) {
                            binding.profileName.text = user.username
                            binding.profileEmail.text = user.email
                        }
                    }
                }
                launch {
                    profileViewModel.avatarUrl.collect { url ->
                        Glide.with(this@ProfileFragment)
                            .load(url)
                            .placeholder(R.drawable.account_circle_24dp)
                            .error(R.drawable.account_circle_24dp)
                            .into(binding.profileImage)
                    }
                }
                launch {
                    profileViewModel.uploadStatus.collect { result ->
                        result?.onFailure { e ->
                            Toast.makeText(context, "Upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            profileViewModel.clearUploadStatus()
                        }
                    }
                }
            }
        }
    }
}
