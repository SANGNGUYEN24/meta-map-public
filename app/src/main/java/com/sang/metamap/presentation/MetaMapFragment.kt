package com.sang.metamap.presentation

import android.widget.PopupWindow
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.sang.metamap.R
import com.sang.metamap.utils.SystemBarUtil

open class MetaMapFragment : Fragment() {
    protected open fun showFragment(fragment: MetaMapFragment, addToBackStack: Boolean = true) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out)
            .replace(R.id.fragment_container_view, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()

    }

    protected fun paddingStatusBar(viewBinding: ViewBinding) {
        viewBinding.root.updatePadding(top = viewBinding.root.paddingTop + SystemBarUtil.getStatusBarHeight())
    }

    protected fun initPopupWindow(viewBinding: ViewBinding) =
        PopupWindow(context).apply {
            contentView = viewBinding.root
            isFocusable = true
            setBackgroundDrawable(null)
            elevation = 60f
            animationStyle = android.R.style.Animation_Dialog
        }
}
