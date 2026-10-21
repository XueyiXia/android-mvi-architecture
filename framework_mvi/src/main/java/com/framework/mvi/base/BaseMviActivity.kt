package com.framework.mvi.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: MVI Activity 基类
 */
abstract class BaseMviActivity<
    BINDING : ViewDataBinding,
    S : MviState,
    I : MviIntent,
    E : MviEffect,
    VM : BaseMviViewModel<S, I, E>
> : AppCompatActivity() {

    protected abstract val viewModel: VM
    protected abstract fun inflateBinding(): BINDING

    private var mBinding: BINDING? = null
    protected val mBindingView: BINDING
        get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = inflateBinding()
        setContentView(mBindingView.root)

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

        initView(mBindingView.root, savedInstanceState)
    }

    protected abstract fun initView(rootView: View, savedInstanceState: Bundle?)

    protected abstract fun renderState(state: S)

    protected open fun handleEffect(effect: E) = Unit

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}
