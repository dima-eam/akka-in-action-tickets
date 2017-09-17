package com.goticks

import org.scalatest.{MustMatchers, WordSpecLike}

class EventMarshallingSpec extends WordSpecLike
  with MustMatchers
  with EventMarshalling {

  "EventMarshalling" must {
    "Success deserialize event description" in {
      //      val json = EventDescription(5, RockMusic).toJson
      //      val desc = json.convertTo[EventDescription]
    }
  }
}
