//package com.elliottsj.ftw.fragments
//
//import android.app.Fragment
//import android.os.Bundle
//import android.view.{View, ViewGroup, LayoutInflater}
//import android.widget.ProgressBar
//import com.elliottsj.ftw.R
//
//class ProgressFragment extends Fragment {
//
//  private var mProgress: ProgressBar = _
//
//  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
//    val rootView = inflater.inflate(R.layout.fragment_progress, container, false)
//
//    mProgress = rootView.findViewById(android.R.id.progress).asInstanceOf[ProgressBar]
//
//    rootView
//  }
//
//  override def onResume(): Unit = {
//    super.onResume()
//
//    mProgress.setVisibility(View.VISIBLE)
//  }
//}
