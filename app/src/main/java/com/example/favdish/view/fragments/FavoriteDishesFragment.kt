package com.example.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentFavoriteDishesBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.adapters.FavDishAdapter
import com.example.favdish.viewmodel.DashboardViewModel
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentFavoriteDishesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mFavDishViewModel : FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentFavoriteDishesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFavDishViewModel.favoriteDishes.observe(viewLifecycleOwner){
            dishes ->
            dishes.let {
                binding.rvFavoriteDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)
                val adapter = FavDishAdapter(this)
                binding.rvFavoriteDishesList.adapter = adapter
                if (it.isNotEmpty()){
                    binding.rvFavoriteDishesList.visibility = View.VISIBLE
                    binding.tvNoFavoriteDishesAvailable.visibility = View.GONE
                    adapter.dishesList(it)
                }else{
                    binding.rvFavoriteDishesList.visibility = View.GONE
                    binding.tvNoFavoriteDishesAvailable.visibility = View.VISIBLE
                }
            }
        }
    }

    fun dishDetails(favDish: FavDish){
        findNavController().navigate(FavoriteDishesFragmentDirections.actionNavigationFavoriteDishesToNavigationDishDetails(favDish))
        if (requireActivity() is MainActivity){
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity){
            (activity as MainActivity?)!!.showBottomNavigationView()
        }
    }
}