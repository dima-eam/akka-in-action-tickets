package com.goticks

import com.goticks.EventCategories.RockMusic
import org.scalatest.{FlatSpec, MustMatchers, WordSpecLike}

class EventMarshallingSpec extends FlatSpec
  with WordSpecLike
  with MustMatchers
  with EventMarshalling {

  "EventMarshalling" must {
    "Success deserialize event description" in {
      val json = EventDescription(5, RockMusic).toJson
      val desc = json.convertTo[EventDescription]
    }
  }
}
