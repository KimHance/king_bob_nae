package com.example.king_bob_nae.features.home.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.king_bob_nae.R
import com.example.king_bob_nae.base.BaseFragment
import com.example.king_bob_nae.databinding.FragmentHomeBinding
import com.example.king_bob_nae.features.home.presentation.adapter.UserListAdapter
import com.example.king_bob_nae.features.home.presentation.viewmodel.HomeViewModel
import com.example.king_bob_nae.features.myprofile.presentation.MyProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val userListAdapter by lazy { UserListAdapter(homeViewModel) }
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        collectFlows()
        initApiCall()
    }

    private fun initApiCall() {
        with(homeViewModel) {
            getFriendList()
            getHomeStatus()
        }
    }

    private fun initView() {
        homeViewModel.isTutorial.value = false
        binding.commonHomeLayout.rvFriends.apply {
            adapter = userListAdapter
        }
        binding.commonHomeLayout.tvKkilog.text = "끼록"
        binding.ivMy.setOnClickListener {
            startActivity(Intent(requireActivity(), MyProfileActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.commonHomeLayout.ivAdd.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_followingFragment)
        }
        binding.commonHomeLayout.levelUpLottie.setOnClickListener {
            if (binding.progressLv.progress >= binding.progressLv.max && binding.home!!.totalKkilogCount <= 30) {
                homeViewModel.postLevelUp()
                val dialog = HomeLevelUpDialog()
                dialog.show(requireActivity().supportFragmentManager, "level_up")
            }
        }
    }

    private fun collectFlows() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeViewModel.run {
                    launch {
                        homeUserFriendList.collect {
                            userListAdapter.submitList(it)
                        }
                    }

                    launch {
                        userList.collectLatest {
                            binding.home = it
                            binding.commonHomeLayout.home = it
                        }
                    }

                    launch {
                        goFriendsHomeFragmentEvent.collect {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToFriendsHomeFragment(it)
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }
}
