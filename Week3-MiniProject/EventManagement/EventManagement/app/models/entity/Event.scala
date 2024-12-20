package models.entity

import models.enums.EventStatus.EventStatus
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.time.LocalDate

case class Event(
                  id: Option[Long] = None,
                  eventType: String,
                  eventName: String,
                  eventDate: LocalDate,
                  guestCount: Long,
                  specialRequirements: Option[String] = None,
                  eventStatus: Option[EventStatus] = None
                )

object Event {
  private val idReads: Reads[Option[Long]] = (JsPath \ "id").readNullable[Long]
  private val eventTypeReads: Reads[String] = (JsPath \ "eventType").read[String]
  private val eventNameReads: Reads[String] = (JsPath \ "eventName").read[String]
  private val eventDateReads: Reads[LocalDate] = (JsPath \ "eventDate").read[LocalDate]
  private val guestCountReads: Reads[Long] = (JsPath \ "guestCount").read[Long]
  private val specialRequirementsReads: Reads[Option[String]] = (JsPath \ "specialRequirements").readNullable[String]
  private val eventStatusReads: Reads[Option[EventStatus]] =
    (JsPath \ "eventStatus").readNullable[EventStatus]

  // Combine all the reads
  implicit val eventReads: Reads[Event] = (
    idReads and
      eventTypeReads and
      eventNameReads and
      eventDateReads and
      guestCountReads and
      specialRequirementsReads and
      eventStatusReads
    )(Event.apply _)

  // Use Json.writes to generate Writes automatically
  implicit val eventWrites: Writes[Event] = Json.writes[Event]

  // Combine Reads and Writes into Format
  implicit val eventFormat: Format[Event] = Format(eventReads, eventWrites)
}