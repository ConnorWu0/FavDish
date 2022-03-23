package com.example.favdish.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.DialogCustomProgressBinding
import com.example.favdish.databinding.FragmentRandomDishBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.model.entities.RandomDish
import com.example.favdish.utils.Constants
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.example.favdish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {


    private var _binding: FragmentRandomDishBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mRandomDishViewModel: RandomDishViewModel

    private var mProgressDialog: Dialog? = null
    private var _dialogCustomProgressBinding: DialogCustomProgressBinding? = null
    private val dialogCustomProgressBinding get() = _dialogCustomProgressBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentRandomDishBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }
    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(requireActivity())
        _dialogCustomProgressBinding = DialogCustomProgressBinding.inflate(layoutInflater)
        mProgressDialog?.let {
            it.setContentView(dialogCustomProgressBinding.root)
            it.show()
        }
    }

    private fun hideProgressDialog(){
        mProgressDialog?.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)
        mRandomDishViewModel.getRandomRecipeFromAPI()
        randomDishViewModelObserver()
        binding.srlRandomDish.setOnRefreshListener {
            mRandomDishViewModel.getRandomRecipeFromAPI()
        }
    }

    private fun randomDishViewModelObserver(){
        mRandomDishViewModel.randomDishResponse.observe(viewLifecycleOwner,
            {randomDishResponse -> randomDishResponse?.let {
                Log.i("Random Dish Response","${randomDishResponse.recipes[0]}")
                if (binding.srlRandomDish.isRefreshing){
                    binding.srlRandomDish.isRefreshing = false
                }
                setRandomDishResponseInUI(randomDishResponse.recipes[0])
            }})
        mRandomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner,
            {dataError ->
                dataError?.let {
                    Log.e("Random Dish API Error", "$dataError")
                    if (binding.srlRandomDish.isRefreshing){
                        binding.srlRandomDish.isRefreshing = false
                    }
                }
            })
        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner,
            {loadRandomDish ->
                loadRandomDish?.let {
                    Log.i("Random Dish Loading","$loadRandomDish")
                    if (loadRandomDish && !binding.srlRandomDish.isRefreshing){
                        showCustomProgressDialog()
                    }else{
                        hideProgressDialog()
                    }
                }
            })
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe){
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(binding.ivDishImage)
        var dishType = "other"
        if (recipe.dishTypes.isNotEmpty()){
            dishType = recipe.dishTypes[0]
            binding.tvType.text = dishType
        }
        binding.tvCategory.text = getString(R.string.other)
        var ingredients = ""
        for (value in recipe.extendedIngredients){
            if (ingredients.isEmpty()){
                ingredients = value.original
            }else{
                ingredients = ingredients + ", \n" + value.original
            }
        }

        binding.tvIngredients.text = ingredients

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            binding.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        }else{
            @Suppress("DEPRECATION")
            binding.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        binding.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )

        var addedToFavorites = false

        binding.tvCookingTime.text = getString(R.string.lbl_estimate_cooking_time,
        recipe.readyInMinutes.toString())
        binding.ivFavoriteDish.setOnClickListener {
            if (addedToFavorites){
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_LONG
                ).show()
            }else{
                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )
                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }
                mFavDishViewModel.insert(randomDishDetails)
                addedToFavorites = true
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )
                Toast.makeText(requireActivity(),
                    getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _dialogCustomProgressBinding = null
    }
}