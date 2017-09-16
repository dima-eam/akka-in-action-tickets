package com.goticks

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.goticks.BoxOffice._
import com.goticks.EventCategories.{EventCategory, RockMusic}
import com.goticks.TicketSeller._
import org.scalatest.{MustMatchers, WordSpecLike}

class BoxOfficeSpec extends TestKit(ActorSystem("testBoxOffice"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {

  "The BoxOffice" must {
    "Create an event and get tickets from the correct Ticket Seller" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val eventName = "RHCP"

      boxOffice ! CreateEvent(eventName, 10, RockMusic)
      expectMsg(EventCreated(Event(eventName, 10, RockMusic)))

      boxOffice ! GetEvents
      expectMsg(Events(Vector(Event(eventName, 10, RockMusic))))

      boxOffice ! BoxOffice.GetEvent(eventName)
      expectMsg(Some(Event(eventName, 10, RockMusic)))

      boxOffice ! GetTickets(eventName, 2)
      expectMsg(EventTickets(eventName, Vector(Ticket(1), Ticket(2))))
    }

    "Return an empty tickets vector for unknown event" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      boxOffice ! GetTickets("DavidBowie", 1)
      val tickets = EventTickets("DavidBowie")
      expectMsg(tickets)
      assert(tickets.entries.isEmpty)
    }

    "Create a child actor when an event is created and sends it a Tickets message" in {
      val boxOffice = system.actorOf(Props(
        new BoxOffice {
          override def createTicketSeller(name: String, category: EventCategory): ActorRef = testActor
        }
      )
      )

      val tickets = 3
      val eventName = "RHCP"
      val expectedTickets = (1 to tickets).map(Ticket).toVector
      boxOffice ! CreateEvent(eventName, tickets, RockMusic)
      expectMsg(Add(expectedTickets))
      expectMsg(EventCreated(Event(eventName, tickets, RockMusic)))
    }

    "Get and cancel an event that is not created yet" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val noneExitEventName = "noExitEvent"
      boxOffice ! BoxOffice.GetEvent(noneExitEventName)
      expectMsg(None)

      boxOffice ! CancelEvent(noneExitEventName)
      expectMsg(None)
    }

    "Cancel a ticket which event is not created " in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val noneExitEventName = "noExitEvent"

      boxOffice ! CancelEvent(noneExitEventName)
      expectMsg(None)
    }

    "Cancel a ticket which event is created" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val eventName = "RHCP"
      val tickets = 10
      boxOffice ! CreateEvent(eventName, tickets, RockMusic)
      expectMsg(EventCreated(Event(eventName, tickets, RockMusic)))

      boxOffice ! CancelEvent(eventName)
      expectMsg(Some(Event(eventName, tickets, RockMusic)))
    }
  }


}
