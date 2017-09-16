package com.goticks

import com.goticks.EventCategories.{EventCategory, Uncategorized}
import spray.json._

case class EventDescription(tickets: Int, eventCategory: EventCategory = Uncategorized) {
  require(tickets > 0)
}

case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

case class Error(message: String)

object EventCategories {

  case class EventCategory(name: String) {
    require(name.length > 0)
  }

  object Uncategorized extends EventCategory("Uncategorized")

  object RockMusic extends EventCategory("Rock Music")

  val values = Seq(Uncategorized, RockMusic)
}

trait EventMarshalling extends DefaultJsonProtocol {

  import BoxOffice._

  implicit val eventCategoryFormat = jsonFormat1(EventCategory)
  implicit val eventDescriptionFormat = jsonFormat2(EventDescription)
  implicit val eventFormat = jsonFormat3(Event)
  implicit val eventsFormat = jsonFormat1(Events)
  implicit val ticketRequestFormat = jsonFormat1(TicketRequest)
  implicit val ticketFormat = jsonFormat1(TicketSeller.Ticket)
  implicit val ticketsFormat = jsonFormat2(TicketSeller.EventTickets)
  implicit val errorFormat = jsonFormat1(Error)
}
