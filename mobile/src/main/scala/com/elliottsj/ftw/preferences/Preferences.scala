package com.elliottsj.ftw.preferences

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.elliottsj.protobus.Agency

import scala.slick.ast.{TableNode, TableExpansion}
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable

/**
 * Convenience class to store and retrieve saved preferences, including:
 * - saved transit agencies
 * - saved stops
 * - saved stop routes
 */
class Preferences(context: Context) {
  final val DATABASE_NAME = "preferences.db"
  final val DATABASE_VERSION = 1

  // Creates the database if it doesn't exist, then gets a SQLDroid JDBC connection
  lazy val db = {
    val sqliteDb = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)

    // TODO: database migrations
    sqliteDb.setVersion(DATABASE_VERSION)

    Database.forURL("jdbc:sqlite:" + sqliteDb.getPath, driver = "org.sqldroid.SQLDroidDriver")
  }
  lazy val agencies = createIfNotExists(TableQuery[Agencies])

  /**
   * Saves a transit agency
   *
   * @param agency a transit agency
   * @return
   */
  def saveAgency(agency: Agency) = db.withSession { implicit session =>
    agencies += agency
  }

  /**
   * Gets the list of saved transit agencies
   *
   * @return
   */
  def getAgencies: Seq[Agency] = db.withSession { implicit session =>
    agencies.list
  }

  /**
   * Create the given table in the database if it doesn't exist.
   * Return the table.
   *
   * @param table a slick table
   * @tparam T the type of tuple contained in the table
   * @return
   */
  private def createIfNotExists[T <: Table[_]](table: TableQuery[T]) = db.withSession { implicit session =>
    // Ugly hack to get table name
    val tableName = table.toNode.asInstanceOf[TableExpansion].table.asInstanceOf[TableNode].tableName

    if (MTable.getTables(tableName).list.isEmpty) table.ddl.create
    table
  }

}

object Preferences {
  def apply(context: Context) = new Preferences(context)
}
