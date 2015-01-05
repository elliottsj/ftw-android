package com.elliottsj.ftw.agencies

import android.content.Context
import android.support.v7.widget.{CardView, RecyclerView}
import android.view.{LayoutInflater, ViewGroup}
import android.widget.TextView
import com.elliottsj.ftw.R
import com.elliottsj.protobus.Agency

object AgencyAdapter {
  class ViewHolder(itemView: CardView) extends RecyclerView.ViewHolder(itemView)
}

class AgencyAdapter(context: Context, agencies: Array[Agency]) extends RecyclerView.Adapter[AgencyAdapter.ViewHolder] {
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): AgencyAdapter.ViewHolder = {
    val v: CardView = LayoutInflater.from(context).inflate(R.layout.agency_card, parent, false).asInstanceOf[CardView]

    // TODO: set view's size, margins, etc. if necessary

    new AgencyAdapter.ViewHolder(v)
  }

  override def getItemCount: Int = agencies.length

  override def onBindViewHolder(holder: AgencyAdapter.ViewHolder, position: Int): Unit = {
    // Map NextBus fields onto the TextViews
    for (nb <- agencies(position).nextbusFields) Map(
      R.id.agency_title -> nb.agencyTitle,
      R.id.agency_short_title -> nb.agencyShortTitle.getOrElse(""),
      R.id.region_title -> nb.agencyRegionTitle
    ).map { case (viewId, text) =>
      holder.itemView.findViewById(viewId).asInstanceOf[TextView].setText(text)
    }
  }
}
