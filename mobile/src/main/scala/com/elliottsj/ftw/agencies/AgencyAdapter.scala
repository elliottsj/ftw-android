package com.elliottsj.ftw.agencies

import android.content.Context
import android.support.v7.widget.{CardView, RecyclerView}
import android.view.{View, LayoutInflater, ViewGroup}
import android.widget.TextView
import com.elliottsj.ftw.R
import com.elliottsj.protobus.Agency

object AgencyAdapter {
  case class ViewHolder(var agency: Agency, agencyCard: CardView) extends RecyclerView.ViewHolder(agencyCard)
}

class AgencyAdapter(context: Context, agencies: Array[Agency], onAgencyClick: Agency => Unit) extends RecyclerView.Adapter[AgencyAdapter.ViewHolder] {
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): AgencyAdapter.ViewHolder = {
    val cardView: CardView = LayoutInflater.from(context).inflate(R.layout.agency_card, parent, false).asInstanceOf[CardView]
    val holder = new AgencyAdapter.ViewHolder(agency = null, agencyCard = cardView)
    cardView.findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = Option(holder.agency) match {
        case Some(a) => onAgencyClick(a)
        case _ => throw new RuntimeException("Clicked on agency card without associated agency")
      }
    })
    holder
  }

  override def getItemCount: Int = agencies.length

  override def onBindViewHolder(holder: AgencyAdapter.ViewHolder, position: Int): Unit = {
    // Assign an agency reference to the holder
    holder.agency = agencies(position)

    // Map NextBus fields onto the TextViews
    for (nb <- agencies(position).nextbusFields) Map(
      android.R.id.title -> nb.agencyTitle,
      R.id.region_title -> nb.agencyRegionTitle
    ).map { case (viewId, text) =>
      holder.itemView.findViewById(viewId).asInstanceOf[TextView].setText(text)
    }
  }
}
