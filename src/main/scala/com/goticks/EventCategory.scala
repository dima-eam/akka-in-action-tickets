package com.goticks

sealed trait EventCategory {
  def category: String = "Uncategorized"
}

case object Uncategorized extends EventCategory

case object RockMusic extends EventCategory {
  override val category = "Rock Music"
}

//  case object Drama extends EventCategory("Drama")
