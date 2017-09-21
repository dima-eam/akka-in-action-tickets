package com.goticks

import akka.actor._
import akka.util.Timeout
import com.goticks.EventCategories.EventCategory

import scala.concurrent.Future

/**
  * Companion for supervisor actor BoxOffice. Contains its [[Props]] and possible events.
  */
object BoxOffice {

  def props(implicit timeout: Timeout) = Props(new BoxOffice)

  def name = "boxOffice"

  case class CreateEvent(name: String, tickets: Int, category: EventCategory)

  case class GetEvent(name: String)

  case object GetEvents

  case class GetTickets(event: String, tickets: Int)

  case class CancelEvent(name: String)

  case class Event(name: String, tickets: Int, category: EventCategory)

  case class Events(events: Vector[Event])

  sealed trait EventResponse

  case class EventCreated(event: Event) extends EventResponse

  case object EventExists extends EventResponse

}

class BoxOffice(implicit timeout: Timeout) extends Actor with ActorLogging {

  import BoxOffice._
  import context._

  /**
    * Creates an actor representing tickets holder for event. New actor live in context and can be
    * referenced via [[akka.actor.ActorContext#actorOf]] method
    *
    * @param name     event name
    * @param category event category
    * @return ref to a new actor
    */
  def createTicketSeller(name: String, category: EventCategory) =
    context.actorOf(TicketSeller.props(name, category), name)

  def receive = {
    case ev: CreateEvent =>
      def create() = {
        val eventTickets = createTicketSeller(ev.name, ev.category)
        val newTickets = (1 to ev.tickets).map { ticketId =>
          TicketSeller.Ticket(ticketId)
        }.toVector
        eventTickets ! TicketSeller.Add(newTickets)
        sender() ! EventCreated(Event(ev.name, ev.tickets, ev.category))
      }

      log.info("Create received: event={}", ev)

      context.child(name).fold(create())(_ => sender() ! EventExists)

    case ev: GetTickets =>
      def notFound() = sender() ! TicketSeller.EventTickets(ev.event)
      def buy(child: ActorRef) =
        child forward TicketSeller.Buy(ev.tickets)

      log.info("Get tickets received: event={}", ev)

      context.child(ev.event).fold(notFound())(buy)

    case GetEvent(event) =>
      def notFound() = sender() ! None
      def getEvent(child: ActorRef) = child forward TicketSeller.GetEvent

      context.child(event).fold(notFound())(getEvent)

    case GetEvents =>
      import akka.pattern.{ask, pipe}

      def getEvents = context.children.map { child =>
        self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
      }
      def convertToEvents(f: Future[Iterable[Option[Event]]]) =
        f.map(_.flatten).map(l => Events(l.toVector))

      pipe(convertToEvents(Future.sequence(getEvents))) to sender()

    case CancelEvent(event) =>
      def notFound() = sender() ! None
      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel
      context.child(event).fold(notFound())(cancelEvent)
  }
}

