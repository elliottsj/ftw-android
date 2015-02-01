package com.elliottsj.ftw.agencies

import android.content.Context
import android.support.v7.widget.{CardView, RecyclerView}
import android.view.{View, LayoutInflater, ViewGroup}
import android.widget.TextView
import com.elliottsj.ftw.R
import com.elliottsj.protobus.{Stop, Agency}

object StopAdapter {
  case class ViewHolder(var stop: Stop, stopCard: CardView) extends RecyclerView.ViewHolder(stopCard)
}

class StopAdapter(context: Context, stops: Array[Stop], onStopClick: Stop => Unit) extends RecyclerView.Adapter[StopAdapter.ViewHolder] {
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): StopAdapter.ViewHolder = {
    val cardView: CardView = LayoutInflater.from(context).inflate(R.layout.stop_card, parent, false).asInstanceOf[CardView]
    val holder = new StopAdapter.ViewHolder(stop = null, stopCard = cardView)
    cardView.findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = Option(holder.stop) match {
        case Some(a) => onStopClick(a)
        case _ => throw new RuntimeException("Clicked on stop card without associated stop")
      }
    })
    holder
  }

  override def getItemCount: Int = stops.length

  override def onBindViewHolder(holder: StopAdapter.ViewHolder, position: Int): Unit = {
    // Assign an agency reference to the holder
    holder.stop = stops(position)

    // Map NextBus fields onto the TextViews
    for (nb <- stops(position).nextbusFields) Map(
      android.R.id.title -> nb.stopTitle
    ).map { case (viewId, text) =>
      holder.itemView.findViewById(viewId).asInstanceOf[TextView].setText(text)
    }
  }
}
