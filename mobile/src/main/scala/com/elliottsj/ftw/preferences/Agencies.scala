package com.elliottsj.ftw.preferences

import com.elliottsj.protobus.Agency

import scala.slick.driver.SQLiteDriver.simple._

class Agencies(tag: Tag) extends Table[Agency](tag, "agencies") {
  def agencyTag = column[String]("tag", O.PrimaryKey)
  def title = column[String]("title")
  def shortTitle = column[Option[String]]("short_title")
  def regionTitle = column[String]("region_title")

  /**
   * Converts a slick row to an agency
   *
   * @param agencyTag the agency tag
   * @param title the agency title
   * @param shortTitle the agency short title
   * @param regionTitle the agency region title
   * @return the agency
   */
  def fromRow(agencyTag: String, title: String, shortTitle: Option[String], regionTitle: String): Agency = {
    Agency(None, Some(Agency.Nextbus(agencyTag, title, shortTitle, regionTitle)))
  }

  /**
   * Converts an agency to a slick row
   *
   * @param agency the agency to convert
   * @return the slick row tuple
   */
  def toRow(agency: Agency): Some[(String, String, Option[String], String)] = {
    val nb = agency.getNextbusFields
    Some((nb.agencyTag, nb.agencyTitle, nb.agencyShortTitle, nb.agencyRegionTitle))
  }
  
  override def * = (agencyTag, title, shortTitle, regionTitle) <> ((fromRow _).tupled, toRow)
}
