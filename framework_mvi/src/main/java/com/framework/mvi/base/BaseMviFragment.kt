package com.framework.mvi.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: MVI Fragment 基类
 */
abstract class BaseMviFragment<
    BINDING : ViewDataBinding,
    S : MviState,
    I : MviIntent,
    E : MviEffect,
    VM : BaseMviViewModel<S, I, E>
> : Fragment() {

    protected abstract val viewModel: VM
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): BINDING

    private var mBinding: BINDING? = null
    protected val mBindingView: BINDING
        get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = inflateBinding(inflater, container)
        return mBindingView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { renderState(it) }
                }

                launch {
                    viewModel.effect.collect { handleEffect(it) }
                }
            }
        }

        initView(view, savedInstanceState)
    }

    protected abstract fun initView(rootView: View, savedInstanceState: Bundle?)

    protected abstract fun renderState(state: S)

    protected open fun handleEffect(effect: E) = Unit

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}
