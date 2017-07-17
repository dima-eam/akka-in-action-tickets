package com.goticks

import spray.json._

case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

case class Error(message: String)


trait EventMarshalling extends DefaultJsonProtocol {

  import BoxOffice._

  implicit object EventCategoryJsonFormat extends RootJsonFormat[EventCategory] {

    def write(ec: EventCategory) = JsObject("category" -> JsString(ec.category))

    def read(value: JsValue) =
      value.asJsObject.fields("category") match {
        case JsString("Rock Music") => RockMusic
        case _ => throw new UninitializedError
      }
  }

  implicit val eventDescriptionFormat = jsonFormat1(EventDescription)
  implicit val eventFormat = jsonFormat3(Event)
  implicit val eventsFormat = jsonFormat1(Events)
  implicit val ticketRequestFormat = jsonFormat1(TicketRequest)
  implicit val ticketFormat = jsonFormat1(TicketSeller.Ticket)
  implicit val ticketsFormat = jsonFormat2(TicketSeller.EventTickets)
  implicit val errorFormat = jsonFormat1(Error)
}
