package com.elliottsj.ftw.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.{Rect, Canvas}
import android.graphics.drawable.Drawable
import android.support.v7.widget.{RecyclerView, LinearLayoutManager}
import android.view.View

object DividerItemDecoration {
  val ATTRS: Array[Int] = Array[Int](android.R.attr.listDivider)

  val HORIZONTAL_LIST: Int = LinearLayoutManager.HORIZONTAL
  val VERTICAL_LIST: Int = LinearLayoutManager.VERTICAL
}

class DividerItemDecoration(context: Context, orientation: Int) extends RecyclerView.ItemDecoration {

  private var mDivider: Drawable = _
  private var mOrientation: Int = _

  val a: TypedArray = context.obtainStyledAttributes(DividerItemDecoration.ATTRS)
  mDivider = a.getDrawable(0)
  a.recycle()
  setOrientation(orientation)

  def setOrientation(orientation: Int): Unit = orientation match {
    case DividerItemDecoration.HORIZONTAL_LIST | DividerItemDecoration.VERTICAL_LIST =>
      mOrientation = orientation
    case _ =>
      throw new IllegalArgumentException("Invalid orientation")
  }

  override def onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State): Unit = mOrientation match {
    case DividerItemDecoration.VERTICAL_LIST => drawVertical(c, parent)
    case DividerItemDecoration.HORIZONTAL_LIST => drawHorizontal(c, parent)
  }

  def drawVertical(c: Canvas, parent: RecyclerView): Unit = {
    val left: Int = parent.getPaddingLeft
    val right: Int = parent.getWidth - parent.getPaddingRight
    val childCount: Int = parent.getChildCount
    0 to childCount foreach { i =>
      val child: View = parent.getChildAt(i)
      val params: RecyclerView.LayoutParams = child.getLayoutParams.asInstanceOf[RecyclerView.LayoutParams]
      val top: Int = child.getBottom + params.bottomMargin
      val bottom: Int = top + mDivider.getIntrinsicHeight
      mDivider.setBounds(left, top, right, bottom)
      mDivider.draw(c)
    }
  }

  def drawHorizontal(c: Canvas, parent: RecyclerView): Unit = {
    val top: Int = parent.getPaddingTop
    val bottom: Int = parent.getHeight - parent.getPaddingBottom
    val childCount: Int = parent.getChildCount
    0 to childCount foreach { i =>
      val child: View = parent.getChildAt(i)
      val params: RecyclerView.LayoutParams = child.getLayoutParams.asInstanceOf[RecyclerView.LayoutParams]
      val left: Int = child.getRight + params.rightMargin
      val right: Int = left + mDivider.getIntrinsicHeight
      mDivider.setBounds(left, top, right, bottom)
      mDivider.draw(c)
    }
  }

  override def getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State): Unit = mOrientation match {
    case DividerItemDecoration.VERTICAL_LIST => outRect.set(0, 0, 0, mDivider.getIntrinsicHeight)
    case DividerItemDecoration.HORIZONTAL_LIST => outRect.set(0, 0, mDivider.getIntrinsicWidth, 0)
  }
}
