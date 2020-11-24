package com.meizu.mstore.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meizu.mstore.sdk.demo.pay.NormalPayFragment
import com.meizu.mstore.sdk.demo.pay.PayAndSignFragment
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(), ViewPager.OnPageChangeListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager?.adapter = MyPagerAdapter(childFragmentManager)


        bottomNavigationBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.page_1 -> {
                    viewPager?.currentItem = 0
                    true
                }
                R.id.page_2 -> {
                    viewPager?.currentItem = 1
                    true
                }
                else -> false
            }
        }

        viewPager?.addOnPageChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager?.removeOnPageChangeListener(this)
    }

    private inner class MyPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> NormalPayFragment.newInstance()
            else -> PayAndSignFragment.newInstance()
        }
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
    }

    override fun onPageSelected(p0: Int) {
        bottomNavigationBar?.selectedItemId = when (p0) {
            0 -> R.id.page_1
            else -> R.id.page_2
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        for (fragment in childFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPageScrollStateChanged(p0: Int) {
    }

}