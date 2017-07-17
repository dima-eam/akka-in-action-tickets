package com.goticks

import akka.actor.{Actor, PoisonPill, Props}

object TicketSeller {

  def props(event: String, category: EventCategory) = Props(new TicketSeller(event, category))

  case class Add(tickets: Vector[Ticket])

  case class Buy(tickets: Int)

  case class Ticket(id: Int)

  case class EventTickets(event: String, entries: Vector[Ticket] = Vector.empty[Ticket])

  case object GetEvent

  case object Cancel

}

class TicketSeller(event: String, category: EventCategory) extends Actor {

  import TicketSeller._

  var tickets = Vector.empty[Ticket]

  override def receive = {

    case Add(newTickets) => tickets = tickets ++ newTickets

    case Buy(nrOfTickets) =>
      val entries = tickets.take(nrOfTickets)
      if (entries.size >= nrOfTickets) {
        sender() ! EventTickets(event, entries)
        tickets = tickets.drop(nrOfTickets)
      } else sender() ! EventTickets(event)

    case GetEvent => sender() ! Some(BoxOffice.Event(event, tickets.size, category))

    case Cancel =>
      sender() ! Some(BoxOffice.Event(event, tickets.size, category))
      self ! PoisonPill
  }
}

