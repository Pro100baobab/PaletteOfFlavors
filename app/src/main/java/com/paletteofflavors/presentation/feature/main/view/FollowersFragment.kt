package com.paletteofflavors.presentation.feature.main.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.paletteofflavors.databinding.FragmentFollowersBinding
import com.paletteofflavors.presentation.feature.main.view.adapter.UserAdapter
import com.paletteofflavors.presentation.feature.main.viewmodel.ProfileViewModel
import com.paletteofflavors.presentation.main.MainActivity
import kotlinx.coroutines.launch

class FollowersFragment(
    private val userId: Int,
    private val type: String // "followers" or "following"
) : Fragment() {

    private var _binding: FragmentFollowersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by lazy {
        (requireActivity() as MainActivity).profileViewModel
    }

    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.followersTitle.text = if (type == "followers") "Подписчики" else "Подписки"
        binding.backButtonFollowers.setOnClickListener {
            (requireActivity() as MainActivity).replaceMainFragment(ProfileFragment())
        }

        setUpRecyclerView()
        observeData()

        if (type == "followers") {
            viewModel.fetchFollowers(userId)
        } else {
            viewModel.fetchFollowing(userId)
        }
    }

    private fun setUpRecyclerView() {
        userAdapter = UserAdapter { user ->
            // Навигация в профиль выбранного пользователя
            (requireActivity() as MainActivity).replaceMainFragment(ProfileFragment(user.id))
        }
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.usersRecyclerView.adapter = userAdapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (type == "followers") {
                    viewModel.followersList.collect { users ->
                        userAdapter.setUsers(users)
                    }
                } else {
                    viewModel.followingList.collect { users ->
                        userAdapter.setUsers(users)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
